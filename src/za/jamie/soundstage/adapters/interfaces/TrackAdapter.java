package za.jamie.soundstage.adapters.interfaces;

import java.util.List;

import za.jamie.soundstage.models.Track;

public interface TrackAdapter {
	
	/**
	 * Gets the list of tracks
	 * @return The list of tracks backing this adapter
	 */
	List<Track> getTrackList();
	
	/**
	 * Get an individual track
	 * @param position Position of the track in the list
	 * @return The track at the given position in the adapter
	 */
	Track getTrack(int position);
}
