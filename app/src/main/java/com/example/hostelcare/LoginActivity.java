package com.example.hostelcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        auth = FirebaseAuth.getInstance();

        // Set up Sign up text click listener programmatically
        TextView txtSignUp = findViewById(R.id.txtSignUp);
        if (txtSignUp != null) {
            txtSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goSignup(v);
                }
            });
        }
    }

    public void loginUser(View view) {
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        // Validate input
        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnSuccessListener(result -> {
                    // Show success popup message
                    Toast.makeText(this, "Login successful! Welcome back.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                }).addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // SIGN UP TEXT CLICK
    public void goSignup(View view) {
        try {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening signup: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
