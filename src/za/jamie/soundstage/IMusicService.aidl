package za.jamie.soundstage;

import za.jamie.soundstage.IMusicStatusCallback;
import za.jamie.soundstage.IPlayQueueCallback;
import za.jamie.soundstage.models.Track;
import android.content.ComponentName;
import android.net.Uri;

oneway interface IMusicService {
	// Access to the queue for the PlayQueueFragment
	void setQueuePosition(int position);	
	void moveQueueItem(int from, int to);
	void removeTrack(int position);
	
	// QueueStatusCallback
	void registerPlayQueueCallback(IPlayQueueCallback callback);
	void unregisterPlayQueueCallback(IPlayQueueCallback callback);
	
	// Access to play controls for the MusicPlayerFragment
	void togglePlayback();
	void next();
	void previous();
	void seek(long position);
	void toggleShuffle();
	void cycleRepeatMode();
	
	// MusicStatusCallback
	void registerMusicStatusCallback(IMusicStatusCallback callback);
	void unregisterMusicStatusCallback(IMusicStatusCallback callback);	
	
	// Opening new music/enqueueing music for the library
	void open(in List<Track> tracks, int position);
	void shuffle(in List<Track> tracks);
	void enqueue(in List<Track> tracks, int action);
	
	// Tell the service to show/remove notification
	void showNotification(in PendingIntent intent);
	void hideNotification();
}