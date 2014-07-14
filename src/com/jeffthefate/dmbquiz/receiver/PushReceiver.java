package com.jeffthefate.dmbquiz.receiver;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.FileCacheSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.R;
import com.jeffthefate.dmbquiz.SetInfo;
import com.jeffthefate.dmbquiz.activity.ActivityMain;

public class PushReceiver extends BroadcastReceiver {
    
    private NotificationCompat.Builder nBuilder;
    private NotificationManager nManager;
    private WakeLock wakeLock;
	private MulticastLock multicastLock;
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	PowerManager pm = 
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                Constants.PUSH_WAKE_LOCK);
        wakeLock.acquire();
        WifiManager wm = (WifiManager) context.getSystemService(
        		Context.WIFI_SERVICE);
        multicastLock = wm.createMulticastLock(Constants.PUSH_WIFI_LOCK);
        multicastLock.acquire();
        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.HONEYCOMB) {
        	new ReceiveTask(intent).execute();
        }
        else {
        	new ReceiveTask(intent).executeOnExecutor(
        			AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    
    private class ReceiveTask extends AsyncTask<Void, Void, Void> {
    	private Intent intent;
    	
    	private ReceiveTask(Intent intent) {
    		this.intent = intent;
    	}
    	
        @Override
        protected Void doInBackground(Void... nothing) {
        	// {"action":"com.jeffthefate.dmb.ACTION_NEW_QUESTIONS"}
            nManager = (NotificationManager) ApplicationEx.getApp()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_NEW_QUESTIONS)) {
                if (!ApplicationEx.isActive() &&
                		SharedPreferencesSingleton.instance().getBoolean(
                				ApplicationEx.getApp().getString(
                						R.string.notification_key),
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
                    nBuilder.setLargeIcon(BitmapFactory.decodeResource(
                    		ResourcesSingleton.instance(),
                                R.drawable.notification_large)).
                        setSmallIcon(R.drawable.notification_large).
                        setWhen(System.currentTimeMillis()).
                        setContentTitle("New questions added").
                        setContentText("Touch to play DMB Trivia").
                        setContentIntent(pendingIntent);
                    nManager.cancel(Constants.NOTIFICATION_NEW_QUESTIONS);
                    nManager.notify(null, Constants.NOTIFICATION_NEW_QUESTIONS,
                    		nBuilder.build());
                }
            }
            else if (action.equals(Constants.ACTION_NEW_SONG)) {
                JSONObject json = null;
                String latestSong = "";
                String latestSet = "";
                String latestSetDate = "";
                String latestSetVenue = "";
                String latestSetCity = "";
                try {
                	if (!intent.hasExtra("com.parse.Data"))
                		throw new JSONException("No data sent!");
                    json = new JSONObject(intent.getExtras().getString(
                            "com.parse.Data"));
                    SetInfo setInfo = new SetInfo();
                    latestSong = json.getString("song");
                    latestSet = json.getString("setlist");
                    setInfo.setSetlist(latestSet);
                    latestSetDate = json.getString("shortDate");
                    setInfo.setSetDate(latestSetDate);
                    latestSetVenue = json.getString("venueName");
                    setInfo.setSetVenue(latestSetVenue);
                    latestSetCity = json.getString("venueCity");
                    setInfo.setSetCity(latestSetCity);
                    setInfo.setArchive(false);
                    FileCacheSingleton fileCacheSingleton =
                    		FileCacheSingleton.instance();
                    fileCacheSingleton.saveSerializableToFile(
                    		Constants.LATEST_SET_FILE, setInfo);
                    ApplicationEx.parseSetlist(latestSet);
                    Editor editor = SharedPreferencesSingleton.instance()
                    		.edit();
                    editor.putString(ResourcesSingleton.instance().getString(
                    		R.string.lastsong_key), latestSong);
                    editor.putString(ResourcesSingleton.instance().getString(
                    		R.string.setlist_key), latestSet);
                    editor.putString(ResourcesSingleton.instance().getString(
                    		R.string.set_date_key), latestSetDate);
                    editor.putString(ResourcesSingleton.instance().getString(
                    		R.string.setvenue_key), latestSetVenue);
                    editor.putString(ResourcesSingleton.instance().getString(
                    		R.string.setcity_key), latestSetCity);
                    editor.putBoolean(ResourcesSingleton.instance().getString(
                    		R.string.archive_key), false);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Updated:\n");
                    sb.append(ApplicationEx.getUpdatedDateString(
                    		Long.parseLong(json.getString("timestamp"))));
                    editor.putString(ResourcesSingleton.instance().getString(
                    		R.string.setstamp_key), sb.toString());
                    if (Build.VERSION.SDK_INT <
                    		Build.VERSION_CODES.GINGERBREAD) {
                    	editor.commit();
                    }
                    else {
                    	editor.apply();
                    }
                    /*
                    StringBuilder sb = new StringBuilder();
                    sb.append("Updated:\n");
                    sb.append(getUpdatedDateString(System.currentTimeMillis()));
                    ApplicationEx.setlistStamp = sb.toString();
                    */
                    Intent setIntent = new Intent(
                    		Constants.ACTION_UPDATE_SETLIST);
                    setIntent.putExtra("success", true);
                    ApplicationEx.getApp().sendBroadcast(setIntent);
                    /*
                    ApplicationEx.parseSetlist();
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
                if (latestSong != null && !latestSong.equals("null") &&
                        !latestSong.equals("") && SharedPreferencesSingleton
                        .instance().getBoolean(ApplicationEx.getApp().getString(
                                R.string.notification_key), true)) {
                	nManager.cancel(Constants.NOTIFICATION_NEW_SONG);
                	ApplicationEx.findMatchingAudio(latestSong);
                    nBuilder = new NotificationCompat.Builder(
                            ApplicationEx.getApp());
                    Bitmap largeIcon = ApplicationEx.resizeImage(
                    		ResourcesSingleton.instance(),
                            ApplicationEx.findMatchingImage(latestSong));
                    nBuilder.setLargeIcon(largeIcon)
                    	.setSmallIcon(R.drawable.notification_large)
                        .setWhen(System.currentTimeMillis())
                        .setTicker(latestSong)
                        .setContentTitle(latestSong)
                        .setLights(0xffff0000, 2000, 10000);
                    if (!latestSet.isEmpty()) {
                    	String bigText = getNotificationString(latestSet,
                    			latestSong);
                    	if (!StringUtils.isBlank(bigText)) {
                    		nBuilder.setStyle(
                    				new NotificationCompat.BigTextStyle()
                    					.bigText(bigText));
                    	}
                    }
                    switch (SharedPreferencesSingleton.instance().getInt(
                    		ResourcesSingleton.instance().getString(
                    				R.string.notificationsound_key), 0)) {
                    case 0:
                    	Log.i(Constants.LOG_TAG, "soundSetting: 0");
                        nBuilder.setVibrate(new long[]{0, 500, 100, 500});
                        nBuilder.setSound(ApplicationEx.notificationSound);
                        break;
                    case 1:
                    	Log.i(Constants.LOG_TAG, "soundSetting: 1");
                        nBuilder.setSound(ApplicationEx.notificationSound);
                        break;
                    case 2:
                    	Log.i(Constants.LOG_TAG, "soundSetting: 2");
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
                    nManager.notify(null, Constants.NOTIFICATION_NEW_SONG,
                    		nBuilder.build());
                }
            }
            /*
            else if (action.equals(Constants.ACTION_UPDATE_AUDIO)) {
            	Log.i(Constants.LOG_TAG, "UPDATE_AUDIO!");
            	JSONObject json = null;
                try {
                	if (!intent.hasExtra("com.parse.Data"))
                		throw new JSONException("No data sent!");
                    json = new JSONObject(intent.getExtras().getString(
                            "com.parse.Data"));
                    JSONArray songsArray = json.getJSONArray("songs");
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<String>>(){}.getType();
                    List<String> list = gson.fromJson(songsArray.toString(), type);
                    if (!ApplicationEx.isDownloading())
                    	ApplicationEx.downloadSongClips(list);
            	} catch (JSONException e) {
            		Log.e(Constants.LOG_TAG, "Bad JSON data!", e);
            	}
            }
            */
            /*
             * JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
             */
            return null;
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
        	multicastLock.release();
        	wakeLock.release();
        }
    }
    
    public static String getNotificationString(String latestSet,
    		String latestSong) {
    	CircularFifoQueue<String> circularFifoQueue =
    			new CircularFifoQueue<String>(7);
    	List<String> latestList = Arrays.asList(
    			StringUtils.split(latestSet, "\n"));
    	latestList = latestList.subList(4, latestList.size()-1);
    	Log.i(Constants.LOG_TAG, "latestList: " + latestList);
    	Log.i(Constants.LOG_TAG, "queue size: " + circularFifoQueue.size());
    	for (String temp : latestList) {
    		if (temp.toLowerCase(Locale.getDefault()).startsWith("notes:")) {
    			break;
    		}
    		if (StringUtils.strip(latestSong).equals(
    				StringUtils.strip(temp))) {
    			Log.i(Constants.LOG_TAG, "Found current song, ending");
    			break;
    		}
    		circularFifoQueue.add(temp);
    		Log.i(Constants.LOG_TAG, "circularFifoQueue: " + circularFifoQueue);
    	}
    	Log.i(Constants.LOG_TAG, "queue size: " + circularFifoQueue.size());
    	StringBuilder sb = new StringBuilder();
    	String setLine = "";
    	Iterator<String> iter = circularFifoQueue.iterator();
    	while (iter.hasNext()) {
    		setLine = iter.next();
    		if (setLine.toLowerCase(Locale.getDefault())
    				.startsWith("show begins")) {
    			Log.i(Constants.LOG_TAG, "Found show begins, exiting");
    			return "";
    		}
    		else if (StringUtils.strip(latestSong).equals(
    				StringUtils.strip(setLine))) {
    			Log.i(Constants.LOG_TAG, "Found current song, skipping");
    			continue;
    		}
    		else if (!StringUtils.isBlank(setLine)) {
    			if (!StringUtils.isBlank(sb.toString())) {
    				Log.i(Constants.LOG_TAG, "Adding new line");
    				sb.append("\n");
    			}
    			Log.i(Constants.LOG_TAG, "Adding " + setLine);
    			sb.append(setLine);
    		}
    		Log.e(Constants.LOG_TAG, sb.toString());
    	}
    	if (!circularFifoQueue.isEmpty()) {
    		if (circularFifoQueue.size() > 1) {
    			sb.insert(0, "LAST " + circularFifoQueue.size() + "\n");
    		}
    		else {
    			sb.insert(0, "LAST\n");
    		}
    	}
    	return sb.toString();
    }
    
}