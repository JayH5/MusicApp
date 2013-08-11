package za.jamie.soundstage.fragments.library;

import java.util.LinkedList;
import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.LibraryAdapter;
import za.jamie.soundstage.adapters.interfaces.TrackListAdapter;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import za.jamie.soundstage.utils.TextUtils;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class SongsFragment extends TrackListFragment {
    
    private SongsAdapter mAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAdapter = new SongsAdapter(getActivity(),
        		R.layout.list_item_two_line_flip, R.layout.list_item_header, null, 0);
        setListAdapter(mAdapter);
        
        CursorManager cm = new CursorManager(getActivity(), mAdapter, 
        		MusicStore.Tracks.CURSOR);
        getLoaderManager().initLoader(0, null, cm);
    }
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, mAdapter.getCursorPosition(position), id);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, 
    		Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.list_fragment_fastscroll, parent, false);
    }
	
	private static class SongsAdapter extends LibraryAdapter implements TrackListAdapter {

		private int mIdColIdx;
		private int mTitleColIdx;
		private int mArtistIdColIdx;
		private int mArtistColIdx;
		private int mAlbumIdColIdx;
		private int mAlbumColIdx;
		private int mDurationColIdx;
		
		public SongsAdapter(Context context, int layout, int headerLayout,
				Cursor c, int flags) {
			super(context, layout, headerLayout, c, flags);
		}

		@Override
		protected void getColumnIndices(Cursor cursor) {
			mIdColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
			mTitleColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
			mArtistIdColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
			mArtistColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
			mAlbumIdColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
			mAlbumColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
			mDurationColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
		}

		@Override
		protected String getSection(Context context, Cursor cursor) {
			return TextUtils.headerFor(cursor.getString(mTitleColIdx));
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView title = (TextView) view.findViewById(R.id.title);
			TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
			
			title.setText(cursor.getString(mTitleColIdx));
			subtitle.setText(cursor.getString(mArtistColIdx));
		}

		@Override
		public List<Track> getTrackList() {
			Cursor cursor = getCursor();
			List<Track> trackList = null;
			if (cursor != null && cursor.moveToFirst()) {
				trackList = new LinkedList<Track>();
				do {
					trackList.add(new Track(
							cursor.getLong(mIdColIdx),
							cursor.getString(mTitleColIdx),
							cursor.getLong(mArtistIdColIdx),
							cursor.getString(mArtistColIdx),
							cursor.getLong(mAlbumIdColIdx),
							cursor.getString(mAlbumColIdx),
							cursor.getLong(mDurationColIdx)));
				} while (cursor.moveToNext());
			}
			return trackList;
		}

		@Override
		public Track getTrack(int position) {
			Cursor cursor = (Cursor) getItem(position);
			Track track = null;
			if (cursor != null) {
				track = new Track(cursor.getLong(mIdColIdx),
						cursor.getString(mTitleColIdx),
						cursor.getLong(mArtistIdColIdx),
						cursor.getString(mArtistColIdx),
						cursor.getLong(mAlbumIdColIdx),
						cursor.getString(mAlbumColIdx),
						cursor.getLong(mDurationColIdx));
			}
			return track;
		}
		
	}
  
}