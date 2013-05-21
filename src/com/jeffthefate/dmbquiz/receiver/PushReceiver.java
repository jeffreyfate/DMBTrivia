package com.jeffthefate.dmbquiz.receiver;

import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.R;
import com.jeffthefate.dmbquiz.activity.ActivityMain;

public class PushReceiver extends BroadcastReceiver {
    
    private NotificationCompat.Builder nBuilder;
    private NotificationManager nManager;
    private Notification notification;
    private SharedPreferences sharedPrefs;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // {"action":"com.jeffthefate.dmb.ACTION_NEW_QUESTIONS"}
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
                //ParseAnalytics.trackAppOpened(notificationIntent);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                            ApplicationEx.getApp(), 0, notificationIntent,
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                nBuilder = new NotificationCompat.Builder(
                        ApplicationEx.getApp());
                nBuilder.setLargeIcon(BitmapFactory.decodeResource(ResourcesSingleton.instance(),
                            R.drawable.notification_large)).
                    setSmallIcon(R.drawable.notification_large).
                    setWhen(System.currentTimeMillis()).
                    setContentTitle("New questions added").
                    setContentText("Touch to play DMB Trivia").
                    setContentIntent(pendingIntent);
                nManager.cancel(Constants.NOTIFICATION_NEW_QUESTIONS);
                notification = nBuilder.build();
                nManager.notify(null, Constants.NOTIFICATION_NEW_QUESTIONS,
                        notification);
            }
        }
        else if (action.equals(Constants.ACTION_NEW_SONG)) {
            JSONObject json = null;
            try {
                json = new JSONObject(intent.getExtras().getString(
                        "com.parse.Data"));
                ApplicationEx.latestSong = json.getString("song");
                ApplicationEx.setlist = json.getString("setlist");
                /*
                StringBuilder sb = new StringBuilder();
                sb.append("Updated:\n");
                sb.append(getUpdatedDateString(System.currentTimeMillis()));
                ApplicationEx.setlistStamp = sb.toString();
                */
                StringBuilder sb = new StringBuilder();
                sb.append("Updated:\n");
                sb.append(getUpdatedDateString(Long.parseLong(json.getString("timestamp"))));
                ApplicationEx.setlistStamp = sb.toString();
                ApplicationEx.parseSetlist();
                Intent setIntent = new Intent(Constants.ACTION_UPDATE_SETLIST);
                setIntent.putExtra("success", true);
                ApplicationEx.getApp().sendBroadcast(setIntent);
                /*
                ParseQuery setlistQuery = new ParseQuery("Setlist");
                setlistQuery.addDescendingOrder("setDate");
                setlistQuery.setLimit(1);
                setlistQuery.findInBackground(new FindCallback() {
                    @Override
                    public void done(List<ParseObject> setlists, ParseException e) {
                        Intent intent = new Intent(Constants.ACTION_UPDATE_SETLIST);
                        if (e != null) {
                            Log.e(Constants.LOG_TAG, "Error getting setlist!", e);
                            intent.putExtra("success", false);
                        }
                        else {
                            ApplicationEx.df.setTimeZone(TimeZone.getDefault());
                            StringBuilder sb = new StringBuilder();
                            sb.append("Updated:\n");
                            sb.append(DateFormat.format(ApplicationEx.df.toLocalizedPattern(), setlists.get(0).getUpdatedAt()));
                            ApplicationEx.setlistStamp = sb.toString();
                            intent.putExtra("success", true);
                        }
                        ApplicationEx.getApp().sendBroadcast(intent);
                    }
                });
                */
            } catch (JSONException e) {
                Log.e(Constants.LOG_TAG, "Bad push notification data!", e);
            }
            if (ApplicationEx.latestSong != null &&
                    !ApplicationEx.latestSong.equals("null") &&
                    !ApplicationEx.latestSong.equals("") &&
                    sharedPrefs.getBoolean(ApplicationEx.getApp().getString(
                            R.string.notification_key), true)) {
                ApplicationEx.createNotificationUri(
                        ApplicationEx.findMatchingAudio(ResourcesSingleton.instance(),
                                ApplicationEx.latestSong));
                nBuilder = new NotificationCompat.Builder(
                        ApplicationEx.getApp());
                Bitmap largeIcon = ApplicationEx.resizeImage(ResourcesSingleton.instance(),
                        ApplicationEx.findMatchingImage(
                                ApplicationEx.latestSong));
                nBuilder.setLargeIcon(largeIcon).
                    setSmallIcon(R.drawable.notification_large).
                    setWhen(System.currentTimeMillis()).
                    setTicker(ApplicationEx.latestSong).
                    setContentTitle(ApplicationEx.latestSong).
                    setLights(0xffff0000, 2000, 10000);
                switch (SharedPreferencesSingleton.instance().getInt(
                		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0)) {
                case 0:
                    nBuilder.setVibrate(new long[]{0, 500, 100, 500});
                    nBuilder.setSound(ApplicationEx.notificationSound);
                    break;
                case 1:
                    nBuilder.setSound(ApplicationEx.notificationSound);
                    break;
                case 2:
                    nBuilder.setVibrate(new long[]{0, 500, 100, 500});
                    break;
                }
                Intent notificationIntent = new Intent(
                        ApplicationEx.getApp(), ActivityMain.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                notificationIntent.putExtra("setlist", true);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                            ApplicationEx.getApp(), 0, notificationIntent,
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                nBuilder.setContentIntent(pendingIntent);
                StringBuilder sb = new StringBuilder();
                if (ApplicationEx.setlistList != null &&
                        !ApplicationEx.setlistList.isEmpty()) {
                    sb.append(ApplicationEx.setlistList.get(2));
                    sb.append(" - ");
                    sb.append(ApplicationEx.setlistList.get(3));
                    nBuilder.setContentText(sb.toString());
                }
                nManager.cancel(Constants.NOTIFICATION_NEW_QUESTIONS);
                notification = nBuilder.build();
                nManager.notify(null, Constants.NOTIFICATION_NEW_QUESTIONS,
                        notification);
            }
        }
        /*
         * JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
         */
    }
    
    private static String getUpdatedDateString(long millis) {
        ApplicationEx.df.setTimeZone(TimeZone.getDefault());
        Date date = new Date();
        date.setTime(millis);
        return ApplicationEx.df.format(date.getTime());
    }
}