package com.crackdress.wordgrab.settings;

import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.LifecycleRegistryOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.crackdress.wordgrab.DividerItemDecoration;
import com.crackdress.wordgrab.R;
import com.crackdress.wordgrab.adapters.ContactsSettingsAdapter;
import com.crackdress.wordgrab.model.ContactsViewModel;
import com.crackdress.wordgrab.repository.AppDatabase;
import com.crackdress.wordgrab.fragments.DeleteDialogFragment;
import com.crackdress.wordgrab.model.Contact;

import java.util.List;

public class ContactsActivity extends AppCompatActivity implements ContactsSettingsAdapter.ContactOnClickListener,
        LifecycleRegistryOwner, DeleteDialogFragment.DeleteDialogEventListener {

    private static final int CONTACT_PICKER_RESULT = 110;
    private static final String TAG = ContactsActivity.class.getSimpleName();

    RecyclerView rvContacts;
    ContactsSettingsAdapter mAdapter;
    Contact mSelectedContact;
    ContactsViewModel mViewModel;
    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.addItemDecoration(new DividerItemDecoration(this));

        mAdapter = new ContactsSettingsAdapter(this);
        rvContacts.setAdapter(mAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent it = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(it, CONTACT_PICKER_RESULT);
        });
        mViewModel = ViewModelProviders.of(this).get(ContactsViewModel.class);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.getContacts().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(@Nullable List<Contact> contacts) {
//                Log.i(TAG, "onChanged: got " + contacts.size() + " items from LiveData database");
                mAdapter.update(contacts);
            }
        });
    }

    private void refreshDisplay() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_PICKER_RESULT && resultCode == RESULT_OK && null != data) {
            Uri contactUri = data.getData();
//            Log.i(TAG, "onActivityResult, Contact Selected: " + contactUri.toString());

            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            Contact contact = new Contact();
            if (cursor.moveToFirst()) {
                contact.setDisplayName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                contact.setThumbUri(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)));
                contact.setId(cursor.getLong(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID)));
                contact.setUri(contactUri.toString());
            }
            cursor.close();

            AppDatabase.getDatabase(this).contactsModel().insert(contact);
//            dao.open();
//            dao.insert(contact);
//            dao.close();
            //do what you want...
        }
    }


    @Override
    public void onContactItemClicked(int position) {
        mSelectedContact = mViewModel.getContacts().getValue().get(position);
        DeleteDialogFragment dialog = new DeleteDialogFragment();
        Bundle b = new Bundle();
        b.putString("Title", getString(R.string.delete_contact_dialog_title));
        b.putString("Message", getString(R.string.delete_contact_dialog_message));
        dialog.setArguments(b);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onDeleteDialogEvent() {
//        Log.i(TAG, "onDeleteDialogEvent: about to delete contact: " + mSelectedContact);
        AppDatabase.getDatabase(this).contactsModel().delete(mSelectedContact);
        mSelectedContact = null;
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }
}
