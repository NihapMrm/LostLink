package com.nihap.lostlink.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nihap.lostlink.Home;
import com.nihap.lostlink.Login;
import com.nihap.lostlink.R;

public class OnboardActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext, btnPrev, btnSkip;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            goToHome(currentUser.getEmail());
            return;
        }
        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnSkip = findViewById(R.id.btnSkip);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Page change listener to control button visibility
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                btnPrev.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
                btnNext.setText(position == adapter.getItemCount() - 1 ? "Finish" : "Next >");
            }
        });

        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                launchLogin();
            }
        });

        btnPrev.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1);
            }
        });

        btnSkip.setOnClickListener(v -> launchLogin());
    }

    private void launchLogin() {
        startActivity(new Intent(OnboardActivity.this, Login.class));
        finish();
    }

    void goToHome(String email) {
        Intent intent = new Intent(OnboardActivity.this, Home.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}
