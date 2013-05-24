package za.jamie.soundstage.fragments.musicplayer;

import java.util.List;

import za.jamie.soundstage.IPlayQueueCallback;
import za.jamie.soundstage.MusicQueueWrapper;
import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.PlayQueueAdapter;
import za.jamie.soundstage.models.Track;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;

import com.mobeta.android.dslv.DragSortListView;

public class PlayQueueFragment extends DialogFragment implements 
		AdapterView.OnItemClickListener, DragSortListView.DragSortListener {
	
	private static final long VIBE_DURATION = 15;
	private static final int SCROLL_OFFSET = 15;
	private static final int SCROLL_DURATION = 250; // 250ms
	
	private PlayQueueAdapter mAdapter;
	private DragSortListView mDslv;
	
	private boolean mFirstOpen = true;
	
	private MusicQueueWrapper mService;
	
	private Vibrator mVibrator;
	
	public static PlayQueueFragment newInstance() {		
		return new PlayQueueFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAdapter = new PlayQueueAdapter(getActivity(), R.layout.list_item_play_queue,
				R.layout.list_item_play_queue_selected, null);
		
		mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mService = (MusicQueueWrapper) activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_play_queue, container, false);
		
		mDslv = (DragSortListView) v.findViewById(R.id.dslv);
		mDslv.setDragSortListener(this);
		mDslv.setOnItemClickListener(this);
		mDslv.setAdapter(mAdapter);
		
		final Button saveButton = (Button) v.findViewById(R.id.play_queue_save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO
				
			}
		});
		
		final Button closeButton = (Button) v.findViewById(R.id.play_queue_close_button);
		closeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getDialog().dismiss();				
			}
		});
		
		final ImageButton goToPositionButton = (ImageButton) v.findViewById(R.id.scrollToPosition);
		goToPositionButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				scrollToPosition();				
			}
		});
		
		return v;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		mDslv = null;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {        
        mService.registerPlayQueueCallback(mCallback);
        mService.requestPlayQueue();
        
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mService.unregisterPlayQueueCallback(mCallback);
	}
	
	public void scrollToPosition() {
		final int queuePosition = mAdapter.getQueuePosition();
		if (mDslv != null && queuePosition > -1) {
			mDslv.smoothScrollToPositionFromTop(queuePosition, SCROLL_OFFSET, SCROLL_DURATION);
		}
	}
	
	private IPlayQueueCallback mCallback = new IPlayQueueCallback.Stub() {

		@Override
		public void deliverTrackList(final List<Track> trackList) throws RemoteException {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mAdapter.addAll(trackList);					
				}
				
			});
			
		}

		@Override
		public void deliverPosition(final int position) throws RemoteException {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mAdapter.setQueuePosition(position);
					if (mFirstOpen) {
						mDslv.setSelectionFromTop(position, SCROLL_OFFSET);
						mFirstOpen = false;
					}
				}
				
			});
			
		}
		
	};

	@Override
	public void remove(int which) {
		mAdapter.remove(which);
		mService.removeTrack(which);
		
		if (mAdapter.getCount() == 0) {
			getDialog().dismiss();
		}
	}

	@Override
	public void drop(int from, int to) {
		if (from != to) {
			mAdapter.move(from, to);
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
