package za.jamie.soundstage.fragments.albumbrowser;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.albumbrowser.AlbumTrackListAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.models.AlbumStatistics;
import za.jamie.soundstage.providers.MusicLoaders;
import za.jamie.soundstage.utils.AppUtils;

public class AlbumTrackListFragment extends TrackListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

	private static final String EXTRA_ALBUM_ID = "extra_album_id";
	
	private long mAlbumId;
	
	private AlbumTrackListAdapter mAdapter;
	
	private AlbumStatisticsCallback mCallback;
	
	// Header view (the album stats fragment) is the list header in portrait
	private View mStatsHeader;
	
	private FlippingViewHelper mFlipHelper;
	
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
				R.layout.list_item_album_track, R.layout.list_item_header, null, 0);
		
		ViewFlipper flipper = new ViewFlipper(getActivity(), R.id.list_item, R.id.flipped_view);
		mFlipHelper = new FlippingViewHelper(getMusicActivity(), flipper);
		mAdapter.setFlippingViewHelper(mFlipHelper);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
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
		final ListView lv = getListView();
		if (mStatsHeader != null) {
			setListAdapter(null);
			lv.addHeaderView(mStatsHeader);
		}
		setListAdapter(mAdapter);
		mFlipHelper.initFlipper(lv);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (AlbumStatisticsCallback) activity;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
        if (mStatsHeader != null) {
			position--;
        }
        super.onListItemClick(l, v, mAdapter.getCursorPosition(position), id);
	}
	
	public AlbumStatistics getAlbumStatistics() {
		final Cursor cursor = mAdapter.getCursor();
		if (cursor != null && cursor.moveToFirst()) {			
			// Get all the column indexes we need
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
				builder.addArtist(cursor.getLong(artistIdColIdx),
							cursor.getString(artistColIdx))
						.addDuration(cursor.getLong(durationColIdx))
						.addYear(cursor.getInt(yearColIdx));
				
			} while (cursor.moveToNext());
			
			return builder.create();
		}
		return null;
	}

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return MusicLoaders.albumSongs(getActivity(), mAlbumId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (!mAdapter.isEmpty() && mCallback != null) {
            mCallback.deliverAlbumStatistics(getAlbumStatistics());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

	
	/**
	 * Interface for delivery of collected album statistics. The Activity that adds this fragment
	 * must implement this interface.
	 *
	 */
	public interface AlbumStatisticsCallback {
		void deliverAlbumStatistics(AlbumStatistics stats);
	}

}
