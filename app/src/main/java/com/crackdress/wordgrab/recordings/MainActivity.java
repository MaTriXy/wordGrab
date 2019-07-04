package com.crackdress.wordgrab.recordings;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.LifecycleRegistryOwner;
import androidx.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crackdress.wordgrab.AppDeviceAdmin;
import com.crackdress.wordgrab.model.RecordingViewModel;
import com.crackdress.wordgrab.fragments.DeleteDialogFragment;
import com.crackdress.wordgrab.details.DetailsActivity;
import com.crackdress.wordgrab.fragments.EditDialogFragment;
import com.crackdress.wordgrab.settings.SettingsActivity;
import com.crackdress.wordgrab.R;
import com.crackdress.wordgrab.adapters.RecordingsRecyclerAdapter;
import com.crackdress.wordgrab.model.Recording;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements RecordingsRecyclerAdapter.RecyclerItemClickListener,
        ActionMode.Callback, DeleteDialogFragment.DeleteDialogEventListener, RecordingsContract.View,
        EditDialogFragment.EditDialogEventListener, LifecycleRegistryOwner {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 100;
    private static final int DEVICE_ADMIN_REQUEST_REQUEST_CODE = 110;
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String RECORDING_EXTRA = "RecordingId";

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;
    RecyclerView rvRecordings;
    RecordingsRecyclerAdapter mRecordingsRecyclerAdapter;
    TextView tvHeader;
    ActionMode mActionMode;
    Toolbar mToolbar;
    int mItemsCount;
    List<Recording> mSelectedRecordings;

    DrawerLayout mDrawerLayout;
    NavigationView mNavView;
    ActionBarDrawerToggle mActionBarDrawerToggle;

    RecordingsPresenter mPresenter;
    SearchView mSearchView;
    ProgressBar pb;

    RecordingViewModel mRecordingViewModel;
    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        checkPermissions();
        //
        setDevicePolicy();

        setViews();
        mSelectedRecordings = new ArrayList<>();
        mPresenter = new RecordingsPresenter(this, this, this);
        mRecordingViewModel = ViewModelProviders.of(this).get(RecordingViewModel.class);
        subscribeUiRecordings();
        mPresenter.loadRecordings(null, null);
    }


    private void setViews() {

        rvRecordings = (RecyclerView) findViewById(R.id.rvRecordingList);
        rvRecordings.setLayoutManager(new LinearLayoutManager(this));

        rvRecordings.addItemDecoration(new com.crackdress.wordgrab.DividerItemDecoration(MainActivity.this));

        mRecordingsRecyclerAdapter = new RecordingsRecyclerAdapter(this);
        mRecordingsRecyclerAdapter.setItemClickedListener(this);
        rvRecordings.setAdapter(mRecordingsRecyclerAdapter);

        tvHeader = (TextView) findViewById(R.id.tvListHeader);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_closed, R.string.drawer_opened) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                Log.i(TAG, "onDrawerOpened");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
//                Log.i(TAG, "onDrawerClosed");
            }
        };

        mActionBarDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mNavView = (NavigationView) findViewById(R.id.nvView);
        mNavView.setNavigationItemSelectedListener(item -> {
//            Log.i(TAG, "onNavigationItemSelected");

            int itemId = item.getItemId();
            if (itemId == R.id.action_settings) {
//                Log.i(TAG, "onNavigationItemSelected: about to open settings screen");
                openSettings();
            }
            //mPresenter.drawerSettingsItemClicked(itemId);
            return true;
        });
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void subscribeUiRecordings() {

    }


    private void setDevicePolicy() {

        try {
            // Initiate DevicePolicyManager.
            mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            mComponentName = new ComponentName(this, AppDeviceAdmin.class);

            if (!mDevicePolicyManager.isAdminActive(mComponentName)) {
//                Log.i(TAG, "setDevicePolicy");
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
                startActivityForResult(intent, DEVICE_ADMIN_REQUEST_REQUEST_CODE);
            } else {
//                Log.i(TAG, "setDevicePolicy passed...");
                // mDPM.lockNow();
                // Intent intent = new Intent(MainActivity.this,
                // TrackDeviceService.class);
                // startService(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {

        String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.MODIFY_AUDIO_SETTINGS};

        boolean permissionCheck = hasPermissions(this, permissions);
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
//        Log.i(TAG, "checkPermissions permissionCheck " + permissionCheck);
        if (!permissionCheck) {
            // User may have declined earlier, ask Android if we should show him a reason

            // request the permission.
            // CALLBACK_NUMBER is a integer constants
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);// The callback method gets the result of the request.

        } else {
// got permission use it
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, do your work....
                } else {
                    // permission denied
                    // Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Phone state permission required for this app, goodbye", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEVICE_ADMIN_REQUEST_REQUEST_CODE == requestCode) {
//            Log.i(TAG, "onActivityResult");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) item.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                Toast.makeText(MainActivity.this, "SearchOnQueryTextSubmit " + query, Toast.LENGTH_SHORT).show();
                if (!mSearchView.isIconified()) {
                    mSearchView.setIconified(true);
                }
                item.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                Log.i(TAG, "onQueryTextChange: " + newText);
                mPresenter.queryTextChange(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
            //This action will open the navigation drawer
            return true;
        }

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_all) {
            mPresenter.loadRecordings(null, null);
            // refreshDisplay();
        }

        if (id == R.id.action_incoming) {
            //    mPresenter.loadRecordings(DbContract.RecordingsEntry.COL_INCOMING, new String[]{RecordingsDao.SQL_TRUE});
            mPresenter.incomingOptionClicked();
            mRecordingsRecyclerAdapter.clearSelectedItems();
//            refreshDisplay();
        }

        if (id == R.id.action_outgoing) {
            //  mPresenter.loadRecordings(DbContract.RecordingsEntry.COL_INCOMING, new String[]{RecordingsDao.SQL_FALSE});
//            refreshDisplay();
            mPresenter.outgoingOptionClicked();
            mRecordingsRecyclerAdapter.clearSelectedItems();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(int position, Recording recording) {
        if (mActionMode != null) {
            mPresenter.recordingItemSelected(position);
        } else {
            mPresenter.recordingItemClicked(recording);
        }
    }

    @Override
    public void onItemLongClick(int position, Recording recording) {
//        Log.i(TAG, "onItemLongClick: ");
        mPresenter.recordingItemLongClick();
        mPresenter.recordingItemSelected(position);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(ActivityCompat.getColor(this, R.color.colorPrimaryDark));

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mToolbar.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            mToolbar.setLayoutParams(params);
        }


        mode.setTitle("Selected");
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_delete:
                mPresenter.deleteAction();
                break;
            case R.id.action_share:
                mPresenter.shareRecording(mSelectedRecordings.get(0).getUri());
                break;
            case R.id.action_edit:
                mPresenter.addCommentAction();
                break;
        }
        return false;
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mRecordingsRecyclerAdapter.clearActivatedItems();
        mActionMode = null;
    }

    @Override
    public void setPresenter(Object presenter) {

    }

    @Override
    public void showRecordings(List<Recording> recordings) {
        runOnUiThread(() -> {
            mRecordingsRecyclerAdapter.updateRecordings(recordings);
            Log.i(TAG, "showRecordings");
            pb.setVisibility(View.GONE);
            if (recordings.size() > 0) {
                tvHeader.setText(getString(R.string.calls, recordings.size()));
            } else {
                tvHeader.setText(R.string.no_calls);
            }
        });
    }

    @Override
    public void showNoRecordings() {

    }

    @Override
    public void showDeleteDialog() {
        DeleteDialogFragment dialogFragment = new DeleteDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Title", getString(R.string.delete_dialog_title));
        bundle.putString("Message", getString(R.string.delete_dialog_message));

        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "Tag");
    }

    @Override
    public void showEditCommentDialog() {
        EditDialogFragment dialog = new EditDialogFragment();
        Bundle b = new Bundle();
        b.putString("Title", getString(R.string.comment_dialog_title));
        b.putString("Message", getString(R.string.comment_dialog_message));
        b.putString("Comment", mSelectedRecordings.get(0).getComment());
        dialog.setArguments(b);
        dialog.show(getSupportFragmentManager(), "EditDialog");
    }

    @Override
    public void showRecordingDetails(long recordingId) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(RECORDING_EXTRA, recordingId);
        startActivity(intent);
    }

    @Override
    public void selectRecordingItem(int position) {
        recyclerToggleSelection(position);
    }

    @Override
    public void startActionMode() {
        mActionMode = startSupportActionMode(this);
    }

    @Override
    public void startShareIntent(Uri uri) {
        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.setType("audio/*");

        intentShare.putExtra(Intent.EXTRA_STREAM, uri);
        // share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + ", " + tripTitle);
        startActivity(Intent.createChooser(intentShare, "Sharing Recording"));
    }

    @Override
    public void onEditDone(String comment) {

        if (mActionMode != null) {
            mActionMode.finish();
        }

        if (comment != null && comment.length() > 0) {
            Recording rec = mSelectedRecordings.get(0);
            rec.setComment(comment);
            mPresenter.updateRecording(rec);
        }

    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }


    public class DeviceAdminDemo extends DeviceAdminReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

        }

        public void onEnabled(Context context, Intent intent) {
        }


        public void onDisabled(Context context, Intent intent) {
        }
    }

    public static boolean hasPermissions(Activity context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {

                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                        // show an explanation to the user
                        // Good practise: don't block thread after the user sees the explanation, try again to request the permission.
                        Toast.makeText(context, "Asking again permission: " + permission, Toast.LENGTH_SHORT).show();
                    }

                    return false;
                }
            }
        }
        return true;
    }


    private void recyclerToggleSelection(int idx) {
        mRecordingsRecyclerAdapter.toggleActivatedItem(idx);
        if (mActionMode != null) {
            mItemsCount = mRecordingsRecyclerAdapter.getSelectedItemsCount();

            if (mItemsCount == 0) {
                mActionMode.finish();
            } else {
                String title = "Selected " + mItemsCount;
                mActionMode.setTitle(title);

                //selectedTrip = trips.get(idx);
                mSelectedRecordings.clear();
                mSelectedRecordings.addAll(mRecordingsRecyclerAdapter.getSelectedItems());
                Log.i(TAG, "Selected " + mSelectedRecordings.size() + " trips for action..");

                if (mItemsCount == 1) {
                    //   setPosition(position);
                    mActionMode.getMenu().findItem(R.id.action_share).setVisible(true);
                    // mActionMode.getMenu().findItem(R.id.action_upload_trip).setVisible(true);
                } else {
                    mActionMode.getMenu().findItem(R.id.action_share).setVisible(false);
                    mActionMode.getMenu().findItem(R.id.action_save).setVisible(false);
                    mActionMode.getMenu().findItem(R.id.action_edit).setVisible(false);
                    //   mActionMode.getMenu().findItem(R.id.action_upload_trip).setVisible(false);
                }

                if (mItemsCount == 2) {
                    // mActionMode.getMenu().findItem(R.id.action_merge_trips).setVisible(true);
                } else {
                    // mActionMode.getMenu().findItem(R.id.action_merge_trips).setVisible(false);
                }

                MainActivity.this.invalidateOptionsMenu();

            }
            Log.v(TAG, "actionMode is not null");
        } else {
            Log.v(TAG, "actionMode is null");
        }
    }


    @Override
    public void onDeleteDialogEvent() {
        mActionMode.finish();
        mPresenter.deleteRecordings(mSelectedRecordings);
    }


    public void openSettings() {
        Log.i(TAG, "About to open settings Activity");
        Intent prefsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        mDrawerLayout.closeDrawer(mNavView);
        startActivity(prefsIntent);
    }
}
