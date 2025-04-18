package com.example.project_mobile.Navbar.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.project_mobile.R;
import com.example.project_mobile.authencation.LoginActivity;
import com.example.project_mobile.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AccountManagerFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private TextView studentIdTextView, studentNameTextView, studentPhoneTextView, studentEmailTextView;
    private ImageView studentImageView;
    private Button logoutButton, changePasswordButton;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account_manager, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        studentIdTextView = rootView.findViewById(R.id.studentIdTextView);
        studentNameTextView = rootView.findViewById(R.id.studentNameTextView);
        studentPhoneTextView = rootView.findViewById(R.id.studentPhoneTextView);
        studentEmailTextView = rootView.findViewById(R.id.studentEmailTextView);
        studentImageView = rootView.findViewById(R.id.studentImageView);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadImageToFirestore(uri);
            }
        });
        // Khởi tạo ActivityResultLauncher để yêu cầu quyền
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
            Boolean readMediaImagesGranted = permissions.getOrDefault
                    (android.Manifest.permission.READ_MEDIA_IMAGES, false);
            Boolean readExternalStorageGranted = permissions.getOrDefault
                    (android.Manifest.permission.READ_EXTERNAL_STORAGE, false);

            if (readMediaImagesGranted != null && readMediaImagesGranted) {
                // Quyền READ_MEDIA_IMAGES được cấp (Android 13+)
                imagePickerLauncher.launch("image/*");
            } else if (readExternalStorageGranted != null && readExternalStorageGranted) {
                // Quyền READ_EXTERNAL_STORAGE được cấp (Android 12 trở xuống)
                imagePickerLauncher.launch("image/*");
            } else {
                Toast.makeText(getActivity(), getContext().getString(R.string.permission_library), Toast.LENGTH_SHORT).show();
            }
        });

        studentImageView.setOnClickListener(v -> {
            checkAndRequestStoragePermission();
        });

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

            SharedPreferences prefs = requireActivity().getSharedPreferences("AppSettings",
                    requireActivity().MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("login_time");
            editor.apply();

            startActivity(new Intent(getActivity(), LoginActivity.class));
            Toast.makeText(getActivity(), getContext().getString(R.string.logout_successful), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        });


        return rootView;

    }

    private void checkAndRequestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: Yêu cầu quyền READ_MEDIA_IMAGES
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES});
            } else {
                imagePickerLauncher.launch("image/*");
            }
        } else {
            // Android 12 trở xuống: Yêu cầu quyền READ_EXTERNAL_STORAGE
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE});
            } else {
                imagePickerLauncher.launch("image/*");
            }
        }
    }

    private void loadUserInfo(String userId) {
        firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
           if(documentSnapshot.exists()) {
               User user = documentSnapshot.toObject(User.class);
               if(user != null) {
                  if(getContext() != null) {
                      studentIdTextView.setText(getContext().getString(R.string.student_id) + ": " + user.getStudentId());
                      studentNameTextView.setText(getContext().getString(R.string.name) + ": " + user.getName());
                      studentEmailTextView.setText(getContext().getString(R.string.email) + ": " + user.getEmail());
                      studentPhoneTextView.setText(getContext().getString(R.string.phone_number) + ": " + user.getPhoneNumber());

                  }
                   if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                       try {
                           byte[] decodedBytes = Base64.decode(user.getPhotoUrl(), Base64.DEFAULT);
                           Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                           studentImageView.setImageBitmap(decodedBitmap);
                       } catch (Exception e) {
                           studentImageView.setImageResource(R.drawable.profile);
                           Log.e("AccountManager", "Error decoding Base64 image: " + e.getMessage());
                       }
                   } else {
                       studentImageView.setImageResource(R.drawable.profile);
                   }
               }
           }
           else {
               Log.d("user data", "User data not found");
           }
        }).addOnFailureListener(e -> {
            if(getContext() != null) {
                studentIdTextView.setText(getContext().getString(R.string.student_id));
                studentNameTextView.setText(getContext().getString(R.string.name));
                studentEmailTextView.setText(getContext().getString(R.string.email));
                studentPhoneTextView.setText(getContext().getString(R.string.phone_number));
            }
            studentImageView.setImageResource(R.drawable.profile);
        });
    }

    private void uploadImageToFirestore(Uri imageUri) {
        if (currentUser == null) return;

        try {
            // Đọc ảnh từ URI
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Nén ảnh thủ công bằng cách giảm kích thước
            int targetWidth = 300;
            int targetHeight = 300;
            Bitmap compressedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true);

            // Chuyển ảnh đã nén thành Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Lưu chuỗi Base64 vào Firestore
            firestore.collection("users").document(currentUser.getUid()).update
                        ("photoUrl", base64Image).addOnSuccessListener(documentSnapshot -> {
                if(getContext() != null) {
                    Toast.makeText(getActivity(), getContext().getString(R.string.updated_photo_successfully), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                if(getContext() != null) {
                    Toast.makeText(getActivity(), getContext().getString(R.string.failed_update_user), Toast.LENGTH_SHORT).show();
                }
            });


            // Hiển thị ảnh từ Bitmap đã nén
            studentImageView.setImageBitmap(compressedBitmap);

        } catch (Exception e) {
            if(getContext() != null) {
                Toast.makeText(getActivity(), getContext().getString(R.string.error_processing_photo), Toast.LENGTH_SHORT).show();
            }
            Log.e("AccountManager", "Error uploading image: " + e.getMessage());
        }
    }

}