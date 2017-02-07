package com.involveit.shiners.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.involveit.shiners.R;
import com.involveit.shiners.activities.DialogActivity;
import com.involveit.shiners.logic.Constants;
import com.involveit.shiners.logic.Helper;
import com.involveit.shiners.logic.JsonProvider;
import com.involveit.shiners.logic.MeteorBroadcastReceiver;
import com.involveit.shiners.logic.cache.CacheEntity;
import com.involveit.shiners.logic.cache.CachingHandler;
import com.involveit.shiners.logic.objects.Chat;
import com.involveit.shiners.logic.objects.response.GetChatsResponse;
import com.involveit.shiners.logic.proxies.MessagesProxy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class MessagesFragment extends Fragment {
    private static final String TAG = "MessagesFragment";
    public MessagesFragment() {
        // Required empty public constructor
    }

    private boolean messagesRequestPending = true;
    private ProgressDialog progressDialog;
    private ListView listView;

    public static MessagesFragment newInstance() {
        MessagesFragment fragment = new MessagesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        listView = (ListView) view.findViewById(R.id.dialogs_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Chat chat = ((ChatsArrayAdapter)listView.getAdapter()).getItem(i);
                UUID requestId = MessagesProxy.startGettingMessagesAsync(getActivity(), chat.id, 0, 20);
                Intent intent = new Intent(getActivity(), DialogActivity.class);
                intent.putExtra(DialogActivity.EXTRA_CHAT, (Parcelable) chat);
                intent.putExtra(DialogActivity.EXTRA_REQUEST_ID, requestId);
                startActivity(intent);
            }
        });
        listView.setEmptyView(view.findViewById(R.id.dialogs_list_empty_view));

        CacheEntity<ArrayList<Chat>> cache = CachingHandler.getCacheObject(getActivity(), CachingHandler.KEY_DIALOGS);

        if (cache == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getText(R.string.message_loading_chats));
            progressDialog.show();
            progressDialog.setCancelable(false);
        } else {
            createListView(cache.getObject());

            if (!cache.isStale()){
                messagesRequestPending = false;
            }
        }

        if (messagesRequestPending && MeteorSingleton.getInstance().isConnected()){
            loadChats();
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        meteorBroadcastReceiver.unregister(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        meteorBroadcastReceiver.register(getActivity());
    }

    private void createListView(ArrayList<Chat> chats){
        ChatsArrayAdapter adapter = (ChatsArrayAdapter) listView.getAdapter();
        if (adapter == null){
            adapter = new ChatsArrayAdapter(getActivity(), R.layout.row_messages, chats);
            adapter.setNotifyOnChange(true);
            listView.setAdapter(adapter);
        } else {
            Helper.mergeDataToArrayAdapter(chats, adapter, true);
        }
    }

    private void loadChats(){
        messagesRequestPending = false;
        HashMap<String,Object> map = new HashMap<>();
        map.put("take", 50);
        map.put("skip", 0);

        MeteorSingleton.getInstance().call(Constants.MethodNames.GET_CHATS, new Object[]{map}, new ResultListener() {
            @Override
            public void onSuccess(String result) {
                if (progressDialog != null){
                    progressDialog.dismiss();
                    progressDialog = null;
                }

                GetChatsResponse res = JsonProvider.defaultGson.fromJson(result, GetChatsResponse.class);
                if (res.success){
                    createListView(res.result);
                    CachingHandler.setObject(getActivity(), CachingHandler.KEY_DIALOGS, res.result);
                } else {
                    Toast.makeText(getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error, String reason, String details) {
                if (progressDialog != null){
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                Log.d(TAG, "Error: " + error);
                Log.d(TAG, "Reason: " + reason);
                Log.d(TAG, "Details: " + details);
                Toast.makeText(getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class ChatsArrayAdapter extends ArrayAdapter<Chat>{
        public ChatsArrayAdapter(Context context, int resource, List<Chat> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //return super.getView(position, convertView, parent);
            ViewHolder viewHolder;
            if (convertView == null){
                LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(R.layout.row_messages, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mTxtFrom = (TextView) convertView.findViewById(R.id.message_row_txt_from);
                viewHolder.mTxtMessage = (TextView) convertView.findViewById(R.id.message_row_txt_message);
                viewHolder.mImgAccountPhoto = (ImageView) convertView.findViewById(R.id.messages_row_img);
                viewHolder.mTxtDate = (TextView) convertView.findViewById(R.id.message_row_txt_date);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Chat chat = getItem(position);
            viewHolder.mTxtFrom.setText(chat.getOtherParty().username);
            viewHolder.mTxtMessage.setText(chat.lastMessage.text);
            viewHolder.mTxtDate.setText(Helper.formatDate(chat.lastMessageTimestamp));

            if (chat.getOtherParty().image != null) {
                Picasso.with(getContext()).load(chat.getOtherParty().image.getImageUrl()).into(viewHolder.mImgAccountPhoto);
            }

            return convertView;
        }

        private static class ViewHolder{
            private TextView mTxtFrom;
            private TextView mTxtMessage;
            private TextView mTxtDate;
            private ImageView mImgAccountPhoto;
        }
    }

    private MeteorBroadcastReceiver meteorBroadcastReceiver = new MeteorBroadcastReceiver() {
        @Override
        public void connected() {
            if (messagesRequestPending){
                loadChats();
            }
        }

        @Override
        public void disconnected() {

        }
    };
}
