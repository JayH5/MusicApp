package za.jamie.soundstage.fragments.musicplayer;

import java.lang.ref.WeakReference;

import za.jamie.soundstage.R;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.MusicService;
import za.jamie.soundstage.service.MusicServiceWrapper;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import za.jamie.soundstage.widgets.RepeatingImageButton;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicPlayerFragment extends Fragment implements 
		SeekBar.OnSeekBarChangeListener {

	// Rate at which the repeat listeners for the seek buttons refresh in ms
	private static final int REPEAT_INTERVAL = 260;
	
	// Time handler message
    private static final int REFRESH_TIME = 1;
	
	private ImageFetcher mImageWorker;
	
	private ImageButton mPlayPauseButton;
	private RepeatingImageButton mPreviousButton;
	private RepeatingImageButton mNextButton;
	
	private TextView mTrackText;
	private TextView mAlbumText;
	private TextView mArtistText;
	private ImageView mAlbumArt;
	
	private SeekBar mProgress;
	private TextView mElapsedTime;
	private TextView mTotalTime;
	private long mDuration;
	
	private TimeHandler mTimeHandler;
	
	private long mStartSeekPos = 0;
    private long mLastSeekEventTime;
    private long mPosOverride = -1;
    private boolean mFromTouch = false;
    private boolean mIsPaused = false;
	
	private boolean mShown = false;
	
	public MusicPlayerFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mImageWorker = ImageUtils.getImageFetcher(getActivity());
		
		mTimeHandler = new TimeHandler(this);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		final IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.PLAYSTATE_CHANGED);
        filter.addAction(MusicService.META_CHANGED);
        filter.addAction(MusicService.SHUFFLEMODE_CHANGED);
        filter.addAction(MusicService.REPEATMODE_CHANGED);
        
        getActivity().registerReceiver(mPlayStatusReceiver, filter);
	}
	
	@Override
	public void onDestroy() {
        super.onDestroy();
        mIsPaused = false;
        
        getActivity().unregisterReceiver(mPlayStatusReceiver);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setRetainInstance(true);
		
		// Make sure everything is up to date
        updateMeta(MusicServiceWrapper.getCurrentTrack());
		updatePlayState(MusicServiceWrapper.isPlaying());
        
        // Refresh the current time
        final long next = refreshCurrentTime();
        queueNextRefresh(next);
	}
	
	public void onHide() {
		mShown = false;
		mTimeHandler.removeCallbacks(null);
	}
	
	public void onShow() {
		mShown = true;
		// Refresh the current time
        final long next = refreshCurrentTime();
        queueNextRefresh(next);
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
        
        mPlayPauseButton.setOnClickListener(mPlayButtonListener);
        
        mPreviousButton.setOnClickListener(mPlayButtonListener);
        mPreviousButton.setRepeatListener(mRewListener, REPEAT_INTERVAL);
        
        mNextButton.setOnClickListener(mPlayButtonListener);
        mNextButton.setRepeatListener(mFfwdListener, REPEAT_INTERVAL);
        
        mTrackText = (TextView) v.findViewById(R.id.music_player_track_name);
        mAlbumText = (TextView) v.findViewById(R.id.music_player_album_name);
        mArtistText = (TextView) v.findViewById(R.id.music_player_artist_name);
        mAlbumArt = (ImageView) v.findViewById(R.id.music_player_album_art);
        ensureSquareImageView();
        
        mProgress = (SeekBar) v.findViewById(R.id.seek_bar);
        mElapsedTime = (TextView) v.findViewById(R.id.elapsedTime);
        mTotalTime = (TextView) v.findViewById(R.id.totalTime);
        
        return v;
    }
	
	private void ensureSquareImageView() {
		int slidingMenuOffset = getResources()
				.getDimensionPixelSize(R.dimen.slidingmenu_offset);
		DisplayMetrics dm = ImageUtils.getDisplayMetrics(getActivity());
		int imageWidth = dm.widthPixels - slidingMenuOffset;
		
		mAlbumArt.getLayoutParams().height = imageWidth;
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
                mStartSeekPos += mDuration;
                newpos += mDuration;
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
            	MusicServiceWrapper.seek(newpos);
                mLastSeekEventTime = delta;
            }
            if (repcnt >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }
            refreshCurrentTime();
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
            if (newpos >= mDuration) {
                // move to next track
            	MusicServiceWrapper.next(getActivity());
                mStartSeekPos -= mDuration; // is OK to go negative
                newpos -= mDuration;
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicServiceWrapper.seek(newpos);
                mLastSeekEventTime = delta;
            }
            if (repcnt >= 0) {
                mPosOverride = newpos;
            } else {
                mPosOverride = -1;
            }
            refreshCurrentTime();
        }
    }
    
    /* Used to update the current time string */
    private long refreshCurrentTime() {
        try {
            final long pos = mPosOverride < 0 ? 
            		MusicServiceWrapper.position() : mPosOverride;
            
            if (pos >= 0 && mDuration > 0) {
                mElapsedTime.setText(TextUtils.getTrackDurationText(getResources(), pos));
                final int progress = (int)(1000 * pos / mDuration);
                mProgress.setProgress(progress);
                
            } else {
                mProgress.setProgress(0);
            }
            // calculate the number of milliseconds until the next full second,
            // so the counter can be updated at just the right time
            final long remaining = 1000 - pos % 1000;
            // approximate how often we would need to refresh the slider to
            // move it smoothly
            int width = mProgress.getWidth();
            if (width == 0) {
                width = 320;
            }
            final long smoothrefreshtime = mDuration / width;
            if (smoothrefreshtime > remaining) {
                return remaining;
            }
            if (smoothrefreshtime < 20) {
                return 20;
            }
            return smoothrefreshtime;
        } catch (final Exception ignored) {

        }
        return 500;
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
		if (!fromUser) {
            return;
        }
        final long now = SystemClock.elapsedRealtime();
        if (now - mLastSeekEventTime > 250) {
            mLastSeekEventTime = now;
            mPosOverride = mDuration * progress / 1000;
            MusicServiceWrapper.seek(mPosOverride);
            if (!mFromTouch) {
                // refreshCurrentTime();
                mPosOverride = -1;
            }
        }
		
	}

	@Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mLastSeekEventTime = 0;
        mFromTouch = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mPosOverride = -1;
        mFromTouch = false;
    }
    
    private void queueNextRefresh(final long delay) {
        if (!mIsPaused && mShown) {
            final Message message = mTimeHandler.obtainMessage(REFRESH_TIME);
            mTimeHandler.removeMessages(REFRESH_TIME);
            mTimeHandler.sendMessageDelayed(message, delay);
        }
    }
    
    private void updateMeta(Track track) {
    	if (track != null) {
			mTrackText.setText(track.getTitle());
			mAlbumText.setText(track.getAlbum());
			mArtistText.setText(track.getArtist());
			mImageWorker.loadAlbumImage(track, mAlbumArt);
			
			mDuration = track.getDuration();
			mTotalTime.setText(TextUtils.getTrackDurationText(getResources(), mDuration));
		}
    }
    
    private void updatePlayState(boolean isPlaying) {
    	if (isPlaying) {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_pause);
			mElapsedTime.clearAnimation();
			queueNextRefresh(1);
		} else {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_play);
			refreshCurrentTime();
			mElapsedTime.startAnimation(AnimationUtils.loadAnimation(getActivity(), 
					R.anim.fade_in_out));
			mTimeHandler.removeCallbacks(null);
		}
    }
    
    private View.OnClickListener mPlayButtonListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (v == mPlayPauseButton) {
				MusicServiceWrapper.togglePlayback(getActivity());
			} else if (v == mNextButton) {
				MusicServiceWrapper.next(getActivity());
			} else if (v == mPreviousButton) {
				MusicServiceWrapper.previous(getActivity());
			}
			
		}
	};
    
    /**
     * Used to update the current time string
     */
    private static final class TimeHandler extends Handler {

        private final WeakReference<MusicPlayerFragment> mAudioPlayer;

        public TimeHandler(MusicPlayerFragment player) {
            mAudioPlayer = new WeakReference<MusicPlayerFragment>(player);
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case REFRESH_TIME:
                    final long next = mAudioPlayer.get().refreshCurrentTime();
                    mAudioPlayer.get().queueNextRefresh(next);
                    break;
                default:
                    break;
            }
        }
    }
    
    private final BroadcastReceiver mPlayStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(MusicService.PLAYSTATE_CHANGED)) {
				updatePlayState(intent.getBooleanExtra(MusicService.PLAYSTATE_CHANGED_STATE, 
						false));
			} else if (action.equals(MusicService.META_CHANGED)) {
				updateMeta((Track) intent.getParcelableExtra(MusicService.META_CHANGED_TRACK));
			} else if (action.equals(MusicService.SHUFFLEMODE_CHANGED)) {
				// TODO: Shuffle
			} else if (action.equals(MusicService.REPEATMODE_CHANGED)) {
				// TODO: Repeat
			}
		}
		
	};

}
