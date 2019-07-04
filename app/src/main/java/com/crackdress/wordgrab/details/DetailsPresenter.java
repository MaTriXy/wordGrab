package com.crackdress.wordgrab.details;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import android.content.Context;
import android.net.Uri;

import com.crackdress.wordgrab.model.Recording;
import com.crackdress.wordgrab.repository.AppDatabase;
import com.crackdress.wordgrab.utils.Utils;



public class DetailsPresenter extends AndroidViewModel implements DetailsContract.Presenter {

    public static final String TAG = DetailsPresenter.class.getSimpleName();
    Recording mRecording;
    Context mContext;
    DetailsContract.View detailsView;


    public DetailsPresenter(Application application){
        super(application);
        mContext = application;
    }

    public void setDetailsView(DetailsContract.View view){
        detailsView = view;
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unSubscribe() {

    }

    @Override
    public void addCommentAction() {
        detailsView.showEditCommentDialog();
    }

    @Override
    public void updateRecording(Recording recording) {
        mRecording = recording;
        AppDatabase.getDatabase(mContext).recordingModel().update(mRecording);
        loadRecording(recording.getId());
    }

    @Override
    public void deleteAction() {
        detailsView.showDeleteDialog();
    }

    @Override
    public void shareRecordingAction(String path) {
        Uri uri = Utils.getPathUri(mContext, path);
        detailsView.startShareIntent(uri);
    }

    @Override
    public void loadRecording(long recordingId) {
//        Log.i(TAG, "loadRecording: ");
        mRecording = AppDatabase.getDatabase(mContext).recordingModel().getRecordingById(recordingId);
        detailsView.showRecordingDetails(mRecording);
//        Log.i(TAG, "loadRecording: " + mRecording);
    }

    @Override
    public void addToContactClicked() {
        detailsView.addToContacts(mRecording.getPhoneNumber());
    }

    @Override
    public void deleteRecording() {
        AppDatabase.getDatabase(mContext).recordingModel().delete(mRecording);
        loadRecording(mRecording.getId());
    }

    @Override
    public void callActionClicked() {
        detailsView.openDialer(mRecording.getPhoneNumber());
    }

    @Override
    public void smsActionClicked() {
        detailsView.sendMessage(mRecording.getPhoneNumber());
    }

    @Override
    public void whatsappActionClicked() {
        detailsView.sendWhatsapp(mRecording.getPhoneNumber());

    }


}
