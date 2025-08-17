package com.nihap.lostlink;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast; // Added for a simple toast message

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    // Declare the button as a member variable
    private Button logoutBtn;
    private TextView nameView, emailView;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ImageView profileImage;
    private String currentImageUrl = "";


    LinearLayout editProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameView = view.findViewById(R.id.nameView);
        emailView = view.findViewById(R.id.emailView);
        profileImage = view.findViewById(R.id.profileImage);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadUserData();


        editProfile = view.findViewById(R.id.editProfile);
        editProfile.setOnClickListener(v -> {
            // Open EditProfileFragment
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new EditProfileFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });


//        logoutBtn = view.findViewById(R.id.logout_btn);
//
//        logoutBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseAuth.getInstance().signOut();
//
//                Intent intent = new Intent(getActivity(), Login.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
//                startActivity(intent);
//                getActivity().finish();
//            }
//        });
    }

    private void loadUserData(){
        String uid = auth.getCurrentUser().getUid();
        int radiusInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                500, // your dp value
                getResources().getDisplayMetrics()
        );

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        nameView.setText(doc.getString("name"));
                        emailView.setText(doc.getString("email"));
                        currentImageUrl = doc.getString("profileImage");

                        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(currentImageUrl)
                                    .transform(new RoundedCorners(radiusInPx))
                                    .placeholder(R.drawable.ic_default_profile)
                                    .into(profileImage);
                        }

                    }
                });


    }
}
