package za.jamie.soundstage.fragments.albumbrowser;

import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.activities.ArtistBrowserActivity;
import za.jamie.soundstage.bitmapfun.ImageFetcher;
import za.jamie.soundstage.fragments.ImageDialogFragment;
import za.jamie.soundstage.loaders.AlbumSummaryLoader;
import za.jamie.soundstage.models.AlbumSummary;
import za.jamie.soundstage.utils.ImageUtils;
import za.jamie.soundstage.utils.TextUtils;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
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

public class AlbumSummaryFragment extends Fragment implements 
		LoaderManager.LoaderCallbacks<AlbumSummary> {
	
	private static final String EXTRA_ALBUM_ID = "extra_album_id";
	private static final String TAG_IMAGE_DIALOG = "tag_image_dialog";
	
	private static final int LOADER_ID = 0;
	
	private ImageFetcher mImageWorker;
	
	private TextView mNumTracksText;
	private TextView mDurationText;
	private TextView mYearsText;
	
	private ImageView mAlbumArt;
	
	//private SummaryAdapter mAdapter;
	
	private long mAlbumId;
	
	private List<String> mArtists;
	private List<Long> mArtistIds;

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
		
		//mAdapter = new AlbumSummaryAdapter(getActivity(), null);
		
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
		
		browseArtistButton.setOnClickListener(mArtistButtonListener);		
		addToQueueButton.setOnClickListener(mAddToQueueListener);
		
		return view;
	}
	
	private View.OnClickListener mAddToQueueListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
	};

	@Override
	public Loader<AlbumSummary> onCreateLoader(int id, Bundle args) {
		/*return CursorDefinitions.getAlbumBrowserCursorParams(mAlbumId)
				.getCursorLoader(getActivity());*/
		return new AlbumSummaryLoader(getActivity(), mAlbumId);
	}

	@Override
	public void onLoadFinished(Loader<AlbumSummary> loader, AlbumSummary data) {
		switch (loader.getId()) {
		case LOADER_ID:
			//mAdapter.swapCursor(data);
			loadSummary(data);
			break;
		default:
			break;
		}		
	}

	@Override
	public void onLoaderReset(Loader<AlbumSummary> loader) {
		return;
	}
	
	private void loadSummary(AlbumSummary summary) {
		final Resources res = getResources();
		mNumTracksText.setText(TextUtils.getNumTracksText(res, summary.numTracks));
		mDurationText.setText(TextUtils.getStatsDurationText(res, summary.duration));			
		mYearsText.setText(TextUtils.getYearText(res, summary.firstYear, summary.lastYear));
		
		mArtists = summary.artists;
		mArtistIds = summary.artistIds;
	}
	
	private void launchArtistBrowser(String artist, long artistId) {
		final Intent intent = new Intent(getActivity(), ArtistBrowserActivity.class);
		intent.putExtra(ArtistBrowserActivity.EXTRA_ARTIST, artist);
		intent.putExtra(ArtistBrowserActivity.EXTRA_ARTIST_ID, artistId);
		
		startActivity(intent);
	}
	
	private View.OnClickListener mArtistButtonListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (mArtists.isEmpty()) {
				return;
			}
			if (mArtists.size() == 1) {
				launchArtistBrowser(mArtists.get(0), mArtistIds.get(0));
			} else {
				final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setItems(mArtists.toArray(new String[mArtists.size()]), mArtistDialogListener)
					.setTitle("Browse artists")
					.show();
			}
		}
	};
	
	private DialogInterface.OnClickListener mArtistDialogListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			launchArtistBrowser(mArtists.get(which), mArtistIds.get(which));
		}
	};
	
}