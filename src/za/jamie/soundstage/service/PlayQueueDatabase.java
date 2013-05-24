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
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;


public class PlayQueueDatabase extends SQLiteOpenHelper {
	
	private static final String TAG = "PlayQueueDatabase";
	
	private static final String DATABASE_NAME = "playqueue.db";
    private static final int DATABASE_VERSION = 13;
    
    private static final String TABLE_NAME_TRACK_SET = "trackset";
    private static final String TABLE_NAME_TRACK_ORDER = "trackorder";
    private static final String TABLE_NAME_SHUFFLE_MAP = "shufflemap";
    private static final String TABLE_NAME_STATE = "state";
    
    private static final String COLUMN_NAME_TRACK_ID = "track_id";
    private static final String COLUMN_NAME_QUEUE_POSITION = "queue_position";
    private static final String COLUMN_NAME_SHUFFLE_POSITION = "shuffle_position";
    
    private static final String COLUMN_NAME_STATE_KEY = "state_key";
    private static final String COLUMN_NAME_STATE_VALUE = "state_value";
    
    private static final String STATE_UPDATE_WHERE = COLUMN_NAME_STATE_KEY + "=?";
    protected static final String STATE_KEY_PLAY_POSITION = "play_position";
    protected static final String STATE_KEY_SHUFFLE_ENABLED = "shuffle_enabled";
    protected static final String STATE_KEY_REPEAT_MODE = "repeat_mode";
    
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
    
    private static final String CREATE_TABLE_STATE = "CREATE TABLE "
    		+ TABLE_NAME_STATE + " ("
    		+ COLUMN_NAME_STATE_KEY + " STRING, "
    		+ COLUMN_NAME_STATE_VALUE + " INTEGER)";
    
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
		db.execSQL(CREATE_TABLE_STATE);
		initStateTable(db);
	}
	
	private void initStateTable(SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_STATE_VALUE, 0);
		try {
			db.beginTransaction();
			values.put(COLUMN_NAME_STATE_KEY, STATE_KEY_PLAY_POSITION);
			db.insert(TABLE_NAME_STATE, null, values);
			values.put(COLUMN_NAME_STATE_KEY, STATE_KEY_SHUFFLE_ENABLED);
			db.insert(TABLE_NAME_STATE, null, values);
			values.put(COLUMN_NAME_STATE_KEY, STATE_KEY_REPEAT_MODE);
			db.insert(TABLE_NAME_STATE, null, values);
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error initializing state table.", e);
		} finally {
			db.endTransaction();
		}
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
        dropTable(db, TABLE_NAME_STATE);

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
		return trackList;
	}
	
	public List<Integer> getShuffleMap() {
		SQLiteDatabase db = getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_NAME_SHUFFLE_MAP, 
				new String[] { COLUMN_NAME_SHUFFLE_POSITION }, // columns
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
					shuffleMap.add(cursor.getInt(0));
				} while (cursor.moveToNext());
			}
		}
		return shuffleMap;
	}
	
	public void open(final List<Track> trackList) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				open(getWritableDatabase(), trackList);
				return null;
			}
			
		}.execute();
	}
	
	private void open(SQLiteDatabase db, List<Track> trackList) {		
		ContentValues values = new ContentValues();
		db.beginTransaction();
		try {
			// Clear the current tables
			db.delete(TABLE_NAME_TRACK_SET, null, null);			
			db.delete(TABLE_NAME_TRACK_ORDER, null, null);
			
			// Add the new values
			for (Track track : trackList) {
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
			// "Move" the item to position -1 temporarily
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
	
	public void saveShuffleMap(final List<Integer> shuffleMap) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				saveShuffleMap(getWritableDatabase(), shuffleMap);
				return null;
			}
			
		}.execute();
	}
	
	private void saveShuffleMap(SQLiteDatabase db, List<Integer> shuffleMap) {
		ContentValues values = new ContentValues();
		db.beginTransaction();
		try {
			// Clear the table
			db.delete(TABLE_NAME_SHUFFLE_MAP, null, null);
			// Add the new values
			for (int i = 0; i < shuffleMap.size(); i++) {
				values.put(COLUMN_NAME_QUEUE_POSITION, i);
				values.put(COLUMN_NAME_SHUFFLE_POSITION, shuffleMap.get(i));
				db.insert(TABLE_NAME_SHUFFLE_MAP, null, values);
			}
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error saving shuffle map.", e);
		} finally {
			db.endTransaction();
		}
	}
	
	public void savePlayPosition(int playPosition) {
		saveState(STATE_KEY_PLAY_POSITION, playPosition);
	}
	
	public void saveShuffleEnabled(boolean shuffleEnabled) {
		saveState(STATE_KEY_SHUFFLE_ENABLED, shuffleEnabled ? 1 : 0);
	}
	
	public void saveRepeatMode(int repeatMode) {
		saveState(STATE_KEY_REPEAT_MODE, repeatMode);
	}
	
	public void saveState(final String key, final int value) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				saveState(getWritableDatabase(), key, value);
				return null;
			}
		}.execute();
	}
	
	private void saveState(SQLiteDatabase db, String key, int value) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_STATE_VALUE, value);
		db.update(TABLE_NAME_STATE, values, STATE_UPDATE_WHERE, new String[] { key });
	}
	
	public Bundle getState() {
		Cursor cursor = getReadableDatabase().query(TABLE_NAME_STATE, null, null, 
				null, null, null, null);
		Bundle bundle = new Bundle();
		if (cursor != null && cursor.moveToFirst()) {
			final int keyColIdx = cursor.getColumnIndexOrThrow(COLUMN_NAME_STATE_KEY);
			final int valueColIdx = cursor.getColumnIndexOrThrow(COLUMN_NAME_STATE_VALUE);
			
			do {
				bundle.putInt(cursor.getString(keyColIdx), cursor.getInt(valueColIdx));
			} while (cursor.moveToNext());
		}
		
		return bundle;
	}
}