package com.crackdress.wordgrab;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;

public class BaseActivity extends AppCompatActivity {

    public static final String TAG = BaseActivity.class.getSimpleName();

    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ");
    }
}
