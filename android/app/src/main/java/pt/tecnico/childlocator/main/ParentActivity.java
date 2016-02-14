package pt.tecnico.childlocator.main;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pt.tecnico.childlocator.helper.EncryptNetworkController;
import pt.tecnico.childlocator.helper.SessionManager;
import pt.tecnico.childlocator.services.QuickstartPreferences;
import pt.tecnico.childlocator.services.RegistrationIntentService;

public class ParentActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String TAG = "PARENT MAIN";

    private ListView listView;
    private ChildAdapter adapter;
    private List<Child> list;
    private TextView parentName;
    private Toolbar mToolbar;
    private long userId;
    private ProgressDialog pDialog;
    private GoogleMap mMap;
    private boolean mapLoaded = false;
    private Response.ErrorListener error;
    private SessionManager session;


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    public static final String FOCUS_CHILD = "pt.tecnico.childlocator.FOCUS_CHILD";

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * WE ARE NOT UPDATING DATA IN THIS CASE!
             */
            updateSOS();

        }

    };


    public void updateSOS() {
        if(session.getDangerChildId()==0)
            return;
        for(Child s : list)
            if(s.id == session.getDangerChildId()) {
                int index = list.indexOf(s);
                if (mapLoaded) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(s.marker.getPosition(), 15F);
                    mMap.animateCamera(cu);
                    s.marker.setTitle(s.name + " - SENT YOU SOS!!");
                    s.marker.showInfoWindow();
                    s.warning = true;
                    list.set(index, s);
                    adapter.notifyDataSetChanged();
                    session.setDangerChildId(0);
                    break;
                }
            }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        error = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.hide();
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        R.string.error_network_unknown, Toast.LENGTH_LONG).show();
            }
        };

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.d(TAG,"TOKEN SENT");
                } else {
                    Log.d(TAG, "TOKEN NOT SENT");
                }
            }
        };

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
            Log.d(TAG,"started service");
        }

        setContentView(R.layout.activity_parent);
        list = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);
        parentName = (TextView) findViewById(R.id.parentName);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextColor(0xFFFFFFFF);
        mToolbar.inflateMenu(R.menu.toolbar_menu_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setCancelable(false);

        //ADD CHILD BEHAVIOUR
        mToolbar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                pDialog.show();
                updateMe();
                return false;
            }
        });

        mToolbar.getMenu().getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(ParentActivity.this,
                       AddActivity.class);
                intent.putExtra("id",userId);
             //   intent.putExtra("data",jObj.getJSONObject("data").toString());
                startActivity(intent);
                return false;
            }
        });
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.header_list, listView, false);
        listView.addHeaderView(header);

        adapter = new ChildAdapter();
        listView.setAdapter(adapter);


        Intent intent = getIntent();
        try {
            JSONObject data = new JSONObject(intent.getStringExtra("data"));
            updateView(data);
        } catch (JSONException e) {
            e.printStackTrace();
            pDialog.hide();
            Toast.makeText(getApplicationContext(),
                    getString(R.string.error_server_internal), Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                //finish();
                // We don't want to finish the app just because the user cannot receive notifications
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FOCUS_CHILD);
        bManager.registerReceiver(bReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }


    public void updateView(JSONObject data) {
        try {
            //FIX THIS!
            list = new ArrayList<>();
            JSONObject user = data.getJSONObject("user");
            parentName.setText(user.getString("person_name"));
            userId = user.getLong("id");
            JSONArray childs = data.getJSONArray("childs");
            for (int i = 0; i < childs.length(); i++) {
                JSONObject jsonSolo = (JSONObject) childs.get(i);
                Child s = new Child();
                s.id = jsonSolo.getLong("id");
                s.name = jsonSolo.getString("name");
                s.date = jsonSolo.getString("last_update");
                s.lat = jsonSolo.getDouble("lat");
                s.lon = jsonSolo.getDouble("lon");
                s.warning = false;
                list.add(s);
            }
            adapter.notifyDataSetChanged();
            updateMap();
        } catch (JSONException e) {
            e.printStackTrace();
            pDialog.hide();
            Toast.makeText(getApplicationContext(),
                    getString(R.string.error_server_internal), Toast.LENGTH_LONG).show();
        }

    }

    public void updateMap() {
        if(mMap!=null) {
            mMap.clear();

            for(Child i : list) {
                LatLng location = new LatLng(i.lat, i.lon);

                Marker m = mMap.addMarker(new MarkerOptions().position(location).title(i.name));
                i.marker = m;

            }

        }

    }

    public void updateMe() {

        EncryptNetworkController updateController = new EncryptNetworkController(TAG) {
            @Override
            public JSONObject getParams() {
                JSONObject json = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    data.put("user_id",userId);

                    json.put("action", "update_parent");
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
                Log.d(TAG, "Update Response: " + response);

                try {
                    JSONObject jObj = new JSONObject(response);
                    String status = jObj.getString("status");
                    // Check for error node in json
                    pDialog.hide();
                    if (status.equals("success")) {
                        JSONObject data = jObj.getJSONObject("data");
                        updateView(data);

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

        updateController.fetchData(success,error);


    }


    public void tryDelete(final long id) {
        pDialog.show();

        EncryptNetworkController deleteController = new EncryptNetworkController(TAG) {
            @Override
            public JSONObject getParams() {
                JSONObject json = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    data.put("child_id",id);

                    json.put("action", "delete_child");
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
                Log.d(TAG, "Delete Response: " + response);

                try {
                    JSONObject jObj = new JSONObject(response);
                    String status = jObj.getString("status");
                    // Check for error node in json
                    if (status.equals("success")) {
                        updateMe();

                    } else if(status.equals("error")){
                        pDialog.hide();
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

        deleteController.fetchData(success,error);


    }

    public class ChildAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public ChildAdapter() {
            inflater = LayoutInflater.from(getBaseContext());
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int arg) {
            return list.get(arg);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder holder;

            if (convertView == null) {
                view = inflater.inflate(R.layout.main_child_row, parent, false);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.name);
                holder.date = (TextView) view.findViewById(R.id.date);
                holder.layout = (RelativeLayout) view.findViewById(R.id.layout);
                holder.removeChild = (ImageView) view.findViewById(R.id.removeChild);
                view.setTag(holder);
            }  else {
                holder = (ViewHolder) view.getTag();
            }

            final Child s = list.get(position);
            holder.name.setText(s.name);
            holder.date.setText(s.date);

            holder.layout.setBackgroundColor(s.warning ? Color.RED : Color.TRANSPARENT);

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mapLoaded) {
                        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(s.marker.getPosition(), 15F);
                        mMap.animateCamera(cu);
                        s.marker.showInfoWindow();
                    }

                }
            });

            holder.removeChild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(ParentActivity.this)
                            .setTitle(R.string.delete_child_title)
                            .setMessage(getString(R.string.confirm_delete_child) + " " + s.name + "?")
                            .setPositiveButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setNegativeButton(R.string.proceed_button, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    tryDelete(s.id);
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
            return view;
        }

    }

    public class Child {
        String name,date;
        double lat,lon;
        Marker marker;
        long id;
        boolean warning;
    }

    static class ViewHolder {
        TextView name,date;
        RelativeLayout layout;
        ImageView removeChild;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mapLoaded = true;
                updateSOS();
                }


        });
        updateMap();
    }
}
