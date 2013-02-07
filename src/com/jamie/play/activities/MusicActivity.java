package com.jamie.play.activities;

import static com.jamie.play.service.MusicServiceWrapper.mService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import net.simonvt.menudrawer.MenuDrawer;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import com.jamie.play.IMusicService;
import com.jamie.play.R;
import com.jamie.play.bitmapfun.ImageCache;
import com.jamie.play.bitmapfun.ImageFetcher;
import com.jamie.play.fragments.musicplayer.MusicPlayerFragment;
import com.jamie.play.service.MusicService;
import com.jamie.play.service.MusicServiceWrapper;
import com.jamie.play.service.MusicServiceWrapper.ServiceToken;
import com.jamie.play.service.MusicStateListener;
import com.jamie.play.utils.AppUtils;

public class MusicActivity extends FragmentActivity implements ServiceConnection,
		MusicStateListener, MenuDrawer.OnDrawerStateChangeListener {
	
	private static final String PLAYER_TAG = "player";
	private static final String STATE_MENUDRAWER = "menudrawer";
	
	private ServiceToken mServiceToken;
	private BroadcastReceiver mPlaybackStatus;
	private List<MusicStateListener> mMusicStateListeners = 
			new ArrayList<MusicStateListener>();
	
	private boolean mIsBackPressed;
	
	private ImageFetcher mImageWorker;
	
	private Vibrator mVibrator;
	
	private MenuDrawer mDrawer;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mServiceToken = MusicServiceWrapper.bindToService(this, this);
		mPlaybackStatus = new PlaybackStatus(this);
		
		mImageWorker = new ImageFetcher(this);
		mImageWorker.setImageCache(ImageCache.findOrCreateCache(this));
		
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		// Set up the menu drawer to display the player
		mDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);
		mDrawer.setMenuView(R.layout.slidingmenu_frame);
		mDrawer.setDropShadow(R.drawable.slidingmenu_shadow);
		mDrawer.setOnDrawerStateChangeListener(this);
		
		// Initialize the music player fragment
		final FragmentManager fm = getSupportFragmentManager();
		MusicPlayerFragment frag = (MusicPlayerFragment) fm.findFragmentByTag(PLAYER_TAG);
		if (frag == null) {
			frag = new MusicPlayerFragment();
		}
		addMusicStateListener(frag);
		fm.beginTransaction().replace(R.id.menu_frame, frag)
			.commit();	
		
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public MenuDrawer getMenuDrawer() {
		return mDrawer;
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        mImageWorker.setExitTasksEarly(false);
        MusicServiceWrapper.killForegroundService(this);
        refreshListeners();
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        
        final IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.PLAYSTATE_CHANGED);
        filter.addAction(MusicService.SHUFFLEMODE_CHANGED);
        filter.addAction(MusicService.REPEATMODE_CHANGED);
        filter.addAction(MusicService.META_CHANGED);
        filter.addAction(MusicService.REFRESH);
        registerReceiver(mPlaybackStatus, filter);
    }
	
	@Override
    protected void onPause() {
        super.onPause();
        mImageWorker.setExitTasksEarly(true);
        if (MusicServiceWrapper.isPlaying() || mIsBackPressed) {
            if (AppUtils.isApplicationSentToBackground(this)) {
            	MusicServiceWrapper.startBackgroundService(this);
            }
        }
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mServiceToken != null) {
        	MusicServiceWrapper.unbindFromService(mServiceToken);
            mServiceToken = null;
        }

        // Unregister the receiver
        try {
            unregisterReceiver(mPlaybackStatus);
        } catch (final Throwable e) {
            //$FALL-THROUGH$
        }

        // Remove any music status listeners
        mMusicStateListeners.clear();
    }
	
	@Override
    public void onBackPressed() {
		final int drawerState = mDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mDrawer.closeMenu();
            return;
        }
        mIsBackPressed = true;
		super.onBackPressed();
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Set the "home-as-up" button to show the sliding menu
			//getSlidingMenu().showMenu();
			mDrawer.openMenu();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mService = IMusicService.Stub.asInterface(service);
		
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mService = null;
		
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
	
	private void refreshListeners() {
		for (MusicStateListener listener : mMusicStateListeners) {
			listener.onMetaChanged();
			listener.onPlayStateChanged();
			listener.onRefresh();
			listener.onShuffleOrRepeatModeChanged();
		}
	}
	
	public ImageFetcher getImageFetcher() {
		return mImageWorker;
	}
	
	private final static class PlaybackStatus extends BroadcastReceiver {

        private final WeakReference<MusicStateListener> mReference;

        public PlaybackStatus(final MusicStateListener listener) {
            mReference = new WeakReference<MusicStateListener>(listener);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action.equals(MusicService.META_CHANGED)) {
                mReference.get().onMetaChanged();
            } else if (action.equals(MusicService.PLAYSTATE_CHANGED)) {
                mReference.get().onPlayStateChanged();
            } else if (action.equals(MusicService.REPEATMODE_CHANGED)
                    || action.equals(MusicService.SHUFFLEMODE_CHANGED)) {
                mReference.get().onShuffleOrRepeatModeChanged();
            } else if (action.equals(MusicService.REFRESH)) {
                mReference.get().onRefresh();
            }
        }
    }
	
	public void addMusicStateListener(MusicStateListener listener) {
		if (listener != null) {
			mMusicStateListeners.add(listener);
		}
	}

	@Override
	public void onMetaChanged() {
		for (MusicStateListener listener : mMusicStateListeners) {
			if (listener != null) {
				listener.onMetaChanged();
			}
		}
		
	}

	@Override
	public void onPlayStateChanged() {
		for (MusicStateListener listener : mMusicStateListeners) {
			if (listener != null) {
				listener.onPlayStateChanged();
			}
		}
		
	}

	@Override
	public void onShuffleOrRepeatModeChanged() {
		for (MusicStateListener listener : mMusicStateListeners) {
			if (listener != null) {
				listener.onShuffleOrRepeatModeChanged();
			}
		}
		
	}

	@Override
	public void onRefresh() {
		for (MusicStateListener listener : mMusicStateListeners) {
			if (listener != null) {
				listener.onRefresh();
			}
		}
		
	}

	@Override
	public void onDrawerStateChange(int oldState, int newState) {
		if (oldState == MenuDrawer.STATE_CLOSED
				&& newState == MenuDrawer.STATE_DRAGGING) {
			mVibrator.vibrate(15);
		} else if ((oldState == MenuDrawer.STATE_CLOSING 
				|| oldState == MenuDrawer.STATE_DRAGGING)
				&& newState == MenuDrawer.STATE_CLOSED) {
			mVibrator.vibrate(15);
		}
		
	}

}
