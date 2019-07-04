package com.crackdress.wordgrab.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.crackdress.wordgrab.repository.AppDatabase;

import java.util.List;


public class RecordingViewModel extends AndroidViewModel {

    public static final String TAG = RecordingViewModel.class.getSimpleName();

    private LiveData<List<Recording>> mRecordings;

    public RecordingViewModel(Application application) {
        super(application);
        loadRecordings();
    }

    private void loadRecordings() {
        //getApplication is available only because this ViewModel is AndroidViewModel which gets the context in the constructor
//        Log.i(TAG, "loadRecordings..");
        mRecordings = AppDatabase.getDatabase(this.getApplication()).recordingModel().queryAll();
    }

    public LiveData<List<Recording>> getRecordings() {
        return mRecordings;
    }

    @Override
    protected void onCleared() {
//        Log.i(TAG, "onCleared");
        super.onCleared();
    }
}
