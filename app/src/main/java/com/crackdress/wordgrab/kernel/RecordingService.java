package com.crackdress.wordgrab.kernel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.crackdress.wordgrab.R;
import com.crackdress.wordgrab.model.Contact;
import com.crackdress.wordgrab.model.Recording;
import com.crackdress.wordgrab.recordings.MainActivity;
import com.crackdress.wordgrab.repository.AppDatabase;
import com.crackdress.wordgrab.utils.Utils;
import com.google.firebase.crash.FirebaseCrash;

import java.io.IOException;

import static com.crackdress.wordgrab.TheApplication.context;
import static com.crackdress.wordgrab.kernel.CallReceiver.RECORDING_NOTIFICATION;


public class RecordingService extends Service {
    public static final String TAG = RecordingService.class.getSimpleName();


    public static final String VOICE_COMMUNICATION = "COMMUNICATION";
    public static final String VOICE_CALL = "CALL";
    public static final String VOICE_DOWNLINK = "DL";
    public static final String VOICE_UPLINK = "UL";
    public static final String MIC = "MIC";

    String phoneNumber;
    RecordingManager mRecordingManager;
    NotificationManager mNotificationManager;
    private final Handler mHandler = new Handler();
    RecorderConfig mRecorderConfig;

    Context mContext;
    Contact mContact;
    boolean mIncoming;

    BroadcastReceiver callStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
             //   Log.i(TAG, "onReceive: received phone state changed intent");
                ServicePhoneStateListener phoneListener = new ServicePhoneStateListener(
                        context);
                TelephonyManager telephony = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                telephony.listen(phoneListener,
                        PhoneStateListener.LISTEN_CALL_STATE);
            }

            if(intent.getAction().equals(getString(R.string.start_recording_action))){
//                Log.i(TAG, "onReceive: received manual start intent");
                if(mRecordingManager != null && !mRecordingManager.isNowRecording()){
                    startRecording();
                }
            }

            if(intent.getAction().equals(getString(R.string.stop_recording_action))){
//                Log.i(TAG, "onReceive: received manual stop intent");
                if(mRecordingManager != null && mRecordingManager.isNowRecording()){
                    stopRecording();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        mRecordingManager = RecordingManager.getInstance(mContext, path);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String logMessage = "onStartCommand about to record a call.. " + (intent != null ? "intent OK" : "intent is null ") + " flag:" + flags;
        FirebaseCrash.log(logMessage);
        Log.i(TAG, "onStartCommand: " + logMessage);
        //If recordingManager != null and it's already recording this means this is a waiting call, it shouldn't be recorded.
        if(intent != null && !mRecordingManager.isNowRecording()){ //Intent may be null if it recreated by the system after it was killed..
            CallInfo callInfo = intent.getParcelableExtra(context.getString(R.string.call_info_extra));
            phoneNumber = callInfo.phoneNumber;
            mIncoming = callInfo.incoming;
            mContact = Utils.isContactExists(this, phoneNumber);

//            Log.i(TAG, "onStartCommand: about to start recording: " + phoneNumber);
            startRecording();
        }else {
//            Log.i(TAG, "onStartCommand: recording already..");
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class ServicePhoneStateListener extends android.telephony.PhoneStateListener {

        Context context;

        ServicePhoneStateListener(Context c) {
            super();
            context = c;
        }


        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            if (state == TelephonyManager.CALL_STATE_IDLE) { //Call ended...
//                Log.i(CallReceiver.TAG, "onCallStateChanged: Hangup");

                if (mRecordingManager.isNowRecording()) {
//                    Log.i(CallReceiver.TAG, "stopping recording");
                    stopRecording();
                }
            }
        }
    }


    private void startRecording() {
        checkPreferences();
        mRecordingManager.initRecorder(mRecorderConfig);

        try {
            mRecordingManager.startRecording();
            recordingNotification();
            startReceiver();
//            Log.i(TAG, "Recording started!!");
        } catch (IOException | RuntimeException e) {
//            Log.e(TAG, "startRecording: there was an exception", e);
            Toast.makeText(mContext, R.string.unable_start_recording, Toast.LENGTH_SHORT).show();
            if (e instanceof RuntimeException) {
//                Log.e(TAG, "startRecording failed, folding back audio source to default ", e);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("AudioSourceKey", mContext.getString(R.string.voice_communication));
                editor.apply();
            }
            mRecordingManager.stopRecording();
        }
    }

    private void stopRecording(){
        String recordingOutputFile = mRecordingManager.stopRecording();
        if (recordingOutputFile != null) {
            Recording recording = new Recording();
            recording.setUri(recordingOutputFile);

            int duration = 0;

            try {
                duration = Utils.getAudioDuration(mContext, recordingOutputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            recording.setDuration(duration);

            recording.setDate(System.currentTimeMillis());
            recording.setPhoneNumber(phoneNumber);
            recording.setIncoming(mIncoming);
            addRecordingToDb(recording);
            finishedNotification();
            phoneNumber = null;
            stopReceiver();
        }
    }



    private void recordingNotification() {

        Intent stopRecordIntent = new Intent();
        stopRecordIntent.setAction(getString(R.string.stop_recording_action));
        PendingIntent piStopRecord = PendingIntent.getBroadcast(this, 10, stopRecordIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action stopRecord = new NotificationCompat.Action.Builder(0, getString(R.string.stop_record_notification_action), piStopRecord).build();

        showAppNotification(getString(R.string.app_name), getString(R.string.now_recording, (mContact != null ? mContact.getDisplayName() : phoneNumber)), stopRecord, null, R.drawable.ic_recording);
    }



    private void finishedNotification() {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent piStartRecord = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        showAppNotification(mContext.getString(R.string.app_name), getString(R.string.recorded_with, (mContact != null ? mContact.getDisplayName() : phoneNumber)) , null, piStartRecord, R.drawable.ic_recording_pending);
    }


    private void showAppNotification(String title, String text, NotificationCompat.Action action, PendingIntent pendingIntent, int smallIcon) {
        androidx.core.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(smallIcon)
                .setPriority(Notification.PRIORITY_MAX);

        if (action != null) {
            builder.addAction(action);
        }

        Notification notification = builder.build();

        mNotificationManager.cancelAll();
        mNotificationManager.notify(RECORDING_NOTIFICATION, notification);
    }


    private void addRecordingToDb(Recording recording) {
//        Log.i(TAG, "addRecordingToDb: about to add recording to db");
        AppDatabase.getDatabase(mContext).recordingModel().insert(recording);

    }

    private void checkPreferences() {
        mRecorderConfig = new RecorderConfig();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        String audioFormat = prefs.getString("AutoTypeKey", getString(R.string.prefs_amr));
        switch (audioFormat) {
            case "AMR":
//                Log.i(TAG, "checkPreferences: AMR");
                mRecorderConfig.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                mRecorderConfig.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorderConfig.setFileExt(".amr");
                break;
            case "MP3":
//              Log.i(TAG, "checkPreferences: MP3");
                mRecorderConfig.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mRecorderConfig.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mRecorderConfig.setFileExt(".mp3");
                break;
            default:
//                Log.i(TAG, "checkPreferences: default AMR");
                mRecorderConfig.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                mRecorderConfig.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorderConfig.setFileExt(".amr");
                break;
        }


        String audioSource = prefs.getString("AudioSourceKey", mContext.getString(R.string.voice_communication));

        switch (audioSource) {
            case VOICE_COMMUNICATION:
//                Log.i(TAG, "audioSource: COMMUNICATION");
                mRecorderConfig.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                break;
            case VOICE_CALL:
//                Log.i(TAG, "audioSource: VOICE_CALL");
                mRecorderConfig.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                break;
            case VOICE_DOWNLINK:
//                Log.i(TAG, "audioSource: VOICE_DOWNLINK");
                mRecorderConfig.setAudioSource(MediaRecorder.AudioSource.VOICE_DOWNLINK);
                break;
            case VOICE_UPLINK:
//                Log.i(TAG, "audioSource: VOICE_UPLINK");
                mRecorderConfig.setAudioSource(MediaRecorder.AudioSource.VOICE_UPLINK);
                break;
            case MIC:
//                Log.i(TAG, "audioSource: VOICE_UPLINK");
                mRecorderConfig.setAudioSource(MediaRecorder.AudioSource.MIC);
                break;
            default:
//                Log.i(TAG, "audioSource: COMMUNICATION");
                mRecorderConfig.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                break;
        }
    }

    void startReceiver(){
        IntentFilter intentToReceiveFilter = new IntentFilter();

        intentToReceiveFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intentToReceiveFilter.addAction(getString(R.string.stop_recording_action));
        this.registerReceiver(callStateReceiver, intentToReceiveFilter, null,
                mHandler);
    }

    void stopReceiver(){
        unregisterReceiver(callStateReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mRecordingManager != null){
            mRecordingManager.clearRecorder();
        }

    }
}
