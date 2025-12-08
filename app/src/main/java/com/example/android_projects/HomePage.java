package com.example.android_projects;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class HomePage extends AppCompatActivity {

    private EditText edt_pickupLocation;
    private TextView tf_warning;
    private Button btn_search, btn_rental_history;

    private RecyclerView recycler_layout;
    private CamerasAvailableAdapter cameraAdapter;
    private ArrayList<AvailableCamera> listOfCameras;

    private LinearLayout menu_addCamera, menu_feedback, menu_home, menu_aboutus, menu_account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupView();
    }

    private void setupView() {

        edt_pickupLocation = findViewById(R.id.edt_pickupLocation);
        tf_warning = findViewById(R.id.tf_warning);
        btn_search = findViewById(R.id.btn_search);

        // 🔥 Button history rental
        btn_rental_history = findViewById(R.id.btn_rental_history);

        menu_addCamera = findViewById(R.id.menu_addCar);
        menu_feedback = findViewById(R.id.menu_feedback);
        menu_home = findViewById(R.id.menu_home);
        menu_aboutus = findViewById(R.id.menu_aboutus);
        menu_account = findViewById(R.id.menu_account);

        recycler_layout = findViewById(R.id.recycler_layout);

        recycler_layout.setHasFixedSize(true);
        recycler_layout.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        );

        listOfCameras = new ArrayList<>();
        cameraAdapter = new CamerasAvailableAdapter(this, listOfCameras);
        recycler_layout.setAdapter(cameraAdapter);

        loadAllCameras();

        btn_search.setOnClickListener(v -> searchCameraByBrand());

        // 🔥 Go to Rental History
        btn_rental_history.setOnClickListener(v -> {
            startActivity(new Intent(HomePage.this, RentalHistoryActivity.class));
        });

        // Bottom menu
        menu_addCamera.setOnClickListener(v -> startActivity(new Intent(this, addCameraActivity.class)));
        menu_feedback.setOnClickListener(v -> startActivity(new Intent(this, feedback.class)));
        menu_home.setOnClickListener(v -> {});
        menu_aboutus.setOnClickListener(v -> startActivity(new Intent(this, AboutUs.class)));
        menu_account.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));
    }

    // ===================================================
    //               LOAD ALL CAMERA
    // ===================================================
    private void loadAllCameras() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("cameras");

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listOfCameras.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    Boolean isAvailable = data.child("isAvailable").getValue(Boolean.class);

                    if (isAvailable != null && isAvailable) {

                        AvailableCamera cam = new AvailableCamera();
                        cam.setId(data.getKey());
                        cam.setBrand(data.child("brand").getValue(String.class));
                        cam.setName(data.child("model").getValue(String.class));
                        cam.setDescription(data.child("type").getValue(String.class));
                        cam.setAddress(data.child("address").getValue(String.class));

                        Long price = data.child("pricePerDay").getValue(Long.class);
                        cam.setPricePerDay(price != null ? price : 0);

                        cam.setImageUrl(data.child("imageUrl").getValue(String.class));

                        listOfCameras.add(cam);
                    }
                }

                cameraAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePage.this, "Gagal memuat data kamera", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===================================================
    //             SEARCH CAMERA BY BRAND
    // ===================================================
    private void searchCameraByBrand() {

        String keyword = edt_pickupLocation.getText().toString().trim();

        if (TextUtils.isEmpty(keyword)) {
            tf_warning.setText("Masukkan brand kamera");
            return;
        }

        tf_warning.setText("");

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("cameras");

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listOfCameras.clear();

                for (DataSnapshot data : snapshot.getChildren()) {

                    String brand = data.child("brand").getValue(String.class);
                    Boolean isAvailable = data.child("isAvailable").getValue(Boolean.class);

                    if (brand != null &&
                            isAvailable != null && isAvailable &&
                            brand.toLowerCase().contains(keyword.toLowerCase())
                    ) {

                        AvailableCamera cam = new AvailableCamera();
                        cam.setId(data.getKey());
                        cam.setBrand(brand);
                        cam.setName(data.child("model").getValue(String.class));
                        cam.setDescription(data.child("type").getValue(String.class));
                        cam.setAddress(data.child("address").getValue(String.class));

                        Long price = data.child("pricePerDay").getValue(Long.class);
                        cam.setPricePerDay(price != null ? price : 0);

                        cam.setImageUrl(data.child("imageUrl").getValue(String.class));

                        listOfCameras.add(cam);
                    }
                }

                if (listOfCameras.isEmpty()) {
                    tf_warning.setText("Brand kamera tidak ditemukan");
                }

                cameraAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePage.this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
