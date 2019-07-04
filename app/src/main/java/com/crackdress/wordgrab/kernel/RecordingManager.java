package com.crackdress.wordgrab.kernel;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.crackdress.wordgrab.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class RecordingManager {
    private static final String TAG = RecordingManager.class.getSimpleName();

    Context mContext;
    MediaRecorder mRecorder;
    SimpleDateFormat mSdf;
    String mPath;
    File mAudioFile;
    File mRecordingDir;
    String fileExt = ".amr";

    boolean isNowRecording;

    static RecordingManager instance;

    AudioManager audioManager;
    int originalAudioMode;

    private RecordingManager(Context context, String path) {
//        Log.i(TAG, "RecordingManager: created new manager");
        mContext = context;
        mSdf = new SimpleDateFormat("dd-MM-yyyy.hh.mm.ss", Locale.getDefault());
        mPath = path;

        mRecordingDir = new File(Environment.getExternalStorageDirectory(), "/" + mContext.getResources().getString(R.string.app_name).replaceAll("\\s+", ""));

        if (!mRecordingDir.exists()) {
            mRecordingDir.mkdirs();
        }

        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        originalAudioMode = audioManager.getMode();
//        Log.i(TAG, "RecordingManager created, output path: " + mRecordingDir);
    }

    public static RecordingManager getInstance(Context context, String path) {
        if (instance == null) {
            instance = new RecordingManager(context, path);
            return instance;
        } else {
            return instance;
        }
    }

    public void initRecorder(RecorderConfig recorderConfig) {

//        Log.i(TAG, "initRecorder: recorder config: " + recorderConfig);
        mRecorder = new MediaRecorder();
        mRecorder.setOnInfoListener((mr, what, extra) -> {
//            Log.i(TAG, "RecordingManager on new info: " + what);
            switch (what) {
                case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
//                    Log.i(TAG, "RecordingManager: unknown error");
                    break;
                case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                    break;
            }
        });

        mRecorder.setAudioSource(recorderConfig.audioSource);
        mRecorder.setOutputFormat(recorderConfig.outputFormat);
        mRecorder.setAudioEncoder(recorderConfig.audioEncoder);
        fileExt = recorderConfig.getFileExt();

//        Log.i(TAG, "Recorder initiated..");

    }

    public void startRecording() throws IOException, RuntimeException {
        String file_name = "CallRecord_" + mSdf.format(System.currentTimeMillis());
//        Log.i(TAG, "startRecording: " + file_name);
        mAudioFile = File.createTempFile(file_name, fileExt, mRecordingDir);
        mRecorder.setOutputFile(mAudioFile.getAbsolutePath());
//        Log.i(TAG, "Audio file path: " + mAudioFile.getAbsolutePath());

        mRecorder.prepare();

//        audioManager.setMode(AudioManager.MODE_IN_CALL);
//        audioManager.setSpeakerphoneOn(true);

        Log.i(TAG, "startRecording: ");

        mRecorder.start();
        isNowRecording = true;
//        Log.i(TAG, "Recording started..");
    }

    public String stopRecording() {

        if (isNowRecording) {
            clearRecorder();
            isNowRecording = false;
//            Log.i(TAG, "Recording stopped..");
//            audioManager.setMode(originalAudioMode);
        }

        if(mAudioFile != null){
            return mAudioFile.getAbsolutePath();
        }else{
            return null;
        }
    }

    public boolean isNowRecording() {
        return isNowRecording;
    }

    public void clearRecorder() {
        if (mRecorder != null) {

            if(isNowRecording){
                mRecorder.stop();
            }

            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }
}
