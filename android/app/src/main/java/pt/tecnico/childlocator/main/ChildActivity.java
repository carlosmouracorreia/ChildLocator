package pt.tecnico.childlocator.main;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pt.tecnico.childlocator.helper.AppConfig;
import pt.tecnico.childlocator.helper.EncryptNetworkController;
import pt.tecnico.childlocator.helper.SessionManager;
import pt.tecnico.childlocator.services.CoordsIntervalReceiver;

public class ChildActivity extends Activity {

    public static final String TAG = "CHILD MAIN";

    private TextView childName,coordsDebug;
    private Toolbar mToolbar;
    private SessionManager session;
    private ImageView sosButton;
    private long childId;
    private ProgressDialog pDialog;


    //Your activity will respond to this action String
    public static final String RECEIVE_COORDS = "pt.tecnico.childlocator.RECEIVE_COORDS";
    public static final String TERMINATE = "pt.tecnico.childlocator.TERMINATE";

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(RECEIVE_COORDS)) {
                if(coordsDebug!=null) {
                    String location = String.valueOf(session.getLastLatitude()) + " : " + String.valueOf(session.getLastLongitude());
                    coordsDebug.setText(location);
                }
            } else if(intent.getAction().equals(TERMINATE)) {
                Log.d(TAG,"FINISH MF!");
                finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(getApplicationContext());

        //In case someone bypass the login phase, folk is redirected to UnloggedActivity
        if (!session.isLoggedIn()) {
            Intent intent = new Intent(ChildActivity.this,
                    UnloggedActivity.class);

            startActivity(intent);
            finish();
        }

        //In case we previously had a Parent Registered on this device
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getBaseContext());
        try {
            gcm.unregister();
        }
        catch (IOException e) {
            System.out.println("Error Message: " + e.getMessage());
        }

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_COORDS);
        intentFilter.addAction(TERMINATE);
        bManager.registerReceiver(bReceiver, intentFilter);

        setContentView(R.layout.activity_child);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setCancelable(false);

        childId = session.getChildId();
        //Start the Sending Coordinates Service in a Separate thread, always running even if the app is not
        scheduleCoords();

        sosButton = (ImageView) findViewById(R.id.sosButton);
        childName = (TextView) findViewById(R.id.childName);
        coordsDebug = (TextView) findViewById(R.id.coordsDebug);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextColor(0xFFFFFFFF);
        childName.setText(session.getChildName());
        String location = String.valueOf(session.getLastLatitude()) + " : " + String.valueOf(session.getLastLongitude());
        coordsDebug.setText(location);

        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySos();
            }
        });
    }


    public void trySos() {
        pDialog.show();

        EncryptNetworkController sosController = new EncryptNetworkController(TAG) {
            @Override
            public JSONObject getParams() {
                JSONObject json = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    data.put("child_id",session.getChildId());

                    json.put("action", "sos_child");
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
                Log.d(TAG, "SOS Response: " + response);

                try {
                    JSONObject jObj = new JSONObject(response);
                    String status = jObj.getString("status");
                    // Check for error node in json
                    pDialog.hide();
                    if (status.equals("success")) {
                        new AlertDialog.Builder(ChildActivity.this)
                                .setTitle(R.string.sos_alert)
                                .setMessage(R.string.sos_sent)
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                    }
                                })

                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();


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

        sosController.fetchData(success,error);
    }

    public void scheduleCoords() {
        Log.d(TAG, "startAlarmManager");
        Context context = getBaseContext();
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent gpsTrackerIntent = new Intent(context, CoordsIntervalReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);


        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                AppConfig.SEND_COORDINATES_INTERVAL,
                pendingIntent);
    }

}
