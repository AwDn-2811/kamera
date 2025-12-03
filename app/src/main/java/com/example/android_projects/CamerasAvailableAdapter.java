package com.example.android_projects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CamerasAvailableAdapter extends RecyclerView.Adapter<CamerasAvailableAdapter.ViewHolder> {

    Context context;
    ArrayList<CameraCard> list;

    public CamerasAvailableAdapter(Context context, ArrayList<CameraCard> list) {
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
        CameraCard cam = list.get(position);

        // Ambil data dari getter
        holder.brand.setText(cam.getBrand());
        holder.model.setText(cam.getModel());
        holder.type.setText(cam.getType());
        holder.price.setText(cam.getPrice());

        // Load image
        Glide.with(context)
                .load(cam.getImageUrl())
                .into(holder.image);
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
