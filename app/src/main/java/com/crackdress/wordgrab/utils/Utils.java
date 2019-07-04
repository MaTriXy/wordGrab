package com.crackdress.wordgrab.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.widget.Toast;

import com.crackdress.wordgrab.R;
import com.crackdress.wordgrab.model.Contact;
import com.crackdress.wordgrab.model.Recording;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Utils {

    public static int getAudioDuration(Context context, String path) throws IOException {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        mmr.release();
        return Integer.parseInt(duration);
    }

    public static List<Recording> getMockRecordings() {
        Recording rec1 = new Recording();
        rec1.setUri(("/storage/emulated/0/FreeCallRecorder/CallRecord_10-05-2017.03:59:352033539671.amr"));
        rec1.setPhoneNumber(("+972-53-5684966"));
        rec1.setIncoming((true));
        rec1.setDuration((120000));
        rec1.setComment(("Call from Ariela"));

        Recording rec2 = new Recording();
        rec2.setUri(("/storage/emulated/0/FreeCallRecorder/CallRecord_10-05-2017.03:59:352033539671.amr"));
        rec2.setPhoneNumber(("+972-53-5684966"));
        rec2.setIncoming((false));
        rec2.setDuration((180000));
        rec2.setComment(("Call to the bank"));

        List<Recording> recordings = new ArrayList<>();

        recordings.add(rec1);
        recordings.add(rec2);

        return recordings;
    }

    public static Contact isContactExists(Context context, String number) {
/// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
        Contact contact = null;
        try {
            if (cur.moveToFirst()) {
                contact = new Contact();
                contact.setId(cur.getInt(cur.getColumnIndex(ContactsContract.PhoneLookup._ID)));
                contact.setDisplayName(cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)));
                return contact;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return contact;
    }

    public static boolean searchTextInRecording(Recording recording, String text) {

        boolean textFounded = false;
        String number = recording.getPhoneNumber();
        if (!number.equals("Unknown")){

            String contact = recording.getContactName();

            if (contact == null ) {
                textFounded = number.contains(text);
            } else {
                textFounded = contact.contains(text);
            }
        }
        String desc = recording.getComment();
        if (desc != null && desc.contains(text)) {
            textFounded = true;
        }

        return textFounded;
    }


    public static void addToContacts(final Context context, final String phoneNumber) {

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);


        context.startActivity(intent);
    }

    public static Uri getPathUri(Context context, String path){
        File file = new File(path);
        if( !file.exists()){
            Toast.makeText(context, R.string.file_not_found, Toast.LENGTH_SHORT).show();
            return null;
        }

        String mimeType = "audio/*" + path.substring(path.lastIndexOf("."));


        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Recording sharing");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, new File(path).getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, "audio/*");
        //String path = new File(new File(trip.getKml()).getParent()) + "/sharing/sharedTrip.png";
        values.put(MediaStore.Images.Media.DATA, path);
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);

        if (uri == null) {
//            Log.i(TAG, "uri is null"); //It means that this file was already in the MediaStore, therefore it can't inserted again. Need to get it's Uri based on the title.
            Uri filesUri = MediaStore.Files.getContentUri("external");
            String[] projection = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.TITLE, MediaStore.MediaColumns.DISPLAY_NAME};
            String selection = MediaStore.MediaColumns.DATA + " = ?";
            String[] args = {path};

            //Updating the display name every share (maybe the user changed the name of the trip)
            ContentValues updateContentValues = new ContentValues();
            updateContentValues.put(MediaStore.Images.Media.DISPLAY_NAME, new File(path).getName());
            int updatedRows = context.getContentResolver().update(filesUri, updateContentValues, selection, args);
//            Log.i(TAG, "Row updated now: " + updatedRows);


            Cursor c = context.getContentResolver().query(filesUri, projection, selection, args, null);
            if (c.getCount() == 1) {
//                Log.i(TAG, "item already exists! getting the already exists uri...");
                c.moveToFirst();
                long rowId = c.getLong(c.getColumnIndex(MediaStore.MediaColumns._ID));
                String title = c.getString(c.getColumnIndex(MediaStore.MediaColumns.TITLE));
//                Log.i(TAG, "Title is: " + title);
//                Log.i(TAG, "Display name: " + c.getString(c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)));
                c.close();
                uri = MediaStore.Files.getContentUri("external", rowId);
//                Log.i(TAG, "refresh scan force uri=" + uri);
            } else {
//                Log.i(TAG, "Keep trying...");
            }
        }

//        Log.i(TAG, "About to save the image in path: " + uri.getPath());

        return uri;
    }
}
