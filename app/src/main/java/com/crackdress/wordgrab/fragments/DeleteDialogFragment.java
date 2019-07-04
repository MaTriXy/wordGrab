package com.crackdress.wordgrab.fragments;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import com.crackdress.wordgrab.R;

public class DeleteDialogFragment extends DialogFragment {



    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        DeleteDialogEventListener eventListener = (DeleteDialogEventListener)getActivity();

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getArguments().getString("Title"))
                .setMessage(getArguments().getString("Message"))
                .setPositiveButton(R.string.dialog_ok, (dialog, which) -> eventListener.onDeleteDialogEvent())
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                }).create();
    }


    public interface DeleteDialogEventListener{
        void onDeleteDialogEvent();
    }


}
