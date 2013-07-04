package za.jamie.soundstage.service.connections;

import za.jamie.soundstage.IPlayQueueCallback;

public interface MusicQueueConnection {
	
	/**
	 * Set the current position in the queue and play.
	 * @param position
	 */
	void setQueuePosition(int position);
	
	/**
	 * Move an item in the queue
	 * @param from Original item position
	 * @param to New item position
	 */
	void moveQueueItem(int from, int to);
	
	/**
	 * Remove a track from the queue
	 * @param position Position in the queue of the track to remove
	 */
	void removeTrack(int position);
	
	/**
	 * Request the current play queue and position to be delivered to any
	 * registered callbacks.
	 */
	void requestPlayQueue();
	
	/**
	 * Register a callback to be notified of queue changes.
	 * @param callback
	 */
	void registerPlayQueueCallback(IPlayQueueCallback callback);
	
	/**
	 * Unregister a callback from receiving queue change notifications.
	 * @param callback
	 */
	void unregisterPlayQueueCallback(IPlayQueueCallback callback);
}
