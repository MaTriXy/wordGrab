package com.crackdress.wordgrab;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;


public class ShareDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ShareDialogEventListener eventListener = (ShareDialogEventListener)getActivity();

        return new AlertDialog.Builder(getActivity())
                // set dialog icon
                .setIcon(android.R.drawable.stat_notify_error)
                // set Dialog Title
                .setTitle(getArguments().getString("Title"))
                // Set Dialog Message
                .setMessage(getArguments().getString("Message"))

                // positive button
                .setPositiveButton("OK", (dialog, which) -> eventListener.onShareDialogEvent())
                // negative button
                .setNegativeButton("Cancel", (dialog, which) -> {
                }).create();
    }


    interface ShareDialogEventListener{
        void onShareDialogEvent();
    }

}
