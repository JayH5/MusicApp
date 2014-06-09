package za.jamie.soundstage.activities;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.artistbrowser.ArtistAlbumListFragment;
import za.jamie.soundstage.fragments.artistbrowser.ArtistSummaryFragment;
import za.jamie.soundstage.fragments.artistbrowser.ArtistTrackListFragment;
import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.utils.AppUtils;
import za.jamie.soundstage.widgets.PagerSlidingTabStrip;

public class ArtistBrowserActivity extends MusicActivity implements
		ArtistSummaryFragment.OnArtistFoundListener,
        ArtistTrackListFragment.ArtistTrackListHost {

	private static final String TAG_SUMMARY_FRAG = "artist_summary";
    private static final String STATE_URI = "state_uri";

	private long mArtistId;
    private String mArtist;
    private Uri mUri;

	private ArtistSummaryFragment mSummaryFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_artist_browser);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_HOME);

		// Set up the view pager and its indicator
		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new SectionsPagerAdapter(
				getFragmentManager()));

		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			AppUtils.loadActionBarTabs(actionBar, viewPager);
		} else {
			PagerSlidingTabStrip indicator = (PagerSlidingTabStrip) findViewById(R.id.tabStrip);
			indicator.setViewPager(viewPager);
		}

		if (savedInstanceState != null) {
            mUri = savedInstanceState.getParcelable(STATE_URI);
        } else {
            mUri = getIntent().getData();
        }
		mArtistId = ContentUris.parseId(mUri);

        // Set up the summary fragment
		final FragmentManager fm = getFragmentManager();
		mSummaryFragment = (ArtistSummaryFragment) fm.findFragmentByTag(TAG_SUMMARY_FRAG);
		if (mSummaryFragment == null) {
			mSummaryFragment = ArtistSummaryFragment.newInstance(mUri);
			fm.beginTransaction()
				.add(R.id.summaryFrame, mSummaryFragment, TAG_SUMMARY_FRAG)
				.commit();
		}
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_URI, mUri);
    }

	@Override
	public boolean navigateUpTo(Intent upIntent) {
		upIntent.putExtra(LibraryActivity.EXTRA_SECTION,
        		LibraryActivity.SECTION_ARTISTS);
        upIntent.putExtra(LibraryActivity.EXTRA_ITEM_ID, mArtistId);

		return super.navigateUpTo(upIntent);
	}

    @Override
    public void playAt(int position) {
        getMusicConnection()
                .open(new MusicItem(mArtistId, mArtist, MusicItem.TYPE_ARTIST), position);
        showPlayer();
    }

    @Override
    public void shuffleAll() {
        getMusicConnection().shuffle(new MusicItem(mArtistId, mArtist, MusicItem.TYPE_ARTIST));
        showPlayer();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return ArtistAlbumListFragment.newInstance(mArtistId);
			case 1:
				return ArtistTrackListFragment.newInstance(mArtistId);
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch(position) {
			case 0:
				return getResources().getString(R.string.library_section_albums);
			case 1:
				return getResources().getString(R.string.library_section_songs);
			}
			return null;
		}

	}

	@Override
	public void onArtistFound(String artist) {
        mArtist = artist;
		getActionBar().setTitle(mArtist);
	}

	@Override
	public void onDurationCalculated(long duration) {
		mSummaryFragment.setDuration(duration);
	}

}
