package com.example.project_mobile.authencation;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.project_mobile.MainActivity;
import com.example.project_mobile.R;
import com.example.project_mobile.models.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class FacebookSignInHelper {

    private FirebaseAuth auth;
    private CallbackManager callbackManager;
    private Activity activity;

    private FirebaseFirestore firestore;
    private AdditionalInfoHelper additionalInfoHelper;

    public FacebookSignInHelper(Activity activity) {
        this.activity = activity;
        auth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
        firestore = FirebaseFirestore.getInstance();
        additionalInfoHelper = new AdditionalInfoHelper(activity);
    }

    // Bắt đầu quá trình đăng nhập Facebook
    public void signIn() {
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(activity, activity.getString(R.string.login_cancelled), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(activity, activity.getString(R.string.login_failed) + ": " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("FacebookSignIn", "Received Facebook token: " + token.getToken());
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential).addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {
                Log.d("FacebookSignIn", "Firebase authentication successful");
                SharedPreferences prefs = activity.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("login_time", System.currentTimeMillis());
                editor.apply();

                FirebaseUser firebaseUser = auth.getCurrentUser();
                String userId = firebaseUser.getUid();
                firestore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User existingUser = documentSnapshot.toObject(User.class);
                        if (existingUser.getStudentId() == null || existingUser.getPhoneNumber() == null || existingUser.getEmail() == null) {
                            additionalInfoHelper.showAdditionalInfoDialog();
                        } else {
                            activity.startActivity(new Intent(activity, MainActivity.class));
                            activity.finish();
                        }
                    } else {
                        String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "no-email@facebook.com";
                        User newUser = new User(null, firebaseUser.getDisplayName(), email, firebaseUser.getPhoneNumber());
                        newUser.saveToFirestore();
                        additionalInfoHelper.showAdditionalInfoDialog();
                    }
                }).addOnFailureListener(e -> {
                    Log.e("FacebookSignIn", "Lỗi lấy dữ liệu Firestore: " + e.getMessage(), e);
                    Toast.makeText(activity, "Lỗi lấy dữ liệu người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                Log.e("FacebookSignIn", "Xác thực Firebase thất bại: " + errorMessage, task.getException());
                Toast.makeText(activity, activity.getString(R.string.authentication_failed) + ": " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // Gọi phương thức này từ onActivityResult() trong Activity
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
