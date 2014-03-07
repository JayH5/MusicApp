package za.jamie.soundstage.fragments.playlistbrowser;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.PlaylistTrackAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.models.PlaylistStatistics;
import za.jamie.soundstage.providers.MusicLoaders;
import za.jamie.soundstage.utils.AppUtils;

public class PlaylistTrackListFragment extends TrackListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

	//private static final String TAG = "TrackListFragment";
    private static final String ARG_PLAYLIST_ID = "arg_playlist_id";
	
	private PlaylistTrackAdapter mAdapter;
	private long mPlaylistId;

    private PlaylistStatisticsCallback mCallback;

    private View mStatsHeader;

    private FlippingViewHelper mFlipHelper;
	
	public static PlaylistTrackListFragment newInstance(long playlistId) {		
		final Bundle args = new Bundle();
		args.putLong(ARG_PLAYLIST_ID, playlistId);
		
		PlaylistTrackListFragment frag = new PlaylistTrackListFragment();
		frag.setArguments(args);
		return frag;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (PlaylistStatisticsCallback) activity;
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAdapter = new PlaylistTrackAdapter(getActivity(), 
				R.layout.list_item_two_line, null, 0);
		
		setListAdapter(mAdapter);
		
		mPlaylistId = getArguments().getLong(ARG_PLAYLIST_ID);

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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return MusicLoaders.playlistSongs(getActivity(), mPlaylistId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (!mAdapter.isEmpty() && mCallback != null) {
            mCallback.deliverPlaylistStatistics(getPlaylistStatistics());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        if (AppUtils.isPortrait(getResources())) {
            mStatsHeader = inflater.inflate(R.layout.list_item_playlist_summary, null, false);
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

    public PlaylistStatistics getPlaylistStatistics() {
        final Cursor cursor = mAdapter.getCursor();
        if (cursor != null && cursor.moveToFirst()) {
            // Get all the column indexes we need
            int albumIdColIdx = mAdapter.getAlbumIdColIdx();
            int albumColIdx = mAdapter.getAlbumColIdx();
            int artistIdColIdx = mAdapter.getArtistIdColIdx();
            int artistColIdx = mAdapter.getArtistColIdx();
            int durationColIdx = mAdapter.getDurationColIdx();

            // Create a stats builder, add album title and number of tracks
            PlaylistStatistics.Builder builder = new PlaylistStatistics.Builder()
                    .setNumTracks(cursor.getCount());

            // Iterate through the cursor collecting artists, duration and year
            do {
                String artist = cursor.getString(artistColIdx);
                builder.addAlbum(cursor.getLong(albumIdColIdx), cursor.getString(albumColIdx), artist)
                        .addArtist(cursor.getLong(artistIdColIdx), artist)
                        .addDuration(cursor.getLong(durationColIdx));

            } while (cursor.moveToNext());

            return builder.create();
        }
        return null;
    }

    /**
     * Interface for delivery of collected playlist statistics. The Activity that adds this fragment
     * must implement this interface.
     */
    public interface PlaylistStatisticsCallback {
        void deliverPlaylistStatistics(PlaylistStatistics stats);
    }

}
