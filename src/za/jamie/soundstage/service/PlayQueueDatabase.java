package za.jamie.soundstage.service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import za.jamie.soundstage.models.Track;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;


public class PlayQueueDatabase extends SQLiteOpenHelper {
	
	private static final String TAG = "PlayQueueDatabase";
	
	private static final String DATABASE_NAME = "playqueue.db";
    private static final int DATABASE_VERSION = 4;
    
    private static final String TABLE_NAME_PLAY_QUEUE = "playqueue";
    
    private static final String COLUMN_NAME_TRACK_ID = "track_id";
    private static final String COLUMN_NAME_QUEUE_POSITION = "queue_position";
    
    private static final String CREATE_TABLE_PLAY_QUEUE = "CREATE TABLE " + 
    			TABLE_NAME_PLAY_QUEUE + " (" + 
    		"_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
    		COLUMN_NAME_QUEUE_POSITION + " INTEGER, " +
    		COLUMN_NAME_TRACK_ID + " INTEGER, " +
    		MediaStore.Audio.Media.TITLE + " STRING, " +
    		MediaStore.Audio.Media.ARTIST_ID + " INTEGER, " +
    		MediaStore.Audio.Media.ARTIST + " STRING, " +
    		MediaStore.Audio.Media.ALBUM_ID + " INTEGER, " +
    		MediaStore.Audio.Media.ALBUM + " STRING, " +
    		MediaStore.Audio.Media.DURATION + " INTEGER)";
    
    private int mQueueLength;
    
