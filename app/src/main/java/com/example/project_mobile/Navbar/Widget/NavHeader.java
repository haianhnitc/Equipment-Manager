package com.example.project_mobile.Navbar.Widget;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.project_mobile.R;
import com.example.project_mobile.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class NavHeader {
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private final FirebaseFirestore firestore;
    private TextView studentNameTextView, studentEmailTextView;
    private ImageView studentImage;
    private ListenerRegistration userListener;

    public NavHeader(View headerView) {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        studentNameTextView = headerView.findViewById(R.id.studentNameHeaderNav);
        studentEmailTextView = headerView.findViewById(R.id.studentEmailHeaderNav);
        studentImage = headerView.findViewById(R.id.studentImage);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            loadHeaderNav(userId);
        }
    }

    private void loadHeaderNav(String userId) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        userListener = userRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("NavHeader", "Error listening to user data: " + e.getMessage());
                studentNameTextView.setText("");
                studentEmailTextView.setText("");
                studentImage.setImageResource(R.drawable.profile);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    studentNameTextView.setText(user.getName() != null ? user.getName() : "");
                    studentEmailTextView.setText(user.getEmail() != null ? user.getEmail() : "");
                    if (user.getPhotoUrl() != null) {
                        try {
                            byte[] decodedBytes = Base64.decode(user.getPhotoUrl(), Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            studentImage.setImageBitmap(decodedBitmap);
                        } catch (Exception ex) {
                            Log.e("NavHeader", "Error decoding Base64 image: " + ex.getMessage());
                            studentImage.setImageResource(R.drawable.profile);
                        }
                    } else {
                        studentImage.setImageResource(R.drawable.profile);
                    }
                }
            } else {
                studentNameTextView.setText("");
                studentEmailTextView.setText("");
                studentImage.setImageResource(R.drawable.profile);
            }
        });
    }

}