package com.nihap.lostlink;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private Context context;
    private List<ReportDataClass> reports;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public ReportAdapter(Context context, List<ReportDataClass> reports) {
        this.context = context;
        this.reports = reports;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView itemName, location, date, postby, reportType;
        Button viewBtn;


        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            itemName = itemView.findViewById(R.id.itemName);
            location = itemView.findViewById(R.id.location);
            date = itemView.findViewById(R.id.date);
            postby = itemView.findViewById(R.id.postby);
            reportType = itemView.findViewById(R.id.report_type);
            viewBtn = itemView.findViewById(R.id.btn_view);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.report_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ReportDataClass report = reports.get(position);
        holder.itemName.setText(report.getItemName());
        holder.location.setText(report.getLocation());
        holder.reportType.setText(report.getReportType());
        Timestamp ts = report.getTimeStamp();
        if (ts != null) {
            Date date = ts.toDate();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            holder.date.setText(dateFormat.format(date));
        } else {
            holder.date.setText("N/A");
        }

        holder.postby.setText(report.getUserName());


        holder.viewBtn.setOnClickListener(v -> {
            ReportDetailBottomSheet bottomSheet = new ReportDetailBottomSheet(report);
            bottomSheet.show(((FragmentActivity) context).getSupportFragmentManager(), "report_detail");
        });

        Glide.with(context).load(report.getImageUrl()).into(holder.image); // Load image
    }





    @Override
    public int getItemCount() {
        return reports.size();
    }


}
