package za.jamie.soundstage.fragments.artistbrowser;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.adapters.artistbrowser.ArtistAlbumListAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.MusicListFragment;
import za.jamie.soundstage.providers.MusicLoaders;

public class ArtistAlbumListFragment extends MusicListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

	private static final String EXTRA_ARTIST_ID = "extra_artist_id";

	private ArtistAlbumListAdapter mAdapter;
	private View mSpacerView;

	private FlippingViewHelper mFlipHelper;

	private long mArtistId;

	public static ArtistAlbumListFragment newInstance(long artistId) {
		Bundle args = new Bundle();
		args.putLong(EXTRA_ARTIST_ID, artistId);

		ArtistAlbumListFragment frag = new ArtistAlbumListFragment();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mAdapter = new ArtistAlbumListAdapter(getActivity(),
        		R.layout.list_item_artist_album, null, 0);

        ViewFlipper flipper = new ViewFlipper(getActivity(), R.id.list_item, R.id.flipped_view);
        mFlipHelper = new FlippingViewHelper((MusicActivity) getActivity(), flipper);
        mAdapter.setFlippingViewHelper(mFlipHelper);

        mArtistId = getArguments().getLong(EXTRA_ARTIST_ID);
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

		mSpacerView = inflater.inflate(R.layout.list_item_spacer, null, false);

		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setListAdapter(null);
		final ListView lv = getListView();
		lv.addHeaderView(mSpacerView);
		setListAdapter(mAdapter);
		mFlipHelper.initFlipper(lv);
	}

	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		final Uri data = ContentUris.withAppendedId(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
		final Intent intent = new Intent(Intent.ACTION_VIEW)
			.setDataAndType(data, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE);

		startActivity(intent);
	}

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return MusicLoaders.artistAlbums(getActivity(), mArtistId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
