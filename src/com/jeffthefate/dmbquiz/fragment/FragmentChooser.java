package com.jeffthefate.dmbquiz.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.FileCacheSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
import com.jeffthefate.dmbquiz.Constants;
import com.jeffthefate.dmbquiz.R;
import com.jeffthefate.dmbquiz.SetInfo;
import com.jeffthefate.dmbquiz.SetlistAdapter;

public class FragmentChooser extends FragmentBase {
    
	private ExpandableListView setlistListView;
	private ProgressBar setlistProgress;
    
    private Button retryButton;
    private TextView networkText;
    
    public FragmentChooser() {}
    
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
        View v = inflater.inflate(R.layout.chooser, container, false);
        setlistListView = (ExpandableListView) v.findViewById(R.id.SetlistList);
        setlistListView.setOnChildClickListener(
        		new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View view,
					int groupPos, int childPos, long id) {
				SharedPreferencesSingleton.putInt(
						R.string.selected_group_key, groupPos);
				SharedPreferencesSingleton.putInt(
						R.string.selected_child_key, childPos);
				SetlistAdapter setlistAdapter = 
						(SetlistAdapter) setlistListView
							.getExpandableListAdapter();
				setlistAdapter.notifyDataSetChanged();
				SetInfo setInfo = new SetInfo();
				setInfo.setKey(setlistAdapter.getKey(groupPos, childPos));
				if (mCallback.getLatestKey() != null && setInfo.getKey() != null
						&& mCallback.getLatestKey().equals(setInfo.getKey())) {
					setInfo = mCallback.getLatestSetInfo();
				}
				String setlist = setlistAdapter.getSetlist(groupPos, childPos);
				setInfo.setSetlist(setlist);
				if (0 == groupPos && 0 == childPos) {
					mCallback.getCurrFrag().setSetlistStampVisible(true);
					mCallback.getCurrFrag().setSetlistText(setlist, true);
					setInfo.setArchive(false);
				}
				else {
					mCallback.getCurrFrag().setSetlistStampVisible(false);
					mCallback.getCurrFrag().setSetlistText(setlist, false);
					setInfo.setArchive(true);
				}
				if (mCallback.getCurrFrag() instanceof FragmentPager) {
	                ((FragmentPager)mCallback.getCurrFrag()).setPage(1);
	            }
				mCallback.setSelectedSetInfo(setInfo);
				return true;
			}
        });
        setlistProgress = (ProgressBar) v.findViewById(R.id.SetlistProgress);
        retryButton = (Button) v.findViewById(R.id.RetryButton);
		retryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	tracker.sendEvent(Constants.CATEGORY_FRAGMENT_UI,
            			Constants.ACTION_BUTTON_PRESS, "chooserRetry", 1l);
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
        mCallback.checkSetlistMap(setlistListView, setlistProgress);
        SetInfo selectedSetInfo = mCallback.getSelectedSetInfo();
        if (selectedSetInfo != null) {
        	SetlistAdapter setlistAdapter = (SetlistAdapter) setlistListView
            		.getExpandableListAdapter();
            if (setlistAdapter != null) {
            	setlistAdapter.setSelected(selectedSetInfo.getKey(),
            			setlistListView);
            }
            else {
            	setlistListView.setSelected(false);
            }
        }
        else {
    		ApplicationEx.getSetlist();
        }
    }
    
    @Override
    public void showNetworkProblem() {
        enableButton(true);
        if (mCallback != null) {
            mCallback.setNetworkProblem(true);
        }
        try {
            setlistListView.setVisibility(View.GONE);
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
    public void updateSetText() {
    	// Update the list to the one that was just pushed
    	if (setlistListView != null) {
	    	SetlistAdapter setlistAdapter =
	    			(SetlistAdapter) setlistListView.getExpandableListAdapter();
	    	if (setlistAdapter != null) {
	    		if (setlistAdapter.getKey(0, 0).equals(
	    				mCallback.getLatestKey())) {
	    			setlistListView.setSelectedChild(0, 0, true);
	    		}
	    	}
    	}
    }

}