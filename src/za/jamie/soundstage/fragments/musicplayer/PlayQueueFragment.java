package za.jamie.soundstage.fragments.musicplayer;

import java.util.List;

import za.jamie.soundstage.IQueueStatusCallback;
import za.jamie.soundstage.MusicQueueWrapper;
import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.PlayQueueAdapter;
import za.jamie.soundstage.models.Track;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.mobeta.android.dslv.DragSortListView;

public class PlayQueueFragment extends DialogFragment implements AdapterView.OnItemClickListener, 
		DragSortListView.DropListener, DragSortListView.RemoveListener {
		
	private PlayQueueAdapter mAdapter;
	
	private MusicQueueWrapper mService;
	
	public static PlayQueueFragment newInstance() {		
		return new PlayQueueFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAdapter = new PlayQueueAdapter(getActivity(), R.layout.list_item_play_queue,
        		R.layout.list_item_play_queue, R.layout.list_item_play_queue, 
        		null, -1);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		
		final View v = inflater.inflate(R.layout.fragment_play_queue, container, false);
		
		DragSortListView dslv = (DragSortListView) v.findViewById(R.id.dslv);
		
		dslv.setAdapter(mAdapter);
		
		dslv.setDropListener(this);
		dslv.setRemoveListener(this);
		dslv.setOnItemClickListener(this);
		
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mService = (MusicQueueWrapper) activity;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.title_play_queue);
        
        mService.registerQueueStatusCallback(mCallback);
        mService.requestQueueStatusRefresh();
        
	    return dialog;
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
	
}
