package com.crackdress.wordgrab.adapters;

import android.app.Activity;
import android.content.Context;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crackdress.wordgrab.R;
import com.crackdress.wordgrab.model.Contact;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class ContactsSettingsAdapter extends RecyclerView.Adapter<ContactsSettingsAdapter.ContactsViewHolder>{


    private static final String TAG = ContactsSettingsAdapter.class.getSimpleName();
    List<Contact> mContacts;
    LayoutInflater inflater;
    Context mContext;

    ContactOnClickListener onClickListener;

    public ContactsSettingsAdapter(Activity context) {
        inflater = LayoutInflater.from(context);
        mContext = context;
        mContacts = new ArrayList<>();
        onClickListener = (ContactOnClickListener) context;
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View itemView = inflater.inflate(android.R.layout.simple_list_item_checked, parent, false);
        View itemView = inflater.inflate(R.layout.contact_list_item, parent, false);
        return new ContactsViewHolder(itemView);    }

    @Override
    public void onBindViewHolder(ContactsViewHolder holder, int position) {
        holder.tvContactName.setText(mContacts.get(position).getDisplayName());
        Log.i(TAG, "onBindViewHolder: " + mContacts.get(position).getThumbUri() + ", " + mContacts.get(position));


        Picasso.with(mContext).load(mContacts.get(position).getThumbUri())
                .error(ActivityCompat.getDrawable(mContext,R.drawable.ic_contact))
                .placeholder(ActivityCompat.getDrawable(mContext,R.drawable.ic_contact))
                .into(holder.ivContactThumb);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onContactItemClicked(position);
            }
        });
    }


    @Override
    public int getItemCount() {

        return mContacts.size();
    }


    public void update(List<Contact> contacts) {
        if (this.mContacts != null) {
            this.mContacts.clear();
        }
        if(contacts != null){
            this.mContacts.addAll(contacts);
        }
        notifyDataSetChanged();

    }

    class ContactsViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivContactThumb;
        private TextView tvContactName;

        private View itemView;

        private ContactsViewHolder(View itemView) {
            super(itemView);

            tvContactName = (TextView) itemView.findViewById(R.id.tvContactName);
            ivContactThumb = (ImageView) itemView.findViewById(R.id.ivContactListThumb);
            this.itemView = itemView;
        }

    }


    public interface ContactOnClickListener{

        void onContactItemClicked(int position);

    }
}
