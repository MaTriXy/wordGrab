package com.crackdress.wordgrab.adapters;

import android.app.Activity;
import android.content.Context;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crackdress.wordgrab.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DetailsActionsAdapter extends RecyclerView.Adapter<DetailsActionsAdapter.ActionsViewHolder>{

    Context mContext;
    List<Integer> icons;
    List<String> actions;
    DetailsActionClickListener itemClickListener;

    private LayoutInflater inflater;

    public DetailsActionsAdapter(Activity context, DetailsActionClickListener actionClickListener) {
        inflater = LayoutInflater.from(context);
        this.mContext = context;
        itemClickListener = actionClickListener;
    }

    @Override
    public ActionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.details_actions_list_item, parent, false);
        return new ActionsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ActionsViewHolder holder, int position) {
        holder.tvAction.setText(actions.get(position));
        holder.ivActionIcon.setImageDrawable(ActivityCompat.getDrawable(mContext, icons.get(position)));
        holder.itemView.setOnClickListener(view -> {

            itemClickListener.onActionItemClicked(icons.get(position));
        });
    }


    @Override
    public int getItemCount() {
        return actions.size();
    }


    public class ActionsViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivActionIcon;
        private TextView tvAction;

        private View itemView;

        private ActionsViewHolder(View itemView) {
            super(itemView);
            ivActionIcon = (ImageView) itemView.findViewById(R.id.ivActionIcon);
            tvAction = (TextView) itemView.findViewById(R.id.tvActionItemTitle);

            this.itemView = itemView;
        }
    }


    public void updateData(Map<Integer, String> data){

        if(icons != null){
            icons.clear();
        }

        if(actions != null) {
            actions.clear();
        }
        icons = new ArrayList<Integer>(data.keySet());
        actions = new ArrayList<String>(data.values());
        notifyDataSetChanged();
    }

    public interface DetailsActionClickListener {
        void onActionItemClicked(int actionId);
    }
}
