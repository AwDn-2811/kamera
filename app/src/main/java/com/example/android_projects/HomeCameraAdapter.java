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
        View v = LayoutInflater.from(context).inflate(R.layout.home_camera_card, parent, false);
        return new HomeCameraHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeCameraHolder holder, int position) {
        HomeCameraCard camera = list.get(position);

        holder.txt_name.setText(camera.getBrand());
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
        TextView txt_name, txt_price;

        HomeCameraCard cameraData;

        public HomeCameraHolder(@NonNull View itemView) {
            super(itemView);

            img_camera = itemView.findViewById(R.id.camera_img);
            txt_name = itemView.findViewById(R.id.cameraName_txt);
            txt_price = itemView.findViewById(R.id.cameraPrice_txt);

            itemView.setOnClickListener(this);
        }

        public void bind(HomeCameraCard data) {
            this.cameraData = data;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), CameraInformation.class);
            intent.putExtra("cameraID", cameraData.getID());
            view.getContext().startActivity(intent);
        }
    }
}
