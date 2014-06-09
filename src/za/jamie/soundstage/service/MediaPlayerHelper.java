package za.jamie.soundstage.service;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

/**
 * Created by jamie on 2014/03/15.
 */
public class MediaPlayerHelper {

    private static final String TAG = "MediaPlayerHelper";

    private MediaPlayer mCurrentMediaPlayer;
    private MediaPlayer mNextMediaPlayer;

    private MediaPlayerVolumeHandler mVolumeHandler;

    private boolean mIsInitialized;

    private MediaPlayerListener mListener;

    private final Context mContext;

    public MediaPlayerHelper(Context context) {
        mContext = context;
        mVolumeHandler = new MediaPlayerVolumeHandler();

        setCurrentMediaPlayer(createMediaPlayer());

        broadcastAudioEffectControlSession(true);
    }

    private void setCurrentMediaPlayer(MediaPlayer mp) {
        mVolumeHandler.setMediaPlayer(mp);
        mCurrentMediaPlayer = mp;
    }

    private MediaPlayer createMediaPlayer() {
        MediaPlayer mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        mp.setOnCompletionListener(mOnCompletionListener);
        mp.setOnErrorListener(mOnErrorListener);
        return mp;
    }

    /** Notify any equalizers/audio effects that we're going to play music. */
    private void broadcastAudioEffectControlSession(boolean open) {
        String action = open ? AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION :
                AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION;
        final Intent intent = new Intent(action);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mCurrentMediaPlayer.getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mContext.getPackageName());
        mContext.sendBroadcast(intent);
    }

    public int getAudioSessionId() {
        return mCurrentMediaPlayer.getAudioSessionId();
    }

    public boolean setDataSource(Uri uri) {
        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, uri);
        if (mIsInitialized) {
            setNextDataSource(null);
        }
        return mIsInitialized;
    }

    public void setNextDataSource(Uri uri) {
        mCurrentMediaPlayer.setNextMediaPlayer(null);

        if (mNextMediaPlayer != null) {
            mNextMediaPlayer.release();
            mNextMediaPlayer = null;
        }

        if (uri == null) {
            return;
        }

        mNextMediaPlayer = createMediaPlayer();
        mNextMediaPlayer.setAudioSessionId(mCurrentMediaPlayer.getAudioSessionId());

        if (setDataSourceImpl(mNextMediaPlayer, uri)) {
            mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
        } else {
            mNextMediaPlayer.release();
            mNextMediaPlayer = null;
        }
    }

    private boolean setDataSourceImpl(MediaPlayer mp, Uri uri) {
        mp.reset();
        try {
            mp.setDataSource(mContext, uri);
            mp.prepare();
        } catch (IOException e) {
            Log.e(TAG, "Failed to set data source.", e);
            return false;
        }

        return true;
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public void start() {
        mCurrentMediaPlayer.start();
    }

    public void pause() {
        mCurrentMediaPlayer.pause();
    }

    public void reset() {
        mCurrentMediaPlayer.reset();
        mIsInitialized = false;
    }

    public void release() {
        mVolumeHandler.stopFade();
        mVolumeHandler.setMediaPlayer(null);

        broadcastAudioEffectControlSession(false);

        mCurrentMediaPlayer.release();
        if (mNextMediaPlayer != null) {
            mNextMediaPlayer.release();
        }

        mIsInitialized = false;
    }

    /** Get the current playback position. */
    public int getCurrentPosition() {
        return mCurrentMediaPlayer.getCurrentPosition();
    }

    /** Get the duration of the current file. */
    public int getDuration() {
        return mCurrentMediaPlayer.getDuration();
    }

    /** Seeks to specified time position. */
    public void seekTo(long msec) {
        mCurrentMediaPlayer.seekTo((int) msec);
    }

    public boolean isLooping() {
        return mCurrentMediaPlayer.isLooping();
    }

    public void setLooping(boolean looping) {
        mCurrentMediaPlayer.setLooping(looping);
    }

    public void setVolume(float volume) {
        mCurrentMediaPlayer.setVolume(volume, volume);
    }

    public void setMediaPlayerListener(MediaPlayerListener listener) {
        mListener = listener;
    }

    public MediaPlayerVolumeHandler getVolumeHandler() {
        return mVolumeHandler;
    }

    private void notifyTrackWentToNext() {
        if (mListener != null) {
            mListener.onTrackWentToNext();
        }
    }

    private void notifyTrackEnded() {
        if (mListener != null) {
            mListener.onTrackEnded();
        }
    }

    private void notifyServerDied() {
        if (mListener != null) {
            mListener.onServerDied();
        }
    }

    private final MediaPlayer.OnCompletionListener mOnCompletionListener =
            new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mp != mCurrentMediaPlayer) {
                return;
            }

            if (mNextMediaPlayer != null) {
                mCurrentMediaPlayer.release();
                setCurrentMediaPlayer(mNextMediaPlayer);
                mNextMediaPlayer = null;
                notifyTrackWentToNext();
            } else {
                notifyTrackEnded();
            }
        }
    };

    private final MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (mp != mCurrentMediaPlayer) {
                return false;
            }

            switch(what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    mIsInitialized = false;
                    mCurrentMediaPlayer.release();
                    setCurrentMediaPlayer(createMediaPlayer());
                    notifyServerDied();
                    return true;
                default:
                    break;
            }
            return false;
        }
    };

    public interface MediaPlayerListener {
        void onTrackWentToNext();
        void onTrackEnded();
        void onServerDied();
    }
}
