package za.jamie.soundstage.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

/**
 * Created by jamie on 2014/06/08.
 */
public final class MessengerUtils {

    private static final String TAG = "MessengerUtils";
    private static final boolean DEBUG_TIMESTAMP = false;

    public static void send(Messenger messenger, int what) throws RemoteException {
        messenger.send(Message.obtain(null, what));
    }

    public static void send(Messenger messenger, int what, int arg1) throws RemoteException {
        messenger.send(Message.obtain(null, what, arg1, 0));
    }

    public static void send(Messenger messenger, int what, int arg1, int arg2)
            throws RemoteException {
        messenger.send(Message.obtain(null, what, arg1, arg2));
    }

    public static void send(Messenger messenger, int what, boolean bool) throws RemoteException {
        messenger.send(Message.obtain(null, what, bool ? 1 : 0, 0));
    }

    public static void send(Messenger messenger, int what, Object obj) throws RemoteException {
        messenger.send(Message.obtain(null, what, obj));
    }

    public static void send(Messenger messenger, int what, Messenger replyTo)
            throws RemoteException {
        Message msg = Message.obtain(null, what);
        msg.replyTo = replyTo;
        messenger.send(msg);
    }

    public static void sendTimestamped(Messenger messenger, int what, int arg1)
            throws RemoteException {
        messenger.send(Message.obtain(null, what, arg1, intTimestamp()));
    }

    public static long readTimestamp(Message msg) {
        if (DEBUG_TIMESTAMP) {
            Log.d(TAG, "Message when: " +msg.getWhen());
        }
        return intToLongTimestamp(msg.arg2);
    }

    public static Parcelable readParcelable(Context context, Message msg, String key) {
        Bundle data = msg.getData();
        data.setClassLoader(context.getClassLoader());
        return data.getParcelable(key);
    }

    public static <T extends Parcelable> List<T> readParcelableList(Context context, Message msg,
            String key) {
        Bundle data = msg.getData();
        data.setClassLoader(context.getClassLoader());
        return data.getParcelableArrayList(key);
    }

    /**
     * Calculate the number of milliseconds since the last hour change. Hence, this timestamp is
     * only valid for an hour.
     * @return An integer timestamp.
     */
    private static int intTimestamp() {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return (int) (now - cal.getTimeInMillis());
    }

    private static long intToLongTimestamp(int intTimestamp) {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long lastHour = cal.getTimeInMillis();
        long timestamp = lastHour + intTimestamp;

        // If the hour has changed since the timestamp was set then go back 1 hour
        if ((int) (now - lastHour) < intTimestamp) {
            timestamp -= (1000 * 60 * 60);
        }

        if (DEBUG_TIMESTAMP) {
            Log.d(TAG, "Message timestamp: " + timestamp);
            Log.d(TAG, "Message transfer delay: " + (now - timestamp));
        }

        return timestamp;
    }

}
