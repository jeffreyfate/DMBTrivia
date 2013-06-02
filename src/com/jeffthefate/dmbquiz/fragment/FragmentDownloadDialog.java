package com.jeffthefate.dmbquiz.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.jeffthefate.dmbquiz.ApplicationEx;
import com.jeffthefate.dmbquiz.ApplicationEx.DatabaseHelperSingleton;
import com.jeffthefate.dmbquiz.R;

public class FragmentDownloadDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = inflater.inflate(R.layout.download, null);
        builder.setView(v);
        builder.setPositiveButton(R.string.ok,
        		new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        		if (!ApplicationEx.isDownloading())
	        		ApplicationEx.downloadSongClips(
	    				DatabaseHelperSingleton.instance()
	        				.getNotificatationsToDownload());
        		dismiss();
        	}
        });
        builder.setNegativeButton(R.string.cancel,
        		new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int id) {
        		dismiss();
        	}
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}