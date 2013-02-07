package com.jamie.play.fragments.musicplayer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.jamie.play.R;
import com.jamie.play.adapters.PlayQueueAdapter;
import com.jamie.play.service.MusicServiceWrapper;

public class PlayQueueFragment extends DialogFragment implements OnClickListener {
	
	private AlertDialog mAlertDialog;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {        
        PlayQueueAdapter adapter = new PlayQueueAdapter(getActivity(), 
        		R.layout.list_item_two_line, MusicServiceWrapper.getQueue());
	   
        mAlertDialog = new AlertDialog.Builder(getActivity())
        		.setCancelable(true)
        		.setTitle("Play Queue")
        		.setAdapter(adapter, this)
        		.create();
        
	    return mAlertDialog;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		int position = MusicServiceWrapper.getQueuePosition();
		mAlertDialog.getListView().setSelection(position);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		MusicServiceWrapper.setQueuePosition(which);		
	}
}
