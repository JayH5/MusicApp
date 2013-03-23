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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.mobeta.android.dslv.DragSortListView;

public class PlayQueueFragment extends DialogFragment implements 
		AdapterView.OnItemClickListener, DragSortListView.DragSortListener {
		
	private static final long VIBE_DURATION = 15;
	
	private PlayQueueAdapter mAdapter;
	
	private MusicQueueWrapper mService;
	
	private Vibrator mVibrator;
	
	public static PlayQueueFragment newInstance() {		
		return new PlayQueueFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAdapter = new PlayQueueAdapter(getActivity(), R.layout.list_item_play_queue,
        		R.layout.list_item_play_queue, R.layout.list_item_play_queue, 
        		null, -1);
		
		mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mService = (MusicQueueWrapper) activity;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final DragSortListView dslv = (DragSortListView) 
				inflater.inflate(R.layout.fragment_play_queue, null);
		dslv.setDragSortListener(this);
		dslv.setOnItemClickListener(this);
		dslv.setAdapter(mAdapter);
        
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dslv)
        	.setTitle(R.string.title_play_queue)
        	.setNegativeButton(R.string.play_queue_close, 
        			new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
        	.setPositiveButton(R.string.play_queue_save, 
        			new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					});
        
        mService.registerQueueStatusCallback(mCallback);
        mService.requestQueueStatusRefresh();
        
	    return builder.create();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mService.unregisterQueueStatusCallback(mCallback);
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

	@Override
	public void remove(int which) {
		mAdapter.remove(which);
		mService.removeTrack(which);
		
		if (mAdapter.getList().isEmpty()) {
			getDialog().dismiss();
		}
	}

	@Override
	public void drop(int from, int to) {
		if (from != to) {
			mAdapter.moveQueueItem(from, to);
			mService.moveQueueItem(from, to);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int which, long id) {
		mService.setQueuePosition(which);
		getDialog().dismiss();
	}

	@Override
	public void drag(int from, int to) {
		mVibrator.vibrate(VIBE_DURATION);		
	}
	
}
