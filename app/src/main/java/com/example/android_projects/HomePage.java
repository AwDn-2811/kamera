package com.example.android_projects;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import androidx.annotation.NonNull;

public class HomePage extends AppCompatActivity {
    private static final String TAG = "HomePage";

    private EditText edt_pickupLocation;
    private TextView tf_startDate, tf_endDate, tf_warning;
    private Button btn_search;

    // Bottom Menu
    private LinearLayout menu_addCamera;
    private LinearLayout menu_feedback;
    private LinearLayout menu_home;
    private LinearLayout menu_aboutus;
    private LinearLayout menu_account;

    private RecyclerView recycler_layout;

    // Adapter & List Kamera
    private HomeCameraAdapter cameraAdapter;
    private ArrayList<CameraCard> listOfCameras;

    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseDatabase.getInstance();
        setupView();
    }

    private void setupView() {
        edt_pickupLocation = findViewById(R.id.edt_pickupLocation);
        tf_startDate = findViewById(R.id.tf_pickupDate);
        tf_endDate = findViewById(R.id.tf_dropoffDate);
        tf_warning = findViewById(R.id.tf_warning);
        btn_search = findViewById(R.id.btn_search);

        // Menu
        menu_addCamera = findViewById(R.id.menu_addCar);
        menu_feedback = findViewById(R.id.menu_feedback);
        menu_home = findViewById(R.id.menu_home);
        menu_aboutus = findViewById(R.id.menu_aboutus);
        menu_account = findViewById(R.id.menu_account);

        recycler_layout = findViewById(R.id.recycler_layout);

        // Load kamera
        getNearCameras();

        // Setup kalender
        setupDatePickers();

        // Tombol search
        btn_search.setOnClickListener(view -> handleSearch());

        // NAVIGASI MENU
        menu_addCamera.setOnClickListener(v -> {
            Intent i = new Intent(this, addCameraActivity.class);
            startActivity(i);
        });

        menu_feedback.setOnClickListener(v -> {
            Intent i = new Intent(this, feedback.class);
            startActivity(i);
        });

        // 🔥 FIX HOME BUTTON (YANG TADI TIDAK BERFUNGSI)
        menu_home.setOnClickListener(v -> {
            // Do nothing, you're already on HomePage
        });

        menu_aboutus.setOnClickListener(v -> {
            Intent i = new Intent(this, AboutUs.class);
            startActivity(i);
        });

        menu_account.setOnClickListener(v -> {
            Intent i = new Intent(this, Profile.class);
            startActivity(i);
        });
    }

    private void setupDatePickers() {
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        tf_startDate.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(HomePage.this, (view1, year1, month1, dayOfMonth) -> {
                month1 += 1;
                tf_startDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, month1, year1));
            }, year, month, day);
            dialog.show();
        });

        tf_endDate.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(HomePage.this, (view1, year1, month1, dayOfMonth) -> {
                month1 += 1;
                tf_endDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, month1, year1));
            }, year, month, day);
            dialog.show();
        });
    }

    private void handleSearch() {
        if (edt_pickupLocation.getText().toString().isEmpty()) {
            tf_warning.setText("Lokasi pengambilan harus diisi");
            return;
        }
        if (tf_startDate.getText().toString().isEmpty()) {
            tf_warning.setText("Tanggal mulai sewa harus diisi");
            return;
        }
        if (tf_endDate.getText().toString().isEmpty()) {
            tf_warning.setText("Tanggal selesai sewa harus diisi");
            return;
        }

        String location = edt_pickupLocation.getText().toString();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .withResolverStyle(java.time.format.ResolverStyle.LENIENT);

        try {
            String cleanStart = tf_startDate.getText().toString().replaceAll("[^0-9/]", "");
            String cleanEnd = tf_endDate.getText().toString().replaceAll("[^0-9/]", "");

            Log.d("DATE_DEBUG", "RawStart=[" + tf_startDate.getText() + "]");
            Log.d("DATE_DEBUG", "CleanStart=[" + cleanStart + "]");

            LocalDate startDate = LocalDate.parse(cleanStart, formatter);
            LocalDate endDate = LocalDate.parse(cleanEnd, formatter);

            if (startDate.isBefore(LocalDate.now())) {
                tf_warning.setText("Tanggal sewa tidak boleh di masa lalu");
                return;
            }
            if (startDate.isAfter(endDate)) {
                tf_warning.setText("Tanggal selesai harus setelah tanggal mulai");
                return;
            }

            Intent i = new Intent(this, CamerasAvailable.class);
            i.putExtra("location", location);
            i.putExtra("startDate", startDate.toString());
            i.putExtra("endDate", endDate.toString());

            SharedPreferences.Editor editor = getSharedPreferences("dates", MODE_PRIVATE).edit();
            editor.putString("fromDate", startDate.toString());
            editor.putString("toDate", endDate.toString());
            editor.apply();

            startActivity(i);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
            tf_warning.setText("Format tanggal salah");
        }
    }

    private void getNearCameras() {

        recycler_layout.setHasFixedSize(true);
        recycler_layout.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        listOfCameras = new ArrayList<>();
        cameraAdapter = new HomeCameraAdapter(this, listOfCameras);
        recycler_layout.setAdapter(cameraAdapter);

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("cameras");

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listOfCameras.clear();
                int count = 0;

                for (DataSnapshot data : snapshot.getChildren()) {

                    Boolean isAvailable = data.child("isAvailable").getValue(Boolean.class);

                    if (isAvailable != null && isAvailable && count < 6) {

                        CameraCard cam = new CameraCard();
                        cam.setId(data.getKey());

                        String brand = data.child("brand").getValue(String.class);
                        String model = data.child("model").getValue(String.class);

                        cam.setBrand(brand);
                        cam.setModel(model);

                        Long price = data.child("pricePerDay").getValue(Long.class);
                        String priceText = "Rp " + (price != null ? price : 0) + " /Hari";
                        cam.setPrice(priceText);

                        cam.setImageUrl(data.child("imageUrl").getValue(String.class));

                        listOfCameras.add(cam);
                        count++;
                    }
                }

                cameraAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "DB Error: " + error.getMessage());
                Toast.makeText(HomePage.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
