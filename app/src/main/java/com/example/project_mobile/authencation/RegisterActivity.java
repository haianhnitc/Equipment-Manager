package com.example.project_mobile.authencation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword, signupName;
    private ImageView signupButton;
    TextView loginTextView;
    private FirebaseFirestore firestore;
    private GoogleSignInHelper googleSignInHelper;
    private FacebookSignInHelper facebookSignInHelper;
    private AdditionalInfoHelper additionalInfoHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.setLocale(this);
        setContentView(R.layout.activity_register);

        loginTextView = findViewById(R.id.textGoToLoginPage);
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        signupName = findViewById(R.id.registerName);
        signupEmail = findViewById(R.id.registerEmail);
        signupPassword = findViewById(R.id.registerPassword);
        signupButton = findViewById(R.id.registerButton);
        additionalInfoHelper = new AdditionalInfoHelper(RegisterActivity.this);


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = signupName.getText().toString();
                String user = signupEmail.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();
                if(name.isEmpty()) {
                    signupName.setError(RegisterActivity.this.getString(R.string.required_name));
                }
                else if(user.isEmpty()) {
                    signupEmail.setError(RegisterActivity.this.getString(R.string.required_email));
                }
                else if(password.isEmpty()) {
                    signupPassword.setError(RegisterActivity.this.getString(R.string.required_password));
                }
                else {
                    auth.createUserWithEmailAndPassword(user, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String userId = auth.getCurrentUser().getUid();
                                User newUser = new User(null, name, user, null);

                                firestore.collection("users").document(userId)
                                        .set(newUser)
                                        .addOnSuccessListener(aVoid -> {
                                            additionalInfoHelper.showAdditionalInfoDialog();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RegisterActivity.this, RegisterActivity.this.getString(R.string.updated_information_user)
                                                    + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                                Toast.makeText(RegisterActivity.this, RegisterActivity.this.getString(R.string.create_account_successful),
                                        Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(RegisterActivity.this, RegisterActivity.this.getString(R.string.create_account_failed)
                                        + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }


            }
        });

        googleSignInHelper = new GoogleSignInHelper(this);
        findViewById(R.id.registerGoogle).setOnClickListener(v -> googleSignInHelper.signIn());

        facebookSignInHelper = new FacebookSignInHelper(this);
        findViewById(R.id.registerFacebook).setOnClickListener(v -> facebookSignInHelper.signIn());
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
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(RegisterActivity.this, RegisterActivity.this.getString(R.string.login_google_failed)+": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        facebookSignInHelper.handleActivityResult(requestCode, resultCode, data);
    }
}
