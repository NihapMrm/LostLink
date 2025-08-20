package com.nihap.lostlink;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EditReportFragment extends Fragment {

    private static final String ARG_REPORT = "report";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private ReportDataClass originalReport;
    private EditText editItemName, editLocation, editDescription;
    private Spinner spinnerReportType;
    private ImageView imagePreview;
    private Button btnSave, btnSelectCamera, btnSelectGallery;
    private ImageButton backButton;
    private TextView titleText;

    private Uri selectedImageUri;
    private Uri photoUri;
    private String currentPhotoPath;
    private String currentImageUrl;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public static EditReportFragment newInstance(ReportDataClass report) {
        EditReportFragment fragment = new EditReportFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REPORT, report);
        fragment.setArguments(args);
        return fragment;
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imagePreview.setImageURI(selectedImageUri);
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    selectedImageUri = photoUri;
                    imagePreview.setImageURI(selectedImageUri);
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            originalReport = (ReportDataClass) getArguments().getSerializable(ARG_REPORT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize views
        backButton = view.findViewById(R.id.backButton);
        titleText = view.findViewById(R.id.titleText);
        editItemName = view.findViewById(R.id.editItemName);
        editLocation = view.findViewById(R.id.editLocation);
        editDescription = view.findViewById(R.id.editDescription);
        spinnerReportType = view.findViewById(R.id.spinnerReportType);
        imagePreview = view.findViewById(R.id.imagePreview);
        btnSelectCamera = view.findViewById(R.id.btnSelectCamera);
        btnSelectGallery = view.findViewById(R.id.btnSelectGallery);
        btnSave = view.findViewById(R.id.btnSave);

        titleText.setText("Edit Report");

        // Set up spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.report_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReportType.setAdapter(adapter);

        // Populate fields with existing data
        if (originalReport != null) {
            editItemName.setText(originalReport.getItemName());
            editLocation.setText(originalReport.getLocation());
            editDescription.setText(originalReport.getDescription());
            currentImageUrl = originalReport.getImageUrl();

            // Set spinner selection
            if (originalReport.getReportType() != null) {
                if (originalReport.getReportType().equals("Lost")) {
                    spinnerReportType.setSelection(0);
                } else if (originalReport.getReportType().equals("Found")) {
                    spinnerReportType.setSelection(1);
                }
            }

            // Load existing image
            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                Glide.with(this).load(currentImageUrl).into(imagePreview);
            }
        }

        // Set up click listeners
        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        btnSelectGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSelectCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        btnSave.setOnClickListener(v -> saveReport());
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                photoUri = FileProvider.getUriForFile(requireContext(),
                        "com.nihap.lostlink.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(takePictureIntent);
            } catch (IOException ex) {
                Toast.makeText(getContext(), "Error occurred while creating the file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission is required to take pictures", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveReport() {
        String itemName = editItemName.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String reportType = spinnerReportType.getSelectedItem().toString();

        if (itemName.isEmpty() || location.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        if (selectedImageUri != null) {
            // Upload new image first
            uploadImageAndSaveReport(itemName, location, description, reportType);
        } else {
            // Save with existing image URL
            updateReportInFirestore(itemName, location, description, reportType, currentImageUrl);
        }
    }

    private void uploadImageAndSaveReport(String itemName, String location, String description, String reportType) {
        String fileName = "reports/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference().child(fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateReportInFirestore(itemName, location, description, reportType, uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                    Toast.makeText(getContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateReportInFirestore(String itemName, String location, String description, String reportType, String imageUrl) {
        String currentUserId = auth.getCurrentUser().getUid();

        // Find and update the report
        db.collection("reports")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("itemName", originalReport.getItemName())
                .whereEqualTo("reportType", originalReport.getReportType())
                .whereEqualTo("timestamp", originalReport.getTimestamp())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("itemName", itemName);
                        updates.put("location", location);
                        updates.put("description", description);
                        updates.put("reportType", reportType);
                        updates.put("imageUrl", imageUrl);

                        db.collection("reports").document(documentId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Report updated successfully", Toast.LENGTH_SHORT).show();
                                    if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                                        getParentFragmentManager().popBackStack();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    btnSave.setEnabled(true);
                                    btnSave.setText("Save Changes");
                                    Toast.makeText(getContext(), "Failed to update report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save Changes");
                        Toast.makeText(getContext(), "Report not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                    Toast.makeText(getContext(), "Error finding report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
