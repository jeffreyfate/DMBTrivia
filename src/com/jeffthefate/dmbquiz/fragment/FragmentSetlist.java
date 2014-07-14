package com.jeffthefate.dmbquiz.fragment;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
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
import com.jeffthefate.dmbquiz.OnButtonListener;
import com.jeffthefate.dmbquiz.R;
import com.jeffthefate.dmbquiz.SetInfo;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class FragmentSetlist extends FragmentBase implements OnRefreshListener {
    
    private TextView setText;
    private TextView stampText;
    
    private Button retryButton;
    private TextView networkText;
    
    private SwipeRefreshLayout setlistSwipe;
    private ScrollView setlistScroll;
    private RelativeLayout setlistLayoutShot;
    private AutoResizeTextView setTextShot;
    
    public FragmentSetlist() {}
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	if (mCallback != null) {
    		mCallback.setHomeAsUp(true);
    	}
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.setlist, container, false);
        setlistSwipe = (SwipeRefreshLayout) v.findViewById(R.id.SetlistSwipe);
        setlistSwipe.setOnRefreshListener(this);
        setlistSwipe.setColorScheme(R.color.orange, 
                android.R.color.white, 
                R.color.orange, 
                android.R.color.white);
        ViewTreeObserver vto = setlistSwipe.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Calculate the trigger distance.
                final DisplayMetrics metrics = getResources()
                		.getDisplayMetrics();
                Float mDistanceToTriggerSync = 
                		((View) setlistSwipe.getParent()).getHeight() * 0.6f;
                try {
                    // Set the internal trigger distance using reflection.
                    Field field = SwipeRefreshLayout.class.getDeclaredField(
                    		"mDistanceToTriggerSync");
                    field.setAccessible(true);
                    field.setFloat(setlistSwipe, mDistanceToTriggerSync);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Only needs to be done once so remove listener.
                ViewTreeObserver obs = setlistSwipe.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });
        setlistScroll = (ScrollView) v.findViewById(R.id.SetlistScroll);
        setText = (TextView) v.findViewById(R.id.SetText);
        stampText = (TextView) v.findViewById(R.id.StampText);
        setlistLayoutShot = (RelativeLayout) v.findViewById(
        		R.id.SetlistLayoutShot);
        setTextShot = (AutoResizeTextView) v.findViewById(R.id.SetTextShot);
        if (setTextShot != null) {
	        setTextShot.setAddEllipsis(false);
        }
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
		mCallback.setlistBackground(ResourcesSingleton.instance()
				.getResourceEntryName(R.drawable.setlist), background);
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
                    if (ApplicationEx.hasConnection()) {
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
        if (ApplicationEx.hasConnection()) {
        	SetInfo selectedSetInfo = mCallback.getSelectedSetInfo();
        	Log.i(Constants.LOG_TAG, "selectedSetInfo: " + selectedSetInfo);
	        if (selectedSetInfo != null) {
	            setText.setText(selectedSetInfo.getSetlist());
	            if (setTextShot != null) {
	            	setTextShot.setText(selectedSetInfo.getSetlist());
	            	setTextShot.resizeText();
	            }
	            setText.setVisibility(View.VISIBLE);
	            stampText.setText(selectedSetInfo.getSetStamp());
	            if (selectedSetInfo.isArchive()) {
		        	stampText.setVisibility(View.INVISIBLE);
		        }
		        else {
		        	stampText.setVisibility(View.VISIBLE);
		        }
	        }
	        else {
	    		ApplicationEx.getSetlist();
	        }
        }
        else {
        	ApplicationEx.showLongToast(R.string.NoConnectionToast);
            showNetworkProblem();
        }
        /*
        setlist = "Jun 21 2013\nDave Matthews Band\nKlipsch Music Center\nNoblesville, IN\n\nBig Eyed Fish\nGranny\nThe Idea Of You\nBelly Belly Nice\nJoy Ride\n#41\nRooftop\nCaptain\nThe Riff\nSo Much To Say ->\nAnyone Seen The Bridge ->\nToo Much\nRecently\nCrash Into Me";
        setList = new ArrayList<String>();
        Collections.addAll(setList, setlist.split("\n"));
        setlist = "";
        new UpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        */
    }
    /*
    private ArrayList<String> setList;
    
    private class UpdateTask extends AsyncTask<Void, Void, Void> {
    	@Override
        protected Void doInBackground(Void... nothing) {
    		do {
            	if (!StringUtils.isEmpty(setlist))
            		setlist = setlist.concat("\n");
            	setlist = setlist.concat(setList.remove(0));
            	publishProgress();
            	try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {}
            } while (!setList.isEmpty());
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
        	updateSetText();
        }
        
        @Override
        protected void onCancelled(Void nothing) {
        }
        
        @Override
        protected void onPostExecute(Void nothing) {
        }
    }
    */
    
    @Override
    public void updateSetText() {
    	super.updateSetText();
    	Log.i(Constants.LOG_TAG, "FragmentSetlist updateSetText: " + mCallback.getSelectedSetInfo().getSetlist());
    	if (retryButton != null && networkText != null && setText != null) {
    		retryButton.setVisibility(View.GONE);
	        networkText.setVisibility(View.GONE);
	        setText.setText(mCallback.getSelectedSetInfo().getSetlist());
	        if (setTextShot != null) {
            	setTextShot.setText(
            			mCallback.getSelectedSetInfo().getSetlist());
            	setTextShot.resizeText();
	        }
	        setText.setVisibility(View.VISIBLE);
	        if (mCallback.getSelectedSetInfo().isArchive()) {
	        	stampText.setVisibility(View.VISIBLE);
	        }
	        else {
	        	stampText.setText(mCallback.getSelectedSetInfo().getSetStamp());
	        	stampText.setVisibility(View.VISIBLE);
	        }
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
	
	@Override
    public void setSetlistText(String setlistText, boolean canRefresh) {
		if (setText != null) {
			Log.i(Constants.LOG_TAG, "FragmentSetlist setSetlistText setText setlistText: " + setlistText);
			setText.setText(setlistText);
			setlistScroll.scrollTo(0, 0);
		}
		if (setTextShot != null) {
			setTextShot.setText(setlistText);
			setTextShot.resizeText();
		}
		if (!canRefresh) {
			setlistSwipe.setEnabled(false);
		}
		else {
			setlistSwipe.setEnabled(true);
		}
	}
	
	@Override
	public void setSetlistStampVisible(boolean isVisible) {
		Log.i(Constants.LOG_TAG, "setSetlistStampVisible: " + isVisible);
		if (isVisible) {
			stampText.setVisibility(View.VISIBLE);
		}
		else {
			stampText.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onRefresh() {
		if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.HONEYCOMB) {
        	new UpdateSetlistTask().execute();
    	}
        else {
        	new UpdateSetlistTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
	}
	
	private class UpdateSetlistTask extends AsyncTask<Void, Void, String> {
    	
        @Override
        protected String doInBackground(Void... nothing) {
        	ParseQuery<ParseObject> setlistQuery = new ParseQuery<ParseObject>("Setlist");
            setlistQuery.addDescendingOrder("setDate");
            setlistQuery.setLimit(1);
            try {
				List<ParseObject> setlists = setlistQuery.find();
				return setlists.get(0).getString("set");
			} catch (ParseException e) {
				ApplicationEx.showShortToast("Refresh failed");
			}
            return null;
        }
        
        protected void onProgressUpdate(Void... nothing) {
        }
        
        @Override
        protected void onCancelled(String setlist) {
        }
        
        @Override
        protected void onPostExecute(String setlist) {
        	Log.i(Constants.LOG_TAG, "Turning off refreshing");
        	setlistSwipe.setRefreshing(false);
        	if (setlist != null) {
        		setSetlistText(setlist, true);
        		String timestamp = ApplicationEx.getUpdatedDateString(
                		System.currentTimeMillis());
        		SharedPreferencesSingleton.putString(R.string.setlist_key,
        				setlist);
        		SharedPreferencesSingleton.putString(R.string.setstamp_key,
        				timestamp);
        	}
        }
    }

}