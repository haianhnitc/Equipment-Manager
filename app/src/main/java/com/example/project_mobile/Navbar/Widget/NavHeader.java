package com.example.project_mobile.Navbar.Widget;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.project_mobile.R;
import com.example.project_mobile.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NavHeader {
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private final FirebaseFirestore firestore;
    private TextView studentNameTextView, studentEmailTextView;

    public NavHeader(View headerView) {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        studentNameTextView = headerView.findViewById(R.id.studentNameHeaderNav);
        studentEmailTextView = headerView.findViewById(R.id.studentEmailHeaderNav);

        if(currentUser != null) {
            String userId= currentUser.getUid();
            loadHeaderNav(userId);
        }

    }

    private  void loadHeaderNav(String userId ) {

        firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
           if(documentSnapshot.exists()) {
               User user = documentSnapshot.toObject(User.class);
               studentNameTextView.setText(user.getName());
               studentEmailTextView.setText(user.getEmail());
           }
        }).addOnFailureListener(e -> {
            studentNameTextView.setText("");
            studentEmailTextView.setText("");
        });
    }

}
