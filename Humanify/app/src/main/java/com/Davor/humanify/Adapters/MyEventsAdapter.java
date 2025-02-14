package com.Davor.humanify.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Davor.humanify.DTO.DiscussionResponse;
import com.Davor.humanify.DTO.EventResponse;
import com.Davor.humanify.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.MyViewHolder>{
    Context context;
    List<EventResponse> eventResponses;

    public MyEventsAdapter(Context context, List<EventResponse> eventResponses) {
        this.context = context;
        this.eventResponses = eventResponses;
    }

    @NonNull
    @Override
    public MyEventsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.myevent_item,parent,false);
        return new MyEventsAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyEventsAdapter.MyViewHolder holder, int position) {
        if (eventResponses.get(position).getUser().getProfilePicture() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(eventResponses.get(position).getUser().getProfilePicture(), 0, eventResponses.get(position).getUser().getProfilePicture().length);
            holder.imageView.setImageBitmap(bitmap);
        } else {
            holder.imageView.setImageResource(R.drawable.other);
        }
        holder.username.setText(eventResponses.get(position).getUser().getUsername());
        holder.title.setText(eventResponses.get(position).getDescription());
        holder.category.setText(eventResponses.get(position).getCategory().toString());
        holder.description.setText(eventResponses.get(position).getDescription().toString());
        holder.dateTime.setText(eventResponses.get(position).getDateTime().toString());
    }

    @Override
    public int getItemCount() {
        return eventResponses.size();
    }
    public static class MyViewHolder extends  RecyclerView.ViewHolder{
        CircleImageView imageView;
        TextView username, title, category, description, dateTime;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivOrganizerProfile);
            username = itemView.findViewById(R.id.tvOrganizerUsername);
            title = itemView.findViewById(R.id.tvEventTitle);
            category = itemView.findViewById(R.id.tvEventCategory);
            description = itemView.findViewById(R.id.tvEventDescription);
            dateTime = itemView.findViewById(R.id.tvEventDateTime);
        }
    }
}
