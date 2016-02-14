package pt.tecnico.childlocator.main.unlogged;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import pt.tecnico.childlocator.helper.EncryptNetworkController;
import pt.tecnico.childlocator.helper.SessionManager;
import pt.tecnico.childlocator.main.ChildActivity;
import pt.tecnico.childlocator.main.R;

/**
 * Created by carloscorreia on 24/11/15.
 */
public class ChildFrag extends Fragment {

    public static final String TAG = "CHILD LOGIN";

    private EditText mToken;
    private Button mChildSignIn;
    private ProgressDialog pDialog;
    private String token;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_child_login, container, false);

        mToken = (EditText) rootView.findViewById(R.id.token);
        mChildSignIn = (Button) rootView.findViewById(R.id.child_sign_in_button);

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setCancelable(false);

        mChildSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        return rootView;
    }

    public void attemptLogin() {
        mToken.setError(null);
        token = mToken.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(!isTokenValid(token)) {
            mToken.setError(getString(R.string.error_invalid_token_length));
            focusView = mToken;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            pDialog.show();

            EncryptNetworkController childLoginController = new EncryptNetworkController(TAG) {
                @Override
                public JSONObject getParams() {
                    JSONObject json = new JSONObject();
                    JSONObject data = new JSONObject();
                    try {
                        data.put("token",token);

                        json.put("action", "pair_child");
                        json.put("data", data);

                        return json;
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            Response.ErrorListener error = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pDialog.hide();
                    Log.e(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.error_network_unknown, Toast.LENGTH_LONG).show();
                }
            };

            Response.Listener<String> success = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Login Response: " + response);

                    try {
                        JSONObject jObj = new JSONObject(response);
                        String status = jObj.getString("status");
                        pDialog.hide();
                        if (status.equals("success")) {

                            Intent intent = new Intent(getActivity(),
                                    ChildActivity.class);
                            //Here we save user data locally
                            SessionManager session = new SessionManager(getActivity().getApplicationContext());
                            session.setLogin(true);
                            session.setId(jObj.getJSONObject("data").getLong("id"));
                            session.setName(jObj.getJSONObject("data").getString("name"));

                            startActivity(intent);
                            getActivity().finish();

                        } else if(status.equals("error")){

                            String type = jObj.getString("type");
                            if(type.equals("pair_failed")) {
                                mToken.setError(jObj.getString("message"));
                                mToken.requestFocus();
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(),
                                        jObj.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        }


                    } catch (JSONException e) {
                        pDialog.hide();
                        Toast.makeText(getActivity().getApplicationContext(),
                                getString(R.string.error_server_internal), Toast.LENGTH_LONG).show();
                    }
                }
            };

            childLoginController.fetchData(success,error);
        }
    }


    private boolean isTokenValid(String token) {
        return token.length() == 12;
    }
}

