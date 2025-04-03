package com.example.project_mobile.models;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class User {
    private  String studentId;
    private String name;
    private String email;
    private String phoneNumber;


    public User() {}

    public User(String studentId, String name, String email,String phoneNumber) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getStudentId() {return studentId; }
    public void setStudentId(String studentId) {this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() {return phoneNumber;}
    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber; }


    public void saveToFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(this)
                .addOnSuccessListener(aVoid -> {
                    // Thành công
                })
                .addOnFailureListener(e -> {
                    // Lỗi
                });
    }
}
