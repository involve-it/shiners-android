package com.involveit.shiners.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.involveit.shiners.R;
import com.involveit.shiners.logic.Constants;
import com.involveit.shiners.logic.JsonProvider;
import com.involveit.shiners.logic.objects.Chat;
import com.involveit.shiners.logic.objects.Message;
import com.involveit.shiners.logic.objects.response.ResponseBase;
import com.involveit.shiners.logic.objects.response.SendMessageResponse;
import com.involveit.shiners.logic.proxies.MessagesProxy;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

public class DialogActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DialogActivity";

    public static final String EXTRA_CHAT = "com.involveit.shiners.DialogActivity.extras.CHAT";
    public static final String EXTRA_REQUEST_ID = "com.involveit.shiners.DialogActivity.extras.REQUEST_ID";

    private Chat chat;
    private UUID requestId;
    private ListView listView;
    private EditText txtMessage;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        chat = getIntent().getParcelableExtra(EXTRA_CHAT);
        requestId = (UUID) getIntent().getSerializableExtra(EXTRA_REQUEST_ID);

        listView = (ListView) findViewById(R.id.list_view_messages);
        txtMessage = (EditText) findViewById(R.id.txt_message);
        btnSend = (Button)findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);

        getSupportActionBar().setTitle(chat.getOtherParty().username);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d(TAG, "requestId: " + requestId.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(messagesBroadcastReceiver, new IntentFilter(MessagesProxy.BROADCAST_GET_MESSAGES));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(messagesBroadcastReceiver);
    }

    private BroadcastReceiver messagesBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MessagesProxy.BROADCAST_GET_MESSAGES.equals(action)){
                UUID receivedRequestId = (UUID) intent.getSerializableExtra(MessagesProxy.EXTRA_REQUEST_ID);
                Log.d(TAG, "messages received for id: " + receivedRequestId);

                if (receivedRequestId.equals(requestId)){
                    ArrayList<Message> messages = MessagesProxy.getMessagesResult(receivedRequestId);
                    if (messages != null) {
                        MessagesArrayAdapter adapter = new MessagesArrayAdapter(DialogActivity.this, 0, messages);
                        listView.setAdapter(adapter);
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
            request.put("destinationUserId", chat.getOtherParty().id);

            MeteorSingleton.getInstance().call(Constants.MethodNames.ADD_MESSAGE, new Object[]{request}, new ResultListener() {
                @Override
                public void onSuccess(String result) {
                    SendMessageResponse response = JsonProvider.defaultGson.fromJson(result, SendMessageResponse.class);
                    if (response.success){
                        Message message = new Message();
                        message.timestamp = new Date();
                        message.toUserId = chat.getOtherParty().id;
                        message.chatId = chat.id;
                        message.text = txtMessage.getText().toString();
                        message.id = response.result;
                        message.userId = MeteorSingleton.getInstance().getUserId();

                        ((MessagesArrayAdapter)listView.getAdapter()).add(message);
                        txtMessage.post(new Runnable() {
                            @Override
                            public void run() {
                                txtMessage.setText("");
                            }
                        });
                    } else {
                        Toast.makeText(DialogActivity.this, "An error occurred while sending message", Toast.LENGTH_SHORT).show();
                    }

                    enableControls(true);
                }

                @Override
                public void onError(String error, String reason, String details) {
                    enableControls(true);
                    Toast.makeText(DialogActivity.this, "An error occurred while sending message", Toast.LENGTH_SHORT).show();
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

    private static class MessagesArrayAdapter extends ArrayAdapter<Message> {
        private static final int VIEW_TYPE_COUNT = 2;
        private static final int VIEW_TYPE_INCOMING = 0;
        private static final int VIEW_TYPE_OUTGOING = 1;

        public MessagesArrayAdapter(Context context, int resource, List<Message> objects) {
            super(context, resource, objects);
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            Message message = getItem(position);
            if (message.toUserId.equals(MeteorSingleton.getInstance().getUserId())){
                return VIEW_TYPE_INCOMING;
            } else {
                return VIEW_TYPE_OUTGOING;
            }
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
                    default:
                        throw new Error("DialogActivity: view type is not supported");
                }

                LayoutInflater li = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(layout, parent, false);
                convertView.setTag(itemViewType);
            }

            Message message = getItem(position);

            TextView txtMessage = (TextView) convertView.findViewById(R.id.row_message_txt_message);
            txtMessage.setText(message.text);

            return convertView;
        }
    }
}
