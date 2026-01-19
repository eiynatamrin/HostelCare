package com.example.hostelcare;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ComplaintDetailsActivity extends AppCompatActivity {

    EditText commentEdit;
    ImageView imagePreview;
    TextView txtLocation;
    FrameLayout mapContainer;
    MapView mapView;

    RadioGroup statusGroup;
    RadioButton radioPending, radioCompleted;

    String complaintId;

    Marker marker;
    double selectedLat, selectedLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_complaint_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Complaint Details");
        }

        commentEdit = findViewById(R.id.detailComment);
        imagePreview = findViewById(R.id.detailImage);
        txtLocation = findViewById(R.id.txtLocation);
        mapContainer = findViewById(R.id.mapContainer);

        statusGroup = findViewById(R.id.statusGroup);
        radioPending = findViewById(R.id.radioPending);
        radioCompleted = findViewById(R.id.radioCompleted);

        complaintId = getIntent().getStringExtra("complaintId");

        initializeMap();
        loadComplaint();
    }

    private void initializeMap() {
        mapView = new MapView(this);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true); // ENABLE interaction
        mapView.setClickable(true);

        mapView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                GeoPoint geoPoint = (GeoPoint) mapView.getProjection()
                        .fromPixels((int) event.getX(), (int) event.getY());

                selectedLat = geoPoint.getLatitude();
                selectedLng = geoPoint.getLongitude();

                updateMarker(selectedLat, selectedLng);

                String locationName = getLocationName(selectedLat, selectedLng);
                txtLocation.setText("Location: " + locationName);
            }
            return false;
        });

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );

        mapView.setLayoutParams(params);
        mapContainer.addView(mapView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadComplaint() {
        FirebaseFirestore.getInstance()
                .collection("complaints")
                .document(complaintId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    commentEdit.setText(doc.getString("comment"));

                    if (doc.contains("imageBase64")) {
                        imagePreview.setImageBitmap(
                                base64ToBitmap(doc.getString("imageBase64"))
                        );
                    }

                    String status = doc.getString("status");
                    if ("Completed".equalsIgnoreCase(status)) {
                        radioCompleted.setChecked(true);
                    } else {
                        radioPending.setChecked(true);
                    }

                    if (doc.contains("latitude") && doc.contains("longitude")) {
                        selectedLat = doc.getDouble("latitude");
                        selectedLng = doc.getDouble("longitude");

                        updateMarker(selectedLat, selectedLng);

                        String locationName = getLocationName(selectedLat, selectedLng);
                        txtLocation.setText("Location: " + locationName);
                    } else {
                        txtLocation.setText("Location: Tap map to select");
                    }
                });
    }

    private void updateMarker(double lat, double lng) {
        GeoPoint point = new GeoPoint(lat, lng);

        mapView.getController().setZoom(18.0);
        mapView.getController().setCenter(point);

        if (marker != null) {
            mapView.getOverlays().remove(marker);
        }

        marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Selected Location");

        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    private String getLocationName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Location name not available";
    }

    private Bitmap base64ToBitmap(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public void updateComplaint(View view) {

        String comment = commentEdit.getText().toString().trim();
        if (comment.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String status = radioPending.isChecked() ? "Pending" : "Completed";

        FirebaseFirestore.getInstance()
                .collection("complaints")
                .document(complaintId)
                .update(
                        "comment", comment,
                        "status", status,
                        "latitude", selectedLat,
                        "longitude", selectedLng
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Complaint updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                );
    }
}
