package com.nihap.lostlink;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SupportFragment extends Fragment {

    private ImageButton backButton;
    private TextView titleText;
    private Button emailButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_support, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        backButton = view.findViewById(R.id.backButton);
        titleText = view.findViewById(R.id.titleText);
        emailButton = view.findViewById(R.id.emailButton);

        titleText.setText("Support");

        // Set up back button
        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Set up email button to open email app
        emailButton.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:nihapmrm@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "LostLink App Support");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi,\n\nI need help with the LostLink app.\n\nIssue: \n\nThank you!");

            try {
                startActivity(Intent.createChooser(emailIntent, "Send Email"));
            } catch (android.content.ActivityNotFoundException ex) {
                // Handle case where no email app is available
                // You could show a toast or copy email to clipboard
            }
        });
    }
}
