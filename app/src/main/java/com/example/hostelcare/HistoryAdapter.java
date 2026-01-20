package com.example.hostelcare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryAdapter extends BaseAdapter {

    Context context;
    List<Map<String, Object>> list;

    public HistoryAdapter(Context context, List<Map<String, Object>> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_complaint, parent, false);
        }

        TextView txtComment = convertView.findViewById(R.id.txtComment);
        TextView txtDate = convertView.findViewById(R.id.txtDate);
        TextView txtStatus = convertView.findViewById(R.id.txtStatus);
        Button btnDelete = convertView.findViewById(R.id.btnDelete); // ✅ NEW

        Map<String, Object> complaint = list.get(position);

        // 🔹 COMMENT
        String comment = complaint.get("comment") != null
                ? complaint.get("comment").toString()
                : "";
        txtComment.setText(comment);

        // 🔹 DATE
        long timestamp = (long) complaint.get("timestamp");
        String date = new SimpleDateFormat(
                "dd/MM/yyyy", Locale.getDefault()
        ).format(new Date(timestamp));
        txtDate.setText("Date: " + date);

        // 🔹 STATUS
        String status = complaint.get("status").toString();
        txtStatus.setText("Status: " + status);

        if (status.equalsIgnoreCase("Completed")) {
            txtStatus.setTextColor(context.getColor(R.color.status_completed));
        } else {
            txtStatus.setTextColor(context.getColor(R.color.status_pending));
        }

        // 🔹 CLICK → DETAILS PAGE
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ComplaintDetailsActivity.class);
            intent.putExtra("complaintId", complaint.get("id").toString());
            context.startActivity(intent);
        });

        // 🔹 DELETE BUTTON LOGIC
        btnDelete.setOnClickListener(v -> {

            // Optional: prevent deleting completed complaints
            if (status.equalsIgnoreCase("Completed")) {
                Toast.makeText(context,
                        "Completed complaints cannot be deleted",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(context)
                    .setTitle("Delete Complaint")
                    .setMessage("Are you sure you want to delete this complaint?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        String complaintId = complaint.get("id").toString();

                        FirebaseFirestore.getInstance()
                                .collection("complaints")
                                .document(complaintId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    list.remove(position);
                                    notifyDataSetChanged();
                                    Toast.makeText(context,
                                            "Complaint deleted",
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context,
                                                "Delete failed",
                                                Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return convertView;
    }
}
