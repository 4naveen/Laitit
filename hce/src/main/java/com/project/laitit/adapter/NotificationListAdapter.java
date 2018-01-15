package com.project.laitit.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.project.laitit.NotificationDetailsActivity;
import com.project.laitit.R;
import com.project.laitit.model.NotificationList;
import java.util.ArrayList;

/**
 * Created by Personal on 10/4/2017.
 */

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<NotificationList>notificationListArrayList;

    public NotificationListAdapter(Context context, ArrayList<NotificationList> notificationListArrayList) {
        this.context = context;
        this.notificationListArrayList = notificationListArrayList;
    }

    @Override
    public NotificationListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.indi_view_list_notifications, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(NotificationListAdapter.ViewHolder holder, int position) {

        final NotificationList notificationList = notificationListArrayList.get(position);
        holder.time.setText(notificationList.getTime());
        holder.title.setText(notificationList.getTitle());
    }

    @Override
    public int getItemCount() {
        return notificationListArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView time;
        ImageView plus;
        LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
           // plus = (ImageView) itemView.findViewById(R.id.letter);
            layout=(LinearLayout)itemView.findViewById(R.id.notiLayout1);

            time = (TextView) itemView.findViewById(R.id.time);
            title = (TextView) itemView.findViewById(R.id.title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String id = notificationListArrayList.get(getAdapterPosition()).getId();
            Intent i = new Intent(context, NotificationDetailsActivity.class);
            i.putExtra("id",id);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
