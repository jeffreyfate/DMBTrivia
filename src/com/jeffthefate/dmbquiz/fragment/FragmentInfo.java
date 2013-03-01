package com.jeffthefate.dmbquiz.fragment;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jeffthefate.dmbquiz.R;

public class FragmentInfo extends FragmentBase {

	private RelativeLayout infoLayout;
    private TextView infoText;
    private Button doneButton;
    
    public FragmentInfo() {}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info, container, false);
        infoLayout = (RelativeLayout) v.findViewById(R.id.InfoLayout);
        setBackground(getBackgroundDrawable(mCallback.getBackground()));
        infoText = (TextView) v.findViewById(R.id.InfoText);
        infoText.setMovementMethod(new ScrollingMovementMethod());
        doneButton = (Button) v.findViewById(R.id.DoneButton);
        doneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getActivity().onBackPressed();
            } 
        });
        return v;
    }
    
    @Override
	public void setBackground(Drawable background) {
    	if (infoLayout != null) {
    		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
    			infoLayout.setBackgroundDrawable(background);
    		else
    			infoLayout.setBackground(background);
    	}
    }
	
	@Override
	public Drawable getBackground() {
		if (infoLayout == null)
    		return null;
    	else
    		return infoLayout.getBackground();
	}
    
}
