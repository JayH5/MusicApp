package za.jamie.soundstage.fragments.library;

import java.util.ArrayList;
import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.adapters.abs.LibraryAdapter;
import za.jamie.soundstage.adapters.interfaces.TrackListAdapter;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import za.jamie.soundstage.service.MusicService;
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
import android.widget.Toast;

public class SongsFragment extends TrackListFragment {
    
    private SongsAdapter mAdapter;
    private ViewFlipper mFlipper;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAdapter = new SongsAdapter(getActivity(),
        		R.layout.list_item_two_line_flip, R.layout.list_item_header, null, 0);
        setListAdapter(mAdapter);
        
        mFlipper = new ViewFlipper(R.id.list_item, R.id.flipped_view);
        final View.OnClickListener listener = new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				final MusicItem item = (MusicItem) v.getTag();
				final MusicActivity activity = (MusicActivity) getActivity();
				
				int action = 0; 
				switch(v.getId()) {
				case R.id.flipped_view_now:
					action = MusicService.NOW;
					activity.showPlayer();
					break;
				case R.id.flipped_view_next:
					action = MusicService.NEXT;
					Toast.makeText(activity, "'" + item.title + "' will play next." , Toast.LENGTH_SHORT).show();
					break;
				case R.id.flipped_view_last:
					action = MusicService.LAST;
					Toast.makeText(activity, "'" + item.title + "' will play last." , Toast.LENGTH_SHORT).show();
					break;
				case R.id.flipped_view_more:
					break;
				}

				activity.getMusicConnection().enqueue(item, action);
				
				mFlipper.unflip();
			}
		};
		mAdapter.setFlippedViewOnClickListener(listener);
        
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
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final ListView lv = getListView();
		lv.setOnItemLongClickListener(mFlipper);
		lv.setOnScrollListener(mFlipper);
	}
	
	private static class SongsAdapter extends LibraryAdapter implements TrackListAdapter {
		private int mIdColIdx;
		private int mTitleColIdx;
		private int mArtistIdColIdx;
		private int mArtistColIdx;
		private int mAlbumIdColIdx;
		private int mAlbumColIdx;
		private int mDurationColIdx;
		private View.OnClickListener mFlippedViewListener;
		
		public SongsAdapter(Context context, int layout, int headerLayout,
				Cursor c, int flags) {
			super(context, layout, headerLayout, c, flags);
		}
		
		public void setFlippedViewOnClickListener(View.OnClickListener flippedViewListener) {
			mFlippedViewListener = flippedViewListener;
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
			TextView titleView = (TextView) view.findViewById(R.id.title);
			TextView subtitleView = (TextView) view.findViewById(R.id.subtitle);
			
			final String title = cursor.getString(mTitleColIdx);
			titleView.setText(title);
			subtitleView.setText(cursor.getString(mArtistColIdx));
			
			final MusicItem tag = new MusicItem(cursor.getLong(mIdColIdx), title, MusicItem.TYPE_TRACK);
			
			TextView now = (TextView) view.findViewById(R.id.flipped_view_now);
			now.setTag(tag);
			now.setOnClickListener(mFlippedViewListener);
			TextView next = (TextView) view.findViewById(R.id.flipped_view_next);
			next.setTag(tag);
			next.setOnClickListener(mFlippedViewListener);
			TextView last = (TextView) view.findViewById(R.id.flipped_view_last);
			last.setTag(tag);
			last.setOnClickListener(mFlippedViewListener);
			TextView more = (TextView) view.findViewById(R.id.flipped_view_more);
			more.setTag(tag);
			more.setOnClickListener(mFlippedViewListener);			
		}

		@Override
		public List<Track> getTrackList() {
			Cursor cursor = getCursor();
			List<Track> trackList = null;
			if (cursor != null && cursor.moveToFirst()) {
				trackList = new ArrayList<Track>(cursor.getCount());
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