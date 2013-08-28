package za.jamie.soundstage.activities;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.MenuDrawer.Type;
import net.simonvt.menudrawer.Position;
import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.musicplayer.MusicPlayerFragment;
import za.jamie.soundstage.fragments.musicplayer.PlayQueueFragment;
import za.jamie.soundstage.service.MusicConnection;
import za.jamie.soundstage.service.MusicConnection.ConnectionCallbacks;
import za.jamie.soundstage.service.MusicService;
import za.jamie.soundstage.utils.AppUtils;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

public class MusicActivity extends Activity implements MenuDrawer.OnDrawerStateChangeListener {
	
	//private static final String TAG_PLAYER = "player";
	private static final String TAG_PLAY_QUEUE = "play_queue";
	private static final String STATE_MENUDRAWER = "menudrawer";
	
	private static final String ACTION_SHOW_PLAYER = "za.jamie.soundstage.ACTION_SHOW_PLAYER";
	
	private ImageButton mPlayQueueButton;
	
	private Vibrator mVibrator;
	private static final long VIBRATION_LENGTH = 15;
	
	protected MenuDrawer mMenuDrawer;
	
	private MusicPlayerFragment mPlayer;
	private PlayQueueFragment mPlayQueue;
	
	private final MusicConnection mConnection = new MusicConnection();
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up the menu drawer to display the player
		mMenuDrawer = MenuDrawer.attach(this, Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
		
		// Have to set offset... kind of a pain
		final Resources res = getResources();
		int menuSize = res.getDisplayMetrics().widthPixels
				- res.getDimensionPixelOffset(R.dimen.menudrawer_offset);
		mMenuDrawer.setMenuSize(menuSize);		
		mMenuDrawer.setMenuView(R.layout.menudrawer_frame);
		mMenuDrawer.setDropShadow(R.drawable.menudrawer_shadow);
		mMenuDrawer.setOnDrawerStateChangeListener(this);

		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		// Initialize the music player fragment
		final FragmentManager fm = getFragmentManager();
		mPlayer = (MusicPlayerFragment) fm.findFragmentById(R.id.player);
		
		mPlayQueue = (PlayQueueFragment) fm.findFragmentByTag(TAG_PLAY_QUEUE);
		if (mPlayQueue == null) {
			mPlayQueue = PlayQueueFragment.newInstance();
		}
		
		
		mPlayQueueButton = (ImageButton) findViewById(R.id.play_queue_button);
		mPlayQueueButton.setOnClickListener(new View.OnClickListener() {			
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

	/**
	 * Set the content view for this activity. Use this instead of {@link Activity#setContentView(int)}
	 * so that the {@link MenuDrawer} can manage content properly.
	 * @param layoutResId
	 */
	public void setMainContentView(int layoutResId) {
		mMenuDrawer.setContentView(layoutResId);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (ACTION_SHOW_PLAYER.equals(intent.getAction())) {
			mMenuDrawer.openMenu();
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
		final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        }
		super.onBackPressed();
    }
	
	@Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mMenuDrawer.restoreState(inState.getParcelable(STATE_MENUDRAWER));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MENUDRAWER, mMenuDrawer.saveState());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.search, menu);
    	
    	SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        
        return true;
    }

	@Override
	public void onDrawerStateChange(int oldState, int newState) {		
		if (mVibrator != null && 
				(newState == MenuDrawer.STATE_CLOSED || oldState == MenuDrawer.STATE_CLOSED)) {
			mVibrator.vibrate(VIBRATION_LENGTH);
		}
	}
	
	@Override
	public void onDrawerSlide(float openRatio, int offsetPixels) {
		// Complete interface
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
	
	/**
	 * Open the drawer with the player in it
	 */
	public void showPlayer() {
		mMenuDrawer.openMenu();
	}
	
	/**
	 * Close the drawer with the player in it
	 */
	public void hidePlayer() {
		mMenuDrawer.closeMenu();
	}
	
	/**
	 * Check with the player if anything is currently playing.
	 * @return true if something is playing
	 */
	public boolean isPlaying() {
		return mPlayer.isPlaying();
	}
	
	private PendingIntent getNotificationIntent() {
		// Bring back activity as is was with player showing
		Intent intent = Intent.makeMainActivity(
				new ComponentName(this, this.getClass()));
		intent.setAction(ACTION_SHOW_PLAYER)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			.fillIn(getIntent(), 0);
		
		return PendingIntent.getActivity(this, 0, intent, 0);
	}

}
