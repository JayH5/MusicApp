package za.jamie.soundstage.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.util.Log;

import za.jamie.soundstage.models.Track;

public class PlayQueue {
	private final List<Track> mTrackList;
	private int mPosition = -1;

	private final List<Integer> mShuffleMap;
	private boolean mShuffled = false;

	/**
	 * Constructs an empty play queue. The position is initialized as -1.
	 *
	 */
	public PlayQueue() {
		mTrackList = new ArrayList<Track>();
		mShuffleMap = new ArrayList<Integer>();
	}

	/**
	 * Constructs a play queue containing the elements in the specified 
	 * collection and a shuffle map. The position is initialized as -1.
	 *
	 * @param tracks The Collection of Tracks to add to the queue.
	 */
	public PlayQueue(Collection<? extends Track> tracks, 
			List<Integer> shuffleMap) {
		mTrackList = new ArrayList<Track>(tracks);
		mShuffleMap = new ArrayList<Integer>(shuffleMap);
		
		if (!mShuffleMap.isEmpty()) {
			mShuffled = true;
		}
	}

	/**
	 * Clears the current play queue and loads the specified tracks. Sets the
	 * queue position to the one specified and shuffles if requested.
	 *
	 * @param tracks The tracks to be added to the queue
	 * @param position The position to set the queue
	 * @param shuffle True to shuffle the queue, false to use the ordering 
	 * 	described by the collection of tracks.
	 */
	public boolean open(Collection<? extends Track> tracks, int position, 
			boolean shuffle) {

		if (!tracks.equals(mTrackList)) {
			clear();
			mTrackList.addAll(tracks);
			if (shuffle) {
				Log.d("PlayQueue", "Opening, shuffle enabled, track collections unequal");
				shuffle(position);
			} else {
				mShuffled = false;
				moveToPosition(position);
			}
			return true;
		} else {
			if (shuffle) {
				shuffle(position);
			} else {
				mShuffled = false;
				moveToPosition(position);
			}
			return false;
		}
	}

	/**
	 * Shuffles the queue using the Collections.Shuffle() algorithm. The queue
	 * is set to have its first item be the current Track.
	 *
	 */
	public void shuffle() {
		shuffle(mPosition);
	}

	/**
	 * Shuffles the queue using the Collections.Shuffle() algorithm. The queue
	 * is set to have its first item be the item specified by shuffleOn.
	 *
	 * @param The position of the Track that should be placed at the head of the
	 *  queue.
	 */
	public void shuffle(int shuffleOn) {
		// Initialize the map
		mShuffleMap.clear();
		final int len = size();
		for (int i = 0; i < len; i++) {
			if (i == shuffleOn) {
				continue;
			}
			mShuffleMap.add(i);
		}

		Collections.shuffle(mShuffleMap);
		
		if (isPositionValid(shuffleOn)) {
			mShuffleMap.add(0, shuffleOn);
		}
		
		mShuffled = true;
		moveToFirst();
	}

	/**
	 * Disables the shuffle, remaining on the current Track.
	 *
	 */
	public void unShuffle() {
		if (isPositionValid(mPosition)) {
			mPosition = getUnshuffledPosition(mPosition);
		}
		mShuffleMap.clear();
		mShuffled = false;
	}

	/**
	 *
	 * @return True if the queue is currently shuffled
	 */
	public boolean isShuffled() {
		return mShuffled;
	}

	/**
	 * Fetches the track found at the current position.
	 *
	 */
	public Track current() {
		if (isPositionValid(mPosition)) {
			return mTrackList.get(getShuffledPosition(mPosition));
		}
		return null;
	}

	/**
	 * Moves the queue to the next position and fetches the track at that
	 * position.
	 *
	 * @return The Track at the next position, null if that Track does not 
	 * 	exist.
	 */
	public Track next() {
		if (moveToNext()) {
			return current();
		}
		return null;
	}

	/**
	 * Moves the queue to the previous position and fetches the track at that
	 * position.
	 *
	 * @return The Track at the previous position, null if that Track does not 
	 * 	exist.
	 */
	public Track previous() {
		if (moveToPrevious()) {
			return current();
		}
		return null;
	}

	/**
	 * Fetches the track at the next position without moving the current 
	 * position of the queue.
	 *
	 * @return The Track at the next position, null if that Track does not 
	 * 	exist.
	 */
	public Track peekNext() {
		if (isPositionValid(mPosition + 1)) {
			return mTrackList.get(getShuffledPosition(mPosition) + 1);
		}
		return null;
	}
	
	public Track peekFirst() {
		if (!isEmpty()) {
			return mTrackList.get(getShuffledPosition(0));
		}
		return null;
	}

	/**
	 * Moves the queue to the next position.
	 *
	 * @return True if the position was successfully changed.
	 */
	public boolean moveToNext() {
		if (isPositionValid(mPosition + 1)) {
			mPosition++;
			return true;
		}
		return false;
	}

	/**
	 * Moves the queue to the previous position.
	 *
	 * @return True if the position was successfully changed.
	 */
	public boolean moveToPrevious() {
		if (isPositionValid(mPosition - 1)) {
			mPosition--;
			return true;
		}
		return false;
	}

	/**
	 * Moves the queue to the specified position.
	 *
	 * @param position The position to move to.
	 * @return True if the position was successfully changed.
	 */
	public boolean moveToPosition(int position) {
		if (isPositionValid(position)) {
			mPosition = getUnshuffledPosition(position);
			return true;
		}
		return false;
	}

	/**
	 * Moves the queue to the first position in the queue.
	 *
	 * @return True if the position was successfully changed.
	 */
	public boolean moveToFirst() {
		if (!mTrackList.isEmpty()) {
			mPosition = 0;
			return true;
		}
		return false;
	}

	/**
	 * Moves the queue to the last position in the queue.
	 *
	 * @return True if the position was successfully changed.
	 */
	public boolean moveToLast() {
		if (!mTrackList.isEmpty()) {
			mPosition = mTrackList.size() - 1;
			return true;
		}
		return false;
	}
	
	/**
	 * Moves one queue item from a position to a new position
	 * @param from The original position of the queue item
	 * @param to The new position of the queue item
	 * @return True if the current position has changed
	 */
	public boolean moveItem(int from, int to) {
		boolean positionMoved = false;
		if (from != to) {
			mTrackList.add(to, mTrackList.remove(from));
			if (from == mPosition) {
				mPosition = to;
				positionMoved = true;
			} else if (from < mPosition && to > mPosition) {
				mPosition--;
				positionMoved = true;
			} else if (from > mPosition && to < mPosition) {
				mPosition++;
				positionMoved = true;
			}
		}
		return positionMoved;
	}

	/**
	 * Checks if the queue is at the first position.
	 *
	 * @return True if the queue is at the first position.
	 */
	public boolean isFirst() {
		if (!mTrackList.isEmpty() && mPosition == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the queue is at the last position.
	 *
	 * @return True if the queue is at the last position.
	 */
	public boolean isLast() {
		if (!mTrackList.isEmpty() && mPosition == mTrackList.size() - 1) {
			return true;
		}
		return false;
	}
	
	public boolean contains(Track track) {
		return mTrackList.contains(track);
	}

	/**
	 * Adds the specified track to the end of the queue.
	 *
	 * @param track The track to add to the queue.
	 */
	public void add(Track track) {
		// If shuffled, add track to random position in unplayed part of queue
		if (mShuffled) {
			int position = (int) (Math.random() * (size() - mPosition) + mPosition);
			mShuffleMap.add(position, size() - 1);
		}

		mTrackList.add(track);
	}

	/**
	 * Adds the specified track to the specified position in the queue.
	 *
	 * @param position The position to add the Track to the queue.
	 * @param track The track to add to the queue.
	 */
	public void add(int position, Track track) {
		if (mShuffled) {
			throw new IllegalStateException("Cannot add tracks to a specfic "
				+ "position with a shuffled queue.");
		}

		if (isPositionValid(position)) {
			mTrackList.add(position, track);
			
			if (position < mPosition) {
				mPosition++;
			}
		}
	}

	/**
	 * Adds the specified collection of Tracks to the end of the queue.
	 *
	 * @param c The Collection of Tracks to add to the queue.
	 */
	public void addAll(Collection<? extends Track> c) {
		if (mShuffled) {
			// TODO: Test this algorithm. Would be super cool if it worked
			final int len = size();
			List<Integer> remainingTracks = 
					mShuffleMap.subList(mPosition, len - 1);

			for (int i = 0; i < c.size(); i++) {
				remainingTracks.add(i + len);
			}

			Collections.shuffle(remainingTracks);
		}

		mTrackList.addAll(c);
	}

	/**
	 * Adds the specified collection of Tracks to the specified position in the 
	 * queue.
	 *
	 * @param position The position to add the Tracks.
	 * @param c The Collection of Tracks to add to the queue.
	 */
	public void addAll(int position, Collection<? extends Track> c) {
		if (mShuffled) {
			throw new IllegalStateException("Cannot add tracks to a specfic "
				+ "position with a shuffled queue.");
		}

		if (isPositionValid(position)) {
			mTrackList.addAll(c);

			if (position < mPosition) {
				mPosition += c.size();
			}
		}
	}

	/**
	 * Removes and returns the Track at the specified position.
	 *
	 * @return The Track that has been removed. If the position is invlaid,
	 * 	returns null.
	 */
	public Track remove(int position) {
		if (isPositionValid(position)) {
			if (mShuffled) {
				mShuffleMap.remove((Integer) position);
			}

			int trackListPosition = getUnshuffledPosition(position);
			if (trackListPosition < mPosition) {
				mPosition--;
			}
			return mTrackList.remove(position);
		}
		return null;
	}

	/**
	 * Gets the position in the track list of a position in the shuffled list
	 * if shuffling is turned on.
	 * @param shuffleMapPosition
	 * @return
	 */
	private int getShuffledPosition(int shuffleMapPosition) {
		if (!mShuffled) {
			return shuffleMapPosition;
		}
		
		return mShuffleMap.get(shuffleMapPosition);
	}
	
	/**
	 * Gets the position in the shuffle map of a position in the track list if
	 * shuffling is turned on.
	 * @param shufflePosition
	 * @return
	 */
	private int getUnshuffledPosition(int shufflePosition) {
		if (!mShuffled) {
			return shufflePosition;
		}

		for (int i = 0; i < size(); i++) {
			if (mShuffleMap.get(i) == shufflePosition) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Removes all Tracks from the queue and resets the current position.
	 *
	 */
	public void clear() {
		mTrackList.clear();
		mPosition = -1;

		if (mShuffled) {
			mShuffleMap.clear();
		}
	}

	/**
	 *
	 * @return The length of the queue.
	 */
	public int size() {
		return mTrackList.size();
	}

	/**
	 *
	 * @return The current position of the queue.
	 */
	public int getPosition() {
		return mPosition;
	}
	
	/**
	 * 
	 * @return True if the queue is empty
	 */
	public boolean isEmpty() {
		return mTrackList.isEmpty();
	}

	/**
	 *
	 * @return A shallow copy of the list of Tracks that backs the queue.
	 */
	public List<Track> getTrackList() {
		return new ArrayList<Track>(mTrackList);
	}

	/**
	 *
	 * @return The track list with any effects of shuffling.
	 */
	public List<Track> getShuffledTrackList() {
		if (mShuffled) {
			List<Track> trackList = new ArrayList<Track>(size());
			for (int position : mShuffleMap) {
				trackList.add(mTrackList.get(position));
			}
			return trackList;
		}
		return getTrackList();
	}

	/**
	 *
	 * @return A shallow copy of the shuffle map.
	 */
	public List<Integer> getShuffleMap() {
		return new ArrayList<Integer>(mShuffleMap);
	}

	private boolean isPositionValid(int position) {
		return position >= 0 && position < mTrackList.size();
	}
}