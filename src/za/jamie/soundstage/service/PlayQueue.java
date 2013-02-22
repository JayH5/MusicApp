package za.jamie.soundstage.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import za.jamie.soundstage.models.Id;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.utils.HexUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;


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
    
    private final PlayQueueDatabase mDatabase;
    
    //private static final String SORT_ORDER = MediaStore.Audio.Media._ID + " ASC";
	
    public PlayQueue(Context context) {
    	mDatabase = new PlayQueueDatabase(context);
    	
    	mPlayQueue = mDatabase.getQueue();
    }
    
    /*public PlayQueue() {
    	mPlayQueue = new ArrayList<Track>();
    }
    
	public PlayQueue(int capacity) {
		mPlayQueue = new ArrayList<Track>(capacity);
	}*/
	
	/**
	 * Uses the contents of a hex string to recreate a queue.
	 * @param context
	 * @param hexString
	 */
	/*public void open(Context context, String hexString) {
		// Split the hex string with the delimiter
		final String[] hexes = hexString.split(HexUtils.DELIMITER);
    	
		// Initialise a store for the trackIds
		int length = hexes.length;
    	final Id[] trackIds = new Id[length];
    	
    	// Don't know exactly how long the selection will be but it will be at least the
    	// length of the number of tracks plus commas
    	final StringBuilder selection = new StringBuilder(length * 2);
    	selection.append(MediaStore.Audio.Media._ID + " IN (");
    	long trackId;
    	for (int i = 0; i < length - 1; i++) {
    		// Convert the hex to a long track id
    		trackId = Long.parseLong(hexes[i], 16);
    		
    		// Store the id as an object
    		trackIds[i] = new Id(trackId);
    		
    		// Add it to the query selection
    		selection.append(trackId)
    			.append(',');
    	}
    	trackId = Long.parseLong(hexes[length - 1], 16);
    	trackIds[length - 1] = new Id(trackId);
    	selection.append(trackId)
    		.append(')');
    	
    	Cursor cursor = context.getContentResolver().query(
        		BASE_URI, PROJECTION, selection.toString(), null, null);
        
        // Create a list of tracks from the cursor
    	if (cursor != null) {
        	Track[] tracks = new Track[cursor.getCount()];
        	if (cursor.moveToFirst()) {
        		int i = 0;
        		do {
        			tracks[i] = new Track(
        					cursor.getLong(0), // Id
        					cursor.getString(1), // Title
        					cursor.getLong(2), // Artist id
        					cursor.getString(3), // Artist
        					cursor.getLong(4), // Album id
        					cursor.getString(5), // Album
        					cursor.getLong(6)); // Duration
        			i++;
        		} while (cursor.moveToNext());
        	}
        	
        	cursor.close();
        	cursor = null;
        	
        	mPlayQueue.clear();
        	mPlayQueue.ensureCapacity(length);
        	for (Id id : trackIds) {
        		int position = Arrays.binarySearch(tracks, id, Id.mComparator);
        		mPlayQueue.add(tracks[position]);
        	}
        }

	}*/
	
	public void addToQueue(final List<Track> list, int position) {
        mPlayQueue.addAll(position, list);
        
        // Update the play position
        if (position < mPlayPosition) {
        	mPlayPosition += list.size();
        }
        
        mDatabase.open(mPlayQueue);
	}
	
	public void addToQueue(final List<Track> list) {
		mPlayQueue.addAll(list);
		
		mDatabase.addAll(list);
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
		
		mDatabase.open(list);
		
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
        
        mDatabase.remove(id);
        	
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
        
        mDatabase.remove(first, last);
        
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
		
		mDatabase.add(track);
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
	
	/*public String toHexString() {
		final StringBuilder builder = new StringBuilder();
    	for (Track track : mPlayQueue) {
    		builder.append(Long.toHexString(track.getId()));
    		builder.append(HexUtils.DELIMITER);
    	}
    	return builder.toString();
	}*/
	
	public boolean listEquals(List<Track> list) {
		return mPlayQueue.equals(list);
	}
	
}
