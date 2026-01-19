package com.example.hostelcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {

    ListView listView;
    List<Map<String, Object>> dataList = new ArrayList<>();
    HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Complaint History");
        }

        listView = findViewById(R.id.listView);
        adapter = new HistoryAdapter(this, dataList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {

            Map<String, Object> item = dataList.get(position);

            Intent intent = new Intent(this, ComplaintDetailsActivity.class);
            intent.putExtra("complaintId", item.get("id").toString());
            startActivity(intent);
        });

        loadHistory();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadHistory() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("complaints")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(query -> {

                    dataList.clear();

                    for (var doc : query) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", doc.getId());
                        item.put("comment", doc.getString("comment"));
                        item.put("status", doc.getString("status"));
                        item.put("timestamp", doc.getLong("timestamp"));
                        dataList.add(item);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
