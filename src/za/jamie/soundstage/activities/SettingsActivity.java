package za.jamie.soundstage.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import za.jamie.soundstage.fragments.settings.SettingsFragment;
import za.jamie.soundstage.service.MusicConnection;
import za.jamie.soundstage.service.MusicService;

/**
 * Created by jamie on 2014/06/06.
 */
public class SettingsActivity extends Activity {

    private final MusicConnection mConnection = new MusicConnection();
    private boolean mDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initService();

        getActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initService() {
        mConnection.registerConnectionObserver(new MusicConnection.ConnectionObserver() {
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
    protected void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
        unbindService(mConnection);
    }

    /**
     * Gets the {@link MusicConnection} instance associated with this SettingsActivity that can
     * be used to access the MusicService's controls.
     * @return The connection to the MusicService
     */
    public MusicConnection getMusicConnection() {
        return mConnection;
    }

}
