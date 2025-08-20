package com.nihap.lostlink;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatImageButton;
import android.Manifest;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


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
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportFragment extends Fragment {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private AppCompatImageButton addButton;
    private ImageView imagePreview;
    private Uri selectedImageUri;
    private Uri photoUri;
    private String currentPhotoPath;
    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<ReportDataClass> reportList;
    private FirebaseFirestore db;
    private FirebaseUser user;
    SwipeRefreshLayout swipeRefreshLayout;
    ConstraintLayout loadingScreen;
    private BottomSheetDialog bottomSheetDialog;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadReports();
        });

       


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

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        selectedImageUri = photoUri;
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
        Button btnSelectCamera = sheetView.findViewById(R.id.btnSelectCamera);
        Button btnSelectGallery = sheetView.findViewById(R.id.btnSelectGallery);
        Spinner reportTypeSpinner = sheetView.findViewById(R.id.reportTypeSpinner);
        EditText itemNameEditText = sheetView.findViewById(R.id.itemNameEditText);
        EditText locationEditText = sheetView.findViewById(R.id.locationEditText);
        EditText radiusInput = sheetView.findViewById(R.id.radiusInput);
        EditText latLongInput = sheetView.findViewById(R.id.latLongInput);
        EditText descriptionEditText = sheetView.findViewById(R.id.descriptionEditText);
        Button submitBtn = sheetView.findViewById(R.id.submitReportButton);
        loadingScreen = sheetView.findViewById(R.id.loading_screen);

        // Set up image selection buttons
        btnSelectCamera.setOnClickListener(v -> openCamera());
        btnSelectGallery.setOnClickListener(v -> openImagePicker());

        locationEditText.setFocusable(false); // prevent keyboard
        locationEditText.setOnClickListener(v -> {
            MapPickerDialogFragment mapDialog = new MapPickerDialogFragment();
            mapDialog.show(getParentFragmentManager(), "map_picker");
        });


        getParentFragmentManager().setFragmentResultListener("location_result", this, (requestKey, bundle) -> {
            String address = bundle.getString("address");
            double lat = bundle.getDouble("lat");
            double lng = bundle.getDouble("lng");
            locationEditText.setText(address);
            int radius = bundle.getInt("radius");
            radiusInput.setText(String.valueOf(radius));

            latLongInput.setText(lat + "," + lng);
        });


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


        imagePreview.setOnClickListener(v -> openImagePicker());

        submitBtn.setOnClickListener(v -> {
            String reportType = reportTypeSpinner.getSelectedItem().toString();
            String itemName = itemNameEditText.getText().toString();
            String location = locationEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            String latLong= latLongInput.getText().toString();
            int radius = Integer.parseInt(radiusInput.getText().toString());



            if (itemName.isEmpty() || location.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadReportToFirebase(reportType, itemName, location, latLong, radius, description, selectedImageUri);



        });

        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(requireContext(),
                        "com.nihap.lostlink.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadReportToFirebase(String reportType, String itemName, String location, String latLang ,int radius, String description, Uri imageUri) {
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
                        uploadReportData(reportType, itemName, location, latLang ,radius, description, imageUrl, userId);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        loadReports();
    }
    private void uploadReportData(String reportType, String itemName, String location, String latLang,int radius, String description, String imageUrl, String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = documentSnapshot.getString("name");
                    Map<String, Object> reportData = new HashMap<>();
                    reportData.put("reportType", reportType);
                    reportData.put("itemName", itemName);
                    reportData.put("location", location);

                    GeoPoint geoPoint;
                    try {
                        String[] latLngParts = latLang.split(",");
                        double latitude = Double.parseDouble(latLngParts[0].trim());
                        double longitude = Double.parseDouble(latLngParts[1].trim());
                        geoPoint = new GeoPoint(latitude, longitude);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Invalid location format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    reportData.put("geoPoint", geoPoint);

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
