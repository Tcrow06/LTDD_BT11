package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    Button loginButton;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.loginEmailEditText);
        passwordEditText = findViewById(R.id.loginPasswordEditText);
        loginButton = findViewById(R.id.loginButton);
        FirebaseAuth.getInstance().setLanguageCode("vi");
        mAuth = FirebaseAuth.getInstance();

        Log.d("FIREBASE_AUTH", "mAuth null? " + (mAuth == null));


        TextView forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        forgotPasswordTextView.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email để khôi phục mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đã gửi email khôi phục mật khẩu", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
                Snackbar.make(v, "Vui lòng nhập đầy đủ email và mật khẩu", Snackbar.LENGTH_SHORT).show();

                return;
            }

            try {


                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("LOGIN", "Đăng nhập thành công!");

                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                    Snackbar.make(v, "Đăng nhập thành công!", Snackbar.LENGTH_SHORT).show();

                                    // Truyền email và tên tài khoản qua Intent
                                    Intent intent = new Intent(this, MenuDisplayActivity.class);
                                    intent.putExtra("email", user.getEmail());
                                    String username = user.getDisplayName() != null ? user.getDisplayName() : user.getEmail().split("@")[0];
                                    intent.putExtra("username", username);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Snackbar.make(v, "Vui lòng xác thực email trước khi đăng nhập.", Snackbar.LENGTH_SHORT).show();
                                    Toast.makeText(this, "Vui lòng xác thực email trước khi đăng nhập.", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                }
                            } else {
                                Exception e = task.getException();
                                Log.e("LOGIN", "Đăng nhập thất bại", e);

                                Snackbar.make(v, "Lỗi: thông tin đăng nhập sai. Vui lòng thử lại", Snackbar.LENGTH_SHORT).show();
                                Toast.makeText(this, "Lỗi: thông tin đăng nhập sai. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                            }
                        });
            }catch (Exception e){
                e.getMessage();
            }
        });


        Button goToSignup = findViewById(R.id.goToSignupButton);
        goToSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}