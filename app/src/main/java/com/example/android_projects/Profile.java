package com.example.android_projects;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Profile extends AppCompatActivity {

    static String user_id;
    private Button updateInfo;   // 🔥 ganti TextView → Button
    private Button userActivity, logout_btn;
    private TextView name, lname, email, contact, gender, dob;
    private DatabaseReference rootDatabaseref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getReference();

        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString("userId", "");

        userActivity.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, UserActivity.class);
            intent.putExtra("userId", user_id);
            startActivity(intent);
        });

        logout_btn.setOnClickListener(v -> {
            SharedPreferences sharedPreferences1 = getSharedPreferences("UserData", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences1.edit();
            editor.putString("userId", null);
            editor.apply();

            Intent intent = new Intent(Profile.this, login.class);
            intent.putExtra("userId", "");
            startActivity(intent);
            finish();
        });

        updateInfo.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, UpdateProfile.class);
            intent.putExtra("userId", user_id);
            startActivity(intent);
        });


        rootDatabaseref = FirebaseDatabase.getInstance().getReference();
        rootDatabaseref.get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot data = task.getResult();

                name.setText(data.child("users").child(user_id).child("firstName").getValue(String.class));
                lname.setText(data.child("users").child(user_id).child("lastName").getValue(String.class));
                email.setText(data.child("users").child(user_id).child("email").getValue(String.class));
                contact.setText(data.child("users").child(user_id).child("phoneNumber").getValue(String.class));
                gender.setText(data.child("users").child(user_id).child("gender").getValue(String.class));
                dob.setText(data.child("users").child(user_id).child("dob").getValue(String.class));
            }
        });
    }

    private void getReference() {
        updateInfo = findViewById(R.id.updateInfoBtn);   // 🟢 sudah Button
        userActivity = findViewById(R.id.userActivity);
        name = findViewById(R.id.fnametxt);
        email = findViewById(R.id.emailtxt);
        contact = findViewById(R.id.phonetxt);
        gender = findViewById(R.id.gendertxt);
        lname = findViewById(R.id.lnametxt);
        dob = findViewById(R.id.dobtxt);
        logout_btn = findViewById(R.id.logout_btn);
    }
}
