package com.jeffthefate.dmbquiz.fragment;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
import com.jeffthefate.dmbquiz.AutoResizeTextView;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.ImageViewEx;
import com.jeffthefate.dmbquiz.R;

public class FragmentSetlist extends FragmentBase {
    
	private String setlist = "";
    private TextView setText;
    private String setStamp = "";
    private TextView stampText;
    private SetlistReceiver setlistReceiver;
    
    private Button retryButton;
    private TextView networkText;
    
    private ScrollView setlistScroll;
    private RelativeLayout setlistLayoutShot;
    private AutoResizeTextView setTextShot;
    
    public FragmentSetlist() {}
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	if (mCallback != null)
    		mCallback.setHomeAsUp(true);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setlistReceiver = new SetlistReceiver();
    }
    
    //private int notifyIndex = 0;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.setlist, container, false);
        setlistScroll = (ScrollView) v.findViewById(R.id.SetlistScroll);
        setText = (TextView) v.findViewById(R.id.SetText);
        stampText = (TextView) v.findViewById(R.id.StampText);
        
        setlistLayoutShot = (RelativeLayout) v.findViewById(
        		R.id.SetlistLayoutShot);
        setTextShot = (AutoResizeTextView) v.findViewById(R.id.SetTextShot);
        if (setTextShot != null)
	        setTextShot.setAddEllipsis(false);
        /*
        stampText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> testSongs = new ArrayList<String>();
                testSongs.add("#34 ");
                testSongs.add("Gaucho");
                testSongs.add("Dreaming Tree ");
                testSongs.add("Alligator Pie");
                testSongs.add("#41");
                testSongs.add("I'll Back You Up");
                testSongs.add("If I Had It All ");
                testSongs.add("Raven");
                testSongs.add("Smooth Rider ");
                testSongs.add("Some Devil ");
                testSongs.add("The Maker");
                JSONObject json = new JSONObject();
                try {
                    json.put("song", testSongs.get(notifyIndex++));
                    if (notifyIndex >= testSongs.size())
                        notifyIndex = 0;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //{ "action": "com.jeffthefate.dmb.ACTION_NEW_SONG", "song": "Ants Marching ", "setlist": "04/28/2013\nDave Matthews Band\nNew Orleans Jazz and Heritage Festival\nNew Orleans, LA\n\nSeven \n(Still Water) \nDon't Drink the Water \nRooftop \nGrey Street \nYou and Me \nShake Me Like a Monkey \nProudest Monkey \nBelly Belly Nice \nJimi Thing \nWhat Would You Say \n#41 \nLouisiana Bayou ->\nAnts Marching \n(song name) indicates a partial song\n-> indicates a fade into the next song\n" }
                Intent intent = new Intent(Constants.ACTION_NEW_SONG);
                intent.putExtra("com.parse.Data", json.toString());
                ApplicationEx.getApp().sendBroadcast(intent);
            }
        });
        */
		background = (ImageViewEx) v.findViewById(R.id.Background);
		mCallback.setlistBackground(
				ResourcesSingleton.instance().getResourceEntryName(R.drawable.setlist), background);
		/*
		try {
		    background.setImageDrawable(mCallback.getDrawable(R.drawable.setlist));
		} catch (OutOfMemoryError memErr) {
		    mCallback.setlistBackground(
	                res.getResourceEntryName(R.drawable.setlist), background);
        }
        */
		retryButton = (Button) v.findViewById(R.id.RetryButton);
		retryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	tracker.sendEvent(Constants.CATEGORY_FRAGMENT_UI,
            			Constants.ACTION_BUTTON_PRESS, "setlistRetry", 1l);
                disableButton(true);
                if (mCallback != null) {
                    if (ApplicationEx.getConnection()) {
                        mCallback.setNetworkProblem(false);
                        ApplicationEx.getSetlist();
                    }
                    else {
                        ApplicationEx.showLongToast(R.string.NoConnectionToast);
                        showNetworkProblem();
                    }
                }
            }
        });
		networkText = (TextView) v.findViewById(R.id.NetworkText);
        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_UPDATE_SETLIST);
        intentFilter.addAction(Constants.ACTION_NEW_SONG);
        ApplicationEx.getApp().registerReceiver(setlistReceiver, intentFilter);
        updateSetAndStamp();
        if (ApplicationEx.getConnection()) {
	        if (!StringUtils.isBlank(setlist) &&
	        		!setlist.equals("Error downloading setlist")) {
	            setText.setText(setlist);
	            if (setTextShot != null)
	            	setTextShot.setText(setlist);
	            setText.setVisibility(View.VISIBLE);
	            stampText.setText(setStamp);
	            stampText.setVisibility(View.VISIBLE);
	        }
	        else
	    		ApplicationEx.getSetlist();
        }
        else {
        	ApplicationEx.showLongToast(R.string.NoConnectionToast);
            showNetworkProblem();
        }
    }
    
    @Override
    public void onPause() {
        ApplicationEx.getApp().unregisterReceiver(setlistReceiver);
        super.onPause();
    }
    
    @Override
    public void updateSetText() {
    	updateSetAndStamp();
    	if (retryButton != null && networkText != null && setText != null) {
    		retryButton.setVisibility(View.GONE);
	        networkText.setVisibility(View.GONE);
	        setText.setText(setlist);
	        if (setTextShot != null)
            	setTextShot.setText(setlist);
	        setText.setVisibility(View.VISIBLE);
	        stampText.setText(setStamp);
	        stampText.setVisibility(View.VISIBLE);
    	}
    }
    
    private void updateSetAndStamp() {
    	setlist = SharedPreferencesSingleton.instance().getString(
        		ResourcesSingleton.instance().getString(R.string.setlist_key),
        		"");
        setStamp = SharedPreferencesSingleton.instance().getString(
        		ResourcesSingleton.instance().getString(R.string.setstamp_key),
        		"");
    }
    
    private class SetlistReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if (Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.HONEYCOMB)
            	new ReceiveTask(context, intent).execute();
            else
            	new ReceiveTask(context, intent).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    
    private class ReceiveTask extends AsyncTask<Void, Void, Void> {
    	private Context context;
    	private Intent intent;
    	private WakeLock wakeLock;
    	
    	private ReceiveTask(Context context, Intent intent) {
    		this.context = context;
    		this.intent = intent;
    	}
    	
        @Override
        protected Void doInBackground(Void... nothing) {
        	PowerManager pm = 
                    (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    Constants.SETLIST_WAKE_LOCK);
            wakeLock.acquire();
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
        	if ((intent.hasExtra("success") &&
            		intent.getBooleanExtra("success", false)) ||
            	!intent.hasExtra("success"))
                updateSetText();
            else
                showNetworkProblem();
        	wakeLock.release();
        }
    }
    
    @Override
    public void showNetworkProblem() {
        enableButton(true);
        if (mCallback != null)
            mCallback.setNetworkProblem(true);
        try {
            setText.setVisibility(View.GONE);
            stampText.setVisibility(View.GONE);
            networkText.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);
        } catch (NullPointerException e) {}
    }
    
    @Override
    public void disableButton(boolean isRetry) {
        retryButton.setBackgroundResource(R.drawable.button_disabled);
        retryButton.setTextColor(ResourcesSingleton.instance().getColor(R.color.light_gray));
        retryButton.setEnabled(false);
    }
    
    @Override
    public void enableButton(boolean isRetry) {
        retryButton.setBackgroundResource(R.drawable.button);
        retryButton.setTextColor(Color.BLACK);
        retryButton.setEnabled(true);
    }
    
    @Override
	public void showResizedSetlist() {
    	if (setlistScroll != null && setlistLayoutShot != null) {
	    	setlistScroll.setVisibility(View.INVISIBLE);
	    	setlistLayoutShot.setVisibility(View.VISIBLE);
    	}
    }

	@Override
	public void hideResizedSetlist() {
		if (setlistScroll != null && setlistLayoutShot != null) {
			setlistScroll.setVisibility(View.VISIBLE);
	    	setlistLayoutShot.setVisibility(View.INVISIBLE);
		}
	}
}