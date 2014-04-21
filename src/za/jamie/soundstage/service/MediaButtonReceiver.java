/*   
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.jamie.soundstage.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * Receives broadcasted intents from media button key events.
 */
public class MediaButtonReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                return;
            }
            
            String action;
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    action = MusicService.ACTION_TOGGLE_PLAYBACK;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    action = MusicService.ACTION_PLAY;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    action = MusicService.ACTION_PAUSE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    action = MusicService.ACTION_STOP;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    action = MusicService.ACTION_NEXT;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    action = MusicService.ACTION_PREVIOUS;
                    break;
                default:
                    return;
            }
            Intent serviceIntent = new Intent(context, MusicService.class);
            serviceIntent.setAction(action);
            context.startService(serviceIntent);
        }
    }

}