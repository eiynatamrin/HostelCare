package com.example.hostelcare;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

public class AboutDeveloperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_developer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About Developers");
        }
        findViewById(R.id.btnGithub).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/eiynatamrin/HostelCare.git"));
            startActivity(intent);
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

