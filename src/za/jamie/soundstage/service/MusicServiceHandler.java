package za.jamie.soundstage.service;

import android.content.ComponentName;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import za.jamie.soundstage.models.MusicItem;
import za.jamie.soundstage.utils.MessengerUtils;

/**
 * Created by jamie on 2014/06/07.
 */
public class MusicServiceHandler extends Handler {

    private static final String TAG = "MusicServiceHandler";

    private final WeakReference<MusicService> mService;

    public MusicServiceHandler(MusicService service) {
        mService = new WeakReference<MusicService>(service);
    }

    @Override
    public void handleMessage(Message msg) {
        final MusicService service = mService.get();
        if (service == null) {
            return;
        }

        switch (msg.what) {
            case MusicConnection.MSG_TOGGLE_PLAYBACK:
                service.togglePlayback();
                break;
            case MusicConnection.MSG_NEXT:
                service.next();
                break;
            case MusicConnection.MSG_PREVIOUS:
                service.previous();
                break;
            case MusicConnection.MSG_SEEK:
                service.seek(msg.arg1);
                break;
            case MusicConnection.MSG_TOGGLE_SHUFFLE:
                service.toggleShuffle();
                break;
            case MusicConnection.MSG_CYCLE_REPEAT:
                service.cycleRepeat();
                break;
            case MusicConnection.MSG_MOVE_QUEUE_ITEM:
                service.moveQueueItem(msg.arg1, msg.arg2);
                break;
            case MusicConnection.MSG_REMOVE_QUEUE_ITEM:
                service.removeTrack(msg.arg1);
                break;
            case MusicConnection.MSG_SET_QUEUE_POSITION:
                service.setQueuePosition(msg.arg1);
                break;
            case MusicConnection.MSG_OPEN:
                service.open((MusicItem) MessengerUtils.readParcelable(service, msg, "item"),
                        msg.arg1);
                break;
            case MusicConnection.MSG_ENQUEUE:
                service.enqueue((MusicItem) MessengerUtils.readParcelable(service, msg, "item"),
                        msg.arg1);
                break;
            case MusicConnection.MSG_SHUFFLE:
                service.shuffle((MusicItem) MessengerUtils.readParcelable(service, msg, "item"));
                break;
            case MusicConnection.MSG_PLAY_ALL:
                service.playAll(msg.arg1);
                break;
            case MusicConnection.MSG_REQUEST_PLAYER_UPDATE:
                service.deliverMusicStatus(msg.replyTo);
                break;
            case MusicConnection.MSG_REGISTER_PLAYER_CLIENT:
                service.registerPlayerClient(msg.replyTo);
                break;
            case MusicConnection.MSG_UNREGISTER_PLAYER_CLIENT:
                service.unregisterPlayerClient(msg.replyTo);
                break;
            case MusicConnection.MSG_REQUEST_QUEUE_UPDATE:
                service.deliverPlayQueue(msg.replyTo);
                break;
            case MusicConnection.MSG_REGISTER_QUEUE_CLIENT:
                service.registerQueueClient(msg.replyTo);
                break;
            case MusicConnection.MSG_UNREGISTER_QUEUE_CLIENT:
                service.unregisterQueueClient(msg.replyTo);
                break;
            case MusicConnection.MSG_REGISTER_ACTIVITY_START:
                service.registerActivityStart((ComponentName) msg.obj);
                break;
            case MusicConnection.MSG_REGISTER_ACTIVITY_STOP:
                service.registerActivityStop((ComponentName) msg.obj);
                break;
            case MusicConnection.MSG_GET_AUDIO_SESSION_ID:
                service.getAudioSessionId(msg.replyTo);
                break;
            default:
                Log.w(TAG, "Unknown message received: " + msg.what);
                break;
        }
    }
}
