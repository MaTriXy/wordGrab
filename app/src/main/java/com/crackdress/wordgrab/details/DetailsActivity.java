package com.crackdress.wordgrab.details;

import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.LifecycleRegistryOwner;
import androidx.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.crackdress.wordgrab.DividerItemDecoration;
import com.crackdress.wordgrab.R;
import com.crackdress.wordgrab.TheApplication;
import com.crackdress.wordgrab.adapters.DetailsActionsAdapter;
import com.crackdress.wordgrab.fragments.DeleteDialogFragment;
import com.crackdress.wordgrab.fragments.EditDialogFragment;
import com.crackdress.wordgrab.model.Contact;
import com.crackdress.wordgrab.model.Recording;
import com.crackdress.wordgrab.recordings.MainActivity;
import com.crackdress.wordgrab.services.PlaybackService;
import com.crackdress.wordgrab.utils.Utils;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

public class DetailsActivity extends AppCompatActivity implements DetailsContract.View, LifecycleRegistryOwner,
        DeleteDialogFragment.DeleteDialogEventListener, EditDialogFragment.EditDialogEventListener {
    public static final String TAG = DetailsActivity.class.getSimpleName();

    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    Recording mRecording;
    long recordId = -1;

    DetailsPresenter mPresenter;
    TextView tvPlayerDuration;
    TextView tvHeaderDuration;
    TextView tvProgress;
    TextView tvPhoneNumber;
    TextView tvDateHeader;
    TextView tvHeaderComment;
    View headerView;
    ToggleButton tbPlay;
    SeekBar mSeekBar;
    long mDuration;


    PlaybackService mPlaybackService;
    Bundle mSavedInstanceState;
    boolean mInstanceStateSaved;
    boolean isBound;
    Intent playbackServiceIntent;
    Toolbar mToolbar;


    RecyclerView rvActions;
    DetailsActionsAdapter mAdapter;
    static SortedMap<Integer, String> actionItems = new TreeMap<>();

    static {
        actionItems.put(R.drawable.ic_details_share, TheApplication.context.getString(R.string.share_recording));
        actionItems.put(R.drawable.ic_details_trash, TheApplication.context.getString(R.string.delete));
        actionItems.put(R.drawable.ic_details_edit, TheApplication.context.getString(R.string.edit));
        actionItems.put(R.drawable.ic_details_call, TheApplication.context.getString(R.string.call));
        actionItems.put(R.drawable.ic_details_sms , TheApplication.context.getString(R.string.sms));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        headerView = findViewById(R.id.listItem);
        headerView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        mPresenter = ViewModelProviders.of(this).get(DetailsPresenter.class);
        mPresenter.setDetailsView(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recordId = getIntent().getLongExtra(MainActivity.RECORDING_EXTRA, -1);
        Log.i(TAG, "onCreate: recording id = " + recordId);

        setViews();

        if (savedInstanceState != null) {
            mSavedInstanceState = savedInstanceState;
            tvProgress.setText(DateUtils.formatElapsedTime(savedInstanceState.getInt("progress")));
        } else {
            tvProgress.setText(DateUtils.formatElapsedTime(0));
        }
        playbackServiceIntent = new Intent(this, PlaybackService.class);
        mPresenter.loadRecording(recordId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(playbackServiceIntent);
        bindService(playbackServiceIntent, mConnection, BIND_AUTO_CREATE);
    }

    private void setViews() {
        tbPlay = (ToggleButton) findViewById(R.id.tbPlayPause);
        tvProgress = (TextView) findViewById(R.id.tvProgress);
        tvPlayerDuration = (TextView) findViewById(R.id.tvPlayerDuration);
        tvHeaderDuration = (TextView) findViewById(R.id.tvDuration);
        tvPhoneNumber = (TextView) findViewById(R.id.tvPhoneNumber);
        tvDateHeader = (TextView) findViewById(R.id.tvDate);
        tvHeaderComment = (TextView) findViewById(R.id.tvDetailsComment);

        rvActions = (RecyclerView) findViewById(R.id.rvDetailsActions);
        rvActions.setLayoutManager(new LinearLayoutManager(this));
        rvActions.addItemDecoration(new DividerItemDecoration(this));
        mAdapter = new DetailsActionsAdapter(this, actionClickListener);
        rvActions.setAdapter(mAdapter);

        tbPlay.setOnClickListener(v -> {
            if (((ToggleButton) v).isChecked()) {
                if (mPlaybackService != null) {
                    mPlaybackService.play();
                }
            } else {
                if (mPlaybackService != null) {
                    mPlaybackService.pause();
                }
            }
        });

//        ibCall = (ImageButton) findViewById(R.id.btCall);
//        ibCall.setOnClickListener(v -> {
//
//        });

        mSeekBar = (SeekBar) findViewById(R.id.sbAudioProgress);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mPlaybackService != null && fromUser) {
                    mPlaybackService.playbackSeek(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //  mPresenter.loadRecording(recordId);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isChangingConfigurations()) {
            //This means the screen is onStop called due to screen rotation, therefore it's crucial to unbind
            //the service to insure the destroyed Activity reference is released.
            //Service will rebound in onStart after activity recreation.
            //If this is not due to screen rotation unbind will be done in onDestroy.
            unbindService(mConnection);
        } else {
            /*
            *This means that onStop called due 2 possible reasons:
             1. Activity is going down because user hit the back button
             2. The user is leaving to another activity such as Phone Dialer or hit the home button
             Therefore, in this step, audio will be paused. if the Activity is going down then the playback Service will
             be cleaned and released in onDestroy()
            * */

            if (mPlaybackService != null && isBound) {
                mPlaybackService.pause();
                tbPlay.setChecked(false);
            }

        }
    }


    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

//            Log.i(TAG, "onServiceConnected");
            PlaybackService.LocalBinder binder = (PlaybackService.LocalBinder) service;
            mPlaybackService = binder.getService();

            if (mPlaybackService != null) {
                isBound = true;
                mPlaybackService.setOnEventListener(mServiceEventListener);
                if (mRecording != null && mSavedInstanceState == null) {
                    try {
                        mPlaybackService.prepareMediaPlayer(mRecording.getUri());
                    } catch (IOException e) {
                        Toast.makeText(DetailsActivity.this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
                        unbindService(mConnection);
                        mPlaybackService.stop();
                        mPlaybackService = null;
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            mPlaybackService = null;
        }
    };


    PlaybackService.ServiceEventListener mServiceEventListener = new PlaybackService.ServiceEventListener() {
        @Override
        public void onPlaybackProgress(int progress) {
            if (tvProgress != null) {
                runOnUiThread(() -> {
                    tvProgress.setText(DateUtils.formatElapsedTime(progress));
                    mSeekBar.setProgress(progress);
                });
            }
        }

        @Override
        public void onAudioPlaying() {

        }

        @Override
        public void onAudioCompleted() {
            tbPlay.setChecked(false);
            mSeekBar.setProgress(mSeekBar.getMax());
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Log.i(TAG, "onSaveInstanceState");
        outState.putBoolean("wasPlayingAudio", mPlaybackService.isPlaying());
        outState.putInt("progress", mPlaybackService.getAudioCurrentPosition());
        outState.putBoolean("isBound", isBound);
        mInstanceStateSaved = true;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            Log.i(TAG, "onDestroy: exiting...");
            if (mPlaybackService != null && isBound) {
                mPlaybackService.stop();
                unbindService(mConnection);
                stopService(playbackServiceIntent);
            }
        } else {
            Log.i(TAG, "onDestroy: orientation change...");
        }

    }

    @Override
    public void setPresenter(Object presenter) {

    }

    @Override
    public void showDeleteDialog() {
        DeleteDialogFragment dialogFragment = new DeleteDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Title", getString(R.string.delete_dialog_title));
        bundle.putString("Message", getString(R.string.delete_dialog_message));

        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "Tag");
    }

    @Override
    public void showEditCommentDialog() {
        EditDialogFragment dialog = new EditDialogFragment();
        Bundle b = new Bundle();
        b.putString("Title", getString(R.string.comment_dialog_title));
        b.putString("Message", getString(R.string.comment_dialog_message));
        b.putString("Comment", mRecording.getComment());
        dialog.setArguments(b);
        dialog.show(getSupportFragmentManager(), "EditDialog");
    }

    @Override
    public void showRecordingDetails(long recordingId) {

    }

    @Override
    public void startShareIntent(Uri uri) {
        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.setType("audio/*");

        intentShare.putExtra(Intent.EXTRA_STREAM, uri);
        // share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + ", " + tripTitle);
        startActivity(Intent.createChooser(intentShare, "Sharing Recording"));
    }


    @Override
    public void showRecordingDetails(Recording recording) {

//        Log.i(TAG, "showRecordingDetails: " + recording);

        if (recording != null) {
            mRecording = recording;

            Contact contact = Utils.isContactExists(this, recording.getPhoneNumber());

            mSeekBar.setMax((int) recording.getDuration() / 1000);
            tvPhoneNumber.setText(contact != null ? contact.getDisplayName() + " - " + recording.getPhoneNumber() : recording.getPhoneNumber());
            tvHeaderDuration.setText(DateUtils.formatElapsedTime(recording.getDuration() / 1000));

            tvPlayerDuration.setText(DateUtils.formatElapsedTime(recording.getDuration() / 1000));
            tvDateHeader.setText(DateUtils.getRelativeDateTimeString(this,
                recording.getDate(),
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS,
                    0));
            tvHeaderComment.setText(mRecording.getComment());


            if (recording.getPhoneNumber().equals("Unknown")) {
                actionItems.remove(R.drawable.ic_details_sms);
                actionItems.remove(R.drawable.ic_details_call);
                actionItems.remove(R.drawable.ic_details_add_person);
            } else {
                actionItems.put(R.drawable.ic_details_call, TheApplication.context.getString(R.string.call));
                actionItems.put(R.drawable.ic_details_sms, TheApplication.context.getString(R.string.sms));
                if (contact != null) {
                    actionItems.remove(R.drawable.ic_details_add_person);
            //        actionItems.put(R.drawable.whatsapp, "Whatsapp message");
                }else{
                    actionItems.put(R.drawable.ic_details_add_person, TheApplication.context.getString(R.string.add_to_contacts));
                }
            }
            mAdapter.updateData(actionItems);
        }else{
            //This may happen if user deletes this recording from the Details Screen (this)
            finish();
        }

    }

    @Override
    public void showContactEditor() {

    }

    @Override
    public void openDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }



    @Override
    public void addToContacts(String phoneNumber) {
        Utils.addToContacts(this, phoneNumber);
    }

    @Override
    public void sendMessage(String phoneNumber) {
//        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null)));
        Uri uri = Uri.parse("smsto:" + phoneNumber);
        Intent it = new Intent(Intent.ACTION_SENDTO, uri);
        it.putExtra("WordGrab", "Write your message here...");
        startActivity(it);

//        Intent sendIntent = new Intent();
//        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.putExtra(Intent.EXTRA_TEXT, "Your message here...");
//        sendIntent.setType("text/plain");
//        startActivity(sendIntent);
    }

    @Override
    public void sendWhatsapp(String phoneNumber) {
        try {
            Intent sendIntent = new Intent("android.intent.action.MAIN");
            //sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
            sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(phoneNumber) + "@s.whatsapp.net"); //phone number without "+" prefix
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } catch(Exception e) {
            Toast.makeText(this, "Error/n" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onDeleteDialogEvent() {
        mPresenter.deleteRecording();
    }

    @Override
    public void onEditDone(String comment) {
        // mPresenter.updateComment(comment);
        mRecording.setComment(comment);
        mPresenter.updateRecording(mRecording);
    }


    DetailsActionsAdapter.DetailsActionClickListener actionClickListener = new DetailsActionsAdapter.DetailsActionClickListener() {
        @Override
        public void onActionItemClicked(int actionId) {

            switch (actionId) {
                case R.drawable.ic_details_call:
//                    Log.i(TAG, "onActionItemClicked: call item clicked");
                    mPresenter.callActionClicked();
                    break;
                case R.drawable.ic_details_edit:
//                    Log.i(TAG, "onActionItemClicked: edit item clicked");
                    mPresenter.addCommentAction();
                    break;
                case R.drawable.ic_details_trash:
//                    Log.i(TAG, "onActionItemClicked: delete item clicked");
                    mPresenter.deleteAction();
                    break;
                case R.drawable.ic_details_add_person:
//                    Log.i(TAG, "onActionItemClicked: add to contact item clicked");
                    mPresenter.addToContactClicked();
                    break;
                case R.drawable.ic_details_share:
//                    Log.i(TAG, "onActionItemClicked: share item clicked");
                    mPresenter.shareRecordingAction(mRecording.getUri());
                    break;
                case R.drawable.ic_details_sms:
//                    Log.i(TAG, "onActionItemClicked: share item clicked");
                    mPresenter.smsActionClicked();
                    break;
                case R.drawable.whatsapp:
//                    Log.i(TAG, "onActionItemClicked: share item clicked");
                    mPresenter.whatsappActionClicked();
                    break;

            }

        }
    };

}
