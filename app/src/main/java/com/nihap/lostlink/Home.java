package com.nihap.lostlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity implements NotificationManager.NotificationUpdateListener {

    BottomNavigationView bottomNavigationView;
    private FrameLayout notificationContainer;
    private TextView notificationBadge;
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        notificationContainer = findViewById(R.id.notificationContainer);
        notificationBadge = findViewById(R.id.notificationBadge);

        // Initialize notification manager
        notificationManager = new NotificationManager(this);
        notificationManager.setNotificationUpdateListener(this);

        // Set notification icon click listener
        notificationContainer.setOnClickListener(v -> {
            // Navigate to chat fragment
            bottomNavigationView.setSelectedItemId(R.id.nav_chat);
            loadFragment(new ChatFragment());
        });

        // Check if opened from notification
        Intent intent = getIntent();
        if (intent.getBooleanExtra("openChat", false)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_chat);
            loadFragment(new ChatFragment());
        } else {
            // Load default fragment
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_reports) {
                fragment = new ReportFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }else if (id == R.id.nav_chat) {
                fragment = new ChatFragment();
            }else if (id == R.id.nav_settings) {
                fragment = new SettingsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
            }

            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update notification badge when activity resumes
        notificationManager.updateNotificationBadge();
    }

    @Override
    public void onNotificationCountChanged(int count) {
        runOnUiThread(() -> {
            if (count > 0) {
                notificationBadge.setText(String.valueOf(count));
                notificationBadge.setVisibility(View.VISIBLE);
            } else {
                notificationBadge.setVisibility(View.GONE);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
