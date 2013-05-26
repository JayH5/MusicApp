package za.jamie.soundstage.activities;

import java.util.List;

import net.simonvt.menudrawer.MenuDrawer;
import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.IMusicStatusCallback;
import za.jamie.soundstage.IPlayQueueCallback;
import za.jamie.soundstage.MusicLibraryWrapper;
import za.jamie.soundstage.MusicPlaybackWrapper;
import za.jamie.soundstage.MusicQueueWrapper;
import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.musicplayer.MusicPlayerFragment;
import za.jamie.soundstage.fragments.musicplayer.PlayQueueFragment;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.MusicService;
import za.jamie.soundstage.utils.AppUtils;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

public class MusicActivity extends FragmentActivity implements MusicLibraryWrapper,
		MusicQueueWrapper, MusicPlaybackWrapper, MenuDrawer.OnDrawerStateChangeListener {
	
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
	
	private IMusicService mService = null;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IMusicService.Stub.asInterface(service);
			
			mPlayer.onServiceConnected();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}		
	};
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set up the menu drawer to display the player
		mDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
		mDrawer.setMenuView(R.layout.slidingmenu_frame);
		mDrawer.setDropShadow(R.drawable.slidingmenu_shadow);
		mDrawer.setOnDrawerStateChangeListener(this);

		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		// Initialize the music player fragment
		final FragmentManager fm = getSupportFragmentManager();
		mPlayer = (MusicPlayerFragment) fm.findFragmentByTag(TAG_PLAYER);
		if (mPlayer == null) {
			mPlayer = new MusicPlayerFragment();
			fm.beginTransaction()
				.add(R.id.menu_frame, mPlayer, TAG_PLAYER)
				.commit();
		}
		
		mPlayQueueButton = (ImageButton) findViewById(R.id.play_queue_button);
		mPlayQueueButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final FragmentManager fm = getSupportFragmentManager();
				if (fm.findFragmentByTag(TAG_PLAY_QUEUE) == null) {
					mPlayQueue = PlayQueueFragment.newInstance();
					mPlayQueue.show(fm, TAG_PLAY_QUEUE);
				}
			}
		});
		
		Intent serviceIntent = new Intent(this, MusicService.class);
		startService(serviceIntent);
		bindService(serviceIntent, mConnection, 0);
	}

	public void setMainContentView(int layoutResId) {
		mDrawer.setContentView(layoutResId);
	}
	
	@Override
    protected void onResume() {
        super.onResume();        
        if (mService != null) {
        	showNotification(false);
        }
        // TODO
        /*if (getIntent().getBooleanExtra(EXTRA_OPEN_DRAWER, false)) {
			mDrawer.openMenu();
		}*/
	}
	
	@Override
    protected void onPause() {
        super.onPause();        
        if (isPlaying() && AppUtils.isApplicationSentToBackground(this)) {
        	showNotification(true);
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

	//////////////////////////
	// AIDL interface
	
	@Override
	public void setQueuePosition(int position) {
		try {
			mService.setQueuePosition(position);
		} catch (RemoteException ignored) {

		}
		
	}

	@Override
	public void moveQueueItem(int from, int to) {
		try {
			mService.moveQueueItem(from, to);
		} catch (RemoteException ignored) {
			
		}		
	}

	@Override
	public void removeTrack(int position) {
		try {
			mService.removeTrack(position);
		} catch (RemoteException ignored) {
			
		}		
	}

	@Override
	public void registerMusicStatusCallback(IMusicStatusCallback callback) {
		try {
			mService.registerMusicStatusCallback(callback);
		} catch (RemoteException ignored) {
			
		}		
	}

	@Override
	public void unregisterMusicStatusCallback(IMusicStatusCallback callback) {
		try {
			mService.unregisterMusicStatusCallback(callback);
		} catch (RemoteException ignored) {
			
		}		
	}

	@Override
	public void open(List<Track> tracks, int position) {
		try {
			mService.open(tracks, position);
		} catch (RemoteException ignored) {
			
		}
		mDrawer.openMenu();
	}
	
	@Override
	public void shuffle(List<Track> tracks) {
		try {
			mService.shuffle(tracks);
		} catch (RemoteException ignored) {
			
		}
		mDrawer.openMenu();
	}

	@Override
	public void enqueue(List<Track> tracks, final int action) {
		try {
			mService.enqueue(tracks, action);
		} catch (RemoteException ignored) {
			
		}
	}

	@Override
	public void togglePlayback() {
		try {
			mService.togglePlayback();
		} catch (RemoteException e) {
			
		}
		
	}

	@Override
	public void next() {
		try {
			mService.next();
		} catch (RemoteException e) {

		}
		
	}

	@Override
	public void previous() {
		try {
			mService.previous();
		} catch (RemoteException e) {

		}
		
	}

	@Override
	public void seek(long position) {
		try {
			mService.seek(position);
		} catch (RemoteException e) {

		}
		
	}
	
	@Override
	public void toggleShuffle() {
		try {
			mService.toggleShuffle();
		} catch (RemoteException e) {
			
		}
		
	}

	@Override
	public void cycleRepeatMode() {
		try {
			mService.cycleRepeatMode();
		} catch (RemoteException e) {
			
		}
		
	}

	@Override
	public void requestMusicStatus() {
		try {
			mService.requestMusicStatus();
		} catch (RemoteException e) {

		}
		
	}

	@Override
	public void requestPlayQueue() {
		try {
			mService.requestPlayQueue();
		} catch (RemoteException e) {

		}
		
	}

	@Override
	public void registerPlayQueueCallback(IPlayQueueCallback callback) {
		try {
			mService.registerPlayQueueCallback(callback);
		} catch (RemoteException e) {

		}
		
	}

	@Override
	public void unregisterPlayQueueCallback(IPlayQueueCallback callback) {
		try {
			mService.unregisterPlayQueueCallback(callback);
		} catch (RemoteException e) {

		}
		
	}
	
	public void showNotification(boolean show) {
		try {
			mService.showNotification(show);
		} catch (RemoteException e) {
			
		}
	}

}
