package com.nihap.lostlink;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatImageButton;
import android.Manifest;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportFragment extends Fragment implements OnMapReadyCallback {
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private AppCompatImageButton addButton;
    private ImageView imagePreview;
    private Uri selectedImageUri;
    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<ReportDataClass> reportList;
    private FirebaseFirestore db;
    private FirebaseUser user;
    SwipeRefreshLayout swipeRefreshLayout;
    ConstraintLayout loadingScreen;
    private BottomSheetDialog bottomSheetDialog;


    private GoogleMap mMap;
    private EditText locationEditText;
    private LatLng selectedLatLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadReports();
        });

        locationEditText = view.findViewById(R.id.locationEditText);



        addButton = view.findViewById(R.id.addButton);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        addButton.setOnClickListener(v -> showAddReportSheet());
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        reportList = new ArrayList<>();
        adapter = new ReportAdapter(getContext(), reportList);
        recyclerView.setAdapter(adapter);



        db = FirebaseFirestore.getInstance();
        loadReports();


        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (imagePreview != null) {
                            imagePreview.setImageURI(selectedImageUri);
                        }
                    }
                }
        );

        db.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    reportList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        ReportDataClass report = doc.toObject(ReportDataClass.class);
                        reportList.add(report);
                    }
                    adapter.notifyDataSetChanged();
                });
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        return view;
    }

    private void loadReports() {
        db.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reportList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ReportDataClass report = doc.toObject(ReportDataClass.class);
                        reportList.add(report);
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading reports", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void showAddReportSheet() {
        Animation clickEffect = AnimationUtils.loadAnimation(getActivity(), R.anim.click);
        addButton.startAnimation(clickEffect);

        bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_add_report, null);
        bottomSheetDialog.setContentView(sheetView);

        ImageButton closeButton = sheetView.findViewById(R.id.close_button);
        imagePreview = sheetView.findViewById(R.id.imagePreview);
        Spinner reportTypeSpinner = sheetView.findViewById(R.id.reportTypeSpinner);
        EditText itemNameEditText = sheetView.findViewById(R.id.itemNameEditText);
        EditText locationEditText = sheetView.findViewById(R.id.locationEditText);
        Spinner radiusSpinner = sheetView.findViewById(R.id.radiusSpinner);
        EditText descriptionEditText = sheetView.findViewById(R.id.descriptionEditText);
        Button submitBtn = sheetView.findViewById(R.id.submitReportButton);
        loadingScreen = sheetView.findViewById(R.id.loading_screen);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.report_types,
                R.layout.spinner_item
        );

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                getContext(),
                R.array.radius_options,
                R.layout.spinner_item
        );

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        adapter2.setDropDownViewResource(R.layout.spinner_dropdown_item);

        reportTypeSpinner.setAdapter(adapter);
        radiusSpinner.setAdapter(adapter2);


        imagePreview.setOnClickListener(v -> openImagePicker());

        submitBtn.setOnClickListener(v -> {
            String reportType = reportTypeSpinner.getSelectedItem().toString();
            String itemName = itemNameEditText.getText().toString();
            String location = locationEditText.getText().toString();
            String radius = radiusSpinner.getSelectedItem().toString();
            String description = descriptionEditText.getText().toString();


            if (itemName.isEmpty() || location.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadReportToFirebase(reportType, itemName, location, radius, description, selectedImageUri);



        });

        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadReportToFirebase(String reportType, String itemName, String location, String radius, String description, Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = "report_images/" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        loadingScreen.setVisibility(View.VISIBLE);


        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        uploadReportData(reportType, itemName, location, radius, description, imageUrl, userId);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                });

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


        // Let user pick location
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear(); // Remove previous marker
            mMap.addMarker(new MarkerOptions().position(latLng));
            selectedLatLng = latLng;

            // Set to EditText
            locationEditText.setText(latLng.latitude + ", " + latLng.longitude);
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        loadReports();
    }
    private void uploadReportData(String reportType, String itemName, String location, String radius, String description, String imageUrl, String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = documentSnapshot.getString("name");
                    Map<String, Object> reportData = new HashMap<>();
                    reportData.put("reportType", reportType);
                    reportData.put("itemName", itemName);
                    reportData.put("location", location);
                    reportData.put("radius", radius);
                    reportData.put("description", description);
                    reportData.put("imageUrl", imageUrl);
                    reportData.put("userId", userId);
                    reportData.put("userName", userName != null ? userName : "Unknown");
                    reportData.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("reports").add(reportData)
                            .addOnSuccessListener(documentReference -> {
                                loadingScreen.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Report submitted", Toast.LENGTH_SHORT).show();
                                bottomSheetDialog.dismiss();

                            })
                            .addOnFailureListener(e -> {
                                loadingScreen.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Failed to submit report", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to get user name", Toast.LENGTH_SHORT).show();
                });


    }

}
