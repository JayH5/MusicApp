package za.jamie.soundstage;

import za.jamie.soundstage.IMusicStatusCallback;
import za.jamie.soundstage.IQueueStatusCallback;
import za.jamie.soundstage.models.Track;

oneway interface IMusicService {
	// Access to the queue for the PlayQueueFragment
	void setQueuePosition(int position);	
	void moveQueueItem(int from, int to);
	void removeTrack(int position);
	void removeTrackById(long id);
	
	// QueueStatusCallback
	void requestQueueStatusRefresh();
	void registerQueueStatusCallback(IQueueStatusCallback callback);
	void unregisterQueueStatusCallback(IQueueStatusCallback callback);
	
	// Access to play controls for the MusicPlayerFragment
	void togglePlayback();
	void next();
	void previous();
	void seek(long position);
	
	// MusicStatusCallback
	void requestMusicStatusRefresh();
	void registerMusicStatusCallback(IMusicStatusCallback callback);
	void unregisterMusicStatusCallback(IMusicStatusCallback callback);	
	
	// Opening new music/enqueueing music for the library
	void open(in List<Track> tracks, int position);
	void enqueue(in List<Track> tracks, int action);
}