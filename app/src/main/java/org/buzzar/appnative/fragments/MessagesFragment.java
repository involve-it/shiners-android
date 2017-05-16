package org.buzzar.appnative.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.buzzar.appnative.R;
import org.buzzar.appnative.activities.DialogActivity;
import org.buzzar.appnative.logic.Constants;
import org.buzzar.appnative.logic.Helper;
import org.buzzar.appnative.logic.JsonProvider;
import org.buzzar.appnative.logic.MeteorBroadcastReceiver;
import org.buzzar.appnative.logic.cache.CacheEntity;
import org.buzzar.appnative.logic.cache.CachingHandler;
import org.buzzar.appnative.logic.objects.Chat;
import org.buzzar.appnative.logic.objects.response.GetChatsResponse;
import org.buzzar.appnative.logic.proxies.MessagesProxy;

import com.amazonaws.util.StringUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private SwipeRefreshLayout layout;
    private boolean loading = false;
    private ArrayList<Chat> chats = new ArrayList<>();
    private boolean moreAvailable = true;

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
                UUID requestId = MessagesProxy.startGettingMessagesAsync(getActivity(), chat.id, 0, Constants.Defaults.DEFAULT_MESSAGES_PAGE);
                Intent intent = new Intent(getActivity(), DialogActivity.class);
                intent.putExtra(DialogActivity.EXTRA_CHAT, (Parcelable) chat);
                intent.putExtra(DialogActivity.EXTRA_REQUEST_ID, requestId);
                startActivity(intent);
            }
        });
        listView.setEmptyView(view.findViewById(R.id.dialogs_list_empty_view));
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (moreAvailable && totalItemCount - (firstVisibleItem + visibleItemCount) < 10){
                    loadChats(true);
                }
            }
        });


        layout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_messages_layout);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!loading && MeteorSingleton.getInstance().isConnected()){
                    loadChats(false);
                } else if (!MeteorSingleton.getInstance().isConnected()){
                    messagesRequestPending = true;
                }
            }
        });

        CacheEntity<ArrayList<Chat>> cache = CachingHandler.getCacheObject(getActivity(), CachingHandler.KEY_DIALOGS);

        if (cache == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getText(R.string.message_loading_chats));
            progressDialog.show();
            progressDialog.setCancelable(false);
        } else {
            populateListView(cache.getObject(), false);

            if (!cache.isStale()){
                messagesRequestPending = false;
            }
        }

        if (messagesRequestPending && MeteorSingleton.getInstance().isConnected()){
            loadChats(false);
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

    private void populateListView(ArrayList<Chat> chats, boolean append){

        ArrayList<Chat> chatsFiltered = new ArrayList<>();
        for(Chat chat : chats) {
            if(chat.lastMessage != null) {
                chatsFiltered.add(chat);
            }
        }
        chats = chatsFiltered;

        ChatsArrayAdapter adapter = (ChatsArrayAdapter) listView.getAdapter();
        moreAvailable = chats.size() == Constants.Defaults.DEFAULT_DIALOGS_PAGE;
        if (adapter == null){
            adapter = new ChatsArrayAdapter(getActivity(), R.layout.row_messages, chats);
            adapter.setNotifyOnChange(true);
            listView.setAdapter(adapter);
        } else {
            if (append){
                adapter.addAll(chats);
            } else {
                Helper.mergeDataToArrayAdapter(chats, adapter, true);
            }
        }
    }

    private void loadChats(final boolean loadMore){
        if (!loading && MeteorSingleton.getInstance().isConnected()) {
            loading = true;
            final ChatsArrayAdapter adapter = (ChatsArrayAdapter) listView.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            messagesRequestPending = false;
            HashMap<String, Object> map = new HashMap<>();
            map.put("take", Constants.Defaults.DEFAULT_DIALOGS_PAGE);
            map.put("skip", (loadMore ? chats.size() : 0));

            MeteorSingleton.getInstance().call(Constants.MethodNames.GET_CHATS, new Object[]{map}, new ResultListener() {
                @Override
                public void onSuccess(String result) {
                    loading = false;
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    GetChatsResponse res = JsonProvider.defaultGson.fromJson(result, GetChatsResponse.class);
                    if (res.success) {
                        populateListView(res.result, loadMore);
                        CachingHandler.setObject(getActivity(), CachingHandler.KEY_DIALOGS, res.result);
                    } else {
                        Toast.makeText(getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                    }

                    layout.setRefreshing(false);
                }

                @Override
                public void onError(String error, String reason, String details) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    Log.d(TAG, "Error: " + error);
                    Log.d(TAG, "Reason: " + reason);
                    Log.d(TAG, "Details: " + details);
                    Toast.makeText(getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                    loading = false;
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    layout.setRefreshing(false);
                }
            });
        }
    }

    private class ChatsArrayAdapter extends ArrayAdapter<Chat>{
        private static final int VIEW_TYPE_CHAT = 0;
        private static final int VIEW_TYPE_LOADING = 1;
        private static final int VIEW_TYPE_COUNT = 2;

        public ChatsArrayAdapter(Context context, int resource, List<Chat> objects) {
            super(context, resource, objects);
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (loading && position == getCount() - 1){
                return VIEW_TYPE_LOADING;
            } else {
                return VIEW_TYPE_CHAT;
            }
        }

        @Override
        public int getCount() {
            return super.getCount() + (loading ? 1 : 0);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //return super.getView(position, convertView, parent);
            ViewHolder viewHolder;
            int viewType = getItemViewType(position);
            if (convertView == null){
                LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                viewHolder = new ViewHolder();
                if(viewType == VIEW_TYPE_CHAT) {
                    convertView = li.inflate(R.layout.row_messages, parent, false);
                    viewHolder.mTxtFrom = (TextView) convertView.findViewById(R.id.message_row_txt_from);
                    viewHolder.mTxtMessage = (TextView) convertView.findViewById(R.id.message_row_txt_message);
                    viewHolder.mImgAccountPhoto = (ImageView) convertView.findViewById(R.id.messages_row_img);
                    viewHolder.mTxtDate = (TextView) convertView.findViewById(R.id.message_row_txt_date);
                    convertView.setTag(viewHolder);
                } else {
                    convertView = li.inflate(R.layout.row_loading_more, parent, false);
                    ((TextView)convertView.findViewById(R.id.row_loading_more_txt)).setText(R.string.row_chats_loading_label);
                }
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (viewType == VIEW_TYPE_CHAT) {
                Chat chat = getItem(position);
                viewHolder.mTxtFrom.setText(chat.getOtherParty().username);
                viewHolder.mTxtMessage.setText(chat.lastMessage == null ? "" : chat.lastMessage.text);
                viewHolder.mTxtDate.setText(Helper.formatDate(getContext(), chat.lastMessageTimestamp));

                if (chat.getOtherParty().image != null) {
                    Picasso.with(getContext()).load(chat.getOtherParty().image.getImageUrl()).into(viewHolder.mImgAccountPhoto);
                }
            }

            return convertView;
        }

        private class ViewHolder{
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
                loadChats(false);
            }
        }

        @Override
        public void disconnected() {

        }
    };
}
