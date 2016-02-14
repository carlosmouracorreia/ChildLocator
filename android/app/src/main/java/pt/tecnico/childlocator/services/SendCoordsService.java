package pt.tecnico.childlocator.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pt.tecnico.childlocator.helper.EncryptNetworkController;
import pt.tecnico.childlocator.helper.SessionManager;
import pt.tecnico.childlocator.main.ChildActivity;

/**
 * Created by carloscorreia on 28/11/15.
 */
public class SendCoordsService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private long childID;
    private RequestQueue mRequestQueue;


    private static final String TAG = "LocationService";

    private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private long child_id;
    private SessionManager session;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        session = new SessionManager(this);
        child_id = session.getChildId();

        if(child_id==0)
            stopSelf();

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        Log.d("CHILD ID", String.valueOf(child_id));

        // if we are currently trying to get a location and the alarm manager has called this again,
        // no need to start processing a new location.
        if (!currentlyProcessingLocation) {
            currentlyProcessingLocation = true;
            startTracking();
        }

        return START_NOT_STICKY;
    }

    private void startTracking() {
        Log.d(TAG, "startTracking");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }


    public String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void uploadCoords(final Location location) {

        EncryptNetworkController updateController = new EncryptNetworkController(TAG) {
            @Override
            public JSONObject getParams() {
                JSONObject json = new JSONObject();
                JSONObject data = new JSONObject();
                try {


                    data.put("child_id",child_id);
                    data.put("lat",Double.toString(location.getLatitude()));
                    data.put("lon",Double.toString(location.getLongitude()));
                    data.put("accuracy",Float.toString(location.getAccuracy()));
                    data.put("last_update",getCurrentTimestamp());

                    json.put("action", "update_location");
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
                Log.e(TAG, "Error: " + error.getMessage());
            }
        };

        Response.Listener<String> success = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "SendCoordsService Response: " + response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    String status = jObj.getString("status");
                    //In case child does not exist anymore on the server
                    if (status.equals("error")) {
                        if(jObj.getString("type").equals("child_not_found")) {
                            Intent RTReturn = new Intent(ChildActivity.TERMINATE);
                            LocalBroadcastManager.getInstance(SendCoordsService.this).sendBroadcast(RTReturn);
                            session.removeAllData();
                            //not guaranteed to stop AlarmThing... verify
                        }
                    }


                } catch (JSONException e) {
                   e.printStackTrace();
                }
            }
        };

        Log.d(TAG,"CONNECTING TO REMOTE SERVER");
        updateController.fetchDataService(success, error,mRequestQueue);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.e(TAG, "position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());

            // we have our desired accuracy of 500 meters so lets quit this service,
            // onDestroy will be called and stop our location uodates
            if (location.getAccuracy() < 500.0f) {
                stopLocationUpdates();

                uploadCoords(location);

                session.setLastLatitude((float) location.getLatitude());
                session.setLastLongitude((float) location.getLongitude());

                Intent RTReturn = new Intent(ChildActivity.RECEIVE_COORDS);
                LocalBroadcastManager.getInstance(this).sendBroadcast(RTReturn);
            }
        }
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        currentlyProcessingLocation = false;
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // milliseconds
        locationRequest.setFastestInterval(1000); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");

        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }

}