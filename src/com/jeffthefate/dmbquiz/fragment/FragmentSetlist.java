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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.DatabaseHelperSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.AutoResizeTextView;
import com.jeffthefate.dmbquiz.AutoResizeTextView.OnTextResizeListener;
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
    
    //private RelativeLayout parentLayout;
    //private RelativeLayout setlistLayout;
    //private ScrollView setlistScroll;
    
    //private boolean textViewResized = false;
    private float currTextSize = 0.0f;
	
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
        parentLayout = (RelativeLayout) v.findViewById(R.id.ParentLayout);
        if (parentLayout != null) {
	        ViewTreeObserver parentVto = parentLayout.getViewTreeObserver();
	        parentVto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if (setText != null && !textViewResized) {
						int parentHeight = parentLayout.getHeight() -
								setText.getPaddingTop() -
								setText.getPaddingBottom();
						int currHeight = setText.getHeight();
						int newHeight = currHeight <= parentHeight ? currHeight : parentHeight;
						setText.setHeight(newHeight);
						if (ApplicationEx.getTextViewHeight() == 0.0f)
							ApplicationEx.setTextViewHeight(newHeight);
					}
				}
	        });
        }
        
        setlistScroll = (ScrollView) v.findViewById(R.id.SetlistScroll);
        setlistLayout = (RelativeLayout) v.findViewById(R.id.SetlistLayout);
        */
        setText = (TextView) v.findViewById(R.id.SetText);
        //setText.setVisibility(View.INVISIBLE);
        
        stampText = (TextView) v.findViewById(R.id.StampText);
        if (ApplicationEx.getTextSize() > 0.0f)
        	stampText.getPaint().setTextSize(ApplicationEx.getTextSize() - 4.0f);
        if (setText instanceof AutoResizeTextView) {
        	//((AutoResizeTextView) setText).setExtraTextLines(2);
        	((AutoResizeTextView) setText).setAddEllipsis(false);
        	((AutoResizeTextView) setText).setOnResizeListener(
        			new OnTextResizeListener() {
				@Override
				public void onTextResize(TextView textView, float oldSize,
						float newSize) {
					Log.w(Constants.LOG_TAG, "onTextResize: " + oldSize + " : " + newSize);
					/*
					parentLayout.removeView(setlistLayout);
					setlistScroll.removeAllViews();
					setlistScroll.addView(setlistLayout, 0);
					*/
					if (stampText != null) {
						stampText.getPaint().setTextSize(newSize - 4.0f);
					}
					ApplicationEx.setTextSize(newSize);
					//textView.setVisibility(View.VISIBLE);
					if (currTextSize == 0.0f || newSize <= currTextSize)
						currTextSize = newSize;
				}
        	});
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
            /*
            if (setText instanceof AutoResizeTextView)
            	((AutoResizeTextView) setText).resizeText();
            	*/
            stampText.setText(ApplicationEx.setlistStamp);
        }
        else
            showNetworkProblem();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_UPDATE_SETLIST);
        intentFilter.addAction(Constants.ACTION_NEW_SONG);
        ApplicationEx.getApp().registerReceiver(setlistReceiver, intentFilter);
        /*
        if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.HONEYCOMB)
        	new UpdateSetTextTask().execute();
        else
        	new UpdateSetTextTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    	*/
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
    	if (retryButton != null && networkText != null && setText != null/*&&
    			stampText != null*/) {
    		retryButton.setVisibility(View.GONE);
	        networkText.setVisibility(View.GONE);
	        //setText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	        setText.setText(ApplicationEx.setlist);
	        /*
	        if (setText instanceof AutoResizeTextView)
            	((AutoResizeTextView) setText).resizeText();
            	*/
	        setText.setVisibility(View.VISIBLE);
	        stampText.setText(ApplicationEx.setlistStamp);
	        stampText.setVisibility(View.VISIBLE);
    	}
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
    /*
    private class UpdateSetTextTask extends AsyncTask<Void, Void, Void> {
    	@Override
        protected Void doInBackground(Void... nothing) {
    		ArrayList<String> setLines = new ArrayList<String>();
    		setLines.add("Gaucho");
    		setLines.add("Rooftop");
    		setLines.add("Crush");
    		setLines.add("Say Goodbye");
    		setLines.add("Big Eyed Fish ->");
    		setLines.add("Bartender");
    		setLines.add("(Kill The Preacher)");
    		setLines.add("Why I Am");
    		setLines.add("Granny");
    		setLines.add("Recently");
    		setLines.add("Pantala Naga Pampa");
    		setLines.add("Rapunzel");
    		setLines.add("");
    		setLines.add("Encore:");
    		setLines.add("Oh*");
    		setLines.add("So Much To Say ->");
    		setLines.add("Anyone Seen The Bridge ->");
    		setLines.add("Too Much (Fake) ->");
    		setLines.add("Ants Marching");
    		setLines.add("");
    		setLines.add("Notes:");
    		setLines.add("* Dave And Tim");
    		setLines.add("(song name) indicates a partial song");
    		setLines.add("-> indicates a segue into next song");
    		do {
	        	try {
	        		Thread.sleep(2000);
	        	} catch (InterruptedException e) {}
	        	ApplicationEx.setlist = ApplicationEx.setlist.concat("\n").concat(setLines.remove(0));
	        	publishProgress();
    		} while (!setLines.isEmpty());
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
}