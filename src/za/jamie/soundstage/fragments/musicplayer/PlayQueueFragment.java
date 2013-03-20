package za.jamie.soundstage.fragments.musicplayer;

import java.util.List;

import za.jamie.soundstage.IQueueStatusCallback;
import za.jamie.soundstage.MusicQueueWrapper;
import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.PlayQueueAdapter;
import za.jamie.soundstage.models.Track;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;

public class PlayQueueFragment extends DialogFragment implements DialogInterface.OnClickListener {
		
	private AlertDialog mAlertDialog;
	private PlayQueueAdapter mAdapter;
	
	private MusicQueueWrapper mService;
	
	public static PlayQueueFragment newInstance() {		
		return new PlayQueueFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAdapter = new PlayQueueAdapter(getActivity(), R.layout.list_item_two_line_faded,
        		R.layout.list_item_two_line_bold, R.layout.list_item_two_line, 
        		null, -1);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mService = (MusicQueueWrapper) activity;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAlertDialog = new AlertDialog.Builder(getActivity())
        		.setCancelable(true)
        		.setTitle("Play Queue")
        		.setAdapter(mAdapter, this)
        		.create();
        
        mService.registerQueueStatusCallback(mCallback);
        mService.requestQueueStatusRefresh();
        
	    return mAlertDialog;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mService.unregisterQueueStatusCallback(mCallback);
	}
	
	/*public void updateQueuePosition() {
		if (this.isVisible()) {
			//mQueuePosition = MusicLibraryWrapper.getQueuePosition();
			mQueuePosition = MusicQueueWrapper.getQueuePosition();
			mAdapter.setQueuePosition(mQueuePosition);
		}
	}
	
	public void updateQueue() {
		if (this.isVisible()) {
			
		}
	}*/

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mService.setQueuePosition(which);
	}
	
	private IQueueStatusCallback mCallback = new IQueueStatusCallback.Stub() {

		@Override
		public void onQueueChanged(final List<Track> queue) throws RemoteException {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mAdapter.setList(queue);
					
				}
				
			});
			
		}

		@Override
		public void onQueuePositionChanged(final int position) throws RemoteException {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mAdapter.setQueuePosition(position);
					
				}
				
			});
			
		}
		
	};
	
}
