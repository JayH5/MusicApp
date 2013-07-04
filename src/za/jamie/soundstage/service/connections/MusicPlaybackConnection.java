package za.jamie.soundstage.service.connections;

import za.jamie.soundstage.IMusicStatusCallback;

public interface MusicPlaybackConnection {
	
	/**
	 * Toggles play/pause
	 */
	void togglePlayback();
	
	/**
	 * Move to and play next track in queue
	 */
	void next();
	
	/**
	 * Move to and play previous track in queue (or seek to start of current track
	 * if less than 2 seconds into current track).
	 */
	void previous();
	
	/**
	 * Seek to the specified position in the current track
	 * @param position Position in milliseconds
	 */
	void seek(long position);
	
	/**
	 * Switches shuffle on/off
	 */
	void toggleShuffle();
	
	/**
	 * Cycle between repeat modes.
	 * No repeat -> repeat all -> repeat one
	 */
	void cycleRepeatMode();
	
	/**
	 * Request the current playback status. Playstate, position, shuffle enabled and repeat mode as well as the current track.
	 */
	void requestMusicStatus();
	
	/**
	 * Register a callback to receive playback state changes.
	 * @param callback
	 */
	void registerMusicStatusCallback(IMusicStatusCallback callback);
	
	/**
	 * Unregister a callback from receiving playback state changes.
	 * @param callback
	 */
	void unregisterMusicStatusCallback(IMusicStatusCallback callback);	
}
