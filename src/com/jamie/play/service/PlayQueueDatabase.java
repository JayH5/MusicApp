package com.jamie.play.service;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.jamie.play.models.Track;

public class PlayQueueDatabase extends SQLiteOpenHelper {
	
	private static final String TAG = "PlayQueueDatabase";
	
	private static final String DATABASE_NAME = "playqueue.db";
    private static final int DATABASE_VERSION = 1;
    
    public static final String TABLE_NAME_QUEUE = "queue";
    
    public static final String COLUMN_NAME_QUEUE_POSITION = "queue_position";
    
    private static final String[] COLUMNS = new String[] {
    	COLUMN_NAME_QUEUE_POSITION,
    	MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION
    };
    
    private static final String[] COLUMNS_CREATE = new String[] {
    	COLUMNS[0] + " INTEGER",
    	COLUMNS[1] + " INTEGER",
    	COLUMNS[2] + " STRING",
    	COLUMNS[3] + " INTEGER",
    	COLUMNS[4] + " STRING",
    	COLUMNS[5] + " INTEGER",
    	COLUMNS[6] + " STRING",
    	COLUMNS[7] + " INTEGER"
    };
    
    /*private static final String[] COLUMN_TYPES = new String[] {
    	"INTEGER",
    	"INTEGER",
    	"TEXT",
    	"INTEGER",
    	"TEXT",
    	"INTEGER",
    	"TEXT",
    	"INTEGER"
    };*/
    
    private static final String CREATE_TABLE = "CREATE TABLE " + 
    			TABLE_NAME_QUEUE + " (" + 
    		COLUMNS_CREATE[0] + " PRIMARY KEY AUTOINCREMENT, " +
    		COLUMNS_CREATE[1] + ", " +
    		COLUMNS_CREATE[2] + ", " +
    		COLUMNS_CREATE[3] + ", " +
    		COLUMNS_CREATE[4] + ", " +
    		COLUMNS_CREATE[5] + ", " +
    		COLUMNS_CREATE[6] + ", " +
    		COLUMNS_CREATE[7] + ")";
    
	public PlayQueueDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Logs that the database is being upgraded
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        // Kills the tables and existing data
        dropTable(db, TABLE_NAME_QUEUE);

        // Recreates the database with a new version
        onCreate(db);		
	}
	
	public void remove(long id) {
		final String where = MediaStore.Audio.Media._ID + "=?";
		
		new AsyncTask<Long, Void, Integer>() {

			@Override
			protected Integer doInBackground(Long... arg0) {
				final String[] whereArgs = new String[] { String.valueOf(arg0[0]) };
				return getWritableDatabase().delete(TABLE_NAME_QUEUE, where, whereArgs);
			}
			
		}.execute(id);
		
	}
	
	public void remove(int from, int to) {
		final String where = COLUMN_NAME_QUEUE_POSITION + " >=? AND " + 
				COLUMN_NAME_QUEUE_POSITION + "<=?";
		
		new AsyncTask<Integer, Void, Integer>() {

			@Override
			protected Integer doInBackground(Integer... params) {
				String[] whereArgs = new String[] { String.valueOf(params[0]), 
						String.valueOf(params[1]) };
				return getWritableDatabase().delete(TABLE_NAME_QUEUE, where, whereArgs);
			}
			
		}.execute(from, to);
		
	}
	
	public void open(final List<Track> list) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				// Clear the current table and create a new one
				final SQLiteDatabase db = getWritableDatabase();
				dropTable(db, TABLE_NAME_QUEUE);
				onCreate(db);
				
				addAll(db, list);
				
				return null;
			}
			
		}.execute((Void[]) null);
		
	}
	
	public void addAll(final List<Track> list) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				addAll(getWritableDatabase(), list);
				return null;
			}
			
		}.execute((Void[]) null);
		
	}
	
	private void addAll(SQLiteDatabase db, List<Track> list) {
		// Insert the list items
		db.beginTransaction();
		try {
			for (Track track : list) {					
				db.insert(TABLE_NAME_QUEUE, null, track.toContentValues());
			}
			db.setTransactionSuccessful();
		} catch (SQLiteException sqle) {
			Log.e(TAG, "Error during transaction when opening new table: ", sqle);
		} finally {
			db.endTransaction();
		}
	}
	
	public void add(Track track) {
		new AsyncTask<Track, Void, Void>() {

			@Override
			protected Void doInBackground(Track... params) {
				getWritableDatabase().insert(TABLE_NAME_QUEUE, null, 
						params[0].toContentValues());
				
				return null;
			}
			
		}.execute(track);
		
	}
	
	public List<Track> getQueue() {
		Cursor cursor = getReadableDatabase().query(
				TABLE_NAME_QUEUE, 
				COLUMNS, 
				null, 
				null, 
				null, 
				null, 
				COLUMN_NAME_QUEUE_POSITION + " ASC");
		
		List<Track> queue = null;
		if (cursor != null) {
			queue = new ArrayList<Track>(cursor.getCount());
			if (cursor.moveToFirst()) {
				do {
					queue.add(new Track(
							cursor.getLong(1),
							cursor.getString(2),
							cursor.getLong(3),
							cursor.getString(4),
							cursor.getLong(5),
							cursor.getString(6),
							cursor.getLong(7)));
				} while (cursor.moveToNext());
				
			}
			cursor.close();
			cursor = null;
		}
		
		return queue;
	}
	
	/*private void createTable(SQLiteDatabase db, String tableName, String[] columnNames, 
			String[] columnTypes) {
		
		final int numColumns = columnNames.length;
		final String[] bindArgs = new String[numColumns];
		
		for (int i = 1; i < numColumns; i++) {
			bindArgs[i] = columnNames[i] + " " + columnTypes[i];
		}
		
		final String CREATE_TABLE = "CREATE TABLE ";
		final int sbLength = CREATE_TABLE.length() + tableName.length() + 2 + numColumns * 2;
		final StringBuilder sb = new StringBuilder(sbLength);
		sb.append(CREATE_TABLE)
			.append(tableName)
			.append(" (");
		for (int i = 0; i < numColumns - 1; i++) {
			sb.append("?,");
		}
		sb.append("?)");
		
		db.execSQL(sb.toString(), bindArgs);
	}*/
	
	private void dropTable(SQLiteDatabase db, String tableName) {
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
	}
}
