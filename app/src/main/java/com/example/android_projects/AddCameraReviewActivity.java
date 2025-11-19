package com.example.android_projects;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddCameraReviewActivity extends AppCompatActivity {

    RatingBar ratingBar;
    EditText input_comment;
    Button btn_submit;

    String cameraID;
    String userID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera_review);

        cameraID = getIntent().getStringExtra("cameraID");

        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        userID = sp.getString("userId", "");

        initViews();
    }

    private void initViews() {
        ratingBar = findViewById(R.id.ratingBar);
        input_comment = findViewById(R.id.input_comment);
        btn_submit = findViewById(R.id.btn_submit_review);

        btn_submit.setOnClickListener(v -> {
            saveReview();
        });
    }

    private void saveReview() {

        int rating = (int) ratingBar.getRating();
        String comment = input_comment.getText().toString();

        if (rating == 0) {
            Toast.makeText(this, "Beri rating dulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            Toast.makeText(this, "Tulis komentar dulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("cameras")
                .child(cameraID)
                .child("reviews")
                .push();

        ref.child("userID").setValue(userID);
        ref.child("rating").setValue(rating);
        ref.child("comment").setValue(comment);

        Toast.makeText(this, "Review berhasil ditambahkan!", Toast.LENGTH_LONG).show();
        finish();
    }
}
