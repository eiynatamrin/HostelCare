package com.example.hostelcare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ComplaintActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 100;
    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int GALLERY_REQUEST = 200;
    private static final int MAP_PICKER_REQUEST = 300;

    ImageView imagePreview;
    String imageBase64 = null;

    double selectedLat = 0;
    double selectedLng = 0;
    String locationName = "";
    boolean hasLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Complaint Form");
        }

        imagePreview = findViewById(R.id.imagePreview);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ================= CAMERA =================

    public void takePhoto(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        if (requestCode == CAMERA_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            openCamera();
        }
    }

    // ================= GALLERY =================

    public void openGallery(View view) {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    // ================= MAP PICKER =================

    public void openMap(View view) {
        Intent intent = new Intent(this, MapPickerActivity.class);
        startActivityForResult(intent, MAP_PICKER_REQUEST);
    }

    // ================= ACTIVITY RESULT =================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == CAMERA_REQUEST) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imagePreview.setImageBitmap(bitmap);
            imageBase64 = bitmapToBase64(bitmap);
        }

        if (requestCode == GALLERY_REQUEST) {
            Uri uri = data.getData();
            imagePreview.setImageURI(uri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        this.getContentResolver(), uri
                );
                imageBase64 = bitmapToBase64(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == MAP_PICKER_REQUEST) {
            selectedLat = data.getDoubleExtra("lat", 0);
            selectedLng = data.getDoubleExtra("lng", 0);
            locationName = data.getStringExtra("locationName");
            hasLocation = true;

            TextView txtPinnedLocation = findViewById(R.id.txtPinnedLocation);
            if (locationName != null && !locationName.isEmpty()) {
                txtPinnedLocation.setText("Location: " + locationName);
            } else {
                txtPinnedLocation.setText(
                        String.format("Location: %.6f, %.6f", selectedLat, selectedLng)
                );
            }
        }
    }

    // ================= IMAGE CONVERT =================

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // ================= SUBMIT COMPLAINT =================

    public void submitComplaint(View view) {

        EditText commentInput = findViewById(R.id.comment);
        String comment = commentInput.getText().toString().trim();

        if (comment.isEmpty()) {
            Toast.makeText(this, "Please enter your complaint", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> complaint = new HashMap<>();
        complaint.put("comment", comment);
        complaint.put("userId", uid);
        complaint.put("status", "Pending");
        complaint.put("timestamp", System.currentTimeMillis());

        if (imageBase64 != null) {
            complaint.put("imageBase64", imageBase64);
        }

        if (hasLocation) {
            complaint.put("latitude", selectedLat);
            complaint.put("longitude", selectedLng);
            complaint.put("locationName", locationName);
        }

        FirebaseFirestore.getInstance()
                .collection("complaints")
                .add(complaint)
                .addOnSuccessListener(doc -> {

                    Toast.makeText(
                            this,
                            "Complaint submitted. Redirecting to Hostel Careline...",
                            Toast.LENGTH_LONG
                    ).show();

                    // ⏱️ Delay 5 seconds before opening WhatsApp
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        openWhatsAppCareline(comment, locationName);
                        finish();
                    }, 5000);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Submission failed", Toast.LENGTH_SHORT).show()
                );
    }

    // ================= WHATSAPP CARELINE =================

    private void openWhatsAppCareline(String issue, String location) {

        String phoneNumber = "60137480988"; // 013-7480988 (Malaysia format)

        String message =
                "📢 HOSTEL MAINTENANCE COMPLAINT\n\n" +
                        "🛠 Issue:\n" + issue + "\n\n" +
                        "📌 Status:\nPending\n\n" +
                        "📍 Location:\n" +
                        (location != null && !location.isEmpty()
                                ? location
                                : "Location not specified") +
                        "\n\n📅 Submitted via:\nHostelCare Mobile Application\n\n" +
                        "⚠️ Image attached in HostelCare App";

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(
                    Uri.parse("https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message))
            );
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }
}
