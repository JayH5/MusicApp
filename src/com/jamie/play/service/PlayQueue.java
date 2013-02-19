package com.jamie.play.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.jamie.play.models.Track;
import com.jamie.play.utils.HexUtils;

public class PlayQueue {

	private List<Track> mPlayQueue;
	private int mPlayPosition = -1;
	private int mNextPlayPosition = -1;
	
	private static final Uri BASE_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final String[] PROJECTION = new String[] {
    	MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION
    };
	
    public PlayQueue() {
    	mPlayQueue = new ArrayList<Track>();
    }
    
	public PlayQueue(int capacity) {
		mPlayQueue = new ArrayList<Track>(capacity);
	}
	
	/**
	 * Uses the contents of a hex string to recreate a queue.
	 * @param context
	 * @param hexString
	 */
	public void open(Context context, String hexString) {
		// Split the hex string with the delimiter
		final String[] hexes = hexString.split(HexUtils.DELIMITER);
    	
		// Initialise a store for the trackIds
		int length = hexes.length;
    	final long[] trackIds = new long[length];
    	
    	// Initialise a stringbuilder for the query
    	final StringBuilder selection = new StringBuilder();
    	selection.append(MediaStore.Audio.Media._ID + " IN (");
    	for (int i = 0; i < length; i++) {
    		// Get the trackId from the string
    		long trackId = Long.parseLong(hexes[i], 16);
    		Log.d("PlayQueue", "Track id from hex: " + trackId);
    		
    		// Store it
    		trackIds[i] = trackId;
    		
    		// Add it to the query selection
    		selection.append(trackId);
    		if (i < length -1) {
    			selection.append(",");
    		}
    	}
    	selection.append(")");
    	
    	Cursor cursor = context.getContentResolver().query(
        		BASE_URI, PROJECTION, selection.toString(), null, null);
        
        if (cursor != null) {
        	// Load ids from cursor for fast sorting
        	length = cursor.getCount();
        	long[] unsortedIds = new long[length];
        	if (cursor.moveToFirst()) {
				for (int i = 0; i < length; i++) {
					unsortedIds[i] = cursor.getLong(0);
					cursor.moveToNext();
				}
			}
        	
        	
        	for (long id : trackIds) {
        		int position = Arrays.binarySearch(unsortedIds, id);
        		if (cursor.moveToPosition(position)) {
        			mPlayQueue.add(new Track(cursor.getLong(0), 
        	        		cursor.getString(1), 
        	        		cursor.getLong(2), 
        	        		cursor.getString(3), 
        	        		cursor.getLong(4), 
        	        		cursor.getString(5),
        	        		cursor.getLong(6)));
        		}
        	}
        	cursor.close();
        	cursor = null;
        }

	}
	
	public void addToQueue(final List<Track> list, int position) {
        mPlayQueue.addAll(position, list);
        
        // Update the play position
        if (position < mPlayPosition) {
        	mPlayPosition += list.size();
        }
	}
	
	public void addToQueue(final List<Track> list) {
		mPlayQueue.addAll(list);
	}
	
	
	/**
	 * Swaps in a new list in place of the current queue
	 * @param list
	 * @return true if queue changed
	 */
	public boolean openList(List<Track> list) {
		if (listEquals(list)) {
			return false;
		}
		
		mPlayQueue.clear();
		mPlayQueue.addAll(list);
		
		return true;
	}
	
	public void moveQueueItem(int from, int to) {
        if (from < to) {
          	mPlayQueue.add(from, mPlayQueue.remove(to));
            if (mPlayPosition == from) {
            	mPlayPosition = to;
            } else if (mPlayPosition >= from && mPlayPosition <= to) {
            	mPlayPosition--;
            }
        } else if (to < from) {
         	mPlayQueue.add(to, mPlayQueue.remove(from));
            if (mPlayPosition == from) {
            	mPlayPosition = to;
            } else if (mPlayPosition >= to && mPlayPosition <= from) {
            	mPlayPosition++;
            }
        }
    }
	
	public int removeTrack(final long id) {
    	int numRemoved = 0;
    		
        // Iterate through play queue to find matching tracks
        for (int i = 0; i < mPlayQueue.size(); i++) {
        	if (mPlayQueue.get(i).getId() == id) {
        		mPlayQueue.remove(i);
        		numRemoved++;
        		if (i < mPlayPosition) {
        			mPlayPosition--;
        		}
        	}
        }
        	
        return numRemoved;
    }
	
	public int removeTracks(int first, int last) {
		if (last < first) {
            return 0;
        }

    	final int numRemoved = last - first + 1;
    	
        if (first <= mPlayPosition && mPlayPosition <= last) {
        	mPlayPosition = first;
        } else if (mPlayPosition > last) {
        	mPlayPosition -= numRemoved;
        }
        
        mPlayQueue.subList(first, last).clear();
        
        return numRemoved;
	}
	
	public void goToNext() {
		mPlayPosition = mNextPlayPosition;
	}
	
	public boolean isStopped() {
		return mPlayPosition < 0;
	}
	
	public List<Track> getQueue() {
        return new ArrayList<Track>(mPlayQueue);
    }
	
	public void add(Track track) {
		mPlayQueue.add(track);
	}
	
	public int getPlayPosition() {
		return mPlayPosition;
	}
	
	public void setPlayPosition(int position) {
		mPlayPosition = position;
	}
	
	public int getNextPlayPosition() {
		return mNextPlayPosition;
	}
	
	public void setNextPlayPosition(int position) {
		mNextPlayPosition = position;
	}
	
	public int size() {
		return mPlayQueue.size();
	}
	
	public boolean isEmpty() {
		return mPlayQueue.isEmpty();
	}
	
	public Track getCurrentTrack() {
		if (mPlayPosition >= 0 && mPlayPosition < mPlayQueue.size()) {
			return mPlayQueue.get(mPlayPosition);
		}
		return null;
	}
	
	public long getCurrentId() {
		if (mPlayPosition >= 0 && mPlayPosition < mPlayQueue.size()) {
			return mPlayQueue.get(mPlayPosition).getId();
		}
		return -1;
	}
	
	public Uri getCurrentUri() {
		if (mPlayPosition >= 0 && mPlayPosition < mPlayQueue.size()) {
			return mPlayQueue.get(mPlayPosition).getUri();
		}
		return null;
	}
	
	public Uri getNextUri() {
		if (mNextPlayPosition >= 0 && mNextPlayPosition < mPlayQueue.size()) {
			return mPlayQueue.get(mNextPlayPosition).getUri();
		}
		return null;
	}
	
	public String toHexString() {
		final StringBuilder builder = new StringBuilder();
    	for (Track track : mPlayQueue) {
    		builder.append(Long.toHexString(track.getId()));
    		builder.append(HexUtils.DELIMITER);
    	}
    	return builder.toString();
	}
	
	public boolean listEquals(List<Track> list) {
		return mPlayQueue.equals(list);
	}
	
}
