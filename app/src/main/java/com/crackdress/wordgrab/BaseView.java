package com.crackdress.wordgrab;

import android.net.Uri;

public interface BaseView<T> {
    void setPresenter(T presenter);
    void showDeleteDialog();
    void showEditCommentDialog();
    void showRecordingDetails(long recordingId);
    void startShareIntent(Uri uri);
}
