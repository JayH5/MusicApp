package za.jamie.soundstage.service;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.jamie.soundstage.models.Track;

public class PlayQueue {
	private final List<Track> mTrackList;
	private int mPosition = -1;

	private final List<Integer> mShuffleMap;
	private boolean mShuffled = false;
	private boolean mLooping = false;
	
	private PlayQueueDatabase mDatabase;

	/**
	 * Constructs an empty play queue. The position is initialized as -1.
	 *
	 */
	public PlayQueue(Context context) {
		mTrackList = new ArrayList<Track>();
		mShuffleMap = new ArrayList<Integer>();
		mDatabase = new PlayQueueDatabase(context);
	}
	
	public static PlayQueue restore(Context context) {
		PlayQueue playQueue = new PlayQueue(context);
		
		playQueue.mTrackList.addAll(playQueue.mDatabase.getTrackList());
		playQueue.mShuffleMap.addAll(playQueue.mDatabase.getShuffleMap());
		
		Bundle state = playQueue.mDatabase.getState();
		playQueue.mPosition = state.getInt(PlayQueueDatabase.STATE_KEY_PLAY_POSITION, -1);
		playQueue.mShuffled = (state.getInt(PlayQueueDatabase.STATE_KEY_SHUFFLE_ENABLED, 0) > 0);
		// mRepeatMode...
		
		return playQueue;
	}
	
	/*public void setShuffleMap(Collection<? extends Integer> shuffleMap) {
		if (shuffleMap.size() != mTrackList.size()) {
			throw new IllegalArgumentException("Shuffle map not the same size as track list!");
		} else if (Collections.min(shuffleMap) < 0 || 
				Collections.max(shuffleMap) >= mTrackList.size()) {
			throw new IllegalArgumentException("Invalid shuffle map.");
		}
		mShuffleMap.clear();
		mShuffleMap.addAll(shuffleMap);
	}*/

	/**
	 * Clears the current play queue and loads the specified tracks. Sets the
	 * queue position to the one specified and shuffles if requested.
	 *
	 * @param trackList The tracks to be added to the queue
	 * @param position The position to set the queue
	 * @param shuffle True to shuffle the queue, false to use the ordering 
	 * 	described by the collection of tracks.
	 */
	public boolean open(List<Track> trackList, int position, boolean shuffle) {
		boolean queueChanged = false;
		if (!trackList.equals(mTrackList)) {
			clear();
			mTrackList.addAll(trackList);
			mDatabase.open(trackList);
			queueChanged = true;
		}
		
		if (shuffle) {
			shuffle(position);
		} else {
			mShuffled = false;
			shuffledChanged();
			moveToPosition(position);
		}
		
		return queueChanged;
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
	 * @param shuffleOn The position of the Track that should be placed at the head of the
	 *  queue.
	 */
	public void shuffle(int shuffleOn) {
		// Initialize the map
		mShuffleMap.clear();
		for (int i = 0, n = size(); i < n; i++) {
			if (i != shuffleOn) {
                mShuffleMap.add(i);
			}
		}

		Collections.shuffle(mShuffleMap);
		
		if (isPositionValid(shuffleOn)) {
			mShuffleMap.add(0, shuffleOn);
		}
		
		shuffleMapChanged();
		
		mShuffled = true;
		shuffledChanged();
		
		moveToFirst();
	}

	/**
	 * Disables the shuffle, remaining on the current Track.
	 *
	 */
	public void unShuffle() {
		if (isPositionValid(mPosition) && mShuffled) {
			mPosition = mShuffleMap.get(mPosition);
			positionChanged();
		}
		mShuffleMap.clear();
		mShuffled = false;
		shuffledChanged();
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
		if (mLooping && isLast() && !mTrackList.isEmpty()) {
			return mTrackList.get(0);
		}		
		if (isPositionValid(mPosition + 1)) {
			return mTrackList.get(getShuffledPosition(mPosition + 1));
		}
		return null;
	}

	/**
	 * Moves the queue to the next position.
	 *
	 * @return True if the position was successfully changed.
	 */
	public boolean moveToNext() {
		if (mLooping && isLast()) {
			return moveToFirst();
		}
		
		if (isPositionValid(mPosition + 1)) {
			mPosition++;
			positionChanged();
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
		if (mLooping && isFirst()) {
			return moveToLast();
		}
		
		if (isPositionValid(mPosition - 1)) {
			mPosition--;
			positionChanged();
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
			mPosition = position;
			positionChanged();
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
			positionChanged();
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
			positionChanged();
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
			if (mShuffled) {
				mShuffleMap.add(to, mShuffleMap.remove(from));
				shuffleMapChanged();
			} else {
				mTrackList.add(to, mTrackList.remove(from));
				mDatabase.move(from, to);
			}
			
			if (from == mPosition) {
				mPosition = to;
				positionMoved = true;
			} else if (from < mPosition && to >= mPosition) {
				mPosition--;
				positionMoved = true;
			} else if (from > mPosition && to <= mPosition) {
				mPosition++;
				positionMoved = true;
			}
			
			if (positionMoved) {
				positionChanged();
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
		return !mTrackList.isEmpty() && mPosition == 0;
	}

	/**
	 * Checks if the queue is at the last position.
	 *
	 * @return True if the queue is at the last position.
	 */
	public boolean isLast() {
		return !mTrackList.isEmpty() && mPosition == mTrackList.size() - 1;
	}
	
	private int end() {
		return mTrackList.size() - 1;
	}
	
	/**
	 * Check if a track is in the queue
	 * @param track
	 * @return True if the track is in the queue
	 */
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
			shuffleMapChanged();
		}

		mDatabase.add(end(), track);
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
			mDatabase.add(position, track);
			if (position <= mPosition) {
				mPosition++;
				positionChanged();
			}
		}
	}

	/**
	 * Adds the specified collection of Tracks to the end of the queue.
	 *
	 * @param tracks The Collection of Tracks to add to the queue.
	 */
	public void addAll(List<Track> tracks) {
		if (mShuffled) {
			// TODO: Test this algorithm. Would be super cool if it worked
			final int len = size();
			List<Integer> remainingTracks = 
					mShuffleMap.subList(mPosition, len - 1);

			for (int i = 0; i < tracks.size(); i++) {
				remainingTracks.add(i + len);
			}

			Collections.shuffle(remainingTracks);
			
			shuffleMapChanged();
		}
		mDatabase.addAll(end(), tracks);
		mTrackList.addAll(tracks);		
	}

	/**
	 * Adds the specified collection of Tracks to the specified position in the 
	 * queue.
	 *
	 * @param position The position to add the Tracks.
	 * @param tracks The Collection of Tracks to add to the queue.
	 */
	public void addAll(int position, List<Track> tracks) {
		if (mShuffled) {
			throw new IllegalStateException("Cannot add tracks to a specfic "
				+ "position with a shuffled queue.");
		}

		if (isPositionValid(position)) {
			mDatabase.addAll(position, tracks);
			mTrackList.addAll(position, tracks);

			if (position <= mPosition) {
				mPosition += tracks.size();
				positionChanged();
			}
		}
	}

	/**
	 * Removes and returns the Track at the specified position.
	 *
	 * @return The Track that has been removed or null if the position is invalid
	 */
	public Track remove(int position) {
		if (isPositionValid(position)) {
			int trackListPosition = position;
			// If shuffled, get the position in the shuffle map
			if (mShuffled) {				
				trackListPosition = mShuffleMap.remove(position);
				shuffleMapChanged();
			}
			if (position < mPosition) {
				mPosition--;
				positionChanged();
			}
			mDatabase.remove(trackListPosition);
			return mTrackList.remove(trackListPosition);
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
	 * Removes all Tracks from the queue and resets the current position.
	 *
	 */
	public void clear() {
		// Don't bother clearing db. Be lazy.
		mTrackList.clear();
		mPosition = -1;
		positionChanged();

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
	 * @return True if the queue is empty
	 */
	public boolean isEmpty() {
		return mTrackList.isEmpty();
	}

	/**
	 *
	 * @return The track list with any effects of shuffling.
	 */
	public List<Track> getPlayQueue() {
		List<Track> trackList = new ArrayList<Track>(size());
		if (mShuffled) {
			for (int position : mShuffleMap) {
				trackList.add(mTrackList.get(position));
			}
		} else {
			trackList.addAll(mTrackList);
		}
		return trackList;
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
	 * @return A shallow copy of the shuffle map.
	 */
	public List<Integer> getShuffleMap() {
		return new ArrayList<Integer>(mShuffleMap);
	}
	
	/**
	 *
	 * @return The current position of the queue.
	 */
	public int getPosition() {
		return mPosition;
	}
	
	public int getTrackListPosition() {
		return getShuffledPosition(mPosition);
	}
	
	public void setLooping(boolean looping) {
		mLooping = looping;
	}
	
	public boolean isLooping() {
		return mLooping;
	}

	private boolean isPositionValid(int position) {
		return position >= 0 && position < mTrackList.size();
	}
	
	public void closeDb() {
		mDatabase.close();
	}
	
	private void positionChanged() {
		mDatabase.savePlayPosition(mPosition);
	}
	
	private void shuffledChanged() {
		mDatabase.saveShuffleEnabled(mShuffled);
	}
	
	private void shuffleMapChanged() {
		mDatabase.saveShuffleMap(mShuffleMap);
	}
}