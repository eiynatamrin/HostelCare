package com.example.hostelcare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Underline the phone number
        TextView txtPhoneNumber = findViewById(R.id.txtPhoneNumber);
        if (txtPhoneNumber != null) {
            String phoneNumber = "+60137480988";
            Spanned underlinedText = Html.fromHtml("<u>" + phoneNumber + "</u>", Html.FROM_HTML_MODE_LEGACY);
            txtPhoneNumber.setText(underlinedText);
        }
    }

    // E-COMPLAINT BUTTON
    public void openComplaint(View view) {
        Intent intent = new Intent(this, ComplaintActivity.class);
        startActivity(intent);
    }

    // HISTORY BUTTON
    public void history(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    // ABOUT DEVELOPER BUTTON
    public void openAboutDeveloper(View view) {
        Intent intent = new Intent(this, AboutDeveloperActivity.class);
        startActivity(intent);
    }

    // CONTACT US BUTTON
    public void contactUs(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+60137480988"));
        startActivity(intent);
    }
}
