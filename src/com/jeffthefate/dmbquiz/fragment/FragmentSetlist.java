package com.jeffthefate.dmbquiz.fragment;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.DatabaseHelperSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.DatabaseHelper;
import com.jeffthefate.dmbquiz.ImageViewEx;
import com.jeffthefate.dmbquiz.R;

public class FragmentSetlist extends FragmentBase {
    
    private TextView setText;
    private TextView stampText;
    private String savedSet;
    private SetlistReceiver setlistReceiver;
    
    private Button retryButton;
    private TextView networkText;
    
    public FragmentSetlist() {
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	if (mCallback != null)
    		mCallback.setHomeAsUp(true);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            savedSet = savedInstanceState.getString("set");
            DatabaseHelperSingleton.instance().setUserValue(savedSet,
                    DatabaseHelper.COL_SETLIST, mCallback.getUserId());
        }
        else {
            if (mCallback.getUserId() != null) {
                savedSet = DatabaseHelperSingleton.instance().getUserStringValue(
                        DatabaseHelper.COL_SETLIST, mCallback.getUserId());
            }
        }
        setlistReceiver = new SetlistReceiver();
    }
    
    //private int notifyIndex = 0;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.setlist, container, false);
        /*
        ViewTreeObserver vto = slidingMenu.getViewTreeObserver(); 
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
            @Override 
            public void onGlobalLayout() { 
                slidingMenu.setThreshold(5);
            } 
        });
        */
        setText = (TextView) v.findViewById(R.id.SetText);
        stampText = (TextView) v.findViewById(R.id.StampText);
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
                    // TODO Auto-generated catch block
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
        if (!StringUtils.isBlank(ApplicationEx.setlist)) {
            setText.setText(ApplicationEx.setlist);
            stampText.setText(ApplicationEx.setlistStamp);
        }
        else
            showNetworkProblem();
        ApplicationEx.getApp().registerReceiver(setlistReceiver,
                new IntentFilter(Constants.ACTION_UPDATE_SETLIST));
    }
    
    @Override
    public void onPause() {
        ApplicationEx.getApp().unregisterReceiver(setlistReceiver);
        super.onPause();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("set", savedSet);
        super.onSaveInstanceState(outState);
    }
    /*
    @Override
    public void onDestroyView() {
        if (background != null) {
            Drawable drawable = background.getDrawable();
            if (drawable != null) {
                if (drawable instanceof BitmapDrawableEx) {
                    BitmapDrawableEx bitmapDrawable = (BitmapDrawableEx) drawable;
                    bitmapDrawable.setIsDisplayed(false);
                }
            }
        }
        super.onDestroyView();
    }
    */
    @Override
    public void updateSetText() {
        retryButton.setVisibility(View.GONE);
        networkText.setVisibility(View.GONE);
        setText.setText(ApplicationEx.setlist);
        setText.setVisibility(View.VISIBLE);
        stampText.setText(ApplicationEx.setlistStamp);
        stampText.setVisibility(View.VISIBLE);
    }
    
    private class SetlistReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("success", false))
                updateSetText();
            else
                showNetworkProblem();
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
    
}