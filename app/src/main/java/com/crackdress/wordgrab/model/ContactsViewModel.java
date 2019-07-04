package com.crackdress.wordgrab.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.crackdress.wordgrab.repository.AppDatabase;

import java.util.List;



public class ContactsViewModel extends AndroidViewModel {

    private LiveData<List<Contact>> mContacts;

    public ContactsViewModel(Application application) {
        super(application);
        loadContacts();
    }

    private void loadContacts() {
        mContacts = AppDatabase.getDatabase(this.getApplication()).contactsModel().queryAll();
    }

    public LiveData<List<Contact>> getContacts() {
        return mContacts;
    }

}
