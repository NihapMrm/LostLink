package com.nihap.lostlink;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.GeoPoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportDetailBottomSheet extends BottomSheetDialogFragment implements OnMapReadyCallback {

    private ReportDataClass report;
    private GoogleMap googleMap;

    public ReportDetailBottomSheet(ReportDataClass report) {
        this.report = report;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.report_detail_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ImageView detailImage = view.findViewById(R.id.detail_image);
        TextView detailName = view.findViewById(R.id.detail_item_name);
        TextView detailLocation = view.findViewById(R.id.detail_location);
        TextView detailType = view.findViewById(R.id.detail_type);
        TextView detailUser = view.findViewById(R.id.detail_user);
        TextView detailDate = view.findViewById(R.id.detail_date);
        TextView detailDesc = view.findViewById(R.id.detail_description);

        detailName.setText(report.getItemName());
        detailLocation.setText(report.getLocation());
        detailType.setText(report.getReportType());
        detailUser.setText(report.getUserName());
        detailDesc.setText(report.getDescription());

        if (report.getTimeStamp() != null) {
            Date date = report.getTimeStamp().toDate();
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            detailDate.setText(df.format(date));
        }

        Glide.with(requireContext()).load(report.getImageUrl()).into(detailImage);

        // Setup Map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        GeoPoint geoPoint = report.getGeoPoint();
        if (geoPoint != null) {
            LatLng location = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
            map.addMarker(new MarkerOptions().position(location).title(report.getItemName()));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
            map.getUiSettings().setAllGesturesEnabled(false);
        }
    }
}
