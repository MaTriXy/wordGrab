package com.crackdress.wordgrab.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crackdress.wordgrab.R;

public class EditDialogFragment extends DialogFragment {

    private static final int COMMENT_MAX_LENGTH = 25;
    EditDialogEventListener eventListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        eventListener = (EditDialogEventListener)getActivity();
        return buildDialog(getActivity());
    }




    public AlertDialog buildDialog(Context context) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setIcon(ActivityCompat.getDrawable(context, R.mipmap.ic_launcher));
        alert.setTitle(getArguments().getString("Title"));
        alert.setMessage(getArguments().getString("Message"));
        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(parms);

        // Set an EditText view to get user input
        final EditText input = new EditText(context);
        final TextView chars = new TextView(context);

        input.setMaxLines(1);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(
                COMMENT_MAX_LENGTH)});

        chars.setPadding(5, 0, 0, 2);
        String comment = getArguments().getString("Comment");
        if (comment != null) {  //This means that recording had a comment already..
            chars.setText(comment.length() + "/" + COMMENT_MAX_LENGTH);
            input.setText(comment);
        } else {
            chars.setText("0/" + COMMENT_MAX_LENGTH);
            input.setText("");
        }
        input.addTextChangedListener(new TextWatcher() {
            // StringBuilder builder = new StringBuilder();
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String text = input.getText().toString();
                chars.setText(text.length() + "/" + COMMENT_MAX_LENGTH);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        LinearLayout.LayoutParams tv1Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tv1Params.bottomMargin = 5;
        layout.addView(input, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(chars, tv1Params);

        alert.setView(layout);

        alert.setPositiveButton(R.string.dialog_ok,
                (dialog, whichButton) -> {
                    String value = input.getText().toString();
                    eventListener.onEditDone(value);
                    // Toast.makeText(context, value + " entered..",
                    // Toast.LENGTH_LONG).show();
                 });

        alert.setNegativeButton(R.string.dialog_cancel,
                (dialog, whichButton) -> {
                    // Canceled.
                });

        alert.setTitle(getString(R.string.app_name));
        alert.setIcon(ActivityCompat.getDrawable(context, R.mipmap.ic_launcher));
        return alert.create();
    }

    public interface EditDialogEventListener{
        void onEditDone(String comment);
    }

}
