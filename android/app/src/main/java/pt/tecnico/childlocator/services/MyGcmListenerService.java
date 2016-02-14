package pt.tecnico.childlocator.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import pt.tecnico.childlocator.helper.MyLifecycleHandler;
import pt.tecnico.childlocator.helper.SessionManager;
import pt.tecnico.childlocator.main.ParentActivity;
import pt.tecnico.childlocator.main.R;
import pt.tecnico.childlocator.main.UnloggedActivity;


public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("title");

        SessionManager manager = new SessionManager(this);
        manager.setDangerChildId(Integer.parseInt(data.getString("child_id")));
        Intent RTReturn = new Intent(ParentActivity.FOCUS_CHILD);
        LocalBroadcastManager.getInstance(MyGcmListenerService.this).sendBroadcast(RTReturn);

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);


        if(!MyLifecycleHandler.isApplicationInForeground() && !MyLifecycleHandler.isApplicationVisible())
            sendNotification(message);


    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, UnloggedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(Color.BLUE)
                .setContentTitle("ChildLocator")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}