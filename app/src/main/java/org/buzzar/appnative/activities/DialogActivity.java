package org.buzzar.appnative.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.buzzar.appnative.R;
import org.buzzar.appnative.logic.Constants;
import org.buzzar.appnative.logic.Helper;
import org.buzzar.appnative.logic.JsonProvider;
import org.buzzar.appnative.logic.MeteorBroadcastReceiver;
import org.buzzar.appnative.logic.MeteorCallbackHandler;
import org.buzzar.appnative.logic.cache.CacheEntity;
import org.buzzar.appnative.logic.cache.CachingHandler;
import org.buzzar.appnative.logic.objects.Chat;
import org.buzzar.appnative.logic.objects.Message;
import org.buzzar.appnative.logic.objects.response.GetChatResponse;
import org.buzzar.appnative.logic.objects.response.ResponseBase;
import org.buzzar.appnative.logic.objects.response.SendMessageResponse;
import org.buzzar.appnative.logic.proxies.MessagesProxy;
import org.buzzar.appnative.logic.ui.MeteorActivityBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class DialogActivity extends MeteorActivityBase implements View.OnClickListener {
    private static final String TAG = "DialogActivity";

    public static final String EXTRA_CHAT = "com.involveit.shiners.DialogActivity.extras.CHAT";
    public static final String EXTRA_REQUEST_ID = "com.involveit.shiners.DialogActivity.extras.REQUEST_ID";

    private Chat chat;
    private UUID requestId;
    private ListView listView;
    private EditText txtMessage;
    private Button btnSend;
    private boolean loading;
    private boolean moreAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        listView = (ListView) findViewById(R.id.list_view_messages);
        txtMessage = (EditText) findViewById(R.id.txt_message);
        btnSend = (Button)findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (moreAvailable && !loading && firstVisibleItem == 0 && totalItemCount > 0){
                    Log.d(TAG, "Getting more messages...");
                    loading = true;
                    ((MessagesArrayAdapter)listView.getAdapter()).notifyDataSetChanged();
                    requestId = MessagesProxy.startGettingMessagesAsync(DialogActivity.this, chat._id, chat.messages.size(), Constants.Defaults.DEFAULT_MESSAGES_PAGE);
                    //listView.setSelection(0);
                }
            }
        });

        chat = getIntent().getParcelableExtra(EXTRA_CHAT);
        requestId = (UUID) getIntent().getSerializableExtra(EXTRA_REQUEST_ID);
        boolean validIntent = false;
        if (chat == null) {
            final String chatId = getIntent().getStringExtra(Constants.Gcm.EXTRA_ID);
            if (chatId != null){
                validIntent = true;
                CacheEntity<ArrayList<Chat>> chatsCache = CachingHandler.getCacheObject(this, CachingHandler.KEY_DIALOGS);
                if (chatsCache != null){
                    chat = Helper.find(chatsCache.getObject(), chatId);
                }
                loading = true;
                if (chat == null) {
                    callMeteorMethod(Constants.MethodNames.GET_CHAT, new Object[]{chatId}, new ResultListener() {
                        @Override
                        public void onSuccess(String result) {
                            GetChatResponse response = JsonProvider.defaultGson.fromJson(result, GetChatResponse.class);
                            if (response.success){
                                chat = response.result;
                                requestId = MessagesProxy.startGettingMessagesAsync(DialogActivity.this, chatId, 0, Constants.Defaults.DEFAULT_MESSAGES_PAGE);
                            } else {
                                navigateUp();
                            }
                        }

                        @Override
                        public void onError(String error, String reason, String details) {
                            navigateUp();
                        }
                    });
                } else {
                    requestId = MessagesProxy.startGettingMessagesAsync(this, chatId, 0, Constants.Defaults.DEFAULT_MESSAGES_PAGE);
                }
                //((MessagesArrayAdapter)listView.getAdapter()).notifyDataSetChanged();
            }
        } else if (requestId != null) {
            loading = true;
            validIntent = true;
            getSupportActionBar().setTitle(chat.getOtherParty().username);
            ArrayList<Message> messages = MessagesProxy.getMessagesResult(requestId);
            if (messages != null){
                populateMessages(messages);
            }
            //((MessagesArrayAdapter)listView.getAdapter()).notifyDataSetChanged();
        }

        if (!validIntent){
            navigateUp();
        }

        setActivityDefaults(true);
    }

    private void navigateUp(){
        Toast.makeText(this, "An error occurred while loading your conversation", Toast.LENGTH_SHORT).show();
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MessagesProxy.BROADCAST_GET_MESSAGES);
        intentFilter.addAction(MeteorCallbackHandler.BROADCAST_MESSAGE_ADDED);
        LocalBroadcastManager.getInstance(this).registerReceiver(messagesBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(messagesBroadcastReceiver);
    }

    private void setAllMessagesSeen(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> messageIds = new ArrayList<>();
                for (Message message:chat.messages){
                    if (!message.seen){
                        messageIds.add(message._id);
                    }
                }
                if (messageIds.size() > 0) {
                    HashMap<String, Object> request = new HashMap<>();
                    request.put("messageIds", messageIds);
                    callMeteorMethod(Constants.MethodNames.MESSAGES_SET_SEEN, new Object[]{request}, new ResultListener() {
                        @Override
                        public void onSuccess(String result) {
                            ResponseBase response = JsonProvider.defaultGson.fromJson(result, ResponseBase.class);
                            if (response.success){
                                for (Message message:chat.messages){
                                    if (messageIds.contains(message._id)){
                                        message.seen = true;
                                    }
                                }
                            } else {
                                Log.w(TAG, "Failed to set 'seen' on messages");
                            }
                        }

                        @Override
                        public void onError(String error, String reason, String details) {
                            Log.w(TAG, "Failed to set 'seen' on messages");
                        }
                    });
                }
            }
        });
    }

    private void populateMessages(final ArrayList<Message> messages){
        /*int initialPosition = listView.getFirstVisiblePosition();
        View view = listView.getChildAt(initialPosition);
        int top = view == null ? 0 : view.getBottom();*/

        if (messages.size() > Constants.Defaults.DEFAULT_MESSAGES_PAGE){
            messages.remove(messages.size() - 1);
            moreAvailable = true;
        } else {
            moreAvailable = false;
        }

        listView.post(new Runnable() {
            @Override
            public void run() {
                MessagesArrayAdapter adapter = (MessagesArrayAdapter) listView.getAdapter();
                if (adapter == null) {
                    Log.d(TAG, "Messages: first page");
                    chat.messages = messages;
                    adapter = new MessagesArrayAdapter(DialogActivity.this, 0, new ArrayList<>(chat.messages));
                    adapter.setNotifyOnChange(false);
                    listView.setAdapter(adapter);
                    //listView.smoothScrollToPosition(messages.size());
                } else {
                    //listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                    //Helper.mergeDataToArrayAdapter(messages, adapter, false);
                    Log.d(TAG, "Messages: next page. Count: " + messages.size() + ", requestid: " + requestId);
                    int i = 0;
                    //adapter.clear();
                    for(Message message : messages){
                        adapter.insert(message, i);
                        chat.messages.add(i++, message);
                    }
                    //adapter.addAll(new ArrayList<>(chat.messages));
                }
                setAllMessagesSeen();

                loading = false;

                adapter.notifyDataSetChanged();

                listView.setSelection(messages.size());
            }
        });

        requestId = null;
    }

    private BroadcastReceiver messagesBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MessagesProxy.BROADCAST_GET_MESSAGES.equals(action)){
                UUID receivedRequestId = (UUID) intent.getSerializableExtra(MessagesProxy.EXTRA_REQUEST_ID);
                Log.d(TAG, "messages received for _id: " + receivedRequestId);

                if (receivedRequestId.equals(requestId)){
                    ArrayList<Message> messages = MessagesProxy.getMessagesResult(receivedRequestId);
                    if (messages != null) {
                        populateMessages(messages);
                    }
                }
            } else if (MeteorCallbackHandler.BROADCAST_MESSAGE_ADDED.equals(action)){
                Log.d(TAG, "New message received.");
                MessagesArrayAdapter adapter = (MessagesArrayAdapter) listView.getAdapter();
                if (adapter != null) {
                    Message message = intent.getParcelableExtra(MeteorCallbackHandler.EXTRA_COLLECTION_OBJECT);
                    ArrayList<String> messageIds = new ArrayList<>();
                    for (Message msg : chat.messages) {
                        messageIds.add(msg._id);
                    }
                    if (!messageIds.contains(message._id) && message.chatId.equals(chat._id)){
                        adapter.add(message);
                        adapter.notifyDataSetChanged();
                        setAllMessagesSeen();
                    }
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_send){
            enableControls(false);

            HashMap<String, Object> request = new HashMap<>();
            request.put("message", txtMessage.getText().toString());
            request.put("type", "message");
            request.put("destinationUserId", chat.getOtherParty()._id);

            callMeteorMethod(Constants.MethodNames.ADD_MESSAGE, new Object[]{request}, new ResultListener() {
                @Override
                public void onSuccess(String result) {
                    SendMessageResponse response = JsonProvider.defaultGson.fromJson(result, SendMessageResponse.class);
                    if (response.success){
                        txtMessage.post(new Runnable() {
                            @Override
                            public void run() {
                                txtMessage.setText("");
                            }
                        });
                    } else {
                        Toast.makeText(DialogActivity.this, R.string.msg_error_while_sending_message, Toast.LENGTH_SHORT).show();
                    }

                    enableControls(true);
                }

                @Override
                public void onError(String error, String reason, String details) {
                    enableControls(true);
                    Toast.makeText(DialogActivity.this, R.string.msg_error_while_sending_message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void enableControls(final boolean enable){
        txtMessage.post(new Runnable() {
            @Override
            public void run() {
                if (enable){
                    btnSend.setText(R.string.btn_label_send);
                } else {
                    btnSend.setText(R.string.btn_label_sending);
                }
                btnSend.setEnabled(enable);
            }
        });
    }

    private class MessagesArrayAdapter extends ArrayAdapter<Message> {
        private static final int VIEW_TYPE_COUNT = 3;
        private static final int VIEW_TYPE_INCOMING = 0;
        private static final int VIEW_TYPE_OUTGOING = 1;
        private static final int VIEW_TYPE_LOADING_MORE = 2;

        public MessagesArrayAdapter(Context context, int resource, List<Message> objects) {
            super(context, resource, objects);
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 && loading){
                return VIEW_TYPE_LOADING_MORE;
            } else {
                Message message = getItem(position);
                if (message.toUserId.equals(MeteorSingleton.getInstance().getUserId())) {
                    return VIEW_TYPE_INCOMING;
                } else {
                    return VIEW_TYPE_OUTGOING;
                }
            }
        }

        @Override
        public int getCount() {
            return super.getCount() + (loading ? 1 : 0);
        }

        @Nullable
        @Override
        public Message getItem(int position) {
            return super.getItem(position - (loading ? 1 : 0));
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int itemViewType = getItemViewType(position);
            if (convertView == null || (int)convertView.getTag() != itemViewType){
                int layout;

                switch (itemViewType){
                    case VIEW_TYPE_INCOMING:
                        layout = R.layout.row_message_incoming;
                        break;
                    case VIEW_TYPE_OUTGOING:
                        layout = R.layout.row_message_outgoing;
                        break;
                    case VIEW_TYPE_LOADING_MORE:
                        layout = R.layout.row_message_loading_more;
                        break;
                    default:
                        throw new Error("DialogActivity: view type is not supported");
                }

                LayoutInflater li = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(layout, parent, false);
                convertView.setTag(itemViewType);
            }

            if (!loading || position != 0) {
                Message message = getItem(position);

                TextView txtMessage = (TextView) convertView.findViewById(R.id.row_message_txt_message);
                txtMessage.setText(message.text);
            }

            return convertView;
        }
    }
}
