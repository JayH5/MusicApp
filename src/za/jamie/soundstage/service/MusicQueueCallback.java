package za.jamie.soundstage.service;

import java.util.List;

import za.jamie.soundstage.models.Track;


public interface MusicQueueCallback {

	void deliverTrackList(List<Track> tracklist, int position, boolean isShuffled);
	void onPositionChanged(int position);
}
