package com.Davor.humanify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.Davor.humanify.DTO.UserResponse;
import com.Davor.humanify.Service.AuthenticationService;
import com.Davor.humanify.Service.UserService;
import com.google.android.material.textfield.TextInputEditText;

public class Login extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button button;
    TextView textView;
    ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        UserService.getCurrentUser(getApplicationContext(), new UserService.CurrentUserCallback() {
            @Override
            public void onSuccess(UserResponse userResponse) {
                if (userResponse != null) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle failure silently or log it
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        editTextEmail = findViewById(R.id.username);
        editTextPassword = findViewById(R.id.password);
        button = findViewById(R.id.loginbutton);
        textView = findViewById(R.id.registerNow);

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        });

        UserService.init(getApplicationContext());

        button.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String username = String.valueOf(editTextEmail.getText());
            String password = String.valueOf(editTextPassword.getText());

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Please enter the required fields", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                return;
            }

            UserService.login(username, password, new UserService.LoginCallback() {
                @Override
                public void onSuccess(String token) {
                    try {
                        AuthenticationService.saveToken(getApplicationContext(), token);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        });
    }
}
