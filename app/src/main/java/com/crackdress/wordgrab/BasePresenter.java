package com.crackdress.wordgrab;

import com.crackdress.wordgrab.model.Recording;


public interface BasePresenter {
    void subscribe();
    void unSubscribe();
    void addCommentAction();
    void updateRecording(Recording recording);
    void deleteAction();
    void shareRecordingAction(String path);
}
