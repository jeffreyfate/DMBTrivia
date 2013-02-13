package com.jeffthefate.dmbquiz.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.R;
import com.jeffthefate.dmbquiz.activity.ActivityMain;

public class PushReceiver extends BroadcastReceiver {
    
    private NotificationCompat.Builder nBuilder;
    private NotificationManager nManager;
    private Notification notification;
    private Resources res;
    private SharedPreferences sharedPrefs;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // {"action":"com.jeffthefate.dmb.ACTION_NEW_QUESTIONS"}
        res = context.getResources();
        nManager = (NotificationManager) ApplicationEx.getApp()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                ApplicationEx.getApp());
        String action = intent.getAction();
        if (action.equals(Constants.ACTION_NEW_QUESTIONS)) {
            if (!ApplicationEx.isActive() && sharedPrefs.getBoolean(
                    ApplicationEx.getApp().getString(R.string.notification_key),
                    true)) {
                // Show notification that starts app
                Intent notificationIntent = new Intent(
                        ApplicationEx.getApp(), ActivityMain.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                            ApplicationEx.getApp(), 0, notificationIntent,
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                nBuilder = new NotificationCompat.Builder(
                        ApplicationEx.getApp());
                nBuilder. setLargeIcon(BitmapFactory.decodeResource(res,
                            R.drawable.notification_large)).
                    setSmallIcon(R.drawable.notification_large).
                    setWhen(System.currentTimeMillis()).
                    setContentTitle("New questions added").
                    setContentText("Tap to play DMB Trivia").
                    setContentIntent(pendingIntent);
                nManager.cancel(Constants.NOTIFICATION_NEW_QUESTIONS);
                notification = nBuilder.build();
                nManager.notify(null, Constants.NOTIFICATION_NEW_QUESTIONS,
                        notification);
            }
        }
    }
}