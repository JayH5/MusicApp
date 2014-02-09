package za.jamie.soundstage.fragments.artistbrowser;

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
import za.jamie.soundstage.activities.MusicActivity;
import za.jamie.soundstage.adapters.artistbrowser.ArtistTrackListAdapter;
import za.jamie.soundstage.adapters.utils.FlippingViewHelper;
import za.jamie.soundstage.animation.ViewFlipper;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.providers.MusicLoaders;
import za.jamie.soundstage.utils.AppUtils;

public class ArtistTrackListFragment extends TrackListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String EXTRA_ARTIST_ID = "extra_artist_id";
	
	private ArtistTrackListAdapter mAdapter;
	
	private ArtistTrackListListener mCallback;
	private View mSpacerView;
	
	private long mArtistId;
	
	private FlippingViewHelper mFlipHelper;
	
	public static ArtistTrackListFragment newInstance(long artistId) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ARTIST_ID, artistId);
		
		final ArtistTrackListFragment frag = new ArtistTrackListFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAdapter = new ArtistTrackListAdapter(getActivity(), 
				R.layout.list_item_two_line, null, 0);		
		mAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				if (mCallback != null) {
					mCallback.onDurationCalculated(calculateDuration());
				}
			}
		});
		
		ViewFlipper flipper = new ViewFlipper(getActivity(), R.id.list_item, R.id.flipped_view);
		mFlipHelper = new FlippingViewHelper(getMusicActivity(), flipper);
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
		if (AppUtils.isLandscape(getResources())) {
			lv.addFooterView(mSpacerView);
		}
		setListAdapter(mAdapter);
		mFlipHelper.initFlipper(lv);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position - 1, id);
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (ArtistTrackListListener) activity;
	}

	private long calculateDuration() {
		final Cursor cursor = mAdapter.getCursor();
		if (cursor != null && cursor.moveToFirst()) {
			long duration = 0;
			int durationColIdx = cursor.getColumnIndexOrThrow(
					MediaStore.Audio.Media.DURATION);
			
			do {
				duration += cursor.getLong(durationColIdx);
			} while (cursor.moveToNext());
			
			return duration;
		}
		return -1;
	}

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return MusicLoaders.artistSongs(getActivity(), mArtistId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    public interface ArtistTrackListListener {
		void onDurationCalculated(long duration);
	}

}
