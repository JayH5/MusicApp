package com.jamie.play.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class PlayQueue {

	private List<Track> mPlayQueue;
	private int mPlayQueueLen = -1;
	private int mPlayPosition = -1;
	private int mNextPlayPosition = -1;
	
	private static final Uri BASE_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final String[] PROJECTION = new String[] {
    	MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM
    };
	
	public PlayQueue(int capacity) {
		mPlayQueue = new ArrayList<Track>(capacity);
	}
	
	private PlayQueue(List<Track> list) {
		mPlayQueue = list;
		mPlayQueueLen = mPlayQueue.size();
	}
	
	public static PlayQueue createFromHexString(Context context, String hexString) {
		long[] trackIds = hexStringToTrackIds(hexString);
		List<Track> tracks = trackIdsToTracks(context, trackIds);
		
		// TODO: Test if this is necessary
		//if (tracks == null || tracks.isEmpty()) {
            //SystemClock.sleep(3000);
            //tracks = trackIdsToTracks(context, trackIds);
		//}

        return new PlayQueue(tracks);
	}
	
	private static long[] hexStringToTrackIds(String hexString) {
    	final String[] hexes = hexString.split(";");
    	final int length = hexes.length;
    	long[] trackIds = new long[length];
    	for (int i = 0; i < length; i++) {
    		trackIds[i] = Long.parseLong(hexes[i], 16);
    	}
    	return trackIds;
    }
	
	private static List<Track> trackIdsToTracks(Context context, long[] trackIds) {
		final int len = trackIds.length;
		final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < len; i++) {
            selection.append(trackIds[i]);
            if (i < len - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        
        Cursor cursor = context.getContentResolver().query(
        		BASE_URI, PROJECTION, selection.toString(), null, null);
        
        ArrayList<Track> tracks = null;
        if (cursor != null) {
        	// Load ids from cursor for fast sorting
        	final int length = cursor.getCount();
        	long[] unsortedIds = new long[length];
        	if (cursor.moveToFirst()) {
				for (int i = 0; i < length; i++) {
					unsortedIds[i] = cursor.getLong(0);
					cursor.moveToNext();
				}
			}
        	
        	tracks = new ArrayList<Track>(length);
        	
        	for (long id : trackIds) {
        		int position = Arrays.binarySearch(unsortedIds, id);
        		if (cursor.moveToPosition(position)) {
        			tracks.add(new Track(cursor.getLong(0), 
        	        		cursor.getString(1), 
        	        		cursor.getLong(2), 
        	        		cursor.getString(3), 
        	        		cursor.getLong(4), 
        	        		cursor.getString(5)));
        		}
        	}
        	cursor.close();
        	cursor = null;
        }
        return tracks;
	}
	
	public void addToQueue(final List<Track> list, int position) {
		// If position < 0 it signals that a new queue should be created
        if (position < 0) {
            position = 0;
            mPlayQueue.clear();
        } else if (position > mPlayQueueLen) {
            position = mPlayQueueLen;
        }
        
        mPlayQueue.addAll(position, list);
        mPlayQueueLen = mPlayQueue.size();
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
        	
        mPlayQueueLen -= numRemoved;
        	
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
        mPlayQueueLen -= numRemoved;
        
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
		mPlayQueueLen++;
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
		return mPlayQueueLen;
	}
	
	public boolean isEmpty() {
		return mPlayQueue.isEmpty();
	}
	
	public Track getCurrentTrack() {
		if (mPlayPosition > 0 && mPlayPosition < mPlayQueue.size()) {
			return mPlayQueue.get(mPlayPosition);
		}
		return null;
	}
	
	public long getCurrentId() {
		if (mPlayPosition > 0 && mPlayPosition < mPlayQueue.size()) {
			return mPlayQueue.get(mPlayPosition).getId();
		}
		return -1;
	}
	
	public Uri getCurrentUri() {
		if (mPlayPosition > 0 && mPlayPosition < mPlayQueue.size()) {
			return mPlayQueue.get(mPlayPosition).getUri();
		}
		return null;
	}
	
	public Uri getNextUri() {
		if (mNextPlayPosition > 0 && mNextPlayPosition < mPlayQueue.size()) {
			return mPlayQueue.get(mNextPlayPosition).getUri();
		}
		return null;
	}
	
	public String toHexString() {
		final StringBuilder builder = new StringBuilder();
    	for (Track track : mPlayQueue) {
    		builder.append(Long.toHexString(track.getId()));
    		builder.append(';');
    	}
    	return builder.toString();
	}
	
}
