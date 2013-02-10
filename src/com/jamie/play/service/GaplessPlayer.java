package com.jamie.play.service;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.PowerManager;

public class GaplessPlayer implements MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener {
	private final WeakReference<Context> mContext;

	private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();
	private MediaPlayer mNextMediaPlayer;

	private PlayerCallbacks mListener;

	private boolean mIsInitialized = false;

	public GaplessPlayer(Context context) {
		mContext = new WeakReference<Context>(context);
		mCurrentMediaPlayer.setWakeMode(mContext.get(), PowerManager.PARTIAL_WAKE_LOCK);
	}

	private boolean setDataSourceImpl(final MediaPlayer player, final Uri uri) {
        try {
            player.reset();
            player.setDataSource(mContext.get(), uri);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.prepare();
        } catch (final IOException todo) {
            // TODO: notify the user why the file couldn't be opened
            return false;
        } catch (final IllegalArgumentException todo) {
            // TODO: notify the user why the file couldn't be opened
            return false;
        }
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        
        // Notify any equalizers/audio effects that we're going to play music.
        final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mContext.get().getPackageName());
        mContext.get().sendBroadcast(intent);
        
        return true;
    }
	
	
	public boolean setDataSource(final Uri uri) {
        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, uri);
        if (mIsInitialized) {
            setNextDataSource(null);
        }
        return mIsInitialized;
    }
	
	public void setNextDataSource(final Uri uri) {
        // Clear the next media player attached to the current one
    	mCurrentMediaPlayer.setNextMediaPlayer(null);
        // If the next media player exists, clear that too
    	if (mNextMediaPlayer != null) {
            mNextMediaPlayer.release();
            mNextMediaPlayer = null;
        }
    	// If uri is null, i.e. we're setting the current data source, exit
        if (uri == null) {
            return;
        }
        
        // Set up a new media player to be the next to play
        mNextMediaPlayer = new MediaPlayer();
        mNextMediaPlayer.setWakeMode(mContext.get(), PowerManager.PARTIAL_WAKE_LOCK);
        mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
        
        // If we set the data source successfully then set this to be next to play
        if (setDataSourceImpl(mNextMediaPlayer, uri)) {
            mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
        } else {
        	// Otherwise release the new player
            if (mNextMediaPlayer != null) {
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
        }
    }

	public void setCallbacks(final PlayerCallbacks listener) {
		mListener = listener;
	}

	public boolean isInitialized() {
		return mIsInitialized;
	}

	public void start() {
		mCurrentMediaPlayer.start();
	}

	public void stop() {
		mCurrentMediaPlayer.reset();
		mIsInitialized = false;
	}

	public void release() {
		stop();
		mCurrentMediaPlayer.release();
	}

	public void pause() {
		mCurrentMediaPlayer.pause();
	}

	public long duration() {
		return mCurrentMediaPlayer.getDuration();
	}

	public long position() {
		return mCurrentMediaPlayer.getCurrentPosition();
	}

	public long seek(final long whereto) {
		mCurrentMediaPlayer.seekTo((int)whereto);
		return whereto;
	}

	public void setVolume(final float vol) {
		mCurrentMediaPlayer.setVolume(vol, vol);
	}

	public void setAudioSessionId(final int sessionId) {
		mCurrentMediaPlayer.setAudioSessionId(sessionId);
	}

	public int getAudioSessionId() {
		return mCurrentMediaPlayer.getAudioSessionId();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
			mCurrentMediaPlayer.release();
			mCurrentMediaPlayer = mNextMediaPlayer;
			mNextMediaPlayer = null;
			mListener.onTrackWentToNext();
		} else {
			mListener.onTrackEnded();
		}

	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			mIsInitialized = false;
			mCurrentMediaPlayer.release();
			mCurrentMediaPlayer = new MediaPlayer();
			mCurrentMediaPlayer.setWakeMode(mContext.get(), PowerManager.PARTIAL_WAKE_LOCK);
			mListener.onServerDied();
			return true;
		default:
			break;
		}
		return false;
	}
	
	public interface PlayerCallbacks {
		public void onTrackWentToNext();
		
		public void onTrackEnded();
		
		public void onServerDied();
	}
}
