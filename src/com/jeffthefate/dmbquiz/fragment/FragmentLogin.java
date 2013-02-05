package com.jeffthefate.dmbquiz.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.R;

public class FragmentLogin extends FragmentBase {
    
    private ProgressBar progress;
    private TextView networkText;
    private TextView loadingText;
    private Button retryButton;

    public FragmentLogin() {}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.load, container, false);
        progress = (ProgressBar) view.findViewById(R.id.Progress);
        networkText = (TextView) view.findViewById(R.id.NetworkText);
        loadingText = (TextView) view.findViewById(R.id.LoadingText);
        loadingText.setText(R.string.LoginLoading);
        retryButton = (Button) view.findViewById(R.id.RetryButton);
        retryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButton(true);
                if (mCallback != null) {
                    if (ApplicationEx.getConnection()) {
                        mCallback.setNetworkProblem(false);
                        mCallback.setupUser();
                        networkText.setVisibility(View.INVISIBLE);
                        retryButton.setVisibility(View.INVISIBLE);
                        progress.setVisibility(View.VISIBLE);
                    }
                    else {
                        Toast.makeText(ApplicationEx.getApp(), "No connection",
                                Toast.LENGTH_LONG).show();
                        showNetworkProblem();
                    }
                }
            }
        });
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            if (mCallback.getNetworkProblem())
                showNetworkProblem();
            else {
                if (ApplicationEx.getConnection()) {
                    mCallback.setNetworkProblem(false);
                    //mCallback.setupUser();
                    networkText.setVisibility(View.INVISIBLE);
                    retryButton.setVisibility(View.INVISIBLE);
                    progress.setVisibility(View.VISIBLE);
                }
                else {
                    Toast.makeText(ApplicationEx.getApp(), "No connection",
                            Toast.LENGTH_LONG).show();
                    showNetworkProblem();
                }
            }
        }
    }
    
    @Override
    public void showNetworkProblem() {
        enableButton(true);
        if (mCallback != null)
            mCallback.setNetworkProblem(true);
        progress.setVisibility(View.INVISIBLE);
        networkText.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.GONE);
        retryButton.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void showLoading(String message) {
        networkText.setVisibility(View.INVISIBLE);
        retryButton.setVisibility(View.INVISIBLE);
        loadingText.setText(message);
        loadingText.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void disableButton(boolean isRetry) {
        if (isRetry) {
            retryButton.setBackgroundResource(R.drawable.button_disabled);
            retryButton.setTextColor(res.getColor(R.color.light_gray));
            retryButton.setEnabled(false);
        }
    }
    
    @Override
    public void enableButton(boolean isRetry) {
        if (isRetry) {
            retryButton.setBackgroundResource(R.drawable.button);
            retryButton.setTextColor(Color.BLACK);
            retryButton.setEnabled(true);
        }
    }
    
}
