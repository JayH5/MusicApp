package za.jamie.soundstage;

import za.jamie.soundstage.models.Track;

oneway interface IPlayQueueCallback {
	void deliverTrackList(in List<Track> trackList, int position, boolean shuffled);
	void onPositionChanged(int position);
}