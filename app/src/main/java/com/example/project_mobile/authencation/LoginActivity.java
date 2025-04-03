package com.example.project_mobile.authencation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_mobile.MainActivity;
import com.example.project_mobile.R;
import com.example.project_mobile.locale.LocaleHelper;
import com.example.project_mobile.models.User;
import com.facebook.login.Login;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText loginEmail, loginPassword;
    private ImageView loginButton;
    TextView registerTextView;
    private GoogleSignInHelper googleSignInHelper;
    private FacebookSignInHelper facebookSignInHelper;
    private FirebaseFirestore firestore;
    private AdditionalInfoHelper additionalInfoHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this);
        setContentView(R.layout.activity_login);

        registerTextView = findViewById(R.id.textGoToRegisterPage);
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        auth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        firestore = FirebaseFirestore.getInstance();
        additionalInfoHelper = new AdditionalInfoHelper(LoginActivity.this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();

                // Kiểm tra email trống hoặc không đúng định dạng
                if (email.isEmpty()) {
                    loginEmail.setError(LoginActivity.this.getString(R.string.required_email));
                    return;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    loginEmail.setError(LoginActivity.this.getString(R.string.enter_valid_email));
                    return;
                }

                // Kiểm tra mật khẩu trống
                if (password.isEmpty()) {
                    loginPassword.setError(LoginActivity.this.getString(R.string.required_password));
                    return;
                }

                // Thực hiện đăng nhập với Firebase
                auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                String userId = auth.getCurrentUser().getUid();
                                firestore.collection("users").document(userId).get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                User existingUser = documentSnapshot.toObject(User.class);
                                                if (existingUser.getStudentId() == null || existingUser.getPhoneNumber() == null) {
                                                    additionalInfoHelper.showAdditionalInfoDialog();
                                                } else {
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                    Toast.makeText(view.getContext(), getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                String errorMessage;
                                if (e instanceof FirebaseAuthInvalidUserException) {
                                    Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.email_not_exist), Toast.LENGTH_SHORT).show();
                                } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Google Sign-In và Facebook Sign-In giữ nguyên
        googleSignInHelper = new GoogleSignInHelper(this);
        findViewById(R.id.loginGoogle).setOnClickListener(v -> googleSignInHelper.signIn());

        facebookSignInHelper = new FacebookSignInHelper(this);
        findViewById(R.id.loginFacebook).setOnClickListener(v -> facebookSignInHelper.signIn());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Xử lý kết quả đăng nhập Google
        // vì ở google có dùng RC_SIGN_IN nhưng facebook thì không
        if (requestCode == GoogleSignInHelper.RC_SIGN_IN) {
            googleSignInHelper.handleSignInResult(data, new GoogleSignInHelper.OnGoogleSignInResultListener() {
                @Override
                public void onSuccess() {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.login_google_successful), Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(LoginActivity.this, LoginActivity.this.getString(R.string.login_google_failed)+ ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Xử lý kết quả đăng nhập Facebook
        facebookSignInHelper.handleActivityResult(requestCode, resultCode, data);
    }
}
