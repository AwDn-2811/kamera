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

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CamerasAvailableAdapter extends RecyclerView.Adapter<CamerasAvailableAdapter.ViewHolder> {

    Context context;
    ArrayList<AvailableCamera> list;

    public CamerasAvailableAdapter(Context context, ArrayList<AvailableCamera> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.camera_available_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AvailableCamera cam = list.get(position);

        // === FORMAT RUPIAH INDONESIA ===
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String hargaFormat = formatRupiah.format(cam.getPricePerDay());

        // Replace "Rp" with "Rp " (biar ada spasi)
        hargaFormat = hargaFormat.replace("Rp", "Rp ");

        holder.brand.setText(cam.getBrand());
        holder.model.setText(cam.getName());
        holder.type.setText(cam.getDescription());
        holder.price.setText(hargaFormat + " / Hari");

        Glide.with(context)
                .load(cam.getImageUrl())
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CameraInformation.class);
            intent.putExtra("cameraId", cam.getId());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView brand, model, type, price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.camera_img);
            brand = itemView.findViewById(R.id.camera_brand_txt);
            model = itemView.findViewById(R.id.camera_model_txt);
            type = itemView.findViewById(R.id.camera_type_txt);
            price = itemView.findViewById(R.id.camera_price_txt);
        }
    }
}
