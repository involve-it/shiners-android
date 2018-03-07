package org.buzzar.appPrityazhenie.logic;
import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yury on 2/7/17.
 */

public class CommunicationProvider {
    public static final int HTTP_OK = 200;
    public static final int OK = -1;
    public static final int FAIL = 1;

    private static final int THREAD_COUNT = 4;

    private static Cache mCache = new NoCache();
    private static RequestQueue mRequestQueue;

    static {
        mRequestQueue = new RequestQueue(mCache, new BasicNetwork(new HurlStack()), THREAD_COUNT);
        mRequestQueue.start();
    }

    public static void makeAsyncPostRequestResponse(final String urlString, final Object param, final HttpResponseCallback httpResponseCallback) {
        final String json = JsonProvider.defaultGson.toJson(param);

        final StringRequest request = new StringRequest(Request.Method.POST, urlString, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (httpResponseCallback != null) {
                    httpResponseCallback.responseReceived(OK, response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (httpResponseCallback != null) {
                    httpResponseCallback.responseReceived(FAIL, error.getMessage());
                }
            }
        }){
            @Override
            public byte[] getBody() throws AuthFailureError {
                //return super.getBody();
                return json.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        mRequestQueue.add(request);
    }


    public interface HttpResponseCallback {
        void responseReceived(int statusCode, String body);
    }
}
