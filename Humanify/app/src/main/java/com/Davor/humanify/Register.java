package com.Davor.humanify;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.Davor.humanify.DTO.UserResponse;
import com.Davor.humanify.Service.UserService;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Locale;

public class Register extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword, editTextUsername, editTextFirstname, editTextLastname;
    Button registerButton, selectBirthdateButton, chooseProfilePicButton;
    TextView loginNowTextView;
    LocalDate birthdate;
    Uri profilePictureUri;
    final Calendar calendar = Calendar.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextUsername = findViewById(R.id.username);
        editTextFirstname = findViewById(R.id.firstname);
        editTextLastname = findViewById(R.id.lastname);
        registerButton = findViewById(R.id.registerbutton);
        selectBirthdateButton = findViewById(R.id.selectbirthdate);
        chooseProfilePicButton = findViewById(R.id.chooseprofilepic);
        loginNowTextView = findViewById(R.id.loginNow);

        loginNowTextView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        selectBirthdateButton.setOnClickListener(v -> showDatePickerDialog());

        chooseProfilePicButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
        });

        registerButton.setOnClickListener(v -> registerUser());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Convert Calendar to LocalDate
            birthdate = Instant.ofEpochMilli(calendar.getTimeInMillis())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            // Display the selected date in the button
            selectBirthdateButton.setText(birthdate.toString());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profilePictureUri = data.getData();
            chooseProfilePicButton.setText("Profile Picture Selected");
        }
    }

    private void registerUser() {
        String email = String.valueOf(editTextEmail.getText()).trim();
        String password = String.valueOf(editTextPassword.getText()).trim();
        String username = String.valueOf(editTextUsername.getText()).trim();
        String firstname = String.valueOf(editTextFirstname.getText()).trim();
        String lastname = String.valueOf(editTextLastname.getText()).trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username) || TextUtils.isEmpty(firstname) || TextUtils.isEmpty(lastname) || birthdate == null || profilePictureUri == null) {
            Toast.makeText(Register.this, "Please fill all the required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send the registration data to your server API (POST request)
        // Here you would convert profilePictureUri to a file if needed
        UserService.register(getApplicationContext(),email,password,username,firstname,lastname,birthdate,profilePictureUri,new UserService.RegisterCallback(){

            @Override
            public void onSuccess(UserResponse userResponse) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }
}
