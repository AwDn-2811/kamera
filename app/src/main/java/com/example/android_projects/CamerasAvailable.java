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
    ArrayList<AvailableCamera> list;

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

                    AvailableCamera cam = new AvailableCamera();

                    cam.setId(data.getKey());
                    cam.setBrand(data.child("brand").getValue(String.class));
                    cam.setName(data.child("name").getValue(String.class));
                    cam.setDescription(data.child("description").getValue(String.class));
                    cam.setAddress(data.child("address").getValue(String.class));

                    Long price = data.child("pricePerDay").getValue(Long.class);
                    cam.setPricePerDay(price != null ? price : 0);

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
