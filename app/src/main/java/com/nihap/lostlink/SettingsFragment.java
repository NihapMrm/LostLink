package com.nihap.lostlink;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {

    private ImageButton buttonBack;
    private LinearLayout layoutChangePassword, layoutFAQ, layoutAboutApp;
    private Switch switchNotifications;

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize views
        buttonBack = view.findViewById(R.id.buttonBack);
        layoutChangePassword = view.findViewById(R.id.layoutChangePassword);
        layoutFAQ = view.findViewById(R.id.layoutFAQ);
        layoutAboutApp = view.findViewById(R.id.layoutAboutApp);
        switchNotifications = view.findViewById(R.id.switchNotifications);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences for notification settings
        sharedPreferences = getActivity().getSharedPreferences("app_settings", getContext().MODE_PRIVATE);

        // Load notification preference
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);

        setupClickListeners();

        return view;
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> getActivity().onBackPressed());

        layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();

            String message = isChecked ? "Push notifications enabled" : "Push notifications disabled";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });

        layoutFAQ.setOnClickListener(v -> showFAQDialog());

        layoutAboutApp.setOnClickListener(v -> showAboutAppDialog());
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);

        EditText editCurrentPassword = dialogView.findViewById(R.id.editCurrentPassword);
        EditText editNewPassword = dialogView.findViewById(R.id.editNewPassword);
        EditText editConfirmPassword = dialogView.findViewById(R.id.editConfirmPassword);

        builder.setView(dialogView)
                .setTitle("Change Password")
                .setPositiveButton("Change", (dialog, which) -> {
                    String currentPassword = editCurrentPassword.getText().toString().trim();
                    String newPassword = editNewPassword.getText().toString().trim();
                    String confirmPassword = editConfirmPassword.getText().toString().trim();

                    if (validatePasswordInputs(currentPassword, newPassword, confirmPassword)) {
                        changePassword(currentPassword, newPassword);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(getContext(), "Please enter current password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(getContext(), "Please enter new password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(getContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void changePassword(String currentPassword, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 ->
                                        Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Failed to change password: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show());
        }
    }

    private void showFAQDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Frequently Asked Questions")
                .setMessage(getFAQContent())
                .setPositiveButton("OK", null)
                .show();
    }

    private String getFAQContent() {
        return "Q: How do I report a lost item?\n" +
                "A: Tap the '+' button on the home screen and select 'Report Lost Item'. Fill in the details and location.\n\n" +

                "Q: How do I report a found item?\n" +
                "A: Tap the '+' button on the home screen and select 'Report Found Item'. Add photos and description.\n\n" +

                "Q: How will I be notified about matches?\n" +
                "A: You'll receive push notifications when potential matches are found for your items.\n\n" +

                "Q: Is my personal information safe?\n" +
                "A: Yes, we only share contact information when there's a confirmed match between lost and found items.\n\n" +

                "Q: How do I contact someone about an item?\n" +
                "A: Use the chat feature available when viewing item details to communicate securely.";
    }

    private void showAboutAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("About LostLink")
                .setMessage(getAboutContent())
                .setPositiveButton("OK", null)
                .show();
    }

    private String getAboutContent() {
        return "LostLink v1.0.0\n\n" +
                "LostLink is a community-driven platform designed to help people reunite with their lost belongings.\n\n" +

                "Features:\n" +
                "• Report lost and found items\n" +
                "• Location-based matching\n" +
                "• Secure messaging system\n" +
                "• Real-time notifications\n" +
                "• Photo sharing capabilities\n\n" +

                "Developed with ❤️ to help bring lost items back home.\n\n" +

                "Contact us: support@lostlink.com\n" +
                "Privacy Policy: www.lostlink.com/privacy\n" +
                "Terms of Service: www.lostlink.com/terms";
    }
}
