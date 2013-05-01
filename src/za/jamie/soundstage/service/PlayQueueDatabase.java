package za.jamie.soundstage.service;

import java.util.ArrayList;
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
    private static final int DATABASE_VERSION = 11;
    
    private static final String TABLE_NAME_TRACK_SET = "trackset";
    private static final String TABLE_NAME_TRACK_ORDER = "trackorder";
    private static final String TABLE_NAME_SHUFFLE_MAP = "shufflemap";
    
    private static final String COLUMN_NAME_TRACK_ID = "track_id";
    private static final String COLUMN_NAME_QUEUE_POSITION = "queue_position";
    private static final String COLUMN_NAME_SHUFFLE_POSITION = "shuffle_position";
    
    private static final String CREATE_TABLE_TRACK_SET = "CREATE TABLE "
    		+ TABLE_NAME_TRACK_SET + " ("
    		+ MediaStore.Audio.Media._ID + " INTEGER PRIMARY KEY, "
    		+ MediaStore.Audio.Media.TITLE + " STRING, "
    		+ MediaStore.Audio.Media.ARTIST_ID + " INTEGER, "
    		+ MediaStore.Audio.Media.ARTIST + " STRING, "
    		+ MediaStore.Audio.Media.ALBUM_ID + " INTEGER, "
    		+ MediaStore.Audio.Media.ALBUM + " STRING, "
    		+ MediaStore.Audio.Media.DURATION + " INTEGER)";
    
    private static final String CREATE_TABLE_TRACK_ORDER = "CREATE TABLE "
    		+ TABLE_NAME_TRACK_ORDER + " ("
    		+ COLUMN_NAME_QUEUE_POSITION + " INTEGER, "
    		+ COLUMN_NAME_TRACK_ID + " INTEGER, "
    		+ "FOREIGN KEY(" + COLUMN_NAME_TRACK_ID + ") REFERENCES "
    		+ TABLE_NAME_TRACK_SET + "(" + MediaStore.Audio.Media._ID + "))";
    
    private static final String CREATE_TABLE_SHUFFLE_MAP = "CREATE TABLE "
    		+ TABLE_NAME_SHUFFLE_MAP + " ("
    		+ COLUMN_NAME_QUEUE_POSITION + " INTEGER, "
    		+ COLUMN_NAME_SHUFFLE_POSITION + " INTEGER)";
    
    private static final String QUERY_TRACK_LIST = "SELECT "
    		+ MediaStore.Audio.Media._ID + ", "
    		+ MediaStore.Audio.Media.TITLE + ", "
    		+ MediaStore.Audio.Media.ARTIST_ID + ", "
    		+ MediaStore.Audio.Media.ARTIST + ", "
    		+ MediaStore.Audio.Media.ALBUM_ID + ", "
    		+ MediaStore.Audio.Media.ALBUM + ", "
    		+ MediaStore.Audio.Media.DURATION
    		+ " FROM " + TABLE_NAME_TRACK_ORDER
    		+ " JOIN " + TABLE_NAME_TRACK_SET
    		+ " ON " + TABLE_NAME_TRACK_ORDER + "." + COLUMN_NAME_TRACK_ID
    		+ "=" + TABLE_NAME_TRACK_SET + "." + MediaStore.Audio.Media._ID
    		+ " ORDER BY " + COLUMN_NAME_QUEUE_POSITION;
    
	public PlayQueueDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_TRACK_SET);
		db.execSQL(CREATE_TABLE_TRACK_ORDER);
		db.execSQL(CREATE_TABLE_SHUFFLE_MAP);
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
        dropTable(db, TABLE_NAME_TRACK_SET);
        dropTable(db, TABLE_NAME_TRACK_ORDER);
        dropTable(db, TABLE_NAME_SHUFFLE_MAP);

        // Recreates the database with a new version
        onCreate(db);
	}

	private void dropTable(SQLiteDatabase db, String tableName) {
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
	}
	
	public List<Track> getTrackList() {
		SQLiteDatabase db = getReadableDatabase();
		
		Cursor cursor = db.rawQuery(QUERY_TRACK_LIST, null);
		
		List<Track> trackList = null;
		if (cursor != null) {
			trackList = new ArrayList<Track>(cursor.getCount());
			if (cursor.moveToFirst()) {
				do {
					trackList.add(new Track(
							cursor.getLong(0), // Id
							cursor.getString(1), // Title
							cursor.getLong(2), // Artist id
							cursor.getString(3), // Artist
							cursor.getLong(4), // Album id
							cursor.getString(5), // Album
							cursor.getLong(6))); // Duration
				} while (cursor.moveToNext());
			}
		}
		if (trackList != null) {
			Log.d(TAG, "Track list found with size: " + trackList.size());
		} else {
			Log.e(TAG, "Cursor was null retrieving track list!");
		}
		return trackList;
	}
	
	public List<Integer> getShuffleMap() {
		SQLiteDatabase db = getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_NAME_SHUFFLE_MAP, 
				null, // columns
				null, // selection
				null, // selectionArgs
				null, // groupBy
				null, // having
				COLUMN_NAME_QUEUE_POSITION); // sortOrder
		
		List<Integer> shuffleMap = null;
		if (cursor != null) {
			shuffleMap = new ArrayList<Integer>(cursor.getCount());
			if (cursor.moveToFirst()) {
				do {
					shuffleMap.add(cursor.getInt(1));
				} while (cursor.moveToNext());
			}
		}
		return shuffleMap;
	}
	
	public void open(final List<Track> trackList, final List<Integer> shuffleMap) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				open(getWritableDatabase(), trackList, shuffleMap);
				return null;
			}
			
		}.execute();
	}
	
	private void open(SQLiteDatabase db, List<Track> trackList, List<Integer> shuffleMap) {
		resetDatabase(db);
		
		ContentValues values = new ContentValues();
		db.beginTransaction();
		try {
			// Insert into track set
			for (Track track : trackList) {
				Log.d(TAG, "Adding track to track set");
				final long id = track.writeToContentValues(values);
				values.put(MediaStore.Audio.Media._ID, id);
				db.insert(TABLE_NAME_TRACK_SET, null, values);
			}
			
			values.clear();
			for (int i = 0; i < trackList.size(); i++) {
				values.put(COLUMN_NAME_QUEUE_POSITION, i);
				values.put(COLUMN_NAME_TRACK_ID, trackList.get(i).getId());
				db.insert(TABLE_NAME_TRACK_ORDER, null, values);
			}
			
			values.clear();
			for (int i = 0; i < shuffleMap.size(); i++) {
				values.put(COLUMN_NAME_QUEUE_POSITION, i);
				values.put(COLUMN_NAME_SHUFFLE_POSITION, shuffleMap.get(i));
				db.insert(TABLE_NAME_SHUFFLE_MAP, null, values);
			}
			
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error opening play queue database.", e);
		} finally {
			db.endTransaction();
		}
	}
	
	public void add(final int position, final Track track) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				add(getWritableDatabase(), position, track);				
				
				return null;
			}
			
		}.execute();
	}
	
	private void add(SQLiteDatabase db, int position, Track track) {
		// Curse you Android and your rubbish SQL update method!
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ")
			.append(TABLE_NAME_TRACK_ORDER)
			.append(" SET ")
			.append(COLUMN_NAME_QUEUE_POSITION + "=")
			.append(COLUMN_NAME_QUEUE_POSITION + "+1")
			.append(" WHERE ")
			.append(COLUMN_NAME_QUEUE_POSITION)
			.append(">=")
			.append(position);
		
		ContentValues values = new ContentValues();
		db.beginTransaction();
		try {
			// Insert into track set
			final long id = track.writeToContentValues(values);
			values.put(MediaStore.Audio.Media._ID, id);
			db.insertWithOnConflict(TABLE_NAME_TRACK_SET, null, values, 
					SQLiteDatabase.CONFLICT_IGNORE);
			
			// Push tracks beyond this one down
			db.execSQL(query.toString());			
			
			// Insert new track at correct position
			values.clear();
			values.put(COLUMN_NAME_QUEUE_POSITION, position);
			values.put(COLUMN_NAME_TRACK_ID, track.getId());
			db.insert(TABLE_NAME_TRACK_ORDER, null, values);
			
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error adding track.", e);
		} finally {
			db.endTransaction();
		}
	}
	
	public void addAll(final int position, final List<Track> tracks) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				addAll(getWritableDatabase(), position, tracks);				
				
				return null;
			}
			
		}.execute();
	}
	
	private void addAll(SQLiteDatabase db, int position, List<Track> tracks) {
		// Curse you Android and your rubbish SQL update method!
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ")
			.append(TABLE_NAME_TRACK_ORDER)
			.append(" SET ")
			.append(COLUMN_NAME_QUEUE_POSITION + "=")
			.append(COLUMN_NAME_QUEUE_POSITION + "+" + tracks.size())
			.append(" WHERE ")
			.append(COLUMN_NAME_QUEUE_POSITION)
			.append(">=")
			.append(position);
		
		ContentValues values = new ContentValues();
		db.beginTransaction();
		try {
			// Insert into track set
			for (Track track : tracks) {
				final long id = track.writeToContentValues(values);
				values.put(MediaStore.Audio.Media._ID, id);
				db.insertWithOnConflict(TABLE_NAME_TRACK_SET, null, values, 
						SQLiteDatabase.CONFLICT_IGNORE);
			}
			
			// Push tracks beyond this one down
			db.execSQL(query.toString());			
			
			// Insert new track at correct position
			// Could squish this into above for loop but that would require many
			// more calls to values.clear() which is assumed to be more costly than
			// iterating a list.
			values.clear();
			for (Track track : tracks) {
				values.put(COLUMN_NAME_QUEUE_POSITION, position);
				values.put(COLUMN_NAME_TRACK_ID, track.getId());
				db.insert(TABLE_NAME_TRACK_ORDER, null, values);
			}
			
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error adding track.", e);
		} finally {
			db.endTransaction();
		}
	}
	
	public void remove(final int position) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				remove(getWritableDatabase(), position);
				return null;
			}
			
		}.execute();
	}
	
	private void remove(SQLiteDatabase db, int position) {
		// Curse you Android and your rubbish SQL update method!
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ")
			.append(TABLE_NAME_TRACK_ORDER)
			.append(" SET ")
			.append(COLUMN_NAME_QUEUE_POSITION + "=")
			.append(COLUMN_NAME_QUEUE_POSITION + "-1")
			.append(" WHERE ")
			.append(COLUMN_NAME_QUEUE_POSITION)
			.append(">=")
			.append(position);
		
		db.beginTransaction();
		try {
			// Delete Track
			String where = COLUMN_NAME_QUEUE_POSITION + "=?";
			String[] whereArgs = new String[] { String.valueOf(position) };
			db.delete(TABLE_NAME_TRACK_ORDER, where, whereArgs);
			
			// Push tracks beyond this one up
			db.execSQL(query.toString());			
			
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error removing track.", e);
		} finally {
			db.endTransaction();
		}
	}
	
	public void move(final int from, final int to) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				move(getWritableDatabase(), from, to);
				return null;
			}
			
		}.execute();
	}
	
	private void move(SQLiteDatabase db, int from, int to) {
		int change = from < to ? -1 : 1;
		
		// Shift all the tracks between "from" and "to" up or down
		String where = COLUMN_NAME_QUEUE_POSITION + "=?";
		String[] whereArgs = new String[1];
		
		ContentValues values = new ContentValues();
		
		db.beginTransaction();
		try {
			// Move the item to position -1 temporarily
			whereArgs[0] = String.valueOf(from);
			values.put(COLUMN_NAME_QUEUE_POSITION, -1);
			db.update(TABLE_NAME_TRACK_ORDER, values, where, whereArgs);
			
			// Shift the intermediary tracks up or down
			for (int i = from + 1; i <= to; i -= change) {
				whereArgs[0] = String.valueOf(i);
				values.put(COLUMN_NAME_QUEUE_POSITION, i + change);				
				
				db.update(TABLE_NAME_TRACK_ORDER, values, where, whereArgs);
			}	
			
			// Move item to new position
			whereArgs[0] = String.valueOf(-1);
			values.put(COLUMN_NAME_QUEUE_POSITION, to);
			db.update(TABLE_NAME_TRACK_ORDER, values, where, whereArgs);
			
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error moving queue items.", e);
		} finally {
			db.endTransaction();
		}
	}
}