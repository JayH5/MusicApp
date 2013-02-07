package com.jamie.play;

import android.graphics.Bitmap;
import android.net.Uri;
import com.jamie.play.service.Track;

interface IMusicService {
    void openFile(String path);
    void open(in List<Track> tracks, int position);
    
    void play();
    void pause();
    void stop();
    void prev();
    void next();
    
    void enqueue(in List<Track> tracks, int action);
    void setQueuePosition(int index);
    void moveQueueItem(int from, int to);
    List<Track> getQueue();
    int getQueuePosition();
    
    int removeTracks(int first, int last);
    int removeTrack(long id); 
    
    void setShuffleMode(int shufflemode);
    void setRepeatMode(int repeatmode);
    int getShuffleMode();
    int getRepeatMode();
    
    void refresh();
    boolean isPlaying();
    
    long duration();
    long position();
    long seek(long pos);
    
    long getAudioId();
    long getArtistId();
    long getAlbumId();
    String getTrackName();
    String getArtistName();
    String getAlbumName();
    Uri getUri();
    
    int getMediaMountedCount();
    int getAudioSessionId();
}