package com.crackdress.wordgrab.recordings;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.annimon.stream.Stream;
import com.crackdress.wordgrab.model.RecordingViewModel;
import com.crackdress.wordgrab.repository.AppDatabase;
import com.crackdress.wordgrab.model.Recording;
import com.crackdress.wordgrab.utils.Utils;

import java.io.File;
import java.util.List;

public class RecordingsPresenter implements RecordingsContract.Presenter {

    public static final String TAG = RecordingsPresenter.class.getSimpleName();

    AppCompatActivity mContext;
    RecordingsContract.View mRecordingsView;
    RecordingViewModel mRecordingViewModel;
    LifecycleOwner mLifeCycleOwner;


    public RecordingsPresenter(AppCompatActivity context, RecordingsContract.View view, LifecycleOwner owner){
        Log.i(TAG, "MoviesPresenter created..");
        mContext = context;
        mRecordingsView = view;
        mRecordingViewModel = ViewModelProviders.of(mContext).get(RecordingViewModel.class);
        mLifeCycleOwner = owner;
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unSubscribe() {

    }

    @Override
    public void loadRecordings(String selection, String[] selectionArgs)
    {
        mRecordingViewModel.getRecordings().observe(mLifeCycleOwner, recordings -> {
            Log.i(TAG, "onChanged: got " + recordings.size() + " items from LiveData database");
            mRecordingsView.showRecordings(recordings);
        });
    }

    @Override
    public void deleteRecordings(List<Recording> selectedRecordings) {

        for(Recording recording : selectedRecordings){
            AppDatabase.getDatabase(mContext).recordingModel().delete(recording);
            File file = new File(recording.getUri());
            if(file.exists()){
                file.delete();
            }
        }
    }


    @Override
    public void recordingItemClicked(Recording recording) {

        Log.i(TAG, "recordingItemClicked: recording ID = " + recording.getId());
        mRecordingsView.showRecordingDetails(recording.getId());

    }

    @Override
    public void recordingItemSelected(int position) {
        mRecordingsView.selectRecordingItem(position);
    }

    @Override
    public void recordingItemLongClick() {
        mRecordingsView.startActionMode();
    }

    @Override
    public void updateRecording(Recording recording) {
        AppDatabase.getDatabase(mContext).recordingModel().update(recording);
    }

    @Override
    public void shareRecordingAction(String path) {
        shareRecording(path);
    }

    @Override
    public void incomingOptionClicked() {
        mRecordingsView.showRecordings(Stream.of(mRecordingViewModel.getRecordings().getValue())
                .filter(recording -> (recording.getIncoming())).collect(com.annimon.stream.Collectors.toList()));
    }

    @Override
    public void outgoingOptionClicked() {
        mRecordingsView.showRecordings(Stream.of(mRecordingViewModel.getRecordings().getValue())
                .filter(recording -> (!recording.getIncoming())).collect(com.annimon.stream.Collectors.toList()));
    }

    @Override
    public void deleteAction() {
        mRecordingsView.showDeleteDialog();
    }

    @Override
    public void addCommentAction() {
        mRecordingsView.showEditCommentDialog();
    }

    @Override
    public void queryTextChange(String text) {
        mRecordingsView.showRecordings(Stream.of(mRecordingViewModel.getRecordings().getValue())
                .filter(recording -> (Utils.searchTextInRecording(recording, text))).collect(com.annimon.stream.Collectors.toList()));
    }

    public void shareRecording(String recordingPath) {
//        Log.i(TAG, "About to share recording: " + recordingPath);

        Uri pathUri = Utils.getPathUri(mContext, recordingPath);

        mRecordingsView.startShareIntent(pathUri);
    }

}
