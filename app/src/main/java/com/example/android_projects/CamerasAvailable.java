package com.example.android_projects;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CamerasAvailable extends AppCompatActivity {

    RecyclerView recyclerView;
    CamerasAvailableAdapter adapter;
    ArrayList<CameraCard> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameras_available);

        recyclerView = findViewById(R.id.recycler_available);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new CamerasAvailableAdapter(this, list);
        recyclerView.setAdapter(adapter);

        loadCameras();
    }

    private void loadCameras() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("cameras");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    CameraCard cam = new CameraCard();

                    // ID kamera
                    cam.setId(data.getKey());

                    // Brand dan Model
                    cam.setBrand(data.child("brand").getValue(String.class));
                    cam.setModel(data.child("model").getValue(String.class));

                    // Type + Resolution
                    String type = data.child("type").getValue(String.class);
                    String resolution = data.child("resolution").getValue(String.class);
                    cam.setType(type + " / " + resolution);

                    // Price
                    Long price = data.child("pricePerDay").getValue(Long.class);
                    cam.setPrice("Rp " + (price != null ? price : 0) + " / hari");

                    // Image URL
                    cam.setImageUrl(data.child("imageUrl").getValue(String.class));

                    list.add(cam);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DB", error.getMessage());
                Toast.makeText(CamerasAvailable.this, "Gagal load kamera!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