	public PlayQueueDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_PLAY_QUEUE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Logs that the database is being upgraded
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        resetDatabase(db);	
	}

	private void resetDatabase(SQLiteDatabase db) {
		// Kills the tables and existing data
        dropTable(db, TABLE_NAME_PLAY_QUEUE);

        // Recreates the database with a new version
        onCreate(db);
	}

	private void dropTable(SQLiteDatabase db, String tableName) {
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
	}
	
	public void open(final Collection<? extends Track> trackList) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				open(getWritableDatabase(), trackList);
				return null;
			}
			
		}.execute();
		
	}

	private void open(SQLiteDatabase db, Collection<? extends Track> tracks) {
		resetDatabase(db);

		// For inserting columns
		ContentValues values = new ContentValues();
								
		db.beginTransaction();
		try {
			values.clear(); // clear out trackId values
			mQueueLength = 0;
			for (Track track : tracks) {
				long id = track.writeToContentValues(values);
				values.put(COLUMN_NAME_TRACK_ID, id);
				values.put(COLUMN_NAME_QUEUE_POSITION, mQueueLength);
				db.insert(TABLE_NAME_PLAY_QUEUE, null, values);
				mQueueLength++;
			}

			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error during transaction when updating table: ", e);
		} finally {
			db.endTransaction();
		}
	}
	
	public void add(final Track track) {
		new AsyncTask<Void, Void, Long>() {

			@Override
			protected Long doInBackground(Void... params) {
				return add(getWritableDatabase(), track);
			}
			
		}.execute();
		
	}
	
	private long add(SQLiteDatabase db, Track track) {
		ContentValues values = new ContentValues();
		long id = track.writeToContentValues(values);
		values.put(COLUMN_NAME_TRACK_ID, id);
		values.put(COLUMN_NAME_QUEUE_POSITION, mQueueLength);
		mQueueLength++;
		return db.insert(TABLE_NAME_PLAY_QUEUE, null, values);
	}

	public void add(final int position, final Track track) {
		new AsyncTask<Void, Void, Long>() {

			@Override
			protected Long doInBackground(Void... params) {
				return add(getWritableDatabase(), position, track);
			}

		}.execute();
	}

	private long add(SQLiteDatabase db, int position, Track track) {
		// Need to insert new value
		ContentValues insertValues = new ContentValues();
		long id = track.writeToContentValues(insertValues);
		insertValues.put(COLUMN_NAME_TRACK_ID, id);
		insertValues.put(COLUMN_NAME_QUEUE_POSITION, position);

		// And update items after in list
		ContentValues updateValues = new ContentValues();
		String where = COLUMN_NAME_QUEUE_POSITION + "=?";
		String[] whereArgs = new String[] { String.valueOf(position) };

		long _id = 0;
		db.beginTransaction();
		try {
			for (int i = position; i < mQueueLength; i++) {
				updateValues.put(COLUMN_NAME_QUEUE_POSITION, i + 1);
				db.update(TABLE_NAME_PLAY_QUEUE, updateValues, where, whereArgs);
			}			
			
			_id = db.insert(TABLE_NAME_PLAY_QUEUE, null, insertValues);
			
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error adding track to play queue.", e);
		} finally {
			db.endTransaction();
		}

		return _id;
	}
	
	public void addAll(final Collection<? extends Track> tracks) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				addAll(getWritableDatabase(), tracks);
				return null;
			}
			
		}.execute();
	}
	
	private void addAll(SQLiteDatabase db, Collection<? extends Track> tracks) {
		ContentValues values = new ContentValues();
		
		db.beginTransaction();
		try {
			for (Track track : tracks) {
				long id = track.writeToContentValues(values);
				values.put(COLUMN_NAME_TRACK_ID, id);
				values.put(COLUMN_NAME_QUEUE_POSITION, mQueueLength);
				db.insert(TABLE_NAME_PLAY_QUEUE, null, values);
				mQueueLength++;
			}
			
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error adding track list to database.", e);
		} finally {
			db.endTransaction();
		}
	}
	
	public void addAll(final int position, final Collection<? extends Track> tracks) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				addAll(getWritableDatabase(), position, tracks);
				return null;
			}
			
		}.execute();
	}
	
	private void addAll(SQLiteDatabase db, int position, 
			Collection<? extends Track> tracks) {
		
		ContentValues values = new ContentValues();
		
		String where = COLUMN_NAME_QUEUE_POSITION + "=?";
		String[] whereArgs;
		final int len = tracks.size();
		
		db.beginTransaction();
		try {
			for (int i = position; i < position + len; i++) {
				whereArgs = new String[] { String.valueOf(i) };
				values.put(COLUMN_NAME_QUEUE_POSITION, i + len);
				db.update(TABLE_NAME_PLAY_QUEUE, values, where, whereArgs);
			}			
			
			values.clear();
			int i = 0;
			for (Track track : tracks) {
				long id = track.writeToContentValues(values);
				values.put(COLUMN_NAME_TRACK_ID, id);
				values.put(COLUMN_NAME_QUEUE_POSITION, position + i);
				
				db.insert(TABLE_NAME_PLAY_QUEUE, null, values);
				i++;
			}
			
			mQueueLength += len;
			
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error adding track list to database.", e);
		} finally {
			db.endTransaction();
		}
	}

	public void remove(final int position) {
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				return remove(getWritableDatabase(), position);
			}
			
		}.execute();
	}

	private int remove(SQLiteDatabase db, int position) {
		String deleteWhere = COLUMN_NAME_QUEUE_POSITION + "=?";
		String[] deleteWhereArgs = new String[] { String.valueOf(position) };
		
		String updateWhere = COLUMN_NAME_QUEUE_POSITION + "=?";
		String[] updateWhereArgs;
		ContentValues values = new ContentValues();

		int numRemoved = 0;
		db.beginTransaction();
		try {
			numRemoved = db.delete(TABLE_NAME_PLAY_QUEUE, deleteWhere, deleteWhereArgs);
			
			for (int i = position + 1; i < mQueueLength; i++) {
				updateWhereArgs = new String[] { String.valueOf(i) };
				values.put(COLUMN_NAME_QUEUE_POSITION, i - 1);
				
				db.update(TABLE_NAME_PLAY_QUEUE, values, updateWhere, updateWhereArgs);
			}
			
			mQueueLength--;
			
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error deleting item from queue.", e);
		} finally {
			db.endTransaction();
		}
		return numRemoved;
	}
	
	public void moveQueueItem(final int from, final int to, final Track track) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				moveQueueItem(getWritableDatabase(), from, to, track);
				return null;
			}
			
		}.execute();
	}
	
	private void moveQueueItem(SQLiteDatabase db, int from, int to, Track track) {
		int change = from < to ? -1 : 1;
		
		// Remove track from old position
		String deleteWhere = COLUMN_NAME_QUEUE_POSITION + "=?";
		String[] deleteWhereArgs = new String[] { String.valueOf(from) };
		
		// Shift all the tracks between "from" and "to" up or down
		String updateWhere = COLUMN_NAME_QUEUE_POSITION + "=?";
		String[] updateWhereArgs;
		ContentValues updateValues = new ContentValues();
		
		// Insert the track back into the queue in its new position
		ContentValues insertValues = new ContentValues();
		long id = track.writeToContentValues(insertValues);
		insertValues.put(COLUMN_NAME_TRACK_ID, id);
		insertValues.put(COLUMN_NAME_QUEUE_POSITION, to);
		
		db.beginTransaction();
		try {
			db.delete(TABLE_NAME_PLAY_QUEUE, deleteWhere, deleteWhereArgs);
			
			// Unfortunately Android gimps the SQL interface so we can't just increment
			// a range of values in one go.
			for (int i = from + 1; i <= to; i -= change) {
				updateValues.put(COLUMN_NAME_QUEUE_POSITION, i + change);
				updateWhereArgs = new String[] { String.valueOf(i) };
				
				db.update(TABLE_NAME_PLAY_QUEUE, updateValues, updateWhere, updateWhereArgs);
			}	
			
			db.insert(TABLE_NAME_PLAY_QUEUE, null, insertValues);
			
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error moving queue items.", e);
		} finally {
			db.endTransaction();
		}
	}
	
	public List<Track> getTrackList() {
		Cursor cursor = getReadableDatabase().query(
				TABLE_NAME_PLAY_QUEUE, // table
				null, // projection
				null, // selection
				null, // selection args
				null, // group by
				null, // having
				COLUMN_NAME_QUEUE_POSITION + " ASC"); // sort order

		List<Track> trackList = null;
		
		if (cursor != null) {
			trackList = new LinkedList<Track>();
			if (cursor.moveToFirst()) {
				do {
					trackList.add(new Track(
						cursor.getLong(2), // Id
						cursor.getString(3), // Title
						cursor.getLong(4), // Artist id
						cursor.getString(5), // Artist
						cursor.getLong(6), // Album id
						cursor.getString(7), // Album
						cursor.getLong(8))); // Duration
				} while (cursor.moveToNext());
			}
			cursor.close();
			mQueueLength = trackList.size();
		}
		return trackList;
	}
}