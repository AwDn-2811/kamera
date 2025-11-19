package com.example.android_projects;

import android.content.SharedPreferences;
import android.os.Bundle;
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

public class RentalHistoryActivity extends AppCompatActivity {

    RecyclerView rv_history;
    RentalHistoryAdapter adapter;
    ArrayList<RentalHistory> list = new ArrayList<>();

    String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rental_history);

        rv_history = findViewById(R.id.rv_rental_history);
        rv_history.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RentalHistoryAdapter(this, list);
        rv_history.setAdapter(adapter);

        // Ambil user ID dari login
        SharedPreferences shared = getSharedPreferences("UserData", MODE_PRIVATE);
        userID = shared.getString("userId", "");

        loadRentalHistory();
    }

    private void loadRentalHistory() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userID)
                .child("rentals");     // Sesuai struktur baru

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();

                for (DataSnapshot rentalSnap : snapshot.getChildren()) {

                    String cameraID = rentalSnap.child("cameraID").getValue(String.class);
                    String fromDate = rentalSnap.child("fromDate").getValue(String.class);
                    String toDate = rentalSnap.child("toDate").getValue(String.class);
                    String status = rentalSnap.child("status").getValue(String.class);

                    RentalHistory history = new RentalHistory();
                    history.id = cameraID;
                    history.fromDate = fromDate;
                    history.toDate = toDate;
                    history.status = status;

                    loadCameraDetails(history);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    private void loadCameraDetails(RentalHistory history) {

        DatabaseReference camRef = FirebaseDatabase.getInstance()
                .getReference("cameras")
                .child(history.id);

        camRef.get().addOnSuccessListener(snapshot -> {

            history.title = snapshot.child("brand").getValue(String.class) + " "
                    + snapshot.child("model").getValue(String.class);

            history.imageUrl = snapshot.child("imageUrl").getValue(String.class); // CLOUDINARY URL

            list.add(history);
            adapter.notifyDataSetChanged();

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Gagal mengambil data kamera", Toast.LENGTH_SHORT).show();
        });
    }
}
