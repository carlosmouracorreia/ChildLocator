package pt.tecnico.childlocator.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import pt.tecnico.childlocator.helper.CommonFuncs;
import pt.tecnico.childlocator.helper.EncryptNetworkController;

public class AddActivity extends Activity {

    public static final String TAG = "ADD CHILD";


    private Toolbar mToolbar;
    private EditText mChildName;
    private TextView mToken,mTime;
    private Button addChild;
    private ProgressDialog pDialog;
    private long userId;
    private LinearLayout addChildForm, getTokenForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Intent intent = getIntent();
        userId = intent.getLongExtra("id",0);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mToolbar.setTitle(R.string.add_child_toolbar);
        mToolbar.setTitleTextColor(0xFFFFFFFF);
        mToolbar.inflateMenu(R.menu.toolbar_menu_back);
        mToolbar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                finish();
                return false;
            }
        });

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setCancelable(false);

        mChildName = (EditText) findViewById(R.id.name);
        addChild = (Button) findViewById(R.id.add_child_button);
        mToken = (TextView) findViewById(R.id.token);
        mTime = (TextView) findViewById(R.id.mTime);
        addChildForm = (LinearLayout) findViewById(R.id.add_child_form);
        getTokenForm = (LinearLayout) findViewById(R.id.get_token_form);

        addChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreateChild();
            }
        });

    }

    public String getCurrentTimestampPlusTime() {
        final long ONE_MINUTE_IN_MILLIS=60000;

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "HH:mm:ss", Locale.getDefault());
        Calendar date = Calendar.getInstance();
        long t = date.getTimeInMillis();
        Date newDate = new Date(t  + (5 * ONE_MINUTE_IN_MILLIS));
        return dateFormat.format(newDate);
    }

    public void attemptCreateChild() {
        mChildName.setError(null);
        final String name = mChildName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(!CommonFuncs.isNameValid(name)) {
            mChildName.setError(getString(R.string.error_invalid_name));
            focusView = mChildName;
            cancel = true;
        }


        if (cancel) {
            focusView.requestFocus();

        } else {
            pDialog.show();

            EncryptNetworkController addChildController = new EncryptNetworkController(TAG) {
                @Override
                public JSONObject getParams() {
                    JSONObject json = new JSONObject();
                    JSONObject data = new JSONObject();
                    try {
                        data.put("parent_id",userId);
                        data.put("name",name);

                        json.put("action", "get_token");
                        json.put("data", data);

                        return json;
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

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
                            mToken.setText(jObj.getString("message"));
                            mTime.setText(getString(R.string.further_close_time) + " " + getCurrentTimestampPlusTime() + "!");
                            addChildForm.setVisibility(View.GONE);
                            getTokenForm.setVisibility(View.VISIBLE);

                        } else if(status.equals("error")){
                            Toast.makeText(getApplicationContext(),
                                    jObj.getString("message"), Toast.LENGTH_LONG).show();
                        }


                    } catch (JSONException e) {
                        pDialog.hide();
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.error_server_internal), Toast.LENGTH_LONG).show();
                    }
                }
            };

            Response.ErrorListener error = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    pDialog.hide();
                    Log.e(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            R.string.error_network_unknown, Toast.LENGTH_LONG).show();
                }
            };

            addChildController.fetchData(success,error);

        }
    }

}
