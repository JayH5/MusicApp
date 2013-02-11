package com.jamie.play.fragments.musicplayer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jamie.play.R;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.service.MusicServiceWrapper;
import com.jamie.play.service.MusicStateListener;
import com.jamie.play.service.Track;
import com.jamie.play.utils.ImageUtils;
import com.jamie.play.widgets.RepeatingImageButton;

public class MusicPlayerFragment extends Fragment implements MusicStateListener {
	
	private static final int REPEAT_INTERVAL = 260;
	
	private ImageFetcher mImageWorker;
	
	private ImageButton mPlayPauseButton;
	private RepeatingImageButton mPreviousButton;
	private RepeatingImageButton mNextButton;
	
	private TextView mTrackText;
	private TextView mAlbumText;
	private TextView mArtistText;
	private ImageView mAlbumArt;
	
	private long mStartSeekPos = 0;
    private long mLastSeekEventTime;
	
	private ImageButton mPlayQueueButton;
	
	public MusicPlayerFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		mImageWorker = ImageUtils.getImageFetcher(getActivity());
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		
		// create ContextThemeWrapper from the original Activity Context with the custom theme
		final Context themedContext = new ContextThemeWrapper(getActivity(), 
				android.R.style.Theme_Holo);
		// clone the inflater using the ContextThemeWrapper
		LayoutInflater localInflater = inflater.cloneInContext(themedContext);
		// inflate using the cloned inflater, not the passed in default	
        final View v = localInflater.inflate(R.layout.fragment_music_player, container, false);
        
        mPlayPauseButton = (ImageButton) v.findViewById(R.id.action_button_play);
        mPreviousButton = (RepeatingImageButton) v.findViewById(R.id.action_button_previous);
        mNextButton = (RepeatingImageButton) v.findViewById(R.id.action_button_next);
        mPlayQueueButton = (ImageButton) v.findViewById(R.id.play_queue_button);
        initButtons();
        
        mTrackText = (TextView) v.findViewById(R.id.music_player_track_name);
        mAlbumText = (TextView) v.findViewById(R.id.music_player_album_name);
        mArtistText = (TextView) v.findViewById(R.id.music_player_artist_name);
        mAlbumArt = (ImageView) v.findViewById(R.id.music_player_album_art);
        ensureSquareImageView();
        
        return v;
    }
	
	private void initButtons() {
		mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MusicServiceWrapper.playOrPause();
				
			}
		});
		
		mPreviousButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MusicServiceWrapper.previous(getActivity());
				
			}
		});
		mPreviousButton.setRepeatListener(mRewListener, REPEAT_INTERVAL);
		
		mNextButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MusicServiceWrapper.next();
				
			}
		});
		mNextButton.setRepeatListener(mFfwdListener, REPEAT_INTERVAL);
		
		mPlayQueueButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FragmentManager fm = getActivity().getSupportFragmentManager();
				PlayQueueFragment frag = new PlayQueueFragment();
				frag.show(fm, "PlayQueueFragment");
			}
		});
	}
	
	private void ensureSquareImageView() {
		int slidingMenuOffset = getResources()
				.getDimensionPixelSize(R.dimen.slidingmenu_offset);
		DisplayMetrics dm = ImageUtils.getDisplayMetrics(getActivity());
		int imageWidth = dm.widthPixels - slidingMenuOffset;
		
		mAlbumArt.getLayoutParams().height = imageWidth;
	}
	
	@Override
	public void onMetaChanged() {
		final Track track = MusicServiceWrapper.getCurrentTrack();
		if (track != null) {
			mTrackText.setText(track.getTitle());
			mAlbumText.setText(track.getAlbum());
			mArtistText.setText(track.getArtist());
		
			mImageWorker.loadAlbumImage(track, mAlbumArt);
		}		
	}

	@Override
	public void onPlayStateChanged() {
		if (MusicServiceWrapper.isPlaying()) {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_pause);
		} else {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_play);
		}
	}

	@Override
	public void onShuffleOrRepeatModeChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
	}
	
	private RepeatingImageButton.RepeatListener mRewListener =
	        new RepeatingImageButton.RepeatListener() {
		
		@Override    
		public void onRepeat(View v, long howlong, int repcnt) {
	        scanBackward(repcnt, howlong);
	    }
	};
	    
	private RepeatingImageButton.RepeatListener mFfwdListener =
	        new RepeatingImageButton.RepeatListener() {
	    
		@Override
		public void onRepeat(View v, long howlong, int repcnt) {
	        scanForward(repcnt, howlong);
	    }
	};
	
	/**
     * Used to scan backwards in time through the current track
     * 
     * @param repcnt The repeat count
     * @param delta The long press duration
     */
    private void scanBackward(final int repcnt, long delta) {
        if (repcnt == 0) {
            mStartSeekPos = MusicServiceWrapper.position();
            mLastSeekEventTime = 0;
        } else {
            if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta = delta * 10;
            } else {
                // seek at 40x after that
                delta = 50000 + (delta - 5000) * 40;
            }
            long newpos = mStartSeekPos - delta;
            if (newpos < 0) {
                // move to previous track
            	MusicServiceWrapper.previous(getActivity());
                final long duration = MusicServiceWrapper.duration();
                mStartSeekPos += duration;
                newpos += duration;
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
            	MusicServiceWrapper.seek(newpos);
                mLastSeekEventTime = delta;
            }
            /*if (repcnt >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }
            refreshCurrentTime();*/
        }
    }

    /**
     * Used to scan forwards in time through the current track
     * 
     * @param repcnt The repeat count
     * @param delta The long press duration
     */
    private void scanForward(final int repcnt, long delta) {
        if (repcnt == 0) {
            mStartSeekPos = MusicServiceWrapper.position();
            mLastSeekEventTime = 0;
        } else {
            if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta = delta * 10;
            } else {
                // seek at 40x after that
                delta = 50000 + (delta - 5000) * 40;
            }
            long newpos = mStartSeekPos + delta;
            final long duration = MusicServiceWrapper.duration();
            if (newpos >= duration) {
                // move to next track
            	MusicServiceWrapper.next();
                mStartSeekPos -= duration; // is OK to go negative
                newpos -= duration;
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicServiceWrapper.seek(newpos);
                mLastSeekEventTime = delta;
            }
            /*if (repcnt >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }
            refreshCurrentTime();*/
        }
    }

}
