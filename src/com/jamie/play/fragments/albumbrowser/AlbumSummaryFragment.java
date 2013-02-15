package com.jamie.play.fragments.albumbrowser;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jamie.play.R;
import com.jamie.play.adapters.abs.SummaryAdapter;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.cursormanager.CursorDefinitions;
import com.jamie.play.fragments.ImageDialogFragment;
import com.jamie.play.utils.ImageUtils;
import com.jamie.play.utils.TextUtils;

public class AlbumSummaryFragment extends Fragment implements 
		LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String EXTRA_ALBUM_ID = "extra_album_id";
	private static final String TAG_IMAGE_DIALOG = "tag_image_dialog";
	
	private static final int LOADER_ID = 0;
	
	private ImageFetcher mImageWorker;
	
	private TextView mNumTracksText;
	private TextView mDurationText;
	private TextView mYearsText;
	
	private ImageView mAlbumArt;
	
	private SummaryAdapter mAdapter;
	
	private long mAlbumId;
	
	//private long mArtistId = -1;

	public static AlbumSummaryFragment newInstance(long albumId) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ALBUM_ID, albumId);
		
		final AlbumSummaryFragment frag = new AlbumSummaryFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAlbumId = getArguments().getLong(EXTRA_ALBUM_ID);
		
		// Get the image worker... can't load artwork until view inflated
		mImageWorker = ImageUtils.getImageFetcher(getActivity());
		
		mAdapter = new AlbumSummaryAdapter(getActivity(), null);
		
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mImageWorker.setExitTasksEarly(false);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mImageWorker.setExitTasksEarly(true);
	}
	
	@Override
	public View onCreateView(
			LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		final View view = inflater.inflate(R.layout.fragment_album_summary, container, 
				false);
		
		mNumTracksText = (TextView) view.findViewById(R.id.albumTracks);
		mDurationText = (TextView) view.findViewById(R.id.albumDuration);
		mYearsText = (TextView) view.findViewById(R.id.albumYear);
		mAlbumArt = (ImageView) view.findViewById(R.id.albumThumb);
		
		mImageWorker.loadImage(String.valueOf(mAlbumId), mAlbumArt);
		mAlbumArt.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final DialogFragment frag = ImageDialogFragment
						.newInstance(String.valueOf(mAlbumId));
				frag.show(getFragmentManager(), TAG_IMAGE_DIALOG);
			}
		});
		
		final ImageButton browseArtistButton = (ImageButton) view
				.findViewById(R.id.browse_artist_button);
		final ImageButton addToQueueButton = (ImageButton) view
				.findViewById(R.id.add_to_queue_button);
		
		browseArtistButton.setOnClickListener(mBrowseArtistListener);		
		addToQueueButton.setOnClickListener(mAddToQueueListener);
		
		return view;
	}
	
	private View.OnClickListener mAddToQueueListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private View.OnClickListener mBrowseArtistListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			/*final Intent i = new Intent(getActivity(), ArtistBrowserActivity.class);
			i.putExtra(ArtistBrowserActivity.EXTRA_ARTIST_ID, mArtistId);
			i.putExtra(ArtistBrowserActivity.EXTRA_ARTIST, mArtist);
			
			startActivity(i);*/
		}
	};

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return CursorDefinitions.getAlbumBrowserCursorParams(mAlbumId)
				.getCursorLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
		case LOADER_ID:
			mAdapter.swapCursor(data);
			break;
		default:
			break;
		}		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case LOADER_ID:
			mAdapter.swapCursor(null);
			break;
		default:
			break;
		}		
	}
	
	private class AlbumSummaryAdapter extends SummaryAdapter {

		public AlbumSummaryAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void loadSummary(Context context, Cursor cursor) {
			// Get the column indexes
			int durationColIdx = 
					cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
			int yearColIdx =
					cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR);
			/*int artistIdColIdx =
					cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
			int artistColIdx =
					cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);*/
						
			int numTracks = cursor.getCount();
						
			long duration = 0;
			int firstYear = Integer.MAX_VALUE;
			int lastYear = 0;
			if (cursor.moveToFirst()) {
				do {
					// Add up the duration
					duration += cursor.getLong(durationColIdx);
					
					// Get the first/last year
					int year = cursor.getInt(yearColIdx);
					firstYear = Math.min(year, firstYear);
					lastYear = Math.max(year, lastYear);
								
					// Maybe there is an easier way of getting the artist id
					/*if (mArtistId < 0) {
						long artistId = cursor.getLong(artistIdColIdx);
						String artist = cursor.getString(artistColIdx);
						if (artist.equals(mArtist)) {
							mArtistId = artistId;
						}
					}*/
								
				} while (cursor.moveToNext());
			}
						
			final Resources res = context.getResources();
			mNumTracksText.setText(TextUtils.getNumTracksText(res, numTracks));
			mDurationText.setText(TextUtils.getStatsDurationText(res, duration));			
			mYearsText.setText(TextUtils.getYearText(res, firstYear, lastYear));
		}
		
	}
	
}