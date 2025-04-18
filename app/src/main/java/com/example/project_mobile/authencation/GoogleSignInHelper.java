package com.example.project_mobile.authencation;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.project_mobile.MainActivity;
import com.example.project_mobile.R;
import com.example.project_mobile.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class GoogleSignInHelper {

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    static final int RC_SIGN_IN = 2005;
    private Activity activity;

    private FirebaseFirestore firestore;
    private AdditionalInfoHelper additionalInfoHelper;

    public GoogleSignInHelper(Activity activity) {
        this.activity = activity;
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        additionalInfoHelper = new AdditionalInfoHelper(activity);

        // Cấu hình các tùy chọn đăng nhập Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.client_id))
                .requestEmail().requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    // Bắt đầu quá trình đăng nhập
    public void signIn() {
        // Đăng xuất tài khoản Google hiện tại trước khi bắt đầu quá trình đăng nhập
        googleSignInClient.signOut().addOnCompleteListener(activity, task -> {
            // Sau khi đăng xuất thành công, bắt đầu quá trình chọn tài khoản
            Intent signInIntent = googleSignInClient.getSignInIntent();
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void firebaseAuthWithGoogle(String idToken, OnGoogleSignInResultListener listener) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {

                SharedPreferences prefs = activity.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("login_time", System.currentTimeMillis());
                editor.apply();

                FirebaseUser firebaseUser = auth.getCurrentUser();
                String userId = firebaseUser.getUid();
                firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User existingUser = documentSnapshot.toObject(User.class);
                        if (existingUser.getStudentId() == null || existingUser.getPhoneNumber() == null) {
                            additionalInfoHelper.showAdditionalInfoDialog(); // Hiển thị dialog nếu thiếu thông tin
                        } else {
                            // Đã có thông tin đầy đủ, chuyển đến MainActivity
                            Toast.makeText(activity, activity.getString(R.string.login_google_successful), Toast.LENGTH_SHORT).show();
                            activity.startActivity(new Intent(activity, MainActivity.class));
                            activity.finish();
                        }
                    } else {
                        // Người dùng chưa có trong Firestore, tạo mới
                        User newUser = new User(null, firebaseUser.getDisplayName(), firebaseUser.getEmail(), null);
                        newUser.saveToFirestore(); // Lưu người dùng mới vào Firestore
                        additionalInfoHelper.showAdditionalInfoDialog(); // Yêu cầu nhập thông tin bổ sung
                    }
                });
            } else {
                listener.onFailure(task.getException());
            }
        });

    }

    // Interface để nhận kết quả đăng nhập Google
    public interface OnGoogleSignInResultListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    // Xử lý kết quả sau khi người dùng chọn tài khoản Google
    public void handleSignInResult(Intent data, OnGoogleSignInResultListener listener) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken(), listener);
        } catch (ApiException e) {
            listener.onFailure(e);
        }
    }
}
