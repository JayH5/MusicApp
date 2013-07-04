package za.jamie.soundstage.service;

import android.media.MediaPlayer;

public class GaplessPlayer2 extends MediaPlayer {

	private GaplessPlayer2 mNextPlayer;
	
	@Override
	public void setNextMediaPlayer(MediaPlayer next) {
		if (next != null && !(next instanceof GaplessPlayer2)) {
			throw new ClassCastException("Must set next player to be another GaplessPlayer!");
		}
		super.setNextMediaPlayer(next);
		
		if (next == null && mNextPlayer != null) {
			mNextPlayer.release();
		}
		mNextPlayer = (GaplessPlayer2) next;
	}
	
	public GaplessPlayer2 getNextMediaPlayer() {
		return mNextPlayer;
	}

}
