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

import java.util.ArrayList;

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

        cameraID = getIntent().getStringExtra("cameraID");

        initViews();
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("cameras").child(cameraID);

        ref.get().addOnSuccessListener(snapshot -> {

            if (!snapshot.exists()) {
                Toast.makeText(this, "Data kamera tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            String brand = snapshot.child("brand").getValue(String.class);
            String model = snapshot.child("model").getValue(String.class);
            String type = snapshot.child("type").getValue(String.class);
            String resolution = snapshot.child("resolution").getValue(String.class);
            String price = snapshot.child("price").getValue(String.class);
            String location = snapshot.child("location").getValue(String.class);
            String imageUrl = snapshot.child("imageUrl").getValue(String.class);

            txt_title.setText(brand + " " + model);
            txt_sensor.setText("Sensor: " + type);
            txt_resolution.setText("Resolusi: " + resolution);
            txt_price.setText("Harga: " + price);
            txt_location.setText("Lokasi: " + location);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(camera_img);
            }

        }).addOnFailureListener(e ->
                Toast.makeText(CameraInformation.this, "Gagal mengambil data kamera", Toast.LENGTH_SHORT).show()
        );
    }

    // =================== REVIEW SYSTEM ===================
    private void loadReviews() {
        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference("cameras")
                        .child(cameraID)
                        .child("reviews");

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
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                String photoUrl = snapshot.child("photoUrl").getValue(String.class);

                review.userName = firstName + " " + lastName;

                if (photoUrl != null && !photoUrl.isEmpty()) {
                    review.userPhoto = photoUrl;
                }
            }

            reviewList.add(review);
            reviewAdapter.notifyDataSetChanged();

        });
    }
}
