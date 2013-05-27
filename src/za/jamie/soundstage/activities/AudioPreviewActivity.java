package za.jamie.soundstage.activities;

import za.jamie.soundstage.R;
import za.jamie.soundstage.fragments.AudioPreviewFragment;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;

public class AudioPreviewActivity extends Activity {
	
	private static final String TAG_PLAYER_FRAG = "tag_player_frag";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        Uri uri = intent.getData();
        if (uri == null) {
            finish();
            return;
        }
        
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_audio_preview);
        
        final FragmentManager fm = getFragmentManager();
        AudioPreviewFragment frag = (AudioPreviewFragment) fm.findFragmentByTag(TAG_PLAYER_FRAG);
        if (frag == null) {
        	frag = AudioPreviewFragment.newInstance(uri);
        }
        fm.beginTransaction()
        		.add(R.id.audio_preview_frame, frag, TAG_PLAYER_FRAG)
        		.commit();
	}

}
