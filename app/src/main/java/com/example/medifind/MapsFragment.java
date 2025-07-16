package com.example.medifind;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLatLng;
    private EditText editTextLocation;
    private Button buttonSearch;
    private TextView textViewRouteInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        editTextLocation = view.findViewById(R.id.editTextLocation);
        buttonSearch = view.findViewById(R.id.buttonSearch);
        textViewRouteInfo = view.findViewById(R.id.textViewRouteInfo);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        buttonSearch.setOnClickListener(v -> {
            String location = editTextLocation.getText().toString().trim();
            if (!location.isEmpty()) {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocationName(location, 1);
                    if (!addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        LatLng searchLatLng = new LatLng(address.getLatitude(), address.getLongitude());

                        if (mMap != null) {
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(searchLatLng).title(location));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng, 15));

                            if (currentLatLng != null) {
                                drawRoute(currentLatLng, searchLatLng);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error finding location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(getContext(), "Google Play Services not available", Toast.LENGTH_LONG).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            return;
        }

        try {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Location permission error", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (mMap != null) {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                }
            } else {
                LocationRequest locationRequest = LocationRequest.create()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .setInterval(10000)
                        .setFastestInterval(5000);

                fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        Location updatedLocation = locationResult.getLastLocation();
                        if (updatedLocation != null) {
                            currentLatLng = new LatLng(updatedLocation.getLatitude(), updatedLocation.getLongitude());
                            if (mMap != null) {
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            }
                            fusedLocationClient.removeLocationUpdates(this);
                        }
                    }
                }, Looper.getMainLooper());
            }
        });
    }

    private void drawRoute(LatLng origin, LatLng dest) {
        String urlString = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + dest.latitude + "," + dest.longitude +
                "&key=AIzaSyDaIznz2GoGbBMckZfNE73_ru2FvBab5TA"; // Replace with your actual key

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                JSONArray routes = json.getJSONArray("routes");

                if (routes.length() > 0) {
                    JSONObject leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0);
                    JSONArray steps = leg.getJSONArray("steps");

                    String durationText = leg.getJSONObject("duration").getString("text");
                    String distanceText = leg.getJSONObject("distance").getString("text");

                    PolylineOptions polylineOptions = new PolylineOptions().color(Color.BLUE).width(10f);

                    for (int i = 0; i < steps.length(); i++) {
                        String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                        polylineOptions.addAll(com.google.maps.android.PolyUtil.decode(polyline));
                    }

                    requireActivity().runOnUiThread(() -> {
                        if (mMap != null) {
                            mMap.addPolyline(polylineOptions);
                        }
                        textViewRouteInfo.setText("ETA: " + durationText + " | Distance: " + distanceText);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MapsFragment", "Error fetching directions: " + e.getMessage());
            }
        }).start();
    }

    private boolean isGooglePlayServicesAvailable() {
        int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext());
        return result == ConnectionResult.SUCCESS;
    }
}
