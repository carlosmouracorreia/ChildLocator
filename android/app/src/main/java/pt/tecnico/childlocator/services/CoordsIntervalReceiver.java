package pt.tecnico.childlocator.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by carloscorreia on 28/11/15.
 */
public class CoordsIntervalReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "GpsTrackerAlarmReceiver";
    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Starting Clock");
        context.startService(new Intent(context, SendCoordsService.class));
    }
}