package com.nihap.lostlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    EditText emailET, passwordET;
    TextView registerText;
    Button loginBtn;

    ConstraintLayout loadingScreen;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        registerText = findViewById(R.id.register);
        loginBtn = findViewById(R.id.login_button);


        emailET = findViewById(R.id.email);
        passwordET = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        loadingScreen = findViewById(R.id.loading_screen);
        if (currentUser != null) {
            goToHome(currentUser.getEmail());
            return;
        }

        loginBtn.setOnClickListener(v -> login());

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });
    }

    void login() {

        Animation clickEffect = AnimationUtils.loadAnimation(this,R.anim.click);
        loginBtn.startAnimation(clickEffect);
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        loadingScreen.setVisibility(View.VISIBLE);


        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            loadingScreen.setVisibility(View.GONE);
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        goToHome(user.getEmail());
                    } else {
                        loadingScreen.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Authentication failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void goToHome(String email) {
        Intent intent = new Intent(Login.this, Home.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}