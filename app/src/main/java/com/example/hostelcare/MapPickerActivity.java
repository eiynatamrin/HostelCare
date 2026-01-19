package com.example.hostelcare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION = 1001;

    MapView mapView;
    Marker marker;
    GeoPoint selectedPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // REQUIRED FOR OSM
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map_picker);

        mapView = findViewById(R.id.map);

        // 🔴 REQUIRED SETTINGS
        mapView.setTileSource(TileSourceFactory.MAPNIK); // ✅ THIS FIXES BLANK MAP
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(16.0);
        mapView.getController().setCenter(
                new GeoPoint(3.1390, 101.6869)); // Malaysia

        requestLocationPermission();

        // TAP MAP TO DROP PIN
        MapEventsOverlay overlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                dropPin(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        mapView.getOverlays().add(overlay);

        Button btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> {
            if (selectedPoint == null) {
                Toast.makeText(this, "Tap map to select location", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get location name/address
            String locationName = getLocationName(selectedPoint.getLatitude(), selectedPoint.getLongitude());

            Intent result = new Intent();
            result.putExtra("lat", selectedPoint.getLatitude());
            result.putExtra("lng", selectedPoint.getLongitude());
            result.putExtra("locationName", locationName);
            setResult(RESULT_OK, result);
            finish();
        });

        Button btnMyLocation = findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(v -> goToMyLocation());

        // Search functionality
        EditText searchLocation = findViewById(R.id.searchLocation);
        Button btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(v -> performSearch());

        // Search on Enter key press
        searchLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });

        // Automatically show current location when map opens
        mapView.post(() -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                goToMyLocation();
            }
        });
    }

    private void performSearch() {
        EditText searchLocation = findViewById(R.id.searchLocation);
        String query = searchLocation.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a location to search", Toast.LENGTH_SHORT).show();
            return;
        }

        searchLocation(query);
    }

    private void searchLocation(String query) {
        if (!Geocoder.isPresent()) {
            Toast.makeText(this, "Geocoder not available on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double lat = address.getLatitude();
                double lng = address.getLongitude();
                
                GeoPoint point = new GeoPoint(lat, lng);
                
                // Center map on searched location
                mapView.getController().setZoom(18.0);
                mapView.getController().setCenter(point);
                
                // Drop pin at searched location
                dropPin(point);
                
                // Show address in toast
                String addressLine = address.getAddressLine(0);
                Toast.makeText(this, "Found: " + (addressLine != null ? addressLine : query), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Location not found. Please try a different search term.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error searching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void dropPin(GeoPoint point) {
        selectedPoint = point;

        if (marker != null) {
            mapView.getOverlays().remove(marker);
        }

        marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Selected Location");
        marker.setDraggable(true); // Make the marker draggable
        
        // Set drag listener to update selectedPoint when marker is dragged
        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {
                // Called during drag
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // Update selectedPoint when drag ends
                selectedPoint = marker.getPosition();
                Toast.makeText(MapPickerActivity.this, 
                    "Location updated: " + selectedPoint.getLatitude() + ", " + selectedPoint.getLongitude(), 
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                // Called when drag starts
            }
        });

        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    private void openInGoogleMaps(GeoPoint point) {
        double lat = point.getLatitude();
        double lng = point.getLongitude();

        // Create a URI for Google Maps
        Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng);

        // Create an Intent to open Google Maps
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Try to open Google Maps, if not available, use any map app
        try {
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback to any map application
                Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                if (fallbackIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(fallbackIntent);
                } else {
                    Toast.makeText(this, "No map application available", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening maps: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void goToMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            requestLocationPermission();
            return;
        }

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location loc = null;

        // Try GPS first for better accuracy
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        
        // Fallback to Network provider
        if (loc == null && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (loc != null) {
            GeoPoint point = new GeoPoint(loc.getLatitude(), loc.getLongitude());
            mapView.getController().setZoom(18.0);
            mapView.getController().setCenter(point);
            dropPin(point);
            Toast.makeText(this, "Current location pinned", Toast.LENGTH_SHORT).show();
        } else {
            // Request location update if last known location is not available
            try {
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                        mapView.getController().setZoom(18.0);
                        mapView.getController().setCenter(point);
                        dropPin(point);
                        Toast.makeText(MapPickerActivity.this, "Current location pinned", Toast.LENGTH_SHORT).show();
                        lm.removeUpdates(this);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}

                    @Override
                    public void onProviderEnabled(String provider) {}

                    @Override
                    public void onProviderDisabled(String provider) {}
                };

                // Request a single location update
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                } else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                } else {
                    Toast.makeText(this, "Location services are disabled. Please enable GPS.", Toast.LENGTH_LONG).show();
                }
            } catch (SecurityException e) {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION
            );
        }
    }

    private String getLocationName(double lat, double lng) {
        if (!Geocoder.isPresent()) {
            return String.format("%.6f, %.6f", lat, lng);
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                // Try to get a readable address
                String addressLine = address.getAddressLine(0);
                if (addressLine != null && !addressLine.isEmpty()) {
                    return addressLine;
                }
                // Fallback to feature name or locality
                if (address.getFeatureName() != null && !address.getFeatureName().isEmpty()) {
                    return address.getFeatureName();
                }
                if (address.getLocality() != null && !address.getLocality().isEmpty()) {
                    return address.getLocality();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Fallback to coordinates if geocoding fails
        return String.format("%.6f, %.6f", lat, lng);
    }
}
