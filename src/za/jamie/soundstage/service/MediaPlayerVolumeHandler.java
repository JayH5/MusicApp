package za.jamie.soundstage.service;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by jamie on 2014/03/15.
 */
public class MediaPlayerVolumeHandler extends Handler {

    private static final float VOLUME_DUCK = .2f;
    private static final float STEP_SIZE_FADE_DOWN = .05f;
    private static final float STEP_SIZE_FADE_UP = .01f;
    private static final long DELAY_INTERVAL = 10;

    private static final int MSG_FADEDOWN = 1;
    private static final int MSG_FADEUP = 2;
    private static final int MSG_MUTE = 3;

    private float mVolume = 1.0f;

    private MediaPlayer mPlayer;

    public MediaPlayerVolumeHandler() {
        super();
    }

    public MediaPlayerVolumeHandler(Looper looper) {
        super(looper);
    }

    public void setMediaPlayer(MediaPlayer mp) {
        mPlayer = mp;
    }

    public void fadeDown() {
        stopFadeUp();
        sendEmptyMessage(MSG_FADEDOWN);
    }

    public void fadeUp() {
        stopFadeDown();
        sendEmptyMessage(MSG_FADEUP);
    }

    public void stopFadeDown() {
        removeMessages(MSG_FADEDOWN);
    }

    public void stopFadeUp() {
        removeMessages(MSG_FADEUP);
    }

    public void stopFade() {
        removeCallbacksAndMessages(null);
    }

    public void mute() {
        stopFade();
        sendEmptyMessage(MSG_MUTE);
    }

    @Override
    public void handleMessage(Message msg) {
        final MediaPlayer mp = mPlayer;
        if (mp == null) {
            return;
        }

        switch (msg.what) {
            case MSG_FADEDOWN:
                mVolume -= STEP_SIZE_FADE_DOWN;
                if (mVolume > VOLUME_DUCK) {
                    sendEmptyMessageDelayed(MSG_FADEDOWN, DELAY_INTERVAL);
                } else {
                    mVolume = VOLUME_DUCK;
                }
                break;
            case MSG_FADEUP:
                mVolume += STEP_SIZE_FADE_UP;
                if (mVolume < 1.0f) {
                    sendEmptyMessageDelayed(MSG_FADEUP, DELAY_INTERVAL);
                } else {
                    mVolume = 1.0f;
                }
                break;
            case MSG_MUTE:
                mVolume = 0f;
                break;
        }
        mp.setVolume(mVolume, mVolume);
    }

}
