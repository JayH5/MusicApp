package za.jamie.soundstage;

public interface MusicPlaybackWrapper {
	// Controls
	public void togglePlayback();
	public void next();
	public void previous();
	public void seek(long position);
	public void cycleShuffleMode();
	public void cycleRepeatMode();
	
	// Callbacks
	public void requestMusicStatusRefresh();
	public void registerMusicStatusCallback(IMusicStatusCallback callback);
	public void unregisterMusicStatusCallback(IMusicStatusCallback callback);	
}
