package za.jamie.soundstage.activities;

import static za.jamie.soundstage.service.MusicServiceWrapper.mService;
import net.simonvt.menudrawer.MenuDrawer;
import za.jamie.soundstage.IMusicService;
import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.musicplayer.MusicPlayerFragment;
import za.jamie.soundstage.fragments.musicplayer.PlayQueueFragment;
import za.jamie.soundstage.service.MusicServiceWrapper;
import za.jamie.soundstage.service.MusicServiceWrapper.ServiceToken;
import za.jamie.soundstage.utils.AppUtils;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class MusicActivity extends FragmentActivity implements ServiceConnection, 
		MenuDrawer.OnDrawerStateChangeListener {
	
	private static final String TAG_PLAYER = "player";
	private static final String TAG_PLAY_QUEUE = "play_queue";
	private static final String STATE_MENUDRAWER = "menudrawer";
	
	private ServiceToken mServiceToken;
	
	private ImageButton mPlayQueueButton;
	
	private boolean mIsBackPressed;
	
	private Vibrator mVibrator;
	private static final long VIBRATION_LENGTH = 15;
	
	private MenuDrawer mDrawer;
	
	private MusicPlayerFragment mPlayer;
	private PlayQueueFragment mPlayQueue;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Bind the service
		mServiceToken = MusicServiceWrapper.bindToService(this, this);
		
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
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	public MenuDrawer getMenuDrawer() {
		return mDrawer;
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        MusicServiceWrapper.killForegroundService(this);
	}
	
	@Override
    protected void onPause() {
        super.onPause();
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

	@Override
	public void onDrawerStateChange(int oldState, int newState) {		
		if (newState == MenuDrawer.STATE_CLOSED) {
			mPlayer.onHide();
			mVibrator.vibrate(VIBRATION_LENGTH);
		} else if (oldState == MenuDrawer.STATE_CLOSED) {
			mPlayer.onShow();
			mVibrator.vibrate(VIBRATION_LENGTH);
		}
		
	}

}
