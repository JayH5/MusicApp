package za.jamie.soundstage.service;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.PowerManager;

public class PlayerHelper implements OnCompletionListener, OnErrorListener {

	private final Context mContext;
	private GaplessPlayer2 mPlayer = new GaplessPlayer2();
	
	public PlayerHelper(Context context) {
		mContext = context;
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		switch(what) {
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			mPlayer.release();
			mPlayer = new GaplessPlayer2();
			mPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
			return true;
		}
		
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mp == mPlayer) {
			final GaplessPlayer2 gp = (GaplessPlayer2) mp;
			if (gp.getNextMediaPlayer() != null) {
				mp.release();
				mPlayer = gp;
			}
		}
		
	}
	
}
