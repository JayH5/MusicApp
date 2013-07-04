package za.jamie.soundstage.activities;

import java.util.List;

import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.MenuDrawer.Type;
import net.simonvt.menudrawer.Position;
import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.musicplayer.MusicPlayerFragment;
import za.jamie.soundstage.fragments.musicplayer.PlayQueueFragment;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.MusicService;
import za.jamie.soundstage.service.connections.MusicLibraryConnection;
import za.jamie.soundstage.service.connections.MusicNotificationConnection;
import za.jamie.soundstage.service.proxies.MusicLibraryProxy;
import za.jamie.soundstage.service.proxies.MusicNotificationProxy;
import za.jamie.soundstage.service.proxies.MusicPlaybackProxy;
import za.jamie.soundstage.service.proxies.MusicQueueProxy;
import za.jamie.soundstage.utils.AppUtils;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
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
	
	private MenuDrawer mDrawer;
	
	private MusicPlayerFragment mPlayer;
	private PlayQueueFragment mPlayQueue;
	
	private MusicLibraryConnection mLibraryConnection;
	private MusicNotificationConnection mNotificationConnection;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			IMusicService musicService = IMusicService.Stub.asInterface(service);
			
			mLibraryConnection = new MusicLibraryProxy(musicService);
			mNotificationConnection = new MusicNotificationProxy(musicService);
			
			mPlayer.setServiceConnection(new MusicPlaybackProxy(musicService));
			mPlayQueue.setServiceConnection(new MusicQueueProxy(musicService));
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mLibraryConnection = null;
			mNotificationConnection = null;
			
			mPlayer.setServiceConnection(null);
			mPlayQueue.setServiceConnection(null);
		}		
	};
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up the menu drawer to display the player
		mDrawer = MenuDrawer.attach(this, Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);
		mDrawer.setMenuSize(getResources().getDimensionPixelSize(R.dimen.menu_drawer_width));
		mDrawer.setMenuView(R.layout.slidingmenu_frame);
		mDrawer.setDropShadow(R.drawable.slidingmenu_shadow);
		mDrawer.setOnDrawerStateChangeListener(this);
		
		if (getIntent().getBooleanExtra(EXTRA_OPEN_DRAWER, false)) {
			mDrawer.openMenu();
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

	public void setMainContentView(int layoutResId) {
		mDrawer.setContentView(layoutResId);
	}
	
	@Override
    protected void onResume() {
        super.onResume();        
        hideNotification();
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
		final int drawerState = mDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mDrawer.closeMenu();
            return;
        }
		super.onBackPressed();
    }
	
	@Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mDrawer.restoreState(inState.getParcelable(STATE_MENUDRAWER));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MENUDRAWER, mDrawer.saveState());
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
	
	public MenuDrawer getDrawer() {
		return mDrawer;
	}
	
	public boolean isPlaying() {
		return mPlayer.isPlaying();
	}
	
	public void open(List<Track> tracks, int position) {
		if (mLibraryConnection != null) {
			mLibraryConnection.open(tracks, position);
			mDrawer.openMenu();
		}
	}
	
	public void shuffle(List<Track> tracks) {
		if (mLibraryConnection != null) {
			mLibraryConnection.shuffle(tracks);
			mDrawer.openMenu();
		}
	}

	public void enqueue(List<Track> tracks, int action) {
		if (mLibraryConnection != null) {
			mLibraryConnection.enqueue(tracks, action);
		}
	}
	
	public void showNotification() {
		if (mNotificationConnection != null) {
			final ComponentName component = new ComponentName(this, this.getClass());
			final Uri uri = getIntent().getData();
			mNotificationConnection.showNotification(component, uri);
		}
	}
	
	public void hideNotification() {
		if (mNotificationConnection != null) {
			mNotificationConnection.hideNotification();
		}
	}

}
