package za.jamie.soundstage.fragments.musicplayer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.MusicFragment;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.pablo.Pablo;
import za.jamie.soundstage.pablo.SoundstageUris;
import za.jamie.soundstage.service.MusicConnection;
import za.jamie.soundstage.service.MusicService;
import za.jamie.soundstage.utils.AppUtils;
import za.jamie.soundstage.utils.MessengerUtils;
import za.jamie.soundstage.widgets.DurationTextView;
import za.jamie.soundstage.widgets.RepeatingImageButton;

public class MusicPlayerFragment extends MusicFragment {

	// Rate at which the repeat listeners for the seek buttons refresh in ms
	private static final int REPEAT_INTERVAL = 260;

	// Handle elapsed time text updates
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mSeeking = false;

	// Play control buttons
	private ImageButton mPlayPauseButton;
	private RepeatingImageButton mPreviousButton;
	private RepeatingImageButton mNextButton;

	// Shuffle/repeat buttons
	private ImageButton mShuffleButton;
	private ImageButton mRepeatButton;

	// UI elements for metadata
	private TextView mTrackText;
	private TextView mAlbumText;
	private TextView mArtistText;
	private ImageView mAlbumArt;

	// UI elements for seeking
	private DurationTextView mElapsedTime;
	private DurationTextView mTotalTime;
	private int mDuration;

	private DurationTextView mBigElapsedTime;
	private DurationTextView mBigTotalTime;
	private LinearLayout mSeekInfoHolder;

	// Seek bar
	private int mSeekStartPosition = 0;
    private ObjectAnimator mSeekBarAnimator;
    private Animator mBigSeekAnimator;

    private boolean mIsPlaying = false;

	private int mTimeSync;
	private long mTimeSyncStamp;

    private int mImageSize;

    private boolean isSynced = false;

    private Messenger mPlayerClient;

	private final MusicConnection.ConnectionObserver mConnectionObserver =
			new MusicConnection.ConnectionObserver() {
		@Override
		public void onConnected() {
            MusicConnection connection = getMusicConnection();
			connection.registerPlayerClient(mPlayerClient);
            if (!isSynced) {
                isSynced = connection.requestPlayerUpdate(mPlayerClient);
            }
		}

		@Override
		public void onDisconnected() {

		}
	};

	public MusicPlayerFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Handler playerClientHandler = new PlayerClientHandler(this);
        mPlayerClient = new Messenger(playerClientHandler);

		// Register connection observer
		getMusicConnection().registerConnectionObserver(mConnectionObserver);

