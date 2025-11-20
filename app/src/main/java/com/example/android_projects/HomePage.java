package com.example.android_projects;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

public class HomePage extends AppCompatActivity {
    private static final String TAG = "HomePage";

    private EditText edt_pickupLocation;
    private TextView tf_startDate, tf_endDate, tf_warning;
    private Button btn_search;

    // Menu Navigasi
    private LinearLayout menu_addCamera; // Diubah dari addCar
    private LinearLayout menu_feedback;
    private LinearLayout menu_home;
    private LinearLayout menu_aboutus;
    private LinearLayout menu_account;

    private RecyclerView recycler_layout;

    // Adapter & List untuk Kamera
    private HomeCameraAdapter cameraAdapter;
    private ArrayList<AvailableCamera> listOfCameras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupView();
    }

    private void setupView() {
        edt_pickupLocation = findViewById(R.id.edt_pickupLocation);
        tf_startDate = findViewById(R.id.tf_pickupDate); // Menggunakan ID xml yang sama
        tf_endDate = findViewById(R.id.tf_dropoffDate);  // Menggunakan ID xml yang sama
        btn_search = findViewById(R.id.btn_search);
        tf_warning = findViewById(R.id.tf_warning);

        // Inisialisasi Menu
        menu_addCamera = findViewById(R.id.menu_addCar); // Pastikan ID di XML disesuaikan (misal: menu_addCamera)
        menu_feedback = findViewById(R.id.menu_feedback);
        menu_home = findViewById(R.id.menu_home);
        menu_aboutus = findViewById(R.id.menu_aboutus);
        menu_account = findViewById(R.id.menu_account);

        recycler_layout = findViewById(R.id.recycler_layout);

        // Load Data Kamera Terdekat
        getNearCameras();

        // Setup Date Pickers
        setupDatePickers();

        // Tombol Cari
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSearch();
            }
        });

        // Navigasi Menu
        menu_addCamera.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), addCameraActivity.class); // Sesuaikan nama class
            startActivity(i);
        });

        menu_feedback.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), feedback.class); // Sesuaikan nama class
            startActivity(i);
        });

        menu_aboutus.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), AboutUs.class);
            startActivity(i);
        });

        menu_account.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), Profile.class);
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
                month1 = month1 + 1;
                String date = dayOfMonth + "/" + month1 + "/" + year1;
                tf_startDate.setText(date);
            }, year, month, day);
            dialog.show();
        });

        tf_endDate.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(HomePage.this, (view1, year1, month1, dayOfMonth) -> {
                month1 = month1 + 1;
                String date = dayOfMonth + "/" + month1 + "/" + year1;
                tf_endDate.setText(date);
            }, year, month, day);
            dialog.show();
        });
    }

    private void handleSearch() {
        if (edt_pickupLocation.getText().toString().isEmpty()) {
            tf_warning.setText("Lokasi pengambilan harus diisi");
        } else if (tf_startDate.getText().toString().isEmpty()) {
            tf_warning.setText("Tanggal mulai sewa harus diisi");
        } else if (tf_endDate.getText().toString().isEmpty()) {
            tf_warning.setText("Tanggal selesai sewa harus diisi");
        } else {
            String location = edt_pickupLocation.getText().toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

            try {
                LocalDate startDate = LocalDate.parse(tf_startDate.getText().toString(), formatter);
                LocalDate endDate = LocalDate.parse(tf_endDate.getText().toString(), formatter);

                if (startDate.isBefore(LocalDate.now())) {
                    tf_warning.setText("Tanggal sewa tidak boleh di masa lalu");
                } else if (startDate.isAfter(endDate)) {
                    tf_warning.setText("Tanggal selesai harus setelah tanggal mulai");
                } else {
                    // Pindah ke halaman List Kamera
                    Intent i = new Intent(getApplicationContext(), CamerasAvailable.class); // Ganti CarsAvailable
                    i.putExtra("location", location);
                    i.putExtra("startDate", startDate.toString());
                    i.putExtra("endDate", endDate.toString());

                    // Simpan preferensi tanggal
                    SharedPreferences sharedPreferences = getSharedPreferences("dates", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("fromDate", startDate.toString());
                    editor.putString("toDate", endDate.toString());
                    editor.apply();

                    startActivity(i);
                }
            } catch (Exception e) {
                tf_warning.setText("Format tanggal salah");
            }
        }
    }

    private void getNearCameras() {
        recycler_layout.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler_layout.setLayoutManager(layoutManager);

        listOfCameras = new ArrayList<>();
        // Asumsi Anda punya class HomeCameraAdapter dan HomeCameraModel
        cameraAdapter = new HomeCameraAdapter(this, listOfCameras);
        recycler_layout.setAdapter(cameraAdapter);

        // Referensi ke node "cameras" di Firebase
        DatabaseReference camerasRef = FirebaseDatabase.getInstance().getReference().child("cameras");

        // Mengambil Lokasi User
        LocationHelper locationHelper = new LocationHelper(this);
        String myLocation = locationHelper.getLocation(); // Pastikan method ini mengembalikan lat,long string

        camerasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listOfCameras.clear(); // Bersihkan list sebelum mengisi ulang agar tidak duplikat
                int count = 0;

                for (DataSnapshot cameraSnap : dataSnapshot.getChildren()) {
                    if (count >= 6) break; // Batasi 6 item di beranda

                    // Ambil data lokasi kamera
                    String camLocation = cameraSnap.child("location_lat_long").getValue(String.class);

                    // Hitung Jarak (Pastikan DistanceCalculator menangani null safety)
                    double distance = 0;
                    if (camLocation != null && myLocation != null) {
                        distance = DistanceCalculator.distance(myLocation, camLocation);
                    }

                    // Tampilkan jika jarak dekat (Logic jarak disesuaikan)
                    if (distance <= 50) { // Misal radius 50km
                        count++;

                        final HomeCameraCard camModel = new HomeCameraCard();
                        camModel.id = cameraSnap.getKey();

                        String brand = cameraSnap.child("brand").getValue(String.class);
                        String model = cameraSnap.child("model").getValue(String.class);
                        camModel.name = brand + " " + model; // Gabungkan Brand & Model

                        Integer price = cameraSnap.child("price").getValue(Integer.class);
                        camModel.price = "Rp " + price + " /Hari";

                        // Load Gambar Kamera
                        getCameraImage(camModel.id, camModel);
                    }
                }
                // Jika tidak ada yang dekat, adapter mungkin kosong.
                // Bisa tambahkan logic "else" untuk menampilkan "Rekomendasi Umum"
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private void getCameraImage(String cameraID, HomeCameraCard camModel) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Sesuaikan path storage dengan struktur folder kamera
        StorageReference imageRef = storage.getReference().child("images/cameras/" + cameraID + ".jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(5 * ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            camModel.image = bmp;

            listOfCameras.add(camModel);
            cameraAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            // Jika gagal load gambar (misal file tidak ada), tetap tampilkan data teks
            listOfCameras.add(camModel);
            cameraAdapter.notifyDataSetChanged();
        });
    }
}