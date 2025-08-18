package com.nihap.lostlink;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyReportsAdapter extends RecyclerView.Adapter<MyReportsAdapter.ViewHolder> {
    private Context context;
    private List<ReportDataClass> reports;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private OnReportActionListener listener;

    public interface OnReportActionListener {
        void onReportDeleted();
    }

    public MyReportsAdapter(Context context, List<ReportDataClass> reports, OnReportActionListener listener) {
        this.context = context;
        this.reports = reports;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView itemName, location, date, postby, reportType;
        Button viewBtn, editBtn, deleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            itemName = itemView.findViewById(R.id.itemName);
            location = itemView.findViewById(R.id.location);
            date = itemView.findViewById(R.id.date);
            postby = itemView.findViewById(R.id.postby);
            reportType = itemView.findViewById(R.id.report_type);
            viewBtn = itemView.findViewById(R.id.btn_view);
            editBtn = itemView.findViewById(R.id.btn_edit);
            deleteBtn = itemView.findViewById(R.id.btn_delete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_report_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ReportDataClass report = reports.get(position);

        holder.itemName.setText(report.getItemName());
        holder.location.setText(report.getLocation());
        holder.reportType.setText(report.getReportType());

        Timestamp ts = report.getTimestamp();
        if (ts != null) {
            Date date = ts.toDate();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            holder.date.setText(dateFormat.format(date));
        } else {
            holder.date.setText("N/A");
        }

        holder.postby.setText(report.getUserName());

        // Load image
        Glide.with(context).load(report.getImageUrl()).into(holder.image);

        // View button click
        holder.viewBtn.setOnClickListener(v -> {
            ReportDetailBottomSheet bottomSheet = new ReportDetailBottomSheet(report);
            bottomSheet.show(((FragmentActivity) context).getSupportFragmentManager(), "report_detail");
        });

        // Edit button click
        holder.editBtn.setOnClickListener(v -> {
            // Navigate to EditReportFragment
            EditReportFragment editFragment = EditReportFragment.newInstance(report);
            FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, editFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Delete button click
        holder.deleteBtn.setOnClickListener(v -> {
            showDeleteConfirmationDialog(report, position);
        });
    }

    private void showDeleteConfirmationDialog(ReportDataClass report, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Report")
                .setMessage("Are you sure you want to delete this report? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteReport(report, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteReport(ReportDataClass report, int position) {
        // Get current user ID
        String currentUserId = auth.getCurrentUser().getUid();

        // Verify the report belongs to the current user
        if (!report.getUserId().equals(currentUserId)) {
            Toast.makeText(context, "You can only delete your own reports", Toast.LENGTH_SHORT).show();
            return;
        }

        // Delete from Firestore
        db.collection("reports")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("itemName", report.getItemName())
                .whereEqualTo("reportType", report.getReportType())
                .whereEqualTo("timestamp", report.getTimestamp())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.getDocuments().get(0).getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Remove from local list
                                    reports.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, reports.size());

                                    Toast.makeText(context, "Report deleted successfully", Toast.LENGTH_SHORT).show();

                                    // Notify the listener
                                    if (listener != null) {
                                        listener.onReportDeleted();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to delete report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "Report not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error finding report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }
}
