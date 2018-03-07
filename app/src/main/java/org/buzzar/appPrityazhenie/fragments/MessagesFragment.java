package org.buzzar.appPrityazhenie.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
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

import org.buzzar.appPrityazhenie.R;
import org.buzzar.appPrityazhenie.activities.DialogActivity;
import org.buzzar.appPrityazhenie.logic.Constants;
import org.buzzar.appPrityazhenie.logic.Helper;
import org.buzzar.appPrityazhenie.logic.JsonProvider;
import org.buzzar.appPrityazhenie.logic.analytics.AnalyticsProvider;
import org.buzzar.appPrityazhenie.logic.analytics.TrackingKeys;
import org.buzzar.appPrityazhenie.logic.cache.CacheEntity;
import org.buzzar.appPrityazhenie.logic.cache.CachingHandler;
import org.buzzar.appPrityazhenie.logic.objects.Chat;
import org.buzzar.appPrityazhenie.logic.objects.response.GetChatsResponse;
import org.buzzar.appPrityazhenie.logic.objects.response.ResponseBase;
import org.buzzar.appPrityazhenie.logic.proxies.MessagesProxy;
import org.buzzar.appPrityazhenie.logic.ui.MeteorFragmentBase;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class MessagesFragment extends MeteorFragmentBase implements AdapterView.OnItemLongClickListener {
    private static final String TAG = "MessagesFragment";
    public MessagesFragment() {
        // Required empty public constructor
    }

    private boolean messagesRequestPending = true;
    private ProgressDialog progressDialog;
    private ListView listView;
    private SwipeRefreshLayout layout;
    private boolean loading = false;
    //private ArrayList<Chat> chats = new ArrayList<>();
    private boolean moreAvailable = true;

    public static MessagesFragment newInstance() {
        return new MessagesFragment();
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
                UUID requestId = MessagesProxy.startGettingMessagesAsync(getActivity(), chat._id, 0, Constants.Defaults.DEFAULT_MESSAGES_PAGE);
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
                if (moreAvailable && totalItemCount - (firstVisibleItem + visibleItemCount) < 10 && totalItemCount > 0){
                    loadChats(true);
                }
            }
        });
        listView.setOnItemLongClickListener(this);

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

            ArrayList<Chat> chatsFiltered = new ArrayList<>();
            for(Chat chat : cache.getObject()) {
                if(chat.lastMessage != null) {
                    chatsFiltered.add(chat);
                }
            }
            cache.setObject(chatsFiltered);

            populateListView(cache.getObject(), false);

            if (!cache.isStale()){
                messagesRequestPending = false;
            }
        }

        if (messagesRequestPending){
            loadChats(false);
        }

        AnalyticsProvider.LogScreen(getActivity(), TrackingKeys.Screens.MESSAGES);

        return view;
    }

    private void populateListView(ArrayList<Chat> chats, boolean append){
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
        if (!loading) {
            loading = true;
            final ChatsArrayAdapter adapter = (ChatsArrayAdapter) listView.getAdapter();
            int chatsCount = 0;
            if (adapter != null) {
                chatsCount = adapter.getCount();
                adapter.notifyDataSetChanged();
            }

            messagesRequestPending = false;
            HashMap<String, Object> map = new HashMap<>();
            map.put("take", Constants.Defaults.DEFAULT_DIALOGS_PAGE);
            map.put("skip", (loadMore ? chatsCount : 0));

            callMeteorMethod(Constants.MethodNames.GET_CHATS, new Object[]{map}, new ResultListener() {
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

                        ArrayList<Chat> chatsFiltered = new ArrayList<>();
                        for(Chat chat : res.result) {
                            if(chat.lastMessage != null) {
                                chatsFiltered.add(chat);
                            }
                        }

                        res.result = chatsFiltered;

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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        new AlertDialog.Builder(getActivity()).setItems(R.array.chats_list_actions_menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    deleteChat(position);
                    dialog.dismiss();
                }
            }
        }).show();

        return true;
    }

    private void deleteChat(int index){
        final ChatsArrayAdapter adapter = (ChatsArrayAdapter) listView.getAdapter();
        final Chat chat = adapter.getItem(index);
        if (chat != null){
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.dialog_deleting_dialog));
            progressDialog.setCancelable(false);
            progressDialog.show();

            callMeteorMethod(Constants.MethodNames.DELETE_CHATS, new Object[]{new String[]{chat._id}}, new ResultListener() {
                @Override
                public void onSuccess(String result) {
                    final ResponseBase response = JsonProvider.defaultGson.fromJson(result, ResponseBase.class);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.success){
                                adapter.remove(chat);
                                adapter.notifyDataSetChanged();
                                CachingHandler.setObject(getActivity(), CachingHandler.KEY_DIALOGS, adapter.getChats());
                            } else {
                                Toast.makeText(getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                            }

                            progressDialog.dismiss();
                        }
                    });

                }

                @Override
                public void onError(String error, String reason, String details) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), R.string.message_internal_error, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
                }
            });
        }
    }

    private class ChatsArrayAdapter extends ArrayAdapter<Chat>{
        private static final int VIEW_TYPE_CHAT = 0;
        private static final int VIEW_TYPE_LOADING = 1;
        private static final int VIEW_TYPE_COUNT = 2;

        private List<Chat> chats;

        public ChatsArrayAdapter(Context context, int resource, List<Chat> objects) {
            super(context, resource, objects);
            chats = objects;
        }

        public List<Chat> getChats(){
            return chats;
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
                viewHolder.mTxtMessage.setText(chat.lastMessage.text);
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
}
