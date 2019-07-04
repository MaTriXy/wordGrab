package com.crackdress.wordgrab.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlaybackService extends Service {

    public static final String TAG = PlaybackService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();


    MediaPlayer mMediaPlayer;
    private int mAudioCurrentPosition;
    private TimerTask mTimerTask;
    private Timer mTimer;

    private ServiceEventListener mEventListener;


    public PlaybackService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();
//        Log.i(TAG, "onCreate ");

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mp1 -> {
//            Log.i(TAG, "MediaPlayer onPrepared");
          //  startTimer();
        });

        mMediaPlayer.setOnCompletionListener(mp -> {
            stopTimer();
            if(mEventListener != null){
                mEventListener.onAudioCompleted();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
//        Log.i(TAG, "onBind: ");
        return mBinder;
    }
    

    @Override
    public void onDestroy() {
//        Log.i(TAG, "onDestroy");

        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        super.onDestroy();
    }

    public void prepareMediaPlayer(String path) throws FileNotFoundException, IOException{

//        Log.i(TAG, "prepareMediaPlayer");
        FileInputStream fis;
            fis = new FileInputStream(path);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(fis.getFD());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            fis.close();
            mMediaPlayer.prepareAsync();
    }


    public void play() {
        mMediaPlayer.start();
        startTimer();
    }

    public void pause() {
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            mAudioCurrentPosition = mMediaPlayer.getCurrentPosition();
        }
    }

    public void stop() {
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }
        if(mTimer != null){
            mTimer.cancel();
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        initializeTimerTask();
        mTimer.schedule(mTimerTask, 0, 1000);
    }


    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void initializeTimerTask() {

        mTimerTask = new TimerTask() {

            public void run() {

                mAudioCurrentPosition = mMediaPlayer.getCurrentPosition() / 1000;
//                Log.i(TAG, "progress: " + mAudioCurrentPosition);
                if (mEventListener != null) {
                    mEventListener.onPlaybackProgress(mAudioCurrentPosition);
                }
            }
        };
    }

    public void playbackSeek(int seekTo) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(seekTo);
        }
    }

    public boolean isPlaying() {
        return (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    public int getAudioCurrentPosition(){
        return mAudioCurrentPosition;
    }


    public class LocalBinder extends Binder {
        public PlaybackService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlaybackService.this;
        }
    }

    public void setOnEventListener(ServiceEventListener eventListener) {
//        Log.i(TAG, "setOnEventListener");
        mEventListener = eventListener;
    }

    public interface ServiceEventListener {
        void onPlaybackProgress(int progress);
        void onAudioPlaying();
        void onAudioCompleted();
    }
}
