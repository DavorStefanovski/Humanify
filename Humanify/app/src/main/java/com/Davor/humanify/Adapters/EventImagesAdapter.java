package com.Davor.humanify.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Davor.humanify.R;

import java.util.List;

public class EventImagesAdapter extends RecyclerView.Adapter<EventImagesAdapter.MyViewHolder> {
    Context context;
    List<byte[]> pictures;

    public EventImagesAdapter(Context context, List<byte[]> pictures) {
        this.context = context;
        this.pictures = pictures;
    }

    @NonNull
    @Override
    public EventImagesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.image_item,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventImagesAdapter.MyViewHolder holder, int position) {
        if(pictures.get(position)!=null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(pictures.get(position), 0, pictures.get(position).length);
            holder.imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }
    public static class MyViewHolder extends  RecyclerView.ViewHolder{
        ImageView imageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
