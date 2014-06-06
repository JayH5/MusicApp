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

import android.app.Activity;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

public class MusicActivity extends Activity implements MenuDrawer.OnDrawerStateChangeListener {

	//private static final String TAG_PLAYER = "player";
	private static final String TAG_PLAY_QUEUE = "play_queue";
	private static final String STATE_MENUDRAWER = "menudrawer";

	public static final String ACTION_SHOW_PLAYER = "za.jamie.soundstage.ACTION_SHOW_PLAYER";

	protected MenuDrawer mMenuDrawer;

	private MusicPlayerFragment mPlayer;
	private PlayQueueFragment mPlayQueue;

    private boolean mDestroyed = false;
    private boolean mStartedButNotRegistered = false;

    private final IntentFilter mToastFilter = new IntentFilter("za.jamie.soundstage.TOAST");
    private final BroadcastReceiver mToastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String text = intent.getStringExtra("extra_toast");
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        }
    };

	private final MusicConnection mConnection = new MusicConnection();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initMenuDrawer();
		initFragments();
		initService();

        // If the activity is starting for the first time then the intent arrives here
        Intent startIntent = getIntent();
        if (startIntent != null) {
            if (ACTION_SHOW_PLAYER.equals(startIntent.getAction())) {
                showPlayer();
                // Clear the action so drawer doesn't reopen on configuration changes
                startIntent.setAction(null);
            }
        }
	}

    private void initMenuDrawer() {
        // Set up the menu drawer to display the player
        mMenuDrawer =
                MenuDrawer.attach(this, Type.BEHIND, Position.LEFT, MenuDrawer.MENU_DRAG_WINDOW);

        // Have to set offset... kind of a pain
        final Resources res = getResources();
        int menuSize = res.getDisplayMetrics().widthPixels
                - res.getDimensionPixelOffset(R.dimen.menudrawer_offset);
        mMenuDrawer.setMenuSize(menuSize);
        mMenuDrawer.setMenuView(R.layout.menudrawer_frame);
        mMenuDrawer.setDropShadow(R.drawable.menudrawer_shadow);
        mMenuDrawer.setOnDrawerStateChangeListener(this);
    }

    private void initFragments() {
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
    }

    private void initService() {
        mConnection.requestConnectionCallbacks(new ConnectionCallbacks() {
            @Override
            public void onConnected() {
                if (mStartedButNotRegistered) {
                    mStartedButNotRegistered =
                            !mConnection.registerActivityStarted(getComponentName());
                }
            }

            @Override
            public void onDisconnected() {
                connectToService(); // Reconnect
            }
        });

        connectToService();
    }

    private void connectToService() {
        if (!mDestroyed && !isFinishing()) {
            Intent serviceIntent = new Intent(this, MusicService.class);
            startService(serviceIntent);
            bindService(serviceIntent, mConnection, 0);
        }
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (ACTION_SHOW_PLAYER.equals(intent.getAction())) {
			showPlayer();
		}
	}

    @Override
    protected void onStart() {
        super.onStart();
        mStartedButNotRegistered = !mConnection.registerActivityStarted(getComponentName());
        registerReceiver(mToastReceiver, mToastFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStartedButNotRegistered = false;
        mConnection.registerActivityStop(getComponentName());
        unregisterReceiver(mToastReceiver);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
        mDestroyed = true;
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
        MenuInflater inflater = getMenuInflater();

        // Add default menu options
        inflater.inflate(R.menu.base, menu);
        menu.findItem(R.id.action_settings).setIntent(new Intent(this, SettingsActivity.class));

        // Add search button
    	inflater.inflate(R.menu.search, menu);
        menu.findItem(R.id.menu_search).setIntent(new Intent(this, SearchActivity.class));
        return true;
    }

	@Override
	public void onDrawerStateChange(int oldState, int newState) {
		if (newState == MenuDrawer.STATE_CLOSED || oldState == MenuDrawer.STATE_CLOSED) {
			mMenuDrawer.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		}
	}

	@Override
	public void onDrawerSlide(float openRatio, int offsetPixels) {
		// Complete interface
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
		// Bring back activity as it was with player showing
		Intent intent = new Intent(this, this.getClass());
		intent.setAction(ACTION_SHOW_PLAYER)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		return PendingIntent.getActivity(this, 0, intent, 0);
	}

}
