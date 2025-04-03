package com.example.project_mobile.Navbar.Widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_mobile.R;
import com.example.project_mobile.models.Notification;

import java.util.ArrayList;
import java.util.List;


public class NotificationPagerAdapter extends RecyclerView.Adapter<NotificationPagerAdapter.NotificationViewHolder> {

    List<Notification> notificationList = new ArrayList<>();

    public NotificationPagerAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }


    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_page, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationPagerAdapter.NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.titleTextView.setText(notification.getTitle());
        holder.bodyTextView.setText(notification.getBody());
        holder.timeTextView.setText(notification.getTime());

        // nếu là thông báo mới nhất (vị trí 0), thêm hiệu ứng nổi bật
        if (position == 0) {
            holder.itemView.setScaleX(1.05f);
            holder.itemView.setScaleY(1.05f);
            holder.itemView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0F7FA)); // Màu nhẹ
        } else {
            holder.itemView.setScaleX(1.0f);
            holder.itemView.setScaleY(1.0f);
            holder.itemView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF5F5F5));
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void updateList(List<Notification> newList) {
        notificationList = newList;
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, bodyTextView, timeTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.notification_title);
            bodyTextView = itemView.findViewById(R.id.notification_body);
            timeTextView = itemView.findViewById(R.id.notification_time);
        }
    }
}
