package za.jamie.soundstage.service;

import za.jamie.soundstage.models.Track;

public interface MusicPlaybackCallback {

	void onPositionSync(long position, long timeStamp);
	void onTrackChanged(Track track);
	void onPlayStateChanged(boolean isPlaying);
	void onShuffleStateChanged(boolean isShuffled);
	void onRepeatModeChanged(int repeatMode);
}
