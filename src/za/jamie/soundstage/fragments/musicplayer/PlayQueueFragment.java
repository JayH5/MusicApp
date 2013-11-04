package za.jamie.soundstage.fragments.musicplayer;

import java.util.List;

import za.jamie.soundstage.IPlayQueueCallback;
import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.PlayQueueAdapter;
import za.jamie.soundstage.fragments.MusicDialogFragment;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.MusicConnection;
import android.app.Dialog;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;

import com.mobeta.android.dslv.DragSortListView;

public class PlayQueueFragment extends MusicDialogFragment implements 
		AdapterView.OnItemClickListener, DragSortListView.DragSortListener {
	
	private static final String STATE_LIST_POSITION = "state_list_position";
	private static final String STATE_LIST_OFFSET = "state_list_offset";
	
	private static final int SCROLL_OFFSET = 35;
	private static final int SCROLL_DURATION = 250; // 250ms
	
	private PlayQueueAdapter mAdapter;
	private DragSortListView mDslv;
	
	private View mIsShuffledView;
	
	private int mSavedPosition = -1;
	private int mSavedOffset = -1;
	
	private final MusicConnection.ConnectionCallbacks mConnectionCallback = 
			new MusicConnection.ConnectionCallbacks() {
		@Override
		public void onConnected() {
			getMusicConnection().registerPlayQueueCallback(mCallback);
		}

		@Override
		public void onDisconnected() {
			
		}
	};
	
	public static PlayQueueFragment newInstance() {		
		return new PlayQueueFragment();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAdapter = new PlayQueueAdapter(getActivity(), R.layout.list_item_play_queue,
				R.layout.list_item_play_queue_selected, null);
		
		getMusicConnection().requestConnectionCallbacks(mConnectionCallback);
		
		if (savedInstanceState != null) {
			mSavedPosition = savedInstanceState.getInt(STATE_LIST_POSITION, -1);
			mSavedOffset = savedInstanceState.getInt(STATE_LIST_OFFSET, -1);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (!mAdapter.isEmpty()) {
			outState.putInt(STATE_LIST_POSITION, mDslv.getFirstVisiblePosition());
			final View v = mDslv.getChildAt(0);
			outState.putInt(STATE_LIST_OFFSET, v != null ? v.getTop() : 0);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		
		final View v = inflater.inflate(R.layout.fragment_play_queue, container, false);
		
		mDslv = (DragSortListView) v.findViewById(R.id.dslv);
		mDslv.setDragSortListener(this);
		mDslv.setOnItemClickListener(this);
		mDslv.setAdapter(mAdapter);
		
		Button saveButton = (Button) v.findViewById(R.id.play_queue_save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getMusicConnection().savePlayQueueAsPlaylist("Test");	
			}
		});
		
		Button closeButton = (Button) v.findViewById(R.id.play_queue_close_button);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();				
			}
		});
		
		ImageButton goToPositionButton = (ImageButton) v.findViewById(R.id.scrollToPosition);
		goToPositionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scrollToPosition();				
			}
		});
		
		mIsShuffledView = v.findViewById(R.id.play_queue_is_shuffled);
		
		return v;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getMusicConnection().releaseConnectionCallbacks(mConnectionCallback);
		getMusicConnection().unregisterPlayQueueCallback(mCallback);
	}
	
	public void scrollToPosition() {
		final int queuePosition = mAdapter.getQueuePosition();
		if (queuePosition > -1) {
			mDslv.smoothScrollToPositionFromTop(queuePosition, SCROLL_OFFSET, SCROLL_DURATION);
		}
	}
	
	@Override
	public void remove(int which) {
		mAdapter.remove(which);
		getMusicConnection().removeTrack(which);
		
		if (mAdapter.getCount() == 0) {
			getDialog().dismiss();
		}
	}

	@Override
	public void drop(int from, int to) {
		if (from != to) {
			mAdapter.move(from, to);
			getMusicConnection().moveQueueItem(from, to);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int which, long id) {
		getMusicConnection().setQueuePosition(which);
		getDialog().dismiss();
	}

	@Override
	public void drag(int from, int to) {
		mDslv.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);		
	}
	
	private IPlayQueueCallback mCallback = new IPlayQueueCallback.Stub() {

		@Override
		public void deliverTrackList(final List<Track> trackList, final int position, 
				final boolean isShuffled) throws RemoteException {
			
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mAdapter.clear();
					mAdapter.addAll(trackList);
					mAdapter.setQueuePosition(position);
					if (mSavedPosition != -1 && mSavedOffset != -1) {
						mDslv.setSelectionFromTop(mSavedPosition, mSavedOffset);
					} else {
						mDslv.setSelectionFromTop(position, SCROLL_OFFSET);
					}
					if (isShuffled) {
						mIsShuffledView.setVisibility(View.VISIBLE);
					}
				}
			});
			
		}

		@Override
		public void onPositionChanged(final int position) throws RemoteException {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mAdapter.setQueuePosition(position);
				}
			});
		}
		
	};
}
