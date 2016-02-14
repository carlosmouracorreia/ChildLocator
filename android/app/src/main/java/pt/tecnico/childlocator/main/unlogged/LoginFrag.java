package pt.tecnico.childlocator.main.unlogged;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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

import pt.tecnico.childlocator.helper.CommonFuncs;
import pt.tecnico.childlocator.helper.EncryptNetworkController;
import pt.tecnico.childlocator.helper.SessionManager;
import pt.tecnico.childlocator.main.ParentActivity;
import pt.tecnico.childlocator.main.R;

/**
 * Created by carloscorreia on 24/11/15.
 */
public class LoginFrag extends Fragment {

    public static final String TAG = "LOGIN";


    private EditText mUsername, mPassword;
    private Button signIn;
    private ProgressDialog pDialog;
    private String email,password,salt;
    private Response.ErrorListener error;
    private EncryptNetworkController loginController,saltController;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        error = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.hide();
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getActivity().getApplicationContext(),
                        R.string.error_network_unknown, Toast.LENGTH_LONG).show();
            }
        };

        loginController = new EncryptNetworkController(TAG) {
            @Override
            public JSONObject getParams() {
                JSONObject json = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    data.put("email",email);
                    data.put("pwd",CommonFuncs.getPwdHash(password,salt));

                    json.put("action", "login");
                    json.put("data", data);

                    return json;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        saltController = new EncryptNetworkController(TAG) {
            @Override
            public JSONObject getParams() {
                JSONObject json = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    data.put("email",email);

                    json.put("action", "get_salt");
                    json.put("data", data);

                    return json;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        mUsername = (EditText) rootView.findViewById(R.id.username);
        mPassword = (EditText) rootView.findViewById(R.id.password);
        signIn = (Button) rootView.findViewById(R.id.email_sign_in_button);

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setCancelable(false);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        return rootView;
    }


    public void attemptLogin() {
        mUsername.setError(null);
        mPassword.setError(null);

        email = mUsername.getText().toString();
        password = mPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.error_password_empty));
            focusView = mPassword;
            cancel = true;
        }

        if(!CommonFuncs.isPasswordValid(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mUsername.setError(getString(R.string.error_field_required));
            focusView = mUsername;
            cancel = true;
        } else if (!CommonFuncs.isEmailValid(email)) {
            mUsername.setError(getString(R.string.error_invalid_email));
            focusView = mPassword;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            pDialog.show();

            Response.Listener<String> success = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Salt Response: " + response);

                    try {
                        JSONObject jObj = new JSONObject(response);
                        String status = jObj.getString("status");
                        // Check for error node in json
                        if (status.equals("success")) {
                            salt = jObj.getString("data");
                            actualLogin();
                        } else if(status.equals("error")) {
                            pDialog.hide();
                            mUsername.setError(jObj.getString("message"));
                            mPassword.setError(jObj.getString("message"));
                            mUsername.requestFocus();
                        }
                    } catch (JSONException e) {
                        pDialog.hide();
                        Toast.makeText(getActivity().getApplicationContext(),
                                getString(R.string.error_server_internal), Toast.LENGTH_LONG).show();
                    }
                }
            };

            saltController.fetchData(success, error);

        }
    }


    public void actualLogin() {
        Response.Listener<String> success = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response);

                try {
                    JSONObject jObj = new JSONObject(response);
                    String status = jObj.getString("status");
                    // Check for error node in json
                    pDialog.hide();
                    if (status.equals("success")) {

                        Intent intent = new Intent(getActivity(),
                                ParentActivity.class);
                        intent.putExtra("data", jObj.getJSONObject("data").toString());

                        SessionManager session = new SessionManager(getActivity());
                        session.setParentId(jObj.getJSONObject("data").getJSONObject("user").getLong("id"));

                        startActivity(intent);
                        getActivity().finish();

                    } else if(status.equals("error")){

                        String type = jObj.getString("type");
                        if(type.equals("login_incorrect")) {
                            mUsername.setError(jObj.getString("message"));
                            mPassword.setError(jObj.getString("message"));
                            mUsername.requestFocus();
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

        loginController.fetchData(success,error);
    }
}

