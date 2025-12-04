package com.example.android_projects;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CameraInformation extends AppCompatActivity {

    ImageView camera_img;
    TextView txt_title, txt_sensor, txt_resolution, txt_price, txt_location;
    Button btn_rent, btn_add_review;

    RecyclerView rv_reviews;
    ReviewCameraAdapter reviewAdapter;
    ArrayList<ReviewCamera> reviewList = new ArrayList<>();

    String cameraID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_information);

        initViews();

        cameraID = getIntent().getStringExtra("cameraId");

        loadCameraData();
        loadReviews();
    }

    private void initViews() {
        camera_img = findViewById(R.id.camera_img);
        txt_title = findViewById(R.id.txt_title);
        txt_sensor = findViewById(R.id.txt_sensor);
        txt_resolution = findViewById(R.id.txt_resolution);
        txt_price = findViewById(R.id.txt_price);
        txt_location = findViewById(R.id.txt_location);

        btn_rent = findViewById(R.id.btn_rent);
        btn_add_review = findViewById(R.id.btn_add_review);

        rv_reviews = findViewById(R.id.rv_reviews);
        rv_reviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewCameraAdapter(this, reviewList);
        rv_reviews.setAdapter(reviewAdapter);

        btn_add_review.setOnClickListener(v -> {
            Intent i = new Intent(CameraInformation.this, AddCameraReviewActivity.class);
            i.putExtra("cameraID", cameraID);
            startActivity(i);
        });

        btn_rent.setOnClickListener(v -> {
            Intent i = new Intent(CameraInformation.this, MakePaymentActivity.class);
            i.putExtra("cameraID", cameraID);
            startActivity(i);
        });
    }

    private void loadCameraData() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("cameras")
                .child(cameraID);

        ref.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) {
                Toast.makeText(this, "Data kamera tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            // ===== PAKAI FIELD YANG BENAR DARI DATABASE =====
            String brand = snapshot.child("brand").getValue(String.class);
            String name = snapshot.child("name").getValue(String.class);
            String description = snapshot.child("description").getValue(String.class);
            String address = snapshot.child("address").getValue(String.class);
            String imageUrl = snapshot.child("imageUrl").getValue(String.class);
            Long price = snapshot.child("pricePerDay").getValue(Long.class);

            // ============================
            //   SET KE UI (NEW STRUCTURE)
            // ============================
            txt_title.setText(name != null ? name : brand);

            txt_sensor.setText("Brand: " + brand);
            txt_resolution.setText("Deskripsi: " + description);
            txt_location.setText("Lokasi: " + address);

// === FORMAT RUPIAH INDONESIA ===
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            String hargaFormat = formatRupiah.format(price != null ? price : 0);
            hargaFormat = hargaFormat.replace("Rp", "Rp ");   // biar rapi

            txt_price.setText(hargaFormat + " / Hari");

            if (imageUrl != null) {
                Picasso.get().load(imageUrl).into(camera_img);
            }

        }).addOnFailureListener(e -> {
            Toast.makeText(CameraInformation.this, "Gagal mengambil data", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("reviews")
                .child(cameraID);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                reviewList.clear();

                for (DataSnapshot reviewSnap : snapshot.getChildren()) {

                    String userId = reviewSnap.child("userID").getValue(String.class);
                    int rating = reviewSnap.child("rating").getValue(Integer.class);
                    String comment = reviewSnap.child("comment").getValue(String.class);

                    ReviewCamera review = new ReviewCamera();
                    review.comment = comment;
                    review.rating = rating;

                    loadUserData(userId, review);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadUserData(String userId, ReviewCamera review) {

        DatabaseReference userRef =
                FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.get().addOnSuccessListener(snapshot -> {

            if (snapshot.exists()) {
                String username = snapshot.child("username").getValue(String.class);
                String photoUrl = snapshot.child("profile_image_url").getValue(String.class);

                review.userName = username;

                if (photoUrl != null && !photoUrl.isEmpty()) {
                    review.userPhoto = photoUrl;
                }
            }

            reviewList.add(review);
            reviewAdapter.notifyDataSetChanged();

        });
    }
}
