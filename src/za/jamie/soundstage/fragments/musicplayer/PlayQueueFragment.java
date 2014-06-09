package za.jamie.soundstage.fragments.musicplayer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;

import java.lang.ref.WeakReference;
import java.util.List;

import za.jamie.soundstage.R;
import za.jamie.soundstage.adapters.PlayQueueAdapter;
import za.jamie.soundstage.fragments.MusicDialogFragment;
import za.jamie.soundstage.models.Track;
import za.jamie.soundstage.service.MusicConnection;
import za.jamie.soundstage.service.MusicService;
import za.jamie.soundstage.utils.MessengerUtils;
import za.jamie.soundstage.utils.PlaylistUtils;

public class PlayQueueFragment extends MusicDialogFragment implements
		AdapterView.OnItemClickListener, DragSortListView.DragSortListener {

	private static final String TAG = "PlayQueueFragment";

    /* The minimum size of a track list before it is deserialized asynchronously. */
    private static final int THRESHOLD_ASYNC_QUEUE_LOAD = 100;

    private static final String STATE_LIST_POSITION = "state_list_position";
	private static final String STATE_LIST_OFFSET = "state_list_offset";
    private static final String STATE_SAVE_MODE = "state_save_mode";

	private static final int SCROLL_OFFSET = 35;
	private static final int SCROLL_DURATION = 250; // 250ms

	private PlayQueueAdapter mAdapter;
	private DragSortListView mDslv;

	private TextView mMessageView;
    private TextView mErrorMessageView;
    private EditText mNameField;

    private boolean mIsShuffled;
    private boolean mSaveMode;

	private int mSavedPosition = -1;
	private int mSavedOffset = -1;

	private final MusicConnection.ConnectionObserver mConnectionObserver =
			new MusicConnection.ConnectionObserver() {
		@Override
		public void onConnected() {
            MusicConnection connection = getMusicConnection();
			connection.registerQueueClient(mQueueClient);
            connection.requestQueueUpdate(mQueueClient);
		}
	};

    private Messenger mQueueClient;

	public static PlayQueueFragment newInstance() {
		return new PlayQueueFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new PlayQueueAdapter(getActivity(), R.layout.list_item_play_queue,
				R.layout.list_item_play_queue_selected, null);

        Handler queueClientHandler = new QueueClientHandler(this);
        mQueueClient = new Messenger(queueClientHandler);

		getMusicConnection().registerConnectionObserver(mConnectionObserver);

		if (savedInstanceState != null) {
			mSavedPosition = savedInstanceState.getInt(STATE_LIST_POSITION, -1);
			mSavedOffset = savedInstanceState.getInt(STATE_LIST_OFFSET, -1);

            mSaveMode = savedInstanceState.getBoolean(STATE_SAVE_MODE);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (!mAdapter.isEmpty()) {
			outState.putInt(STATE_LIST_POSITION, mDslv.getFirstVisiblePosition());
			final View v = mDslv.getChildAt(0);
			outState.putInt(STATE_LIST_OFFSET, v != null ? v.getTop() : 0);
		}
        outState.putBoolean(STATE_SAVE_MODE, mSaveMode);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.fragment_play_queue, container, false);

		mDslv = (DragSortListView) v.findViewById(R.id.dslv);
		mDslv.setDragSortListener(this);
		mDslv.setOnItemClickListener(this);
		mDslv.setAdapter(mAdapter);

		ImageButton goToPositionButton = (ImageButton) v.findViewById(R.id.scrollToPosition);
		goToPositionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scrollToPosition();
			}
		});

		mMessageView = (TextView) v.findViewById(R.id.play_queue_is_shuffled);
        mErrorMessageView = (TextView) v.findViewById(R.id.play_queue_error_message);
        mNameField = (EditText) v.findViewById(R.id.play_queue_name_field);

		return v;
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mSaveMode) {
            switchToSaveMode();
        } else {
            switchToListMode();
        }
    }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
        getMusicConnection().unregisterQueueClient(mQueueClient);
		getMusicConnection().unregisterConnectionObserver(mConnectionObserver);
	}

	public void scrollToPosition() {
		final int queuePosition = mAdapter.getQueuePosition();
		if (queuePosition > -1) {
			mDslv.smoothScrollToPositionFromTop(queuePosition, SCROLL_OFFSET, SCROLL_DURATION);
		}
	}

	@Override
	public void remove(int which) {
		mAdapter.remove(which);
		getMusicConnection().removeQueueItem(which);

		if (mAdapter.getCount() == 0) {
			getDialog().dismiss();
		}
	}

	@Override
	public void drop(int from, int to) {
		if (from != to) {
			mAdapter.move(from, to);
			getMusicConnection().moveQueueItem(from, to);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int which, long id) {
		getMusicConnection().setQueuePosition(which);
		getDialog().dismiss();
	}

	@Override
	public void drag(int from, int to) {
		mDslv.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
	}

    private void switchToListMode() {
        mSaveMode = false;
        View root = getView();

        // Hide the save field
        mNameField.setVisibility(View.INVISIBLE);
        mNameField.setText("");

        InputMethodManager imm =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNameField.getWindowToken(), 0);

        // Show list view
        mDslv.setVisibility(View.VISIBLE);

        // Update message
        if (mIsShuffled) {
            mMessageView.setText(R.string.play_queue_is_shuffled);
        } else {
            mMessageView.setVisibility(View.GONE);
        }
        mErrorMessageView.setVisibility(View.GONE);

        // Change the button text and action
        Button saveButton = (Button) root.findViewById(R.id.play_queue_save_button);
        saveButton.setText(R.string.play_queue_save_queue);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToSaveMode();
            }
        });

        Button cancelButton = (Button) root.findViewById(R.id.play_queue_close_button);
        cancelButton.setText(R.string.play_queue_close);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        View goToPositionButton = root.findViewById(R.id.scrollToPosition);
        goToPositionButton.setVisibility(View.VISIBLE);
    }

    private void switchToSaveMode() {
        mSaveMode = true;
        View root = getView();

        // Hide list view
        mDslv.setVisibility(View.GONE);

        // Show the save field
        mNameField.setVisibility(View.VISIBLE);

        // Show the info message and change its text
        mMessageView.setText(R.string.play_queue_enter_name);
        mMessageView.setVisibility(View.VISIBLE);

        // Change the button text and action
        Button saveButton = (Button) root.findViewById(R.id.play_queue_save_button);
        saveButton.setText(R.string.play_queue_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePlayQueue();
            }
        });

        Button cancelButton = (Button) root.findViewById(R.id.play_queue_close_button);
        cancelButton.setText(android.R.string.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToListMode();
            }
        });

        // Hide goto button
        View goToPositionButton = root.findViewById(R.id.scrollToPosition);
        goToPositionButton.setVisibility(View.INVISIBLE);

        // Bring up the keyboard
        mNameField.requestFocus();
        InputMethodManager imm =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mNameField, InputMethodManager.SHOW_IMPLICIT);
    }

    private void savePlayQueue() {
        savePlayQueueAsPlaylist(mNameField.getText().toString());
    }

    private void onPlaylistSaved(boolean success, String message) {
        if (success) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            getDialog().dismiss();
        } else {
            mErrorMessageView.setText(message);
            mErrorMessageView.setVisibility(View.VISIBLE);

            Animator shake = AnimatorInflater.loadAnimator(getMusicActivity(), R.animator.shake);
            shake.setTarget(getView());
            shake.start();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mSaveMode = false;
        mNameField.setText("");
    }

    private void receivePlayQueue(List<Track> tracks, int position, boolean isShuffled) {
        if (mAdapter == null) {
            return;
        }

        mAdapter.clear();
        mAdapter.addAll(tracks);
        mAdapter.setQueuePosition(position);
        if (mSavedPosition != -1 && mSavedOffset != -1) {
            mDslv.setSelectionFromTop(mSavedPosition, mSavedOffset);
        } else {
            mDslv.setSelectionFromTop(position, SCROLL_OFFSET);
        }
        mIsShuffled = isShuffled;
        if (isShuffled) {
            mMessageView.setVisibility(View.VISIBLE);
        }
    }

    private void receivePositionChange(int position) {
        if (mAdapter == null) {
            return;
        }

        mAdapter.setQueuePosition(position);
    }

    private void savePlayQueueAsPlaylist(String name) {
        if (TextUtils.isEmpty(name)) {
            return;
        }

        new SavePlayQueueTask().execute(name);
    }

    private class SavePlayQueueTask extends AsyncTask<String, Void, Message> {
        @Override
        protected Message doInBackground(String... params) {
            final Activity activity = getActivity();
            if (activity == null) {
                return null;
            }

            Message result = Message.obtain();
            String name = params[0];
            ContentResolver resolver = activity.getContentResolver();
            String existingName = PlaylistUtils.findExistingPlaylist(resolver, name);
            if (existingName == null) {
                long id = PlaylistUtils.createPlaylist(resolver, name);
                if (id > 0) {
                    PlaylistUtils.savePlaylistTracks(resolver, id, mAdapter.getObjects());
                    result.arg1 = 0;
                    result.obj = name;
                } else {
                    result.arg1 = 1;
                }
            } else {
                result.arg1 = 1;
                result.obj = existingName;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Message result) {
            String message;
            if (result.arg1 == 0) {
                message = getString(R.string.play_queue_saved, result.obj);
            } else if (result.obj != null) {
                message = getString(R.string.play_queue_already_exists, result.obj);
            } else {
                message = getString(R.string.play_queue_error);
            }
            onPlaylistSaved(result.arg1 == 0, message);
            result.recycle();
        }
    }

    private void receivePlayQueueAsync(Message msg) {
        new LoadPlayQueueTask(msg).execute();
    }

    private class LoadPlayQueueTask extends AsyncTask<Void, Void, List<Track>> {

        final Message mMsg;

        LoadPlayQueueTask(Message msg) {
            // Have to copy the Message because it will probably be recycled
            mMsg = Message.obtain(msg);
        }

        @Override
        protected List<Track> doInBackground(Void... params) {
            return MessengerUtils.readParcelableList(getActivity(), mMsg, "queue");
        }

        @Override
        protected void onPostExecute(List<Track> result) {
            if (result != null) {
                int position = mMsg.getData().getInt("position");
                boolean isShuffled = mMsg.getData().getBoolean("isShuffled");
                receivePlayQueue(result, position, isShuffled);
            } else {
                Log.w(TAG, "Received play queue was null, retrying");
                // Retry?
                //getMusicConnection().requestQueueUpdate(mQueueClient);
            }
            mMsg.recycle();
        }
    }

    private static class QueueClientHandler extends Handler {

        final WeakReference<PlayQueueFragment> mQueue;

        public QueueClientHandler(PlayQueueFragment queue) {
            mQueue = new WeakReference<PlayQueueFragment>(queue);
        }

        @Override
        public void handleMessage(Message msg) {
            final PlayQueueFragment fragment = mQueue.get();
            if (fragment == null) {
                return;
            }

            switch (msg.what) {
                case MusicService.MSG_QUEUE_DELIVER:
                    // If the queue is long we load it asynchronously because deserializing is slow
                    if (msg.arg1 < THRESHOLD_ASYNC_QUEUE_LOAD) {
                        List<Track> queue = MessengerUtils
                                .readParcelableList(fragment.getActivity(), msg, "queue");
                        int position = msg.getData().getInt("position");
                        boolean isShuffled = msg.getData().getBoolean("isShuffled");
                        fragment.receivePlayQueue(queue, position, isShuffled);
                    } else {
                        fragment.receivePlayQueueAsync(msg);
                    }
                    break;
                case MusicService.MSG_QUEUE_CHANGE:
                    // TODO
                    break;
                case MusicService.MSG_QUEUE_POSITION_CHANGE:
                    fragment.receivePositionChange(msg.arg1);
                    break;
            }
        }
    }
}
