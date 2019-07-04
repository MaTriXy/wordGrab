package com.crackdress.wordgrab.kernel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crackdress.wordgrab.R;
import com.crackdress.wordgrab.repository.AppDatabase;
import com.crackdress.wordgrab.model.Contact;
import com.crackdress.wordgrab.utils.Utils;

import static com.crackdress.wordgrab.kernel.AutoRecording.AUTO_OFF;
import static com.crackdress.wordgrab.kernel.AutoRecording.ONLY_CONTACTS;



public class CallReceiver extends BroadcastReceiver {
    public static final String TAG = CallReceiver.class.getSimpleName();

    public static final int RECORDING_NOTIFICATION = 10;
    public static final int PENDING_NOTIFICATION = 20;

    static AppPhoneStateListener phoneStateListener;  //It is essential that the phoneListener will be static static so it's instance will be available for following broadcasts
    boolean mIncoming = false;  //Keeping the mIncoming status static is helped to maintain the call mode when manually starting recording (from notification action)

    AutoRecording autoMode = AutoRecording.RECORD_ALL;
    String mPhoneNumber;
    CallInfo mCallInfo;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            if (phoneStateListener == null) {
//                Log.i(TAG, "onReceive: create phoneStateListener...");
                try {
                    phoneStateListener = new AppPhoneStateListener(context);
                    TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    telephony.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                } catch (Exception e) {
                    Log.e(TAG, "onReceive: " + e.getMessage());
                }
            }
        }

        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
//            Log.i(TAG, "onReceive: new outgoing call");
            mIncoming = false;
            CallInfo info = new CallInfo();
            info.phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            info.incoming = mIncoming;
            saveCallInfoToPreferences(context, info);
        }


        //This is for manual explicit recording from notification action (when there is a call but it's not recorded)
        if (intent.getAction().equals(context.getString(R.string.start_recording_action))) {
            checkPreferences(context);
            startRecordingService(context, mCallInfo);
        }
    }


    private class AppPhoneStateListener extends android.telephony.PhoneStateListener {

        Context context;

        AppPhoneStateListener(Context c) {
            super();
            context = c;
//            Log.i(TAG, "AppPhoneStateListener: created...");
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);


            if (state == TelephonyManager.CALL_STATE_OFFHOOK) { //Call answered...

                Contact contact = null;
                boolean isExceptionContact = false;
                checkPreferences(context);

//                Log.i(TAG, "onCallStateChanged state = " + state + " phone number:" + mCallInfo.phoneNumber);

                contact = Utils.isContactExists(context, mCallInfo.phoneNumber);

                if (contact != null) {
                    isExceptionContact = (AppDatabase.getDatabase(context).contactsModel().getContactById(contact.getId()) != null);
                }

                String notifyTitle = context.getString(R.string.call_with, (contact != null ? contact.getDisplayName() : mCallInfo.phoneNumber));

                switch (autoMode) {  //Cases breaks only if starting recording, else it should reach the default and show a notification for manual recording.
                    case AUTO_OFF:
//                        Log.i(TAG, "onCallStateChanged: auto mode is off!! ");
                        manualNotification(context, notifyTitle);
                        break;
                    case RECORD_ALL:
//                        Log.i(TAG, "onCallStateChanged: recording all! ");
                        startRecordingService(context, mCallInfo);
                        break;
                    case WITHOUT_CONTACTS:
                        if (contact == null) {
//                            Log.i(TAG, "onCallStateChanged: Not a mContact, it will be recorded");
                            startRecordingService(context, mCallInfo);
                        } else {
                            if (isExceptionContact) {
//                                Log.i(TAG, "onCallStateChanged: This mContact is an exception, it will be recorded");
                                startRecordingService(context, mCallInfo);
                            } else {
//                                Log.i(TAG, "onCallStateChanged: This is a contact, no recording");
                                manualNotification(context, notifyTitle);
                            }
                        }
                        break;
                    case ONLY_CONTACTS:
                        if (contact != null) {
                            startRecordingService(context, mCallInfo);
                            break;
                        } else {
                            manualNotification(context, notifyTitle);
                        }
                    default: {
                        manualNotification(context, notifyTitle);
                    }
                }
            }

            if (state == TelephonyManager.CALL_STATE_RINGING) {
//                Log.i(CallReceiver.TAG, "onCallStateChanged: " + "ringing...");
                mIncoming = true;

                if (incomingNumber == null || incomingNumber.isEmpty()) {  //On KitKat, incoming number is empty when it's unknown number
                    incomingNumber = "Unknown";
                }

                CallInfo info = new CallInfo();
                info.phoneNumber = incomingNumber;
                info.incoming = mIncoming;
                saveCallInfoToPreferences(context, info);
            }

            if (state == TelephonyManager.CALL_STATE_IDLE) { //Call ended...
//                Log.i(CallReceiver.TAG, "onCallStateChanged: Hangup");
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(PENDING_NOTIFICATION);
            }
        }
    }

    private void startRecordingService(Context context, CallInfo callInfo) {

//        Log.i(TAG, "startRecordingService ");
        Intent serviceIntent = new Intent(context,
                RecordingService.class);
        serviceIntent.putExtra(context.getString(R.string.call_info_extra), callInfo);
        context.startService(serviceIntent);
    }

    private void checkPreferences(Context context) {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        mCallInfo = new CallInfo();
        mCallInfo.phoneNumber = prefs.getString(context.getString(R.string.phone_number_key), "Unknown");
        mCallInfo.incoming = prefs.getBoolean(context.getString(R.string.incoming_key), false);

        boolean autoRecording = prefs.getBoolean("AutoKey", true);
        if (autoRecording) {
//            Log.i(TAG, "checkPreferences: auto recording true!");

            String autoPref = prefs.getString("AutoOptionsKey", context.getString(R.string.auto_all));

            switch (autoPref) {
                case "ALL":
                    autoMode = AutoRecording.RECORD_ALL;
//                    Log.i(TAG, "checkPreferences: Recording all calls!!");
                    break;
                case "NO_CONTACTS":
                    autoMode = AutoRecording.WITHOUT_CONTACTS;
//                    Log.i(TAG, "checkPreferences: Not recording contacts!!");
                    break;
                case "ONLY_CONTACTS":
                    autoMode = ONLY_CONTACTS;
//                    Log.i(TAG, "checkPreferences: Recording only contacts!!");
                    break;
                default:
                    autoMode = AutoRecording.RECORD_ALL;
            }

        } else {
            autoMode = AUTO_OFF;
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void manualNotification(Context context, String title) {

        Intent startRecordIntent = new Intent(context, CallReceiver.class);
        startRecordIntent.setAction(context.getString(R.string.start_recording_action));
        PendingIntent piStartRecord = PendingIntent.getBroadcast(context, 10, startRecordIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action startRecord = new NotificationCompat.Action.Builder(0, context.getString(R.string.start_recording_notification_action), piStartRecord).build();

        androidx.core.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentText(context.getString(R.string.app_name))
                .setContentIntent(piStartRecord)
                .setContentTitle(title)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_recording_pending)
                .addAction(startRecord)
                .setPriority(Notification.PRIORITY_MAX);


        Notification notification = builder.build();
        //Send notification

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();
        notificationManager.notify(PENDING_NOTIFICATION, notification);

    }

    void saveCallInfoToPreferences(Context context, CallInfo info) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.phone_number_key), info.phoneNumber);
        editor.putBoolean(context.getString(R.string.incoming_key), info.incoming);
        editor.apply();
    }
}


