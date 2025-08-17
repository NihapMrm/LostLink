package com.nihap.lostlink.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nihap.lostlink.R;

public class OnboardFragment extends Fragment {

    private static final String ARG_POSITION = "position";

    public static OnboardFragment newInstance(int position) {
        OnboardFragment fragment = new OnboardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    private int position;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboard, container, false);

        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);
        ImageView image = view.findViewById(R.id.image);

        // Set data based on position
        switch (position) {
            case 0:
                title.setText("Welcome to LostLink!");
                description.setText("Find your lost items easily.");
                image.setImageResource(R.drawable.img_onboard1);  // add your image
                break;
            case 1:
                title.setText("Stay Connected");
                description.setText("Chat with community about found items.");
                image.setImageResource(R.drawable.img_onboard2);
                break;
            case 2:
                title.setText("Get Notified");
                description.setText("Receive alerts instantly.");
                image.setImageResource(R.drawable.img_onboard3);
                break;
        }

        return view;
    }
}
