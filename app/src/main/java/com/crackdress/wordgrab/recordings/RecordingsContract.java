package com.crackdress.wordgrab.recordings;


import com.crackdress.wordgrab.BasePresenter;
import com.crackdress.wordgrab.BaseView;
import com.crackdress.wordgrab.model.Recording;

import java.util.List;

public interface RecordingsContract {

    interface View extends BaseView {
        void showRecordings(List<Recording> recordings);
        void showNoRecordings();
        void selectRecordingItem(int position);
        void startActionMode();
    }

    interface Presenter extends BasePresenter {

        void loadRecordings(String selection, String[] selectionArgs);
        void deleteRecordings(List<Recording> selectedRecordings);

        void recordingItemClicked(Recording recording );
        void recordingItemSelected(int position);
        void recordingItemLongClick();

        void incomingOptionClicked();
        void outgoingOptionClicked();
        void queryTextChange(String text);
    }

}
