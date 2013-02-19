package com.jamie.play.fragments.musicplayer;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.jamie.play.R;
import com.jamie.play.adapters.PlayQueueAdapter;
import com.jamie.play.loaders.WrappedAsyncTaskLoader;
import com.jamie.play.models.Track;
import com.jamie.play.service.MusicServiceWrapper;

public class PlayQueueFragment extends DialogFragment implements  DialogInterface.OnClickListener, 
		LoaderManager.LoaderCallbacks<List<Track>> {
		
	private AlertDialog mAlertDialog;
	private PlayQueueAdapter mAdapter;
	
	private int mQueuePosition;
	
	public static PlayQueueFragment newInstance() {		
		return new PlayQueueFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mQueuePosition = MusicServiceWrapper.getQueuePosition();
		
		mAdapter = new PlayQueueAdapter(getActivity(), R.layout.list_item_two_line_faded,
        		R.layout.list_item_two_line_bold, R.layout.list_item_two_line, 
        		null, mQueuePosition);
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAlertDialog = new AlertDialog.Builder(getActivity())
        		.setCancelable(true)
        		.setTitle("Play Queue")
        		.setAdapter(mAdapter, this)
        		.create();
        
	    return mAlertDialog;
	}
	
	public void updateQueuePosition() {
		if (this.isVisible()) {
			mQueuePosition = MusicServiceWrapper.getQueuePosition();
			mAdapter.setQueuePosition(mQueuePosition);
		}
	}
	
	public void updateQueue() {
		if (this.isVisible()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}
	
	private static class PlayQueueLoader extends WrappedAsyncTaskLoader<List<Track>> {		
		public PlayQueueLoader(Context context) {
			super(context);
		}
		
		@Override
		public List<Track> loadInBackground() {
			return MusicServiceWrapper.getQueue();
		}
	}

	@Override
	public Loader<List<Track>> onCreateLoader(int id, Bundle args) {
		return new PlayQueueLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<Track>> loader, List<Track> data) {
		mAdapter.setList(data);
		
		// Scroll to the current song
		mAlertDialog.getListView().setSelectionFromTop(mQueuePosition, 0);
	}

	@Override
	public void onLoaderReset(Loader<List<Track>> loader) {
		mAdapter.setList(null);		
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		MusicServiceWrapper.setQueuePosition(which);		
	}
	
	
}
