package com.example.project_mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.project_mobile.Navbar.Fragment.AccountManagerFragment;
import com.example.project_mobile.Navbar.Fragment.ClassroomFragment;
import com.example.project_mobile.Navbar.Fragment.ContactFragment;
import com.example.project_mobile.Navbar.Fragment.EquipmentFragment;
import com.example.project_mobile.Navbar.Fragment.HistoryFragment;
import com.example.project_mobile.Navbar.Fragment.NotificationFragment;
import com.example.project_mobile.Navbar.Fragment.SettingFragment;
import com.example.project_mobile.Navbar.Fragment.UserGuideFragment;
import com.example.project_mobile.Navbar.Widget.NavHeader;
import com.example.project_mobile.locale.LocaleHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 100;
    private static final String KEY_SELECTED_ITEM_ID = "selected_item_id"; // Key để lưu trạng thái

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Áp dụng ngôn ngữ đã lưu
        LocaleHelper.setLocale(this);
        setContentView(R.layout.activity_main);

        // Yêu cầu quyền thông báo trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        // Đăng ký topic FCM
        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Subscribed to topic 'all'");
                    } else {
                        Log.e("FCM", "Failed to subscribe to topic 'all'");
                    }
                });

        // Khởi tạo Toolbar và Navigation
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        NavHeader navHeader = new NavHeader(headerView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Xử lý khởi tạo hoặc khôi phục trạng thái
        if (savedInstanceState == null) {
            // Lần đầu khởi tạo
            getSupportActionBar().setTitle(getString(R.string.equipment));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EquipmentFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_deviceManager);
        } else {
            // Khôi phục trạng thái sau recreate
            int selectedItemId = savedInstanceState.getInt(KEY_SELECTED_ITEM_ID, R.id.nav_deviceManager);
            setToolbarTitleFromMenuItem(selectedItemId);
            loadFragmentFromItemId(selectedItemId); // Tải lại fragment tương ứng
            navigationView.setCheckedItem(selectedItemId);
        }

        getDeviceToken();
    }

    // Lưu trạng thái khi activity bị destroy (trước recreate)
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int selectedItemId = navigationView.getCheckedItem() != null ?
                navigationView.getCheckedItem().getItemId() : R.id.nav_deviceManager;
        outState.putInt(KEY_SELECTED_ITEM_ID, selectedItemId);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.notification_permission_granted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        loadFragmentFromItemId(itemId);
        setToolbarTitleFromMenuItem(itemId);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Tải fragment dựa trên itemId
    private void loadFragmentFromItemId(int itemId) {
        if (itemId == R.id.nav_deviceManager) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new EquipmentFragment()).commit();
        } else if (itemId == R.id.nav_setting) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingFragment()).commit();
        } else if (itemId == R.id.nav_notification) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationFragment()).commit();
        } else if (itemId == R.id.account_manager) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AccountManagerFragment()).commit();
        } else if (itemId == R.id.nav_classRoom) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ClassroomFragment()).commit();
        } else if (itemId == R.id.nav_contact) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ContactFragment()).commit();
        } else if (itemId == R.id.nav_history) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HistoryFragment()).commit();
        } else if (itemId == R.id.nav_user_guide) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UserGuideFragment()).commit();
        }
    }

    // Đặt tiêu đề Toolbar dựa trên itemId
    private void setToolbarTitleFromMenuItem(int itemId) {
        if (getSupportActionBar() != null) {
            if (itemId == R.id.nav_deviceManager) {
                getSupportActionBar().setTitle(getString(R.string.equipment));
            } else if (itemId == R.id.nav_setting) {
                getSupportActionBar().setTitle(getString(R.string.setting));
            } else if (itemId == R.id.nav_notification) {
                getSupportActionBar().setTitle(getString(R.string.notification));
            } else if (itemId == R.id.account_manager) {
                getSupportActionBar().setTitle(getString(R.string.account_manager));
            } else if (itemId == R.id.nav_classRoom) {
                getSupportActionBar().setTitle(getString(R.string.classroom));
            } else if (itemId == R.id.nav_contact) {
                getSupportActionBar().setTitle(getString(R.string.contact));
            } else if (itemId == R.id.nav_history) {
                getSupportActionBar().setTitle(getString(R.string.history));
            } else if (itemId == R.id.nav_user_guide) {
                getSupportActionBar().setTitle(getString(R.string.user_guide));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void getDeviceToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.e("FirebaseLog", "Fetching token failed" + task.getException());
                    return;
                }
                String token = task.getResult();
                Log.e("", "Device token: " + token);
            }
        });
    }
}