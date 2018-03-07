package org.buzzar.appPrityazhenie.logic.proxies;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.ArrayMap;

import org.buzzar.appPrityazhenie.logic.Constants;
import org.buzzar.appPrityazhenie.logic.JsonProvider;
import org.buzzar.appPrityazhenie.logic.objects.Message;
import org.buzzar.appPrityazhenie.logic.objects.response.GetMessagesResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;

/**
 * Created by yury on 2/1/17.
 */

public final class MessagesProxy {
    public static final String BROADCAST_GET_MESSAGES = "com.involveit.shiners.MessagesProxy.broadcast.GET_MESSAGES";
    public static final String EXTRA_SUCCESS = "com.involveit.shiners.MessagesProxy.extra.SUCCESS";
    public static final String EXTRA_REQUEST_ID = "com.involveit.shiners.MessagesProxy.extra.REQUEST_ID";

    private static ArrayMap<UUID, ArrayList<Message>> messagesRequests = new ArrayMap<>();

    public static UUID startGettingMessagesAsync(final Context context, String chatId, int skip, int take){
        final UUID requestId = UUID.randomUUID();

        HashMap<String, Object> request = new HashMap<>();
        request.put("chatId", chatId);
        request.put("skip", skip);
        request.put("take", take);

        MeteorSingleton.getInstance().call(Constants.MethodNames.GET_MESSAGES, new Object[]{request}, new ResultListener() {
            @Override
            public void onSuccess(String result) {
                GetMessagesResponse response = JsonProvider.defaultGson.fromJson(result, GetMessagesResponse.class);

                if (response.success){

                    Collections.sort(response.result, new Comparator<Message>() {
                        @Override
                        public int compare(Message message, Message t1) {
                            if (message.timestamp.after(t1.timestamp)){
                                return 1;
                            } else if (message.timestamp.before(t1.timestamp)){
                                return -1;
                            }
                            return 0;
                        }
                    });

                    messagesRequests.put(requestId, response.result);
                }
                Intent intent = new Intent(BROADCAST_GET_MESSAGES);
                intent.putExtra(EXTRA_SUCCESS, response.success);
                intent.putExtra(EXTRA_REQUEST_ID, requestId);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            @Override
            public void onError(String error, String reason, String details) {
                Intent intent = new Intent(BROADCAST_GET_MESSAGES);
                intent.putExtra(EXTRA_SUCCESS, false);
                intent.putExtra(EXTRA_REQUEST_ID, requestId);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

        return requestId;
    }

    public static ArrayList<Message> getMessagesResult(UUID requestId){
        if (messagesRequests.containsKey(requestId)){
            return messagesRequests.remove(requestId);
        }

        return null;
    }
}
