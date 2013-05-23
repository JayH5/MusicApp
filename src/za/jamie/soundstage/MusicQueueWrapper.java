package za.jamie.soundstage;

public interface MusicQueueWrapper {
	// Control
	public void setQueuePosition(int position);	
	public void moveQueueItem(int from, int to);
	public void removeTrack(int position);
	
	// Callback
	public void requestPlayQueue();
	public void registerPlayQueueCallback(IPlayQueueCallback callback);
	public void unregisterPlayQueueCallback(IPlayQueueCallback callback);
}
