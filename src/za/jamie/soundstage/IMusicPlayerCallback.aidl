package za.jamie.soundstage;

import za.jamie.soundstage.models.Track;

oneway interface IMusicPlayerCallback {
	void onPositionSync(long position, long timeStamp);
	void onTrackChanged(in Track track);
	void onPlayStateChanged(boolean isPlaying);
	void onShuffleStateChanged(boolean isShuffled);
	void onRepeatModeChanged(int repeatMode);
}