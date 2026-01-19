package com.example.hostelcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sign Up");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // go back to previous activity
        return true;
    }

    // BACK TO LOGIN TEXT CLICK
    public void goToLogin(View view) {
        finish(); // Go back to login activity
    }


    public void signupUser(View view) {

        EditText fullName = findViewById(R.id.fullName);
        EditText studentId = findViewById(R.id.studentId);
        EditText phone = findViewById(R.id.phone);
        EditText email = findViewById(R.id.signupEmail);
        EditText password = findViewById(R.id.signupPassword);

        String fullNameText = fullName.getText().toString().trim();
        String studentIdText = studentId.getText().toString().trim();
        String phoneText = phone.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        // Validate all fields are filled
        if (fullNameText.isEmpty() || studentIdText.isEmpty() || 
            phoneText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password length
        if (passwordText.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener(result -> {

                    String uid = auth.getCurrentUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("fullName", fullNameText);
                    user.put("studentId", studentIdText);
                    user.put("phone", phoneText);
                    user.put("email", emailText);

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(aVoid -> {
                                // Show success popup message
                                Toast.makeText(this, "Account created successfully! You can now login.", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                }).addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
