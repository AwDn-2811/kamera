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
    ArrayList<AvailableCamera> list;

    public HomeCameraAdapter(Context context, ArrayList<AvailableCamera> list) {
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

        AvailableCamera cam = list.get(position);

        holder.txt_brand.setText(cam.getBrand());
        holder.txt_model.setText(cam.getName());
        holder.txt_type.setText(cam.getDescription());
        holder.txt_price.setText("Rp " + cam.getPricePerDay() + " / Hari");

        Glide.with(context)
                .load(cam.getImageUrl())
                .into(holder.img_camera);

        holder.bind(cam);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class HomeCameraHolder extends RecyclerView.ViewHolder {

        ImageView img_camera;
        TextView txt_brand, txt_price, txt_model, txt_type;

        AvailableCamera cameraData;

        public HomeCameraHolder(@NonNull View itemView) {
            super(itemView);

            img_camera = itemView.findViewById(R.id.camera_img);
            txt_brand = itemView.findViewById(R.id.camera_brand_txt);
            txt_price = itemView.findViewById(R.id.camera_price_txt);
            txt_model = itemView.findViewById(R.id.camera_model_txt);
            txt_type = itemView.findViewById(R.id.camera_type_txt);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), CameraInformation.class);
                intent.putExtra("cameraId", cameraData.getId());
                v.getContext().startActivity(intent);
            });
        }

        public void bind(AvailableCamera data) {
            this.cameraData = data;
        }
    }
}
