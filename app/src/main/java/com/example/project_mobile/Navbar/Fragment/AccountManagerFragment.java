package com.example.project_mobile.Navbar.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.project_mobile.R;
import com.example.project_mobile.authencation.LoginActivity;
import com.example.project_mobile.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountManagerFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private TextView studentIdTextView, studentNameTextView, studentPhoneTextView, studentEmailTextView;
    private ImageView studentImageView;
    private Button logoutButton, changePasswordButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account_manager, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        studentIdTextView = rootView.findViewById(R.id.studentIdTextView);
        studentNameTextView = rootView.findViewById(R.id.studentNameTextView);
        studentPhoneTextView = rootView.findViewById(R.id.studentPhoneTextView);
        studentEmailTextView = rootView.findViewById(R.id.studentEmailTextView);

        if (currentUser != null) {
            loadUserInfo(currentUser.getUid());
        }

        changePasswordButton = rootView.findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(v -> {
            firestore.collection("users").document(currentUser.getUid()).get().
                    addOnSuccessListener(documentSnapshot -> {
               User user = documentSnapshot.toObject(User.class);
                auth.sendPasswordResetEmail(user.getEmail())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), getContext().getString(R.string.reset_password_successful), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), getContext().getString(R.string.reset_password_failed), Toast.LENGTH_SHORT).show();
                            }
                        });
            });


        });

        logoutButton = rootView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            Toast.makeText(getActivity(), getContext().getString(R.string.logout_successful), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        });


        return rootView;

    }

    private void loadUserInfo(String userId) {
        firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
           if(documentSnapshot.exists()) {
               User user = documentSnapshot.toObject(User.class);
               if(user != null) {
                   studentIdTextView.setText(getContext().getString(R.string.student_id) + ": " + user.getStudentId());
                   studentNameTextView.setText(getContext().getString(R.string.name) + ": " + user.getName());
                   studentEmailTextView.setText(getContext().getString(R.string.email) + ": " + user.getEmail());
                   studentPhoneTextView.setText(getContext().getString(R.string.phone_number) + ": " + user.getPhoneNumber());
               }
           }
           else {
               Log.d("user data", "User data not found");
           }
        }).addOnFailureListener(e -> {
            studentIdTextView.setText(getContext().getString(R.string.student_id));
            studentNameTextView.setText(getContext().getString(R.string.name));
            studentEmailTextView.setText(getContext().getString(R.string.email));
            studentPhoneTextView.setText(getContext().getString(R.string.phone_number));
        });
    }
}