package za.jamie.soundstage;

import za.jamie.soundstage.models.Track;

oneway interface IMusicStatusCallback {
	void onPositionSync(long position, long timeStamp);
	void onTrackChanged(in Track track);
	void onPlayStateChanged(boolean isPlaying);
	void onShuffleModeChanged(int shuffleMode);
	void onRepeatModeChanged(int repeatMode);
}