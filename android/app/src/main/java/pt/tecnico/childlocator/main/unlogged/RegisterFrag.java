package pt.tecnico.childlocator.main.unlogged;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import pt.tecnico.childlocator.helper.CommonFuncs;
import pt.tecnico.childlocator.helper.EncryptNetworkController;
import pt.tecnico.childlocator.main.R;
import pt.tecnico.childlocator.main.UnloggedActivity;

/**
 * Created by carloscorreia on 24/11/15.
 */
public class RegisterFrag extends Fragment {

    public static final String TAG = "REGISTER";

    private EditText mUsername,mPassword,mRepeatPwd, mName;
    private Button register;
    private ProgressDialog pDialog;
    private String email,password,name,salt;
    private boolean shownMessage = false;
    private  Response.ErrorListener error;
    private EncryptNetworkController registerController, saltController;

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

        registerController = new EncryptNetworkController(TAG) {
            @Override
            public JSONObject getParams() {
                JSONObject json = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    data.put("email",email);
                    data.put("pwd",CommonFuncs.getPwdHash(password,salt));
                    data.put("name",name);
                    data.put("salt",salt);

                    json.put("action", "register");
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
                    data.put("generate",1);

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
        View rootView = inflater.inflate(R.layout.fragment_register, container, false);

        mUsername = (EditText) rootView.findViewById(R.id.username);
        mPassword = (EditText) rootView.findViewById(R.id.password);
        mRepeatPwd = (EditText) rootView.findViewById(R.id.password_repeat);
        mName = (EditText) rootView.findViewById(R.id.person_name);
        register = (Button) rootView.findViewById(R.id.register);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setCancelable(false);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mPassword.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!shownMessage){
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.password_info)
                            .setMessage(R.string.password_info2)
                            .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })

                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    shownMessage = true;
                }
                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return false;
            }
        });

        return rootView;
    }

    public void attemptRegister() {
        mUsername.setError(null);
        mPassword.setError(null);
        mRepeatPwd.setError(null);
        mName.setError(null);

        email = mUsername.getText().toString();
        password = mPassword.getText().toString();
        String pwdRepeat = mRepeatPwd.getText().toString();
        name = mName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            mUsername.setError(getString(R.string.error_field_required));
            focusView = mUsername;
            cancel = true;
        } else if (!CommonFuncs.isEmailValid(email)) {
            mUsername.setError(getString(R.string.error_invalid_email));
            focusView = mPassword;
            cancel = true;
        }


        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.error_password_empty));
            focusView = mPassword;
            cancel = true;
        }

        if (!CommonFuncs.isPasswordValid(password)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.password_info)
                    .setMessage(R.string.password_info2)
                    .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })

                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;

        } else if (!password.equals(pwdRepeat)) {
            mRepeatPwd.setError(getString(R.string.non_match_pwd));
            focusView = mRepeatPwd;
            cancel = true;
        }

        if (!CommonFuncs.isNameValid(name)) {
            mName.setError(getString(R.string.error_invalid_name));
            focusView = mName;
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
                            actualRegister();
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

    public void actualRegister() {

         Response.Listener<String> success = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Register Response: " + response);

                    try {
                        JSONObject jObj = new JSONObject(response);
                        String status = jObj.getString("status");
                        // Check for error node in json
                        pDialog.hide();
                        if (status.equals("success")) {
                            mUsername.setText("");
                            mPassword.setText("");
                            mRepeatPwd.setText("");
                            mName.setText("");
                            Toast.makeText(getActivity().getApplicationContext(),
                                    jObj.getString("message"), Toast.LENGTH_LONG).show();
                            UnloggedActivity.getInstance().getPager().setCurrentItem(0);

                        } else if(status.equals("error")){

                            String type = jObj.getString("type");
                            if(type.equals("email_exists")) {
                                mUsername.setError(jObj.getString("message"));
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

            registerController.fetchData(success,error);

    }


}

