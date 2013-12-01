package za.jamie.soundstage.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.musicplayer.MusicPlayerFragment;
import za.jamie.soundstage.fragments.musicplayer.PlayQueueFragment;
import za.jamie.soundstage.service.MusicConnection;
import za.jamie.soundstage.service.MusicConnection.ConnectionCallbacks;
import za.jamie.soundstage.service.MusicService;
import za.jamie.soundstage.utils.AppUtils;
import za.jamie.soundstage.widgets.MenuDrawer2;

public class MusicActivity extends Activity implements SlidingPaneLayout.PanelSlideListener {

	private static final String TAG_PLAY_QUEUE = "play_queue";
	
	private static final String ACTION_SHOW_PLAYER = "za.jamie.soundstage.ACTION_SHOW_PLAYER";

    private MenuDrawer2 mMenuDrawer;
    private boolean mPanelClosed = true;
	
	private MusicPlayerFragment mPlayer;
	private PlayQueueFragment mPlayQueue;
	
	private final MusicConnection mConnection = new MusicConnection();
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mMenuDrawer = MenuDrawer2.attach(this);
        mMenuDrawer.setShadowResource(R.drawable.menudrawer_shadow);
        mMenuDrawer.setPanelSlideListener(this);

        final Resources res = getResources();
        mMenuDrawer.setParallaxDistance(res.getDimensionPixelOffset(R.dimen.menudrawer_parallax));
        mMenuDrawer.setCoveredFadeColor(res.getColor(R.color.blackish));
        mMenuDrawer.setSliderFadeColor(res.getColor(android.R.color.transparent));

        int paneWidth = res.getDisplayMetrics().widthPixels
                - res.getDimensionPixelOffset(R.dimen.menudrawer_offset);
        mMenuDrawer.setMenuWidth(paneWidth);
        mMenuDrawer.setMenuView(R.layout.menudrawer_frame);
		
		// Initialize the music player fragment
		final FragmentManager fm = getFragmentManager();
		mPlayer = (MusicPlayerFragment) fm.findFragmentById(R.id.player);
		
		mPlayQueue = (PlayQueueFragment) fm.findFragmentByTag(TAG_PLAY_QUEUE);
		if (mPlayQueue == null) {
			mPlayQueue = PlayQueueFragment.newInstance();
		}
		
		ImageButton playQueueButton = (ImageButton) findViewById(R.id.play_queue_button);
		playQueueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPlayQueue.show(fm, TAG_PLAY_QUEUE);
			}
		});
		
		mConnection.requestConnectionCallbacks(new ConnectionCallbacks() {
			@Override
			public void onConnected() {
				if (!AppUtils.isApplicationSentToBackground(MusicActivity.this)) {
					mConnection.hideNotification();
				}
			}

			@Override
			public void onDisconnected() { }			
		});
		
		Intent serviceIntent = new Intent(this, MusicService.class);
		startService(serviceIntent);
		bindService(serviceIntent, mConnection, 0);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (ACTION_SHOW_PLAYER.equals(intent.getAction())) {
			//mMenuDrawer.openMenu();
            mMenuDrawer.openPane();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mConnection.hideNotification();
	}
	
	@Override
    protected void onPause() {
        super.onPause();        
        if (isPlaying() && AppUtils.isApplicationSentToBackground(this)) {
        	showNotification();
        }
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mConnection);
	}
	
	@Override
    public void onBackPressed() {
        if (!mPanelClosed) {
            mMenuDrawer.closePane();
            return;
        }
		super.onBackPressed();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.search, menu);
    	
    	SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        
        return true;
    }
	
	protected void showNotification() {
		mConnection.showNotification(getNotificationIntent());
	}
	
	/**
	 * Gets the {@link MusicConnection} instance associated with this MusicActivity that can
	 * be used to access the MusicService's controls.
	 * @return The connection to the MusicService
	 */
	public MusicConnection getMusicConnection() {
		return mConnection;
	}

    public boolean playerHidden() {
        return mPanelClosed;
    }
	
	/**
	 * Open the drawer with the player in it
	 */
	public void showPlayer() {
        mMenuDrawer.openPane();
	}
	
	/**
	 * Close the drawer with the player in it
	 */
	public void hidePlayer() {
        mMenuDrawer.closePane();
	}
	
	/**
	 * Check with the player if anything is currently playing.
	 * @return true if something is playing
	 */
	public boolean isPlaying() {
		return mPlayer.isPlaying();
	}
	
	private PendingIntent getNotificationIntent() {
		// Bring back activity as it was with player showing
		Intent intent = new Intent(this, this.getClass());
		intent.setAction(ACTION_SHOW_PLAYER)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		return PendingIntent.getActivity(this, 0, intent, 0);
	}

    @Override
    public void onPanelSlide(View view, float v) {
        if (mPanelClosed) {
            mMenuDrawer.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            mPanelClosed = false;
        }
    }

    @Override
    public void onPanelOpened(View view) {
        // Nothing to do...
    }

    @Override
    public void onPanelClosed(View view) {
        mMenuDrawer.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        mPanelClosed = true;
    }
}
