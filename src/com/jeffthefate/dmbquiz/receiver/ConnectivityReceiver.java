package com.jeffthefate.dmbquiz.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.Constants;
/**
 * Receives when the contexts and widgets should be updated.
 * 
 * @author Jeff Fate
 */
public class ConnectivityReceiver extends BroadcastReceiver {
    
    boolean wifiEnabled = false;
    ConnectivityManager connMan;
 
    @SuppressWarnings("deprecation")
	@Override
    public void onReceive(Context context, Intent intent) {
        connMan = (ConnectivityManager) ApplicationEx.getApp()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (intent.getAction().equalsIgnoreCase(
                ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (intent.hasExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY)) {
                Intent i = new Intent(Constants.ACTION_CONNECTION);
                i.putExtra("hasConnection", !intent.getBooleanExtra(
                        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false));
                ApplicationEx.setConnection(!intent.getBooleanExtra(
                        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false));
                ApplicationEx.getApp().sendBroadcast(i);
            }
            else if (intent.hasExtra(ConnectivityManager.EXTRA_NETWORK_INFO)) {
                NetworkInfo nInfo = connMan.getActiveNetworkInfo();
                Intent i = new Intent(Constants.ACTION_CONNECTION);
                i.putExtra("hasConnection",
                        nInfo == null ? false : nInfo.isConnected());
                ApplicationEx.setConnection(
                        nInfo == null ? false : nInfo.isConnected());
                ApplicationEx.getApp().sendBroadcast(i);
                /*
                if (nInfo != null && nInfo.isConnected() &&
                		SharedPreferencesSingleton.instance().getInt(
            				ResourcesSingleton.instance().getString(
                				R.string.notificationtype_key), 0) == 2 &&
            				!ApplicationEx.isDownloading())
                	ApplicationEx.downloadSongClips(
                			DatabaseHelperSingleton.instance()
                    				.getNotificatationsToDownload());
                */
            }
        }
    }
 
}