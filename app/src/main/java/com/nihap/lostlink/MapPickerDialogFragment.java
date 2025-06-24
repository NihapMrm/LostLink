package com.nihap.lostlink;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.firebase.firestore.GeoPoint;


public class MapPickerDialogFragment extends DialogFragment implements OnMapReadyCallback {
    private Spinner radiusSpinner;
    private Circle locationCircle;
    private int radiusInMeters;
    private GoogleMap googleMap;
    private LatLng selectedLatLng;
    private FusedLocationProviderClient fusedLocationClient;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_picker_dialog, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        SupportMapFragment mapFragment = new SupportMapFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);

        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) { return false; }
        });



        view.findViewById(R.id.select_location_button).setOnClickListener(v -> {
            if (selectedLatLng != null) {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            selectedLatLng.latitude, selectedLatLng.longitude, 1);
                    if (!addresses.isEmpty()) {
                        String address = addresses.get(0).getAddressLine(0);
                        Bundle result = new Bundle();
                        result.putString("address", address);
                        result.putInt("radius", radiusInMeters);
                        result.putDouble("lat", selectedLatLng.latitude);
                        result.putDouble("lng", selectedLatLng.longitude);



                        GeoPoint geoPoint = new GeoPoint(selectedLatLng.latitude, selectedLatLng.longitude);

                        getParentFragmentManager().setFragmentResult("location_result", result);
                        dismiss();
                    }

                } catch (IOException e) { e.printStackTrace(); }
            }
        });
        FloatingActionButton myLocationButton = view.findViewById(R.id.btn_my_location);
        myLocationButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });

        radiusSpinner = view.findViewById(R.id.radiusSpinner);

        radiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = parent.getItemAtPosition(position).toString();
                radiusInMeters = Integer.parseInt(selectedValue);


                // Update existing circle radius if already placed
                if (locationCircle != null) {
                    locationCircle.setRadius(radiusInMeters);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        return view;
    }

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted) {
                    // Fine location granted
                    getLastLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Coarse location granted (fallback)
                    getLastLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private void getLastLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f));
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            selectedLatLng = latLng;
        });
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            getLastLocation();

        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            selectedLatLng = latLng;

            // Remove previous circle
            if (locationCircle != null) {
                locationCircle.remove();
            }

            // Draw new circle with selected radius
            locationCircle = googleMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(radiusInMeters)
                    .strokeColor(0x550000FF)
                    .fillColor(0x220000FF)
                    .strokeWidth(4f));
        });




    }

    private void searchLocation(String query) {
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(latLng).title(query));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                selectedLatLng = latLng;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
