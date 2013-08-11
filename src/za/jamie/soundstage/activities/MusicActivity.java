package za.jamie.soundstage.activities;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.MenuDrawer.Type;
import net.simonvt.menudrawer.Position;
import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.musicplayer.MusicPlayerFragment;
import za.jamie.soundstage.fragments.musicplayer.PlayQueueFragment;
import za.jamie.soundstage.service.MusicConnection;
import za.jamie.soundstage.service.MusicService;
import za.jamie.soundstage.service.MusicService.LocalBinder;
import za.jamie.soundstage.utils.AppUtils;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

public class MusicActivity extends Activity implements MenuDrawer.OnDrawerStateChangeListener {
	
	private static final String TAG_PLAYER = "player";
	private static final String TAG_PLAY_QUEUE = "play_queue";
	private static final String STATE_MENUDRAWER = "menudrawer";
	
	public static final String EXTRA_OPEN_DRAWER = "extra_open_drawer";
	
	private ImageButton mPlayQueueButton;
	
	private Vibrator mVibrator;
	private static final long VIBRATION_LENGTH = 15;
	
	protected MenuDrawer mMenuDrawer;
	
	private MusicPlayerFragment mPlayer;
	private PlayQueueFragment mPlayQueue;
	
	//private final MusicConnection mConnection = new MusicConnection();
	private MusicService mService;
	private boolean mBound;
	private final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;			
		}
		
	};
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up the menu drawer to display the player
		mMenuDrawer = MenuDrawer.attach(this, Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
		mMenuDrawer.setMenuSize(getResources().getDimensionPixelSize(R.dimen.menudrawer_width));
		mMenuDrawer.setMenuView(R.layout.menudrawer_frame);
		mMenuDrawer.setDropShadow(R.drawable.menudrawer_shadow);
		mMenuDrawer.setOnDrawerStateChangeListener(this);
		
		if (getIntent().getBooleanExtra(EXTRA_OPEN_DRAWER, false)) {
			mMenuDrawer.openMenu();
		}

		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		// Initialize the music player fragment
		final FragmentManager fm = getFragmentManager();
		mPlayer = (MusicPlayerFragment) fm.findFragmentByTag(TAG_PLAYER);
		if (mPlayer == null) {
			mPlayer = new MusicPlayerFragment();
			fm.beginTransaction()
				.add(R.id.menu_frame, mPlayer, TAG_PLAYER)
				.commit();
		}
		
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
		
		Intent serviceIntent = new Intent(this, MusicService.class);
		startService(serviceIntent);
		bindService(serviceIntent, mConnection, Context.BIND_ABOVE_CLIENT);
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
    protected void onResume() {
        super.onResume();
        if (mBound) {
        	mService.hideNotification();
        }
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
		if (newState == MenuDrawer.STATE_CLOSED || oldState == MenuDrawer.STATE_CLOSED) {
			mVibrator.vibrate(VIBRATION_LENGTH);
		}
	}
	
	@Override
	public void onDrawerSlide(float openRatio, int offsetPixels) {
		// Complete interface
	}
	
	protected void showNotification() {
		if (mBound) {
			mService.showNotification(getNotificationIntent());
		}
	}
	
	/**
	 * Gets the {@link MusicConnection} instance associated with this MusicActivity that can
	 * be used to access the MusicService's controls.
	 * @return The connection to the MusicService
	 */
	public MusicService getMusicService() {
		if (mBound) {
			return mService;
		}
		return null;
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
		// TODO
		return null;
	}

}
