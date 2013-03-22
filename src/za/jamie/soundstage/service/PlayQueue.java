package za.jamie.soundstage.service;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import za.jamie.soundstage.models.Track;
import android.content.Context;
import android.net.Uri;


public class PlayQueue {

	private int mPlayPosition = -1;
	private int mNextPlayPosition = -1;
	
	private List<Track> mTrackList;
	
    private final PlayQueueDatabase mDatabase;
	
    public PlayQueue(Context context) {
    	mDatabase = new PlayQueueDatabase(context);
    	
    	mTrackList = mDatabase.getTrackList();
    }
	
	public void addAll(int position, final Collection<? extends Track> tracks) {
        mTrackList.addAll(position, tracks);
        
        // Update the play position
        if (position <= mPlayPosition) {
        	mPlayPosition += tracks.size();
        }
        
        // Keep the db in sync
        mDatabase.addAll(position, tracks);
	}
	
	public void addAll(final Collection<? extends Track> tracks) {
		mTrackList.addAll(tracks);
		
		mDatabase.addAll(tracks);
	}
	
	public void add(int position, Track track) {		
		mTrackList.add(position, track);

		if (position <= mPlayPosition) {
			mPlayPosition++;
		}
		
		mDatabase.add(track);
	}
	
	public void add(Track track) {
		mTrackList.add(track);
		
		mDatabase.add(track);
	}
	
	/**
	 * Swaps in a new list in place of the current queue
	 * @param list
	 * @return true if queue changed
	 */
	public boolean openList(Collection<? extends Track> tracks) {
		if (!mTrackList.equals(tracks)) {
			mTrackList.clear();
			mTrackList.addAll(tracks);
				
			mDatabase.open(tracks);

			return true;
		}

		return false;
	}
	
	public void moveQueueItem(int from, int to) {
        Track track = mTrackList.remove(from);
		mTrackList.add(to, track);

        if (mPlayPosition == from) {
        	mPlayPosition = to;
        } else if (from < to) {
        	if (mPlayPosition >= from && mPlayPosition <= to) {
        		mPlayPosition--;
        	}
        } else if (to < from) {
        	if (mPlayPosition >= to && mPlayPosition <= from) {
        		mPlayPosition++;
        	}
        }

        mDatabase.moveQueueItem(from, to, track);
    }
	
	public int removeTrack(long id) {
    	int numRemoved = 0;
    	int position = 0;
    		
        // Iterate through play queue to find matching tracks
        ListIterator<Track> it = mTrackList.listIterator();
        while (it.hasNext()) {
        	if (it.next().getId() == id) {
        		it.remove();
        		numRemoved++;
        		if (position <= mPlayPosition) {
        			mPlayPosition--;
        		}
        	}
        	position++;
        }
    	
        //mDatabase.removeTrack(id);
        	
        return numRemoved;
    }
	
	public void removeTrack(int position) {
		mTrackList.remove(position);

		if (position == mPlayPosition) {
			mPlayPosition++;
		}
		
		mDatabase.remove(position);
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
        return mTrackList;
    }
	
	public long[] getQueueIds() {
		long[] queueIds = new long[mTrackList.size()];
		int i = 0;
		for (Track track : mTrackList) {
			queueIds[i] = track.getId();
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
		return mTrackList.size();
	}
	
	public boolean isEmpty() {
		return mTrackList.isEmpty();
	}
	
	public Track getCurrentTrack() {
		if (mPlayPosition >= 0 && mPlayPosition < mTrackList.size()) {
			return mTrackList.get(mPlayPosition);
		}
		return null;
	}

	public Track getNextTrack() {
		if (mNextPlayPosition >= 0 && mNextPlayPosition < mTrackList.size()) {
			return mTrackList.get(mNextPlayPosition);
		}
		return null;
	}
	
	public long getCurrentId() {
		Track currentTrack = getCurrentTrack();
		if (currentTrack != null) {
			return currentTrack.getId();
		}
		return -1;
	}
	
	public long getNextId() {
		Track nextTrack = getNextTrack();
		if (nextTrack != null) {
			return nextTrack.getId();
		}
		return -1;
	}
	
	public Uri getCurrentUri() {
		Track currentTrack = getCurrentTrack();
		if (currentTrack != null) {
			return currentTrack.getUri();
		}
		return null;
	}
	
	public Uri getNextUri() {
		Track nextTrack = getNextTrack();
		if (nextTrack != null) {
			return nextTrack.getUri();
		}
		return null;
	}
	
}
