package com.jamie.play.fragments.artistbrowser;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jamie.play.R;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.fragments.ImageDialogFragment;
import com.jamie.play.loaders.ArtistSummaryLoader;
import com.jamie.play.models.ArtistSummary;
import com.jamie.play.utils.ImageUtils;
import com.jamie.play.utils.TextUtils;

public class ArtistSummaryFragment extends Fragment implements 
		LoaderManager.LoaderCallbacks<ArtistSummary> {

	private static final String EXTRA_ARTIST_ID = "extra_artist_id";
	
	private static final String TAG_IMAGE_DIALOG = "tag_image_dialog";
	
	private long mArtistId;
	
	private TextView mNumAlbumsText;
	private TextView mNumTracksText;
	private TextView mDurationText;
	private ImageView mArtistImage;
	
	private ImageFetcher mImageWorker;
	
	public static ArtistSummaryFragment newInstance(long artistId) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ARTIST_ID, artistId);
		
		final ArtistSummaryFragment frag = new ArtistSummaryFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mArtistId = getArguments().getLong(EXTRA_ARTIST_ID);
		
		// Get the image worker... can't load artwork until view inflated
		mImageWorker = ImageUtils.getImageFetcher(getActivity());
		
		getLoaderManager().initLoader(0, null, this);
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
		
		final View view = inflater.inflate(R.layout.fragment_artist_summary, container,
				false);
		
		mNumAlbumsText = (TextView) view.findViewById(R.id.artistAlbums);
		mNumTracksText = (TextView) view.findViewById(R.id.artistTracks);
		mDurationText = (TextView) view.findViewById(R.id.artistDuration);
		mArtistImage = (ImageView) view.findViewById(R.id.artistThumb);
		
		mImageWorker.loadArtistImage(mArtistId, mArtistImage);
		mArtistImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final DialogFragment frag = ImageDialogFragment
						.newInstance(mArtistId + ImageFetcher.ARTIST_SUFFIX);
				
				frag.show(getFragmentManager(), TAG_IMAGE_DIALOG);
			}
		});
		
		return view;
	}
	
	private void loadSummary(ArtistSummary summary) {
		final Resources res = getResources();
		mNumAlbumsText.setText(TextUtils.getNumAlbumsText(res, summary.numAlbums));
		mNumTracksText.setText(TextUtils.getNumTracksText(res, summary.numTracks));
		mDurationText.setText(TextUtils.getStatsDurationText(res, summary.duration));
	}

	@Override
	public Loader<ArtistSummary> onCreateLoader(int id, Bundle args) {
		return new ArtistSummaryLoader(getActivity(), mArtistId);
	}

	@Override
	public void onLoadFinished(Loader<ArtistSummary> loader, ArtistSummary data) {
		loadSummary(data);
	}

	@Override
	public void onLoaderReset(Loader<ArtistSummary> loader) {
		return;
	}

}
