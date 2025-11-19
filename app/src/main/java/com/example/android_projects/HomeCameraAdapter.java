package com.example.android_projects;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HomeCameraAdapter extends RecyclerView.Adapter<HomeCameraAdapter.HomeCameraHolder> {

    Context context;
    ArrayList<HomeCameraCard> list;

    public HomeCameraAdapter(Context context, ArrayList<HomeCameraCard> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HomeCameraHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // PERBAIKAN: Menggunakan nama layout yang benar 'camera_available_card' (sesuai file xml yang tersedia)
        View v = LayoutInflater.from(context).inflate(R.layout.camera_available_card, parent, false);
        return new HomeCameraHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeCameraHolder holder, int position) {
        HomeCameraCard camera = list.get(position);

        // PERBAIKAN: Menyesuaikan data dengan field yang ada.
        // Layout memiliki brand, model, type. Di sini kita set brand ke text view brand.
        holder.txt_brand.setText(camera.getBrand());

        // Jika model tersedia di HomeCameraCard, sebaiknya diset juga.
        // Contoh: holder.txt_model.setText(camera.getModel());

        holder.txt_price.setText(camera.getPrice());
        holder.img_camera.setImageBitmap(camera.getImage());

        holder.bind(camera);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class HomeCameraHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView img_camera;
        TextView txt_brand, txt_price, txt_model, txt_type; // Menambahkan variabel untuk view lain jika perlu

        HomeCameraCard cameraData;

        public HomeCameraHolder(@NonNull View itemView) {
            super(itemView);

            // PERBAIKAN: Menyesuaikan ID dengan yang ada di 'camera_available_card.xml'
            img_camera = itemView.findViewById(R.id.camera_img);

            // Di XML id-nya adalah 'camera_brand_txt', bukan 'cameraName_txt'
            txt_brand = itemView.findViewById(R.id.camera_brand_txt);

            // Di XML id-nya adalah 'camera_price_txt', bukan 'cameraPrice_txt'
            txt_price = itemView.findViewById(R.id.camera_price_txt);

            // Opsional: Inisialisasi model dan type jika ingin ditampilkan dan datanya ada
            txt_model = itemView.findViewById(R.id.camera_model_txt);
            txt_type = itemView.findViewById(R.id.camera_type_txt);

            itemView.setOnClickListener(this);
        }

        public void bind(HomeCameraCard data) {
            this.cameraData = data;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), CameraInformation.class);
            // Pastikan method getID() ada di HomeCameraCard (biasanya getId() atau getID())
            intent.putExtra("cameraID", cameraData.getID());
            view.getContext().startActivity(intent);
        }
    }
}