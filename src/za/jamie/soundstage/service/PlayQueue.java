package za.jamie.soundstage.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import za.jamie.soundstage.models.Track;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;


public class PlayQueue {

	private int mPlayPosition = -1;
	private int mNextPlayPosition = -1;
	
	private List<Long> mTrackIdList;
	private Map<Long, Track> mTrackMap;
	
	private static final Uri BASE_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	
    private final PlayQueueDatabase mDatabase;
	
    public PlayQueue(Context context) {
    	mDatabase = new PlayQueueDatabase(context);
    	
    	mTrackMap = mDatabase.getTrackMap();
    	mTrackIdList = mDatabase.getTrackIdList();
    }
	
	public void addAll(int position, final Collection<? extends Track> list) {
        // Keep a list of changes to the set
		List<Track> positiveDiff = new LinkedList<Track>();
		// Iterate through the list, adding ids to track id list, tracks to track set
		Iterator<? extends Track> it = list.iterator();
        for (int i = 0; i < list.size(); i++) {
			Track track = it.next();
			long trackId = track.getId();
			mTrackIdList.add(i + position, trackId);
			
			if (mTrackMap.put(trackId, track) == null) {
				positiveDiff.add(track);
			}
        }
        
        // Update the play position
        if (position < mPlayPosition) {
        	mPlayPosition += list.size();
        }
        
        // Keep the db in sync
        mDatabase.updateTrackSet(positiveDiff, null);
        mDatabase.updateTrackIdList(mTrackIdList);
	}
	
	public void addAll(final Collection<? extends Track> list) {
		// Keep a list of changes to the set
		List<Track> positiveDiff = new LinkedList<Track>();
		List<Long> trackIds = new LinkedList<Long>();
		for (Track track : list) {
			long trackId = track.getId();
			trackIds.add(trackId);
			
			if (mTrackMap.put(trackId, track) == null) {
				positiveDiff.add(track);
			}
		}
		mTrackIdList.addAll(trackIds);
		
		mDatabase.updateTrackSet(positiveDiff, null);
		mDatabase.appendTrackIdsToList(trackIds);
	}
	
	public void add(int position, Track track) {
		long trackId = track.getId();
		
		if (position <= mPlayPosition) {
			mPlayPosition++;
		}
		
		mTrackIdList.add(position, trackId);
		if (mTrackMap.put(trackId, track) == null) {
			mDatabase.addTrackToSet(track);
		}
		mDatabase.updateTrackIdList(mTrackIdList);
	}
	
	public void add(Track track) {
		long trackId = track.getId();
		
		mTrackIdList.add(trackId);
		
		if (mTrackMap.put(trackId, track) == null) {
			mDatabase.addTrackToSet(track);
		}		
		mDatabase.appendTrackIdToList(trackId);
	}
	
	/**
	 * Swaps in a new list in place of the current queue
	 * @param list
	 * @return true if queue changed
	 */
	public boolean openList(List<Track> list) {
		mTrackIdList.clear();
		mTrackMap.clear();
		for (Track track : list) {
			final long trackId = track.getId();
			mTrackIdList.add(trackId);
			mTrackMap.put(trackId, track);
		}
		
		mDatabase.open(mTrackIdList, mTrackMap);
		
		return true;
	}
	
	public void moveQueueItem(int from, int to) {
        if (from < to) {
          	mTrackIdList.add(from, mTrackIdList.remove(to));
            if (mPlayPosition == from) {
            	mPlayPosition = to;
            } else if (mPlayPosition >= from && mPlayPosition <= to) {
            	mPlayPosition--;
            }
        } else if (to < from) {
        	mTrackIdList.add(to, mTrackIdList.remove(from));
            if (mPlayPosition == from) {
            	mPlayPosition = to;
            } else if (mPlayPosition >= to && mPlayPosition <= from) {
            	mPlayPosition++;
            }
        }
    }
	
	public int removeTrack(final long id) {
    	int numRemoved = 0;
    	int position = 0;
    		
        // Iterate through play queue to find matching tracks
        ListIterator<Long> it = mTrackIdList.listIterator();
        while (it.hasNext()) {
        	if (it.next() == id) {
        		it.remove();
        		numRemoved++;
        		if (position < mPlayPosition) {
        			mPlayPosition--;
        		}
        	}
        	position++;
        }
        mTrackMap.remove(id);
    	
        mDatabase.removeTrackFromSet(id);
        	
        return numRemoved;
    }
	
	public void removeTrack(int position) {
		Long removedTrackId = mTrackIdList.remove(position); // Keep it boxed inside
		if (!mTrackIdList.contains(removedTrackId)) {
			mTrackMap.remove(removedTrackId);
			mDatabase.removeTrackFromSet(removedTrackId);
		}
		
		mDatabase.updateTrackIdList(mTrackIdList);
	}
	
	public void closeDb() {
		mDatabase.close();
	}
		
	public void goToNext() {
		mPlayPosition = mNextPlayPosition;
	}
	
	public boolean isStopped() {
		return mPlayPosition < 0;
	}
	
	public List<Track> getQueue() {
        List<Track> queue = new LinkedList<Track>();
        for (Long trackId : mTrackIdList) {
        	queue.add(mTrackMap.get(trackId));
        }
		return queue;
    }
	
	public long[] getQueueIds() {
		long[] queueIds = new long[mTrackIdList.size()];
		int i = 0;
		for (long trackId : mTrackIdList) {
			queueIds[i] = trackId;
			i++;
		}
		return queueIds;
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
		return mTrackIdList.size();
	}
	
	public boolean isEmpty() {
		return mTrackIdList.isEmpty();
	}
	
	public Track getCurrentTrack() {
		long trackId = getCurrentId();
		if (trackId > -1) {
			return mTrackMap.get(trackId);
		}
		return null;
	}
	
	public long getCurrentId() {
		if (mPlayPosition >= 0 && mPlayPosition < mTrackIdList.size()) {
			return mTrackIdList.get(mPlayPosition);
		}
		return -1;
	}
	
	public long getNextId() {
		if (mNextPlayPosition >= 0 && mNextPlayPosition < mTrackIdList.size()) {
			return mTrackIdList.get(mNextPlayPosition);
		}
		return -1;
	}
	
	public Uri getCurrentUri() {
		long trackId = getCurrentId();
		if (trackId > -1) {
			return ContentUris.withAppendedId(BASE_URI, trackId);
		}
		return null;
	}
	
	public Uri getNextUri() {
		long trackId = getNextId();
		if (trackId > -1) {
			return ContentUris.withAppendedId(BASE_URI, trackId);
		}
		return null;
	}
	
}
