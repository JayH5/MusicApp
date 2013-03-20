package za.jamie.soundstage.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final int DATABASE_VERSION = 2;
    
    public static final String TABLE_NAME_TRACK_SET = "trackset";
    public static final String TABLE_NAME_TRACK_IDS = "trackids";
    
    public static final String COLUMN_NAME_QUEUE_POSITION = "queue_position";
    public static final String COLUMN_NAME_TRACK_ID = "track_id";
    
    private static final String[] TRACK_IDS_COLUMNS = new String[] {
    	"queue_position",
    	"track_id"
    };
    
    private static final String CREATE_TABLE_TRACK_IDS = "CREATE TABLE " +
    		TABLE_NAME_TRACK_IDS + " (" +
    		TRACK_IDS_COLUMNS[0] + " INTEGER" + " PRIMARY KEY AUTOINCREMENT, " +
    		TRACK_IDS_COLUMNS[1] + " INTEGER)";
    
    private static final String[] TRACK_SET_COLUMNS = new String[] {
    	MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION
    };
    
    private static final String CREATE_TABLE_TRACK_SET = "CREATE TABLE " + 
    			TABLE_NAME_TRACK_SET + " (" + 
    		TRACK_SET_COLUMNS[0] + " INTEGER" + " PRIMARY KEY, " +
    		TRACK_SET_COLUMNS[1] + " STRING" + ", " +
    		TRACK_SET_COLUMNS[2] + " INTEGER" + ", " +
    		TRACK_SET_COLUMNS[3] + " STRING" + ", " +
    		TRACK_SET_COLUMNS[4] + " INTEGER" + ", " +
    		TRACK_SET_COLUMNS[5] + " STRING" + ", " +
    		TRACK_SET_COLUMNS[6] + " INTEGER)";
    
	public PlayQueueDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create the two tables
		db.execSQL(CREATE_TABLE_TRACK_SET);
		db.execSQL(CREATE_TABLE_TRACK_IDS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Logs that the database is being upgraded
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        resetDatabase(db);	
	}
	
	public void open(final List<Long> trackIdList, final Map<Long, Track> trackMap) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				SQLiteDatabase db = getWritableDatabase();
				resetDatabase(db);
				
				// For inserting columns
				ContentValues values = new ContentValues();
				
				// Get the set of tracks for inserting tracks
				Set<Map.Entry<Long, Track>> trackSet = trackMap.entrySet();
				
				db.beginTransaction();
				try {
					for (Long trackId : trackIdList) {
						values.put(COLUMN_NAME_TRACK_ID, trackId);
						db.insert(TABLE_NAME_TRACK_IDS, null, values);
					}
					values.clear(); // clear out trackId values
					for (Map.Entry<Long, Track> mapEntry : trackSet) {
						mapEntry.getValue().writeToContentValues(values);
						db.insert(TABLE_NAME_TRACK_SET, null, values);
					}
					db.setTransactionSuccessful();
				} catch (SQLiteException e) {
					Log.e(TAG, "Error during transaction when updating table: ", e);
				} finally {
					db.endTransaction();
				}
				
				return null;
			}
			
		}.execute();
		
	}
	
	private void resetDatabase(SQLiteDatabase db) {
		// Kills the tables and existing data
        dropTable(db, TABLE_NAME_TRACK_SET);
        dropTable(db, TABLE_NAME_TRACK_IDS);

        // Recreates the database with a new version
        onCreate(db);
	}
	
	public void addTrackToSet(final Track track) {
		new AsyncTask<Void, Void, Long>() {

			@Override
			protected Long doInBackground(Void... params) {
				return addTrackToSet(getWritableDatabase(), track);
			}
			
		}.execute();
		
	}
	
	private long addTrackToSet(SQLiteDatabase db, Track track) {
		ContentValues values = new ContentValues();
		track.writeToContentValues(values);
		return db.insert(TABLE_NAME_TRACK_SET, null, values);
	}
	
	public void removeTrackFromSet(final long id) {		
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... arg0) {
				return removeTrackFromSet(getWritableDatabase(), id);			
			}
			
		}.execute();
		
	}
	
	private int removeTrackFromSet(SQLiteDatabase db, long id) {
		String where = MediaStore.Audio.Media._ID + "=?";
		String[] whereArgs = new String[] { String.valueOf(id) };
		
		return db.delete(TABLE_NAME_TRACK_SET, where, whereArgs);
	}
	
	public void updateTrackSet(final Collection<? extends Track> positiveDiff, 
			final Collection<? extends Track> negativeDiff) {
		
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... arg0) {
				return updateTrackSet(getWritableDatabase(), positiveDiff, negativeDiff);
			}
			
		}.execute();
	}
	
	private int updateTrackSet(SQLiteDatabase db, Collection<? extends Track> positiveDiff, 
			Collection<? extends Track> negativeDiff) {
		
		int netChange = 0;
		ContentValues values = new ContentValues();
		db.beginTransaction();
		try {
			if (positiveDiff != null) {
				for (Track track : positiveDiff) {
					track.writeToContentValues(values);
					if (db.insert(TABLE_NAME_TRACK_SET, null, values) > -1) {
						netChange++;
					}
				}
			}
			if (negativeDiff != null) {
				String deleteWhere = MediaStore.Audio.Media._ID + "=?";
				for (Track track : negativeDiff) {
					String[] whereArg = new String[] { String.valueOf(track.getId()) };
					netChange -= db.delete(TABLE_NAME_TRACK_SET, deleteWhere, whereArg);
				}
			}					
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error during transaction when updating table: ", e);
		} finally {
			db.endTransaction();
		}
		return netChange;
	}
	
	public void appendTrackIdToList(final long trackId) {
		new AsyncTask<Void, Void, Long>() {

			@Override
			protected Long doInBackground(Void... arg0) {
				return appendTrackIdToList(getWritableDatabase(), trackId);
			}
			
		}.execute();
	}
	
	private long appendTrackIdToList(SQLiteDatabase db, long trackId) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_TRACK_ID, trackId);
		return db.insert(TABLE_NAME_TRACK_IDS, null, values);
	}
	
	public void appendTrackIdsToList(final List<Long> trackIds) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				appendTrackIdsToList(getWritableDatabase(), trackIds);
				return null;
			}
			
		}.execute();
	}
	
	private void appendTrackIdsToList(SQLiteDatabase db, List<Long> trackIds) {
		ContentValues values = new ContentValues();
		db.beginTransaction();
		try {
			for (Long trackId : trackIds) {
				values.put(COLUMN_NAME_TRACK_ID, trackId);
				db.insert(TABLE_NAME_TRACK_IDS, null, values);
			}
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error during transaction when updating table: ", e);
		} finally {
			db.endTransaction();
		}
	}
	
	public void updateTrackIdList(final List<Long> trackIds) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				updateTrackIdList(getWritableDatabase(), trackIds);
				return null;
			}
			
		}.execute();
	}
	
	private void updateTrackIdList(SQLiteDatabase db, List<Long> trackIds) {
		dropTable(db, TABLE_NAME_TRACK_IDS);
		db.execSQL(CREATE_TABLE_TRACK_IDS);
		
		ContentValues values = new ContentValues();
		db.beginTransaction();
		try {
			for (Long trackId : trackIds) {
				values.put(COLUMN_NAME_TRACK_ID, trackId);
				db.insert(TABLE_NAME_TRACK_IDS, null, values);
			}
			db.setTransactionSuccessful();
		} catch (SQLiteException e) {
			Log.e(TAG, "Error during transaction when updating table: ", e);
		} finally {
			db.endTransaction();
		}
	}
	
	public List<Long> getTrackIdList() {
		Cursor cursor = getReadableDatabase().query(
				TABLE_NAME_TRACK_IDS, // table
				new String[] { COLUMN_NAME_TRACK_ID }, // projection
				null, // selection
				null, // selection args
				null, // group by
				null, // having
				null); // sort order, since the queue position is primary key, don't
		// need to specify order
		
		List<Long> trackIds = null;
		if (cursor != null) {
			trackIds = new LinkedList<Long>();
			Log.d(TAG, "Restoring queue of length: " + cursor.getCount());
			if (cursor.moveToFirst()) {
				do {
					trackIds.add(cursor.getLong(0));
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = null;
		}
		return trackIds;
	}
	
	public Map<Long, Track> getTrackMap() {
		Cursor cursor = getReadableDatabase().query(
				TABLE_NAME_TRACK_SET, // table
				null, // projection
				null, // selection
				null, // selection args
				null, // group by
				null, // having
				null); // sort order, since the mediastore id is the primary key, don't
		// need to specify order
		
		Map<Long, Track> trackMap = null;
		if (cursor != null) {
			trackMap = new HashMap<Long, Track>();
			if (cursor.moveToFirst()) {
				do {
					long trackId = cursor.getLong(0);
					Track track = new Track(
							trackId, // Id
							cursor.getString(1), // Title
							cursor.getLong(2), // Artist id							
							cursor.getString(3), // Artist
							cursor.getLong(4), // Album id
							cursor.getString(5), // Album
							cursor.getLong(6)); // Duration
					
					trackMap.put(trackId, track);
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = null;
		}
		return trackMap;				
	}
	
	private void dropTable(SQLiteDatabase db, String tableName) {
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
	}
}
