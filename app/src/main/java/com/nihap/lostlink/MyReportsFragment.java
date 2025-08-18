package com.nihap.lostlink;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyReportsFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ImageButton backButton;
    private TextView titleText;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        backButton = view.findViewById(R.id.backButton);
        titleText = view.findViewById(R.id.titleText);

        titleText.setText("My Reports");

        // Set up back button
        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Set up ViewPager with adapter
        MyReportsPagerAdapter adapter = new MyReportsPagerAdapter(getActivity());
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Lost Items");
                    break;
                case 1:
                    tab.setText("Found Items");
                    break;
            }
        }).attach();
    }

    // ViewPager adapter for Lost and Found tabs
    private static class MyReportsPagerAdapter extends FragmentStateAdapter {

        public MyReportsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return MyReportsListFragment.newInstance(position == 0 ? "Lost" : "Found");
        }

        @Override
        public int getItemCount() {
            return 2; // Lost and Found tabs
        }
    }

    // Fragment for each tab content
    public static class MyReportsListFragment extends Fragment implements MyReportsAdapter.OnReportActionListener {

        private static final String ARG_REPORT_TYPE = "report_type";
        private RecyclerView recyclerView;
        private SwipeRefreshLayout swipeRefreshLayout;
        private MyReportsAdapter adapter; // Changed to MyReportsAdapter
        private List<ReportDataClass> reports;
        private String reportType;
        private FirebaseAuth auth;
        private FirebaseFirestore db;

        public static MyReportsListFragment newInstance(String reportType) {
            MyReportsListFragment fragment = new MyReportsListFragment();
            Bundle args = new Bundle();
            args.putString(ARG_REPORT_TYPE, reportType);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                reportType = getArguments().getString(ARG_REPORT_TYPE);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_my_reports_list, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Initialize views
            recyclerView = view.findViewById(R.id.recyclerView);
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

            // Set up RecyclerView with MyReportsAdapter
            reports = new ArrayList<>();
            adapter = new MyReportsAdapter(getContext(), reports, this); // Pass this as listener
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);

            // Set up SwipeRefreshLayout
            swipeRefreshLayout.setOnRefreshListener(this::loadMyReports);

            // Load reports
            loadMyReports();
        }

        @Override
        public void onReportDeleted() {
            // Refresh the list when a report is deleted
            loadMyReports();
        }

        private void loadMyReports() {
            if (auth.getCurrentUser() == null) {
                swipeRefreshLayout.setRefreshing(false);
                return;
            }

            String currentUserId = auth.getCurrentUser().getUid();

            // First try without ordering to see if we can get any data
            db.collection("reports")
                    .whereEqualTo("userId", currentUserId)
                    .whereEqualTo("reportType", reportType)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        reports.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            ReportDataClass report = document.toObject(ReportDataClass.class);
                            reports.add(report);
                        }

                        // Sort by timestamp manually if reports exist
                        if (!reports.isEmpty() && reports.get(0).getTimestamp() != null) {
                            reports.sort((r1, r2) -> {
                                if (r1.getTimestamp() != null && r2.getTimestamp() != null) {
                                    return r2.getTimestamp().compareTo(r1.getTimestamp()); // Descending order
                                }
                                return 0;
                            });
                        }

                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);

                    })
                    .addOnFailureListener(e -> {
                        swipeRefreshLayout.setRefreshing(false);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error loading reports: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        e.printStackTrace();
                    });
        }
    }
}
