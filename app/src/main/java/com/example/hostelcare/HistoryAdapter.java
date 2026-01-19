package com.example.hostelcare;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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

        Map<String, Object> complaint = list.get(position);

        // 🔹 COMMENT (FIRST)
        String comment = complaint.get("comment") != null ? complaint.get("comment").toString() : "";
        txtComment.setText(comment);

        // 🔹 DATE (SECOND)
        long timestamp = (long) complaint.get("timestamp");
        String date = new SimpleDateFormat(
                "dd/MM/yyyy", Locale.getDefault()
        ).format(new Date(timestamp));

        txtDate.setText("Date: " + date);

        // 🔹 STATUS (THIRD)
        String status = complaint.get("status").toString();
        txtStatus.setText("Status: " + status);

        if (status.equalsIgnoreCase("Completed")) {
            txtStatus.setTextColor(context.getColor(R.color.status_completed));
        } else {
            txtStatus.setTextColor(context.getColor(R.color.status_pending));
        }

        // 🔹 CLICK → COMPLAINT DETAILS PAGE
        convertView.setOnClickListener(v -> {

            Intent intent = new Intent(context, ComplaintDetailsActivity.class);

            intent.putExtra("complaintId", complaint.get("id").toString());
            intent.putExtra("comment", complaint.get("comment").toString());
            intent.putExtra("status", status);
            intent.putExtra("timestamp", timestamp);

            context.startActivity(intent);
        });

        return convertView;
    }
}
