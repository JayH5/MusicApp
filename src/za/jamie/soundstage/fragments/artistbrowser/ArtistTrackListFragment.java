package za.jamie.soundstage.fragments.artistbrowser;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.abs.BasicTrackAdapter;
import za.jamie.soundstage.adapters.wrappers.TrackHeaderFooterAdapterWrapper;
import za.jamie.soundstage.fragments.TrackListFragment;
import za.jamie.soundstage.musicstore.CursorManager;
import za.jamie.soundstage.musicstore.MusicStore;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ArtistTrackListFragment extends TrackListFragment {
	
	private static final String EXTRA_ARTIST_ID = "extra_artist_id";
	
	private long mArtistId;
	
	private BasicTrackAdapter mAdapter;
	
	private ArtistTrackListListener mCallback;
	
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
		
		mArtistId = getArguments().getLong(EXTRA_ARTIST_ID);
		
		mAdapter = new ArtistTrackListAdapter(getActivity(), 
				R.layout.list_item_two_line, null, 0);
		
		ListAdapter listAdapter;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			TrackHeaderFooterAdapterWrapper wrapper = 
					new TrackHeaderFooterAdapterWrapper(getActivity(), mAdapter);
        	wrapper.setHeaderViewResource(R.layout.list_item_spacer);
        	wrapper.setFooterViewResource(R.layout.list_item_spacer);
        	wrapper.setNumHeaders(1);
        	wrapper.setNumFooters(1);
        	listAdapter = wrapper;
        } else {
        	listAdapter = mAdapter;
        }
		
		mAdapter.registerDataSetObserver(mDataSetObserver);
		
		setListAdapter(listAdapter);
		
		final CursorManager cm = new CursorManager(getActivity(), mAdapter, 
				MusicStore.Tracks.getArtistTracks(mArtistId));
		
		getLoaderManager().initLoader(0, null, cm);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position + 1, id);
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = (ArtistTrackListListener) activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
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
	
	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			if (mCallback != null) {
				mCallback.onDurationCalculated(calculateDuration());
			}
		}
	};
	
	public interface ArtistTrackListListener {
		public void onDurationCalculated(long duration);
	}
	
	private static class ArtistTrackListAdapter extends BasicTrackAdapter {
		
		public ArtistTrackListAdapter(Context context, int layout, Cursor c, int flags) {
			super(context, layout, c, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView titleText = (TextView) view.findViewById(R.id.title);
			TextView subtitleText = (TextView) view.findViewById(R.id.subtitle);
			
			titleText.setText(cursor.getString(getTitleColIdx()));
			subtitleText.setText(cursor.getString(getAlbumColIdx()));
		}

	}
}
