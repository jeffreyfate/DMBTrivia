package com.jeffthefate.dmbquiz.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.jeffthefate.dmbquiz.ImageViewEx;
import com.jeffthefate.dmbquiz.R;

public class FragmentInfo extends FragmentBase {

    private Button doneButton;
    
    public FragmentInfo() {}
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	if (mCallback != null) {
    		mCallback.setHomeAsUp(false);
    		mCallback.setInSetlist(false);
    	}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.info, container, false);
        background = (ImageViewEx) v.findViewById(R.id.Background);
		setBackgroundBitmap(mCallback.getBackground(), "splash");
        doneButton = (Button) v.findViewById(R.id.DoneButton);
        doneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getActivity().onBackPressed();
            } 
        });
        return v;
    }
    
}