        Resources res = getResources();
        mImageSize = AppUtils.smallestScreenWidth(res)
                - res.getDimensionPixelOffset(R.dimen.menudrawer_offset);
	}

    @Override
    public void onStart() {
        super.onStart();
        if (!isSynced) {
            isSynced = getMusicConnection().requestPlayerUpdate(mPlayerClient);
        }
    }

	@Override
	public void onStop() {
		super.onStop();
		mHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();

        getMusicConnection().unregisterPlayerClient(mPlayerClient);
        getMusicConnection().unregisterConnectionObserver(mConnectionObserver);
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

        // YAAAAAAYYYY Android. I just love typing.
        // Playback controls
        mPlayPauseButton = (ImageButton) v.findViewById(R.id.action_button_play);
        mPreviousButton = (RepeatingImageButton) v.findViewById(R.id.action_button_previous);
        mNextButton = (RepeatingImageButton) v.findViewById(R.id.action_button_next);

        mPlayPauseButton.setOnClickListener(mButtonListener);
        mPreviousButton.setOnClickListener(mButtonListener);
        mPreviousButton.setRepeatListener(mSeekButtonListener, REPEAT_INTERVAL);
        mNextButton.setOnClickListener(mButtonListener);
        mNextButton.setRepeatListener(mSeekButtonListener, REPEAT_INTERVAL);

        // Shuffle/repeat
        mShuffleButton = (ImageButton) v.findViewById(R.id.action_button_shuffle);
        mRepeatButton = (ImageButton) v.findViewById(R.id.action_button_repeat);

        mShuffleButton.setOnClickListener(mButtonListener);
        mRepeatButton.setOnClickListener(mButtonListener);

        // Metadata info
        mTrackText = (TextView) v.findViewById(R.id.music_player_track_name);
        mAlbumText = (TextView) v.findViewById(R.id.music_player_album_name);
        mAlbumText.setOnClickListener(mMetaListener);
        mArtistText = (TextView) v.findViewById(R.id.music_player_artist_name);
        mArtistText.setOnClickListener(mMetaListener);
        mAlbumArt = (ImageView) v.findViewById(R.id.music_player_album_art);

        SeekBar seekBar = (SeekBar) v.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(mSeekListener);
        mSeekBarAnimator = ObjectAnimator.ofInt(seekBar, "progress", 0, 1000);

        mElapsedTime = (DurationTextView) v.findViewById(R.id.elapsedTime);
        mTotalTime = (DurationTextView) v.findViewById(R.id.totalTime);

        // UI that appears when seeking with seek bar
        mSeekInfoHolder = (LinearLayout) v.findViewById(R.id.seek_info_holder);
        mBigElapsedTime = (DurationTextView) v.findViewById(R.id.big_elapsed_time);
        mBigTotalTime = (DurationTextView) v.findViewById(R.id.big_total_time);

        mBigSeekAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_in_big_seek);
        mBigSeekAnimator.setTarget(mSeekInfoHolder);

        // Make the track title single line if there isn't room for 2 lines
        if (AppUtils.isPortrait(getResources())) {
            ensureInfoHolderHasSpace(v);
        }

        return v;
    }

    private void ensureInfoHolderHasSpace(View root) {
        final View infoHolder = root.findViewById(R.id.music_player_info_holder);
        infoHolder.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        int measuredWidth = infoHolder.getMeasuredWidth();
                        int measuredHeight = infoHolder.getMeasuredHeight();
                        if (measuredWidth > 0 && measuredHeight > 0) {
                            if (measuredHeight < getResources()
                                    .getDimensionPixelSize(R.dimen.music_player_min_info_holder_height)) {
                                mTrackText.setSingleLine();
                            }
                            infoHolder.getViewTreeObserver().removeOnPreDrawListener(this);
                        }
                        return true;
                    }
                });
    }

	/*
	 * Listener for long presses on ffwd/rewind buttons. Skip ahead/back
	 */
	private final RepeatingImageButton.RepeatListener mSeekButtonListener =
            new RepeatingImageButton.RepeatListener() {

        @Override
        public void onRepeat(View view, long duration, int repeatCount) {
            if (view == mPreviousButton) {
                scan(duration, repeatCount, true);
            } else if (view == mNextButton) {
                scan(duration, repeatCount, false);
            }
        }
    };

    /*
     * Listener for seek bar input. Displays extra UI when seeking
     */
    private final SeekBar.OnSeekBarChangeListener mSeekListener =
            new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            beginSeek();
            mBigElapsedTime.setDuration(calculatePosition());
            mBigTotalTime.setDuration(mDuration);
            mBigSeekAnimator.start();
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            final int seekPosition = (int) (mDuration * (progress / 1000.0f));
            mBigElapsedTime.setDuration(seekPosition);
            seek(seekPosition);
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            endSeek();
            mBigSeekAnimator.cancel();
            float alpha = mSeekInfoHolder.getAlpha();
            if (alpha > 0.0f) {
                float fadeIn = alpha / 1.0f;
                ObjectAnimator.ofFloat(mSeekInfoHolder, "alpha", fadeIn, 0.0f)
                        .setDuration((int) (150 * fadeIn))
                        .start();
            }
        }
    };

	/**
     * Used to scan backwards in time through the current track
     *
     * @param repeatCount The repeat count
     */
    private void scan(long duration, int repeatCount, boolean backward) {
        if (repeatCount == 0) {
        	mSeekStartPosition = calculatePosition();
            beginSeek();
        } else {
            if (duration < 5000) {
                // seek at 10x speed for the first 5 seconds
            	duration = duration * 10;
            } else {
                // seek at 40x after that
            	duration = 50000 + (duration - 5000) * 40;
            }
            duration = backward ? -duration : duration;
            final int seekPosition = mSeekStartPosition + (int) duration;
            if (seekPosition < 0) {
                getMusicConnection().previous();
            } else if (seekPosition > mDuration) {
                getMusicConnection().next();
            } else {
            	seek(seekPosition);
            }

            if (repeatCount < 0) {
                endSeek();
            }
        }
    }

    /*
     * When a new track comes in from the server all the UI must get updated
     */
    private void updateTrack(Track track) {
    	if (track != null) {
			mTrackText.setText(track.getTitle());

			mAlbumText.setText(track.getAlbum());
			mAlbumText.setTag(track.getAlbumId()); // bit sneaky

			mArtistText.setText(track.getArtist());
			mArtistText.setTag(track.getArtistId());

			final Uri uri = SoundstageUris.albumImage(track);
			Pablo.with(getActivity())
				    .load(uri)
                    .resize(mImageSize, mImageSize)
                    .centerCrop()
				    .placeholder(mAlbumArt.getDrawable())
                    .error(R.drawable.placeholder_grey)
				    .into(mAlbumArt);

		}
    }

    private void updateDuration(int duration) {
        if (duration > 0) {
            mDuration = duration;
            mTotalTime.setDuration(duration);
            mSeekBarAnimator.setDuration(duration);
        }
    }

    private void updatePlayState(boolean isPlaying) {
    	mIsPlaying = isPlaying;
    	if (isPlaying) {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_pause);
			mElapsedTime.clearAnimation();
		} else {
			mPlayPauseButton.setImageResource(R.drawable.btn_playback_play);
			if (isAdded()) { // Sometimes this is called before fragment is attached, causing NPE
				mElapsedTime.startAnimation(AnimationUtils.loadAnimation(getActivity(),
						R.anim.fade_in_out));
			}
            stopTime();
		}
    }

    private void updateTime(int position, long timeStamp) {
        // Store the sync information
        mTimeSync = position;
        mTimeSyncStamp = timeStamp;

        updateTime(position);
    }

    private void updateTime(int position) {
        // Update the elapsed time indicators
        updateSeekBar(position);
        updateElapsedTime(position);
    }

    private int calculatePosition() {
   		long position = mIsPlaying ?
                mTimeSync + (System.currentTimeMillis() - mTimeSyncStamp) : mTimeSync;
        return (int) position;
    }

    private void seek(int position) {
        getMusicConnection().seek(position);
        updateTime(position, System.currentTimeMillis());
    }

    private void stopTime() {
    	mSeekBarAnimator.cancel();
    	mHandler.removeCallbacks(mTimeRefresh);
    }

    private void updateSeekBar(int position) {
    	if (mIsPlaying && !mSeeking && !mSeekBarAnimator.isStarted()) {
    		mSeekBarAnimator.start();
    	}
    	mSeekBarAnimator.setCurrentPlayTime(position);
    }

    private void beginSeek() {
    	mSeeking = true;
    	updateTime(calculatePosition());
    	if (mIsPlaying) {
    		stopTime();
    	}
    }

    private void endSeek() {
    	mSeeking = false;
    	updateTime(calculatePosition());
    }

    private void updateElapsedTime(int position) {
    	if (position < 0) {
    		position = 0;
    	} else if (position > mDuration) {
    		position = mDuration;
    	}
        mElapsedTime.setDuration(position);
        if (!mSeeking && mIsPlaying) {
            mHandler.postDelayed(mTimeRefresh, 1000 - (position % 1000)); // Update again on the next second
        }
    }

    private void updateShuffleState(boolean shuffleEnabled) {
    	mShuffleButton.setImageResource(shuffleEnabled ?
    			R.drawable.btn_playback_shuffle_all : R.drawable.btn_shuffle);
    }

    private void updateRepeatMode(int repeatMode) {
    	switch (repeatMode) {
    	case MusicService.REPEAT_NONE:
    		mRepeatButton.setImageResource(R.drawable.btn_repeat);
    		break;
    	case MusicService.REPEAT_ALL:
    		mRepeatButton.setImageResource(R.drawable.btn_playback_repeat_all);
    		break;
    	case MusicService.REPEAT_CURRENT:
    		mRepeatButton.setImageResource(R.drawable.btn_playback_repeat_one);
    		break;
    	}
    }

    private final View.OnClickListener mButtonListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == mPlayPauseButton) {
				getMusicConnection().togglePlayback();
			} else if (v == mNextButton) {
				getMusicConnection().next();
			} else if (v == mPreviousButton) {
				getMusicConnection().previous();
			} else if (v == mShuffleButton) {
				getMusicConnection().toggleShuffle();
			} else if (v == mRepeatButton) {
				getMusicConnection().cycleRepeat();
			}

		}
	};

	private final View.OnClickListener mMetaListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == mArtistText) {
				final Uri data = ContentUris.withAppendedId(
						MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
						(Long) mArtistText.getTag());
				startActivity(new Intent(Intent.ACTION_VIEW)
						.setDataAndType(data, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE));
			} else if (v == mAlbumText) {
				final Uri data = ContentUris.withAppendedId(
						MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
						(Long) mAlbumText.getTag());
				startActivity(new Intent(Intent.ACTION_VIEW)
						.setDataAndType(data, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE));
			}
		}

	};

	public boolean isPlaying() {
		return mIsPlaying;
	}

    private final Runnable mTimeRefresh = new Runnable() {
        @Override
        public void run() {
            if (!mSeeking && mDuration != 0) {
                updateElapsedTime(calculatePosition());
            }
        }
    };

    private static class PlayerClientHandler extends Handler {

        final WeakReference<MusicPlayerFragment> mPlayer;

        PlayerClientHandler(MusicPlayerFragment player) {
            mPlayer = new WeakReference<MusicPlayerFragment>(player);
        }

        @Override
        public void handleMessage(Message msg) {
            final MusicPlayerFragment player = mPlayer.get();
            if (player == null) {
                return;
            }

            switch(msg.what) {
                case MusicService.MSG_UPDATE_TRACK:
                    player.updateTrack((Track) MessengerUtils.readParcelable(player.getActivity(),
                            msg, "track"));
                    player.updateDuration(msg.getData().getInt("duration"));
                    break;
                case MusicService.MSG_SYNC_POSITION:
                    player.updateTime(msg.arg1, MessengerUtils.readTimestamp(msg));
                    break;
                case MusicService.MSG_UPDATE_PLAY_STATE:
                    player.updatePlayState(msg.arg1 > 0);
                    break;
                case MusicService.MSG_UPDATE_SHUFFLE_STATE:
                    player.updateShuffleState(msg.arg1 > 0);
                    break;
                case MusicService.MSG_UPDATE_REPEAT_MODE:
                    player.updateRepeatMode(msg.arg1);
                    break;
            }
        }
    }

}
