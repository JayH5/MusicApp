package za.jamie.soundstage.service.connections;

import java.util.List;

import za.jamie.soundstage.models.Track;

public interface MusicLibraryConnection {
	
	/**
	 * Resets the play queue to contain the listed tracks and begins playback at the
	 * specified position.
	 * @param tracks
	 * @param position
	 */
	public void open(List<Track> tracks, int position);
	
	/**
	 * Resets the play queue to contain the listed tracks, enables shuffle and begins
	 * playback of a random track in the queue.
	 * @param tracks
	 */
	public void shuffle(List<Track> tracks);
	
	/**
	 * Adds the specified list of tracks to the queue according to the action specified.
	 * @param tracks
	 * @param action
	 */
	public void enqueue(List<Track> tracks, int action);
}
