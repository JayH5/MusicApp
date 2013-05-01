package za.jamie.soundstage;

public interface MusicQueueWrapper {
	// Control
	public void setQueuePosition(int position);	
	public void moveQueueItem(int from, int to);
	public void removeTrack(int position);
	
	// Callback
	public void requestQueueStatusRefresh();
	public void registerQueueStatusCallback(IQueueStatusCallback callback);
	public void unregisterQueueStatusCallback(IQueueStatusCallback callback);
}
