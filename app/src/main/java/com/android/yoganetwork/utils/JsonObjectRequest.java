package com.android.yoganetwork.utils;

import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonObjectRequest {
    public com.android.volley.toolbox.JsonObjectRequest jsonObjectRequest(String url, JSONObject senderJsonObj) {
        return new com.android.volley.toolbox.JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                response -> {
                    //response of the request
                    Log.d("JSON_RESPONSE", "onResponse: "+response.toString());
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("JSON_RESPONSE", "onResponse: "+error.toString());

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put params
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAAQamk8xY:APA91bHY2PqvH237jhVIXZEI0OlvUQACRVffSLfv_pU7gmO1EZL2wcV2J52AFpC3QL5H16DSsAUHwJ2T7nXiVAYgPGuMmyPXRs8efYuZlOWvIttxIl49GsrMw54939LA8gBFsXGp41S7");

                return headers;
            }
        };
    }
}
