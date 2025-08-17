package com.nihap.lostlink;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.*;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextFullName, editTextEmail, editTextPhone;
    private Button buttonSave;
    private ImageView profileImageView;
    private ImageButton buttonBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DocumentReference userRef;

    private Uri imageUri;
    private String currentImageUrl = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        editTextFullName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        buttonSave = view.findViewById(R.id.buttonSaveProfile);
        profileImageView = view.findViewById(R.id.profileImageView);
        buttonBack = view.findViewById(R.id.buttonBack);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("profile_images");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userRef = db.collection("users").document(uid);
            loadUserData();
        }

        profileImageView.setOnClickListener(v -> openImageChooser());
        buttonSave.setOnClickListener(v -> saveUserData());
        buttonBack.setOnClickListener(v -> getActivity().onBackPressed());

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri); // preview
        }
    }

    private void loadUserData() {
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String Name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String phone = documentSnapshot.getString("phone");
                currentImageUrl = documentSnapshot.getString("profileImage");
                int radiusInPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        500, // your dp value
                        getResources().getDisplayMetrics()
                );
                editTextFullName.setText(Name);
                editTextEmail.setText(email);
                editTextPhone.setText(phone);

                if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(currentImageUrl)
                            .transform(new RoundedCorners(radiusInPx))
                            .placeholder(R.drawable.ic_default_profile)
                            .into(profileImageView);
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getActivity(), "Failed to load profile", Toast.LENGTH_SHORT).show()
        );
    }

    private void saveUserData() {
        String Name = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (TextUtils.isEmpty(Name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
            Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            uploadImageAndSaveData(Name, email, phone);
        } else {
            updateFirestore(Name, phone, currentImageUrl);
        }
    }

    private void uploadImageAndSaveData(String Name, String email, String phone) {
        String fileName = mAuth.getCurrentUser().getUid() + ".jpg";
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            updateFirestore(Name, phone, downloadUrl);
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateFirestore(String Name, String phone, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", Name);
        updates.put("phone", phone);
        updates.put("profileImage", imageUrl);

        userRef.update(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
