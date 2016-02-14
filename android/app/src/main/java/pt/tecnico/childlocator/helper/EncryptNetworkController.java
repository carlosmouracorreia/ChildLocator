package pt.tecnico.childlocator.helper;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by carloscorreia on 31-11-2015.
 */
public abstract class EncryptNetworkController {
    protected String type;


    public EncryptNetworkController(String type) {
        this.type = type;
    }

    /**
     * Network Request happens here.
     * Consequent actions are performed on methods on Response and onError
     */
    public void fetchData(Response.Listener<String> a,Response.ErrorListener b) {
        final String request = AppController.getInstance().encryptContent(getParams().toString());
        Log.d("Encrypted Key for " + type, request);
        StringRequest myRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_API,a,b) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("X-App", "CLAppIST-1.0");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("query", request);
                return params;
            }
        };
        AppController.getInstance().cancelPendingRequests(String.valueOf(type));
        AppController.getInstance().addToRequestQueue(myRequest, String.valueOf(type));
    }

    /**
     * In case we don't have an app going (while using services)  on so we need to pass the request queue and all
     */
    public void fetchDataService(Response.Listener<String> a,Response.ErrorListener b,RequestQueue c) {
        final String request = AppController.getInstance().encryptContent(getParams().toString());
        Log.d("Encrypted Key for " + type, request);
        StringRequest myRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_API,a,b) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("X-App", "CLAppIST-1.0");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("query", request);
                return params;
            }
        };
        c.cancelAll(String.valueOf(type));
        AppController.addToRequestQueue(c,myRequest, String.valueOf(type));
    }
    public abstract JSONObject getParams();
}
