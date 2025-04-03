package com.example.project_mobile.authencation;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_mobile.MainActivity;
import com.example.project_mobile.R;
import com.example.project_mobile.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdditionalInfoHelper {
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final Context context;

    public AdditionalInfoHelper(Context context) {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.context = context;
    }

    public void showAdditionalInfoDialog() {
        // Tạo một Dialog để nhập thông tin bổ sung
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_additional_info);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText editTextStudentId = dialog.findViewById(R.id.editTextStudentId);
        EditText editTextPhoneNumber = dialog.findViewById(R.id.editTextPhoneNumber);
        Button buttonSubmit = dialog.findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(v -> {
            String studentId = editTextStudentId.getText().toString().trim();
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();

            if (studentId.isEmpty()) {
                editTextStudentId.setError(context.getString(R.string.required_student_id));
            } else if (phoneNumber.isEmpty()) {
                editTextPhoneNumber.setError(context.getString(R.string.required_phonenumber));
            } else {
                // Kiểm tra sự tồn tại của studentId và số điện thoại
                checkIfStudentIdOrPhoneExists(studentId, phoneNumber, exists -> {
                    if (exists) {
                        Toast.makeText(context, context.getString(R.string.student_id_or_phonenumber_already), Toast.LENGTH_SHORT).show();
                    } else {
                        // Lưu thông tin người dùng vào Firestore nếu thông tin hợp lệ
                        saveUserInfoToFirestore(studentId, phoneNumber, dialog);
                    }
                });
            }
        });

        dialog.show();
    }

    private void checkIfStudentIdOrPhoneExists(String studentId, String phoneNumber, FirestoreCheckCallback callback) {
        firestore.collection("users")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Student ID đã tồn tại
                        callback.onCheckComplete(true);
                    } else {
                        // Kiểm tra tiếp số điện thoại
                        firestore.collection("users")
                                .whereEqualTo("phoneNumber", phoneNumber)
                                .get()
                                .addOnCompleteListener(phoneTask -> {
                                    if (phoneTask.isSuccessful() && !phoneTask.getResult().isEmpty()) {
                                        // Số điện thoại đã tồn tại
                                        callback.onCheckComplete(true);
                                    } else {
                                        // Cả hai đều không tồn tại
                                        callback.onCheckComplete(false);
                                    }
                                });
                    }
                });
    }

    private void saveUserInfoToFirestore(String studentId, String phoneNumber, Dialog dialog) {
        String userId = auth.getCurrentUser().getUid();

        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User currentUser = documentSnapshot.toObject(User.class);

                        if (currentUser != null) {
                            currentUser.setStudentId(studentId);
                            currentUser.setPhoneNumber(phoneNumber);

                            firestore.collection("users").document(userId)
                                    .set(currentUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, context.getString(R.string.updated_information_user), Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        context.startActivity(new Intent(context, MainActivity.class));
                                        if (context instanceof AppCompatActivity) {
                                            ((AppCompatActivity) context).finish();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, context.getString(R.string.failed_update_user) + ": "
                                                + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context,  context.getString(R.string.failed_retrieve_user) +": "
                            + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    interface FirestoreCheckCallback {
        void onCheckComplete(boolean exists);
    }
}
