package za.jamie.soundstage;

import java.util.List;

import za.jamie.soundstage.models.Track;

public interface MusicLibraryWrapper {
	// Opening new music/enqueueing music for the library
	public void open(List<Track> tracks, int position);
	public void enqueue(List<Track> tracks, int action);
}
