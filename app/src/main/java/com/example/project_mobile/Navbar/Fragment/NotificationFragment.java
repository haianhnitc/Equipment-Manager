package com.example.project_mobile.Navbar.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.project_mobile.Navbar.Widget.NotificationPagerAdapter;
import com.example.project_mobile.R;
import com.example.project_mobile.models.Notification;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private NotificationPagerAdapter adapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        notificationList = new ArrayList<>();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        viewPager = view.findViewById(R.id.view_pager_notifications);
        tabLayout = view.findViewById(R.id.tab_layout_notifications);
        adapter = new NotificationPagerAdapter(notificationList);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText("")).attach();

        fetchNotification();

        return view;
    }

    private void fetchNotification() {
        db.collection("notifications").orderBy("time", Query.Direction.DESCENDING).addSnapshotListener((querySnapshot, error) -> {
            if(error != null) {
                Toast.makeText(getContext(), getContext().getString(R.string.failed_fetch_data_firestore), Toast.LENGTH_LONG).show();
                return;
            }
            if(querySnapshot != null) {
                notificationList.clear();
                for(QueryDocumentSnapshot document : querySnapshot) {
                    Notification notification = document.toObject(Notification.class);
                    notificationList.add(notification);
                }
                adapter.updateList(notificationList);
                new TabLayoutMediator(tabLayout, viewPager,
                        (tab, position) -> {
                            // Không cần tiêu đề, chỉ dùng chấm
                        }).attach();
                viewPager.setCurrentItem(0);
            }
        });
    }


}