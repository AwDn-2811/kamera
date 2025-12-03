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

import java.util.ArrayList;

public class HomeCameraAdapter extends RecyclerView.Adapter<HomeCameraAdapter.HomeCameraHolder> {

    Context context;
    ArrayList<CameraCard> list;

    public HomeCameraAdapter(Context context, ArrayList<CameraCard> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HomeCameraHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.camera_available_card, parent, false);
        return new HomeCameraHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeCameraHolder holder, int position) {
        CameraCard camera = list.get(position);

        holder.txt_brand.setText(camera.getBrand());
        holder.txt_model.setText(camera.getModel());
        holder.txt_type.setText(camera.getType());
        holder.txt_price.setText(camera.getPrice());

        Glide.with(context)
                .load(camera.getImageUrl())
                .into(holder.img_camera);

        holder.bind(camera);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class HomeCameraHolder extends RecyclerView.ViewHolder {

        ImageView img_camera;
        TextView txt_brand, txt_price, txt_model, txt_type;

        CameraCard cameraData;

        public HomeCameraHolder(@NonNull View itemView) {
            super(itemView);

            img_camera = itemView.findViewById(R.id.camera_img);
            txt_brand = itemView.findViewById(R.id.camera_brand_txt);
            txt_price = itemView.findViewById(R.id.camera_price_txt);
            txt_model = itemView.findViewById(R.id.camera_model_txt);
            txt_type = itemView.findViewById(R.id.camera_type_txt);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), CameraInformation.class);
                intent.putExtra("cameraID", cameraData.getId());
                v.getContext().startActivity(intent);
            });
        }

        public void bind(CameraCard data) {
            this.cameraData = data;
        }
    }
}
