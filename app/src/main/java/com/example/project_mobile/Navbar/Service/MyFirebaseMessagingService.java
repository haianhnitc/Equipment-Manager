package com.example.project_mobile.Navbar.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.project_mobile.MainActivity;
import com.example.project_mobile.models.Notification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("FCM", "Received message: " + remoteMessage.getNotification());
        Log.d("FCM", "Data payload: " + remoteMessage.getData());

        String title = null;
        String body = null;

        // Ưu tiên Notification payload
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d("FCM", "Notification payload - Title: " + title + ", Body: " + body);
        }

        // Nếu có Data payload, ghi đè title và body nếu key tồn tại
        else if (remoteMessage.getData().size() > 0) {
            String dataTitle = remoteMessage.getData().get("title");
            String dataBody = remoteMessage.getData().get("body");
            if (dataTitle != null && dataBody != null) {
                title = dataTitle;
                body = dataBody;
                Log.d("FCM", "Data payload overrides - Title: " + title + ", Body: " + body);
            } else {
                Log.d("FCM", "Data payload missing title or body, using Notification payload if available");
            }
        } else {
            Log.d("FCM", "No data payload");
        }

        if (title != null && body != null) {
            Log.d("FCM", "Processing notification with Title: " + title + ", Body: " + body);
            sendNotification(title, body);
            saveNotification(title, body);
        } else {
            Log.e("FCM", "Title or body is null, cannot process notification. Title: " + title + ", Body: " + body);
        }
    }
    private void sendNotification(String title, String body) {
        // Intent để mở MainActivity và điều hướng đến NotificationFragment
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigateTo", "NotificationFragment");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "default_channel_id";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Thay bằng icon của bạn
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo kênh thông báo cho Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Kênh mặc định",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        manager.notify((int) System.currentTimeMillis(), builder.build()); // ID duy nhất cho mỗi thông báo
    }

    private void saveNotification(String title, String body) {
        String time = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(new Date());
        Notification notification = new Notification(title, body, time);

        // Kiểm tra xem thông báo đã tồn tại chưa
        db.collection("notifications")
                .whereEqualTo("title", title)
                .whereEqualTo("body", body)
                .whereEqualTo("time", time)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        // Nếu không tồn tại, mới lưu
                        db.collection("notifications").add(notification)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("FCM", "Lưu thông báo thành công với ID: " + documentReference.getId());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FCM", "Lỗi khi lưu thông báo: ", e);
                                });
                    } else {
                        Log.d("FCM", "Thông báo đã tồn tại, không lưu lại.");
                    }
                });
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM Token", "Token mới: " + token);
    }
}