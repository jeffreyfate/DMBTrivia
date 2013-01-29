package com.jeffthefate.dmbquiz_dev.fragment;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jeffthefate.dmbquiz_dev.R;

public class FragmentInfo extends FragmentBase {

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
    
}
