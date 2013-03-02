package za.jamie.soundstage;

import za.jamie.soundstage.models.Track;

interface IMusicService {
	// Queue creation
    void open(in List<Track> tracks, int position);
    
    // Queue manipulation
    void enqueue(in List<Track> tracks, int action);
    void setQueuePosition(int index);
    void moveQueueItem(int from, int to);
    int removeTracks(int first, int last);
    int removeTrack(long id);
        
    // Queue state
    List<Track> getQueue();
    int getQueuePosition();
    Track getCurrentTrack();
    
    // Play state
    boolean isPlaying();
    int getShuffleMode();
    int getRepeatMode();
    
    // Time position in current track
    long position();
    long seek(long pos);    
}