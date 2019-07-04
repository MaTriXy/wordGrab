package com.crackdress.wordgrab.adapters;

import android.app.Activity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crackdress.wordgrab.R;
import com.crackdress.wordgrab.model.Contact;
import com.crackdress.wordgrab.model.Recording;
import com.crackdress.wordgrab.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.media.CamcorderProfile.get;

public class RecordingsRecyclerAdapter extends RecyclerView.Adapter<RecordingsRecyclerAdapter.RecordingsViewHolder> {

    private static final String TAG = RecordingsRecyclerAdapter.class.getSimpleName();
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    public static final int HOUR_MILISECONDS = 60 * 60 * 1000;

    private LayoutInflater inflater;
    private List<Recording> data = new ArrayList<>();
    private Recording recording;
    private Activity mContext;
    private RecyclerItemClickListener clickListener;
    private SimpleDateFormat mDateFormat;
    private SimpleDateFormat mTimeFormat;

    private SparseBooleanArray sActivatedItems;
    private SparseBooleanArray mSelectedItems;
    private boolean isSelectable = false;


    public RecordingsRecyclerAdapter(Activity context) {
        inflater = LayoutInflater.from(context);
        this.mContext = context;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        mTimeFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
        mTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        sActivatedItems = new SparseBooleanArray();
        mSelectedItems = new SparseBooleanArray();
    }

    public void setItemClickedListener(RecyclerItemClickListener clickListener) {

        this.clickListener = clickListener;

    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = inflater.inflate(R.layout.recording_list_item, parent, false);
        return new RecordingsViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(RecordingsViewHolder holder, final int position) {
        //    Log.i(TAG, "onBindViewHolder displaying item: " + data.get(position));

        Contact contact = Utils.isContactExists(mContext, data.get(position).getPhoneNumber());

        if (contact != null) {
            data.get(position).setContactName(contact.getDisplayName());
            holder.tvPhoneNumber.setText(data.get(position).getContactName());
        } else {
            holder.tvPhoneNumber.setText(data.get(position).getPhoneNumber());
        }
        if (data.get(position).getIncoming()) {
//            Log.i(TAG, "onBindViewHolder: incoming call, set drawable start");
//            holder.ivListItemImage.setImageDrawable(ActivityCompat.getDrawable(context, R.drawable.ic_action_phone_incoming));
//            holder.tvDate.setCompoundDrawablesWithIntrinsicBounds(ActivityCompat.getDrawable(mContext, R.drawable.ic_action_phone_incoming), null, null, null);
            holder.tvDate.setCompoundDrawablesRelativeWithIntrinsicBounds(ActivityCompat.getDrawable(mContext, R.drawable.ic_action_phone_incoming), null, null, null);
        } else {
//            Log.i(TAG, "onBindViewHolder: outgoing call, set drawable start");
//            holder.ivListItemImage.setImageDrawable(ActivityCompat.getDrawable(context, R.drawable.ic_action_phone_outgoing));
//            holder.tvDate.setCompoundDrawablesWithIntrinsicBounds(ActivityCompat.getDrawable(mContext, R.drawable.ic_action_phone_outgoing), null, null, null);
            holder.tvDate.setCompoundDrawablesRelativeWithIntrinsicBounds(ActivityCompat.getDrawable(mContext, R.drawable.ic_action_phone_outgoing), null, null, null);
        }


        holder.tvDate.setText(DateUtils.getRelativeDateTimeString(mContext,
            data.get(position).getDate(),
                DateUtils.SECOND_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                0));


        holder.tvComment.setText(data.get(position).getComment());

        long duration = data.get(position).getDuration();

        holder.tvDuration.setText(DateUtils.formatElapsedTime(duration / 1000));

        holder.itemView.setOnClickListener(view -> {
            clickListener.onItemClicked(position, data.get(position));
            setItemSelected(position);
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clickListener.onItemLongClick(position, data.get(position));
                return true;
            }
        });

        holder.itemView.setActivated(sActivatedItems.get(position, false));
        holder.itemView.setSelected(mSelectedItems.get(position, false));

//        Log.i(TAG, "onBindViewHolder: ");
        //cast holder to VHHeader and set data for header.
    }

    @Override
    public int getItemCount() {
        int count = data.size();

//        Log.i(TAG, "get item count: " + count);
        return count;
    }


    class RecordingsViewHolder extends RecyclerView.ViewHolder {

        private TextView tvPhoneNumber;
        private TextView tvDate;
        private TextView tvDuration;
        private TextView tvComment;
        private ImageView ivListItemImage;

        private View itemView;

        private RecordingsViewHolder(View itemView) {
            super(itemView);
            tvPhoneNumber = (TextView) itemView.findViewById(R.id.tvPhoneNumber);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            tvDuration = (TextView) itemView.findViewById(R.id.tvDuration);
            tvComment = (TextView) itemView.findViewById(R.id.tvComment);
            ivListItemImage = (ImageView) itemView.findViewById(R.id.ivListItemImage);

            this.itemView = itemView;
        }

    }

    public void updateRecordings(List<Recording> recordings) {
        if (this.data != null) {
            this.data.clear();
        }

        this.data.addAll(recordings);
        mContext.runOnUiThread(() -> notifyDataSetChanged());
    }


    public void toggleActivatedItem(int position) {
//        Log.i(RecordingsDao.TAG, "toggleActivatedItem was called");
        if (sActivatedItems.get(position, false)) {
            sActivatedItems.delete(position);
        } else {
            sActivatedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void setItemSelected(int position) {
        Log.i(TAG, "setItemSelected: " + position);
        mSelectedItems.clear();
        mSelectedItems.put(position, true);
        notifyDataSetChanged();
    }

    public void clearActivatedItems() {
        sActivatedItems.clear();
        notifyDataSetChanged();
    }

    public void clearSelectedItems() {
        Log.i(TAG, "clearSelectedItems: ");
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemsCount() {
        return sActivatedItems.size();
    }

    public ArrayList<Recording> getSelectedItems() {
        ArrayList<Recording> items = new ArrayList<>();
        for (int i = 0; i < sActivatedItems.size(); i++) {
            items.add(data.get(sActivatedItems.keyAt(i)));
        }
        return items;
    }

    public interface RecyclerItemClickListener {
        void onItemClicked(int position, Recording recording);

        void onItemLongClick(int position, Recording recording);
    }

}
