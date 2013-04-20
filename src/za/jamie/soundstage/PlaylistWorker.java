package za.jamie.soundstage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

public class PlaylistWorker {
	
	private static final int INSERT_SIZE = 1000;
	
	private final Context mContext;
	
	public PlaylistWorker(Context context) {
		mContext = context;
	}
	
	public void createPlaylistFromQueue(final MusicQueueWrapper queue) {
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
	}
	
	public void createPlaylist(final String name, final long[] trackIds) {
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				long playlistId = createPlaylistImpl(name);
				return addToPlaylistImpl(playlistId, trackIds);
			}
			
			@Override
			protected void onPostExecute(Integer result) {
				Toast.makeText(mContext, "Created playlist '" + name + "' with " + result + " tracks." , 
						Toast.LENGTH_SHORT).show();
			}
			
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
	}
	
	public void addToPlaylist(final long playlistId, final long[] trackIds) {
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				return addToPlaylistImpl(playlistId, trackIds);
			}
			
			@Override
			protected void onPostExecute(Integer result) {
				Toast.makeText(mContext, "Added" + result + " tracks to playlist." , 
						Toast.LENGTH_SHORT).show();
			}
			
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
	}
	
	public void movePlaylistItem(final long playlistId, final int from, 
			final int to) {
		
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				movePlaylistItemImpl(playlistId, from, to);
				return null;
			}
			
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
	}
	
	private long createPlaylistImpl(String name) {
		final ContentResolver resolver = mContext.getContentResolver();
		
		ContentValues values = new ContentValues();
		values.put(MediaStore.Audio.Playlists.NAME, name);
		
		Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
		
		return Long.parseLong(uri.getLastPathSegment());
	}
	
	private int addToPlaylistImpl(long playlistId, long[] trackIds) {
		final ContentResolver resolver = mContext.getContentResolver();
		
		final int numTracks = trackIds.length;			
		ContentValues values[] = null;
		final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", 
				playlistId);
		
		int numAdded = 0;
		for (int i = 0; i < numTracks; i += INSERT_SIZE) {
			int valuesSize = Math.min(numTracks - i, INSERT_SIZE);
			
			if (values == null || values.length != valuesSize) {
				values = new ContentValues[valuesSize];
			}
			
			for (int j = 0; j < valuesSize; j++) {
				if (values[j] == null) {
					values[j] = new ContentValues();
				}
				values[j].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, trackIds[i + j]);
			}
			
			numAdded += resolver.bulkInsert(uri, values);
		}
		return numAdded;
	}
	
	private void movePlaylistItemImpl(long playlistId, int from, int to) {
		MediaStore.Audio.Playlists.Members.moveItem(mContext.getContentResolver(), 
				playlistId, from, to);
	}
}
