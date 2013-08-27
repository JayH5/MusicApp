package za.jamie.soundstage.fragments.albumbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.BasicTrackAdapter;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import za.jamie.soundstage.utils.AppUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class AlbumTrackListFragment extends TrackListFragment {

	private static final String EXTRA_ALBUM_ID = "extra_album_id";
	
	private long mAlbumId;
	
	private AlbumTrackListAdapter mAdapter;
	
	private AlbumStatisticsCallback mCallback;
	
	private View mStatsHeader;
	
	public static AlbumTrackListFragment newInstance(long albumId) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ALBUM_ID, albumId);
		
		AlbumTrackListFragment frag = new AlbumTrackListFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAlbumId = getArguments().getLong(EXTRA_ALBUM_ID);
				
		mAdapter = new AlbumTrackListAdapter(getActivity(), 
				R.layout.list_item_track, null, 0);
		
		mAdapter.registerDataSetObserver(mDataSetObserver);
		
		final CursorManager cm = new CursorManager(getActivity(), mAdapter, 
				MusicStore.Tracks.getAlbumTracks(mAlbumId));
		
		getLoaderManager().initLoader(0, null, cm);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		
		if (AppUtils.isPortrait(getResources())) {
			mStatsHeader = inflater.inflate(R.layout.list_item_album_summary, null, false);
		}
		
		return v;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (mStatsHeader != null) {
			setListAdapter(null);
			getListView().addHeaderView(mStatsHeader);
		}
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (AlbumStatisticsCallback) activity;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mStatsHeader != null) {
			super.onListItemClick(l, v, position - 1, id);
		} else {
			super.onListItemClick(l, v, position, id);
		}
	}
	
	public AlbumStatistics getAlbumStatistics() {
		final Cursor cursor = mAdapter.getCursor();
		if (cursor != null && cursor.moveToFirst()) {			
			// Get all the column indexes we need
			int artistKeyColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_KEY);
			int yearColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR);
			int artistIdColIdx = mAdapter.getArtistIdColIdx();
			int artistColIdx = mAdapter.getArtistColIdx();
			int durationColIdx = mAdapter.getDurationColIdx();
			
			// Create a stats builder, add album title and number of tracks
			AlbumStatistics.Builder builder = new AlbumStatistics.Builder(mAlbumId)
					.setTitle(cursor.getString(mAdapter.getAlbumColIdx()))
					.setNumTracks(cursor.getCount());
			
			// Iterate through the cursor collecting artists, duration and year
			do {
				builder.addArtist(cursor.getString(artistKeyColIdx),
							cursor.getLong(artistIdColIdx),
							cursor.getString(artistColIdx))
						.addDuration(cursor.getLong(durationColIdx))
						.addYear(cursor.getInt(yearColIdx));
				
			} while (cursor.moveToNext());
			
			return builder.create();
		}
		return null;
	}
	
	public interface AlbumStatisticsCallback {
		public void deliverAlbumStatistics(AlbumStatistics stats);
	}
	
	private DataSetObserver mDataSetObserver = new DataSetObserver() {		
		@Override
		public void onChanged() {
			if (mCallback != null) {
				mCallback.deliverAlbumStatistics(getAlbumStatistics());
			}
		}
	};
	
	private static class AlbumTrackListAdapter extends BasicTrackAdapter {

		private int mTrackNumColIdx;
		private boolean mIsCompilation = false;
		
		public AlbumTrackListAdapter(Context context, int layout, Cursor c,
				int flags) {
			
			super(context, layout, c, flags);
		}

		@Override
		protected void getColumnIndices(Cursor cursor) {
			super.getColumnIndices(cursor);
			mTrackNumColIdx = cursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
			
			// Search to determine if there is more than one artist
			if (cursor.moveToFirst()) {
				final long artistId = cursor.getLong(getArtistIdColIdx());
				while (cursor.moveToNext()) {
					if (artistId != cursor.getLong(getArtistIdColIdx())) {
						mIsCompilation = true;
						break;
					}
				}
			}
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {			
			TextView titleText = (TextView) view.findViewById(R.id.title);
			TextView subtitleText = (TextView) view.findViewById(R.id.subtitle);
			TextView trackNumText = (TextView) view.findViewById(R.id.trackNumber);
			
			titleText.setText(cursor.getString(getTitleColIdx()));
			
			if (mIsCompilation) {
				subtitleText.setText(cursor.getString(getArtistColIdx()));
			} else {
				long duration = cursor.getLong(getDurationColIdx());
				subtitleText.setText(TextUtils.getTrackDurationText(duration));
			}
			
			int trackNum = cursor.getInt(mTrackNumColIdx);
			trackNumText.setText(TextUtils.getTrackNumText(trackNum));
		}
		
	}
}
