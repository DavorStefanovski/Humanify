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
import com.Davor.humanify.R;

import java.util.ArrayList;
import java.util.List;

public class EventDiscussionsAdapter extends RecyclerView.Adapter<EventDiscussionsAdapter.MyViewHolder> {
    Context context;
    List<DiscussionResponse> discussionResponses;

    public EventDiscussionsAdapter(Context context, List<DiscussionResponse> discussionResponses) {
        this.context = context;
        this.discussionResponses = discussionResponses;
    }
    public void updateDiscussions(List<DiscussionResponse> newDiscussions) {
        this.discussionResponses = newDiscussions;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }
    @NonNull
    @Override
    public EventDiscussionsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.discussion_item,parent,false);
        return new EventDiscussionsAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventDiscussionsAdapter.MyViewHolder holder, int position) {
        if (discussionResponses.get(position).getUser().getProfilePicture() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(discussionResponses.get(position).getUser().getProfilePicture(), 0, discussionResponses.get(position).getUser().getProfilePicture().length);
            holder.imageView.setImageBitmap(bitmap);
        } else {
            holder.imageView.setImageResource(R.drawable.other);
        }
        holder.username.setText(discussionResponses.get(position).getUser().getUsername());
        holder.text.setText(discussionResponses.get(position).getText());
        holder.datetime.setText(discussionResponses.get(position).getDateTime().toString());
    }

    @Override
    public int getItemCount() {
        return discussionResponses.size();
    }
    public static class MyViewHolder extends  RecyclerView.ViewHolder{
        ImageView imageView;
        TextView username, text, datetime;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.comment_profile_pic);
            username = itemView.findViewById(R.id.comment_username);
            text = itemView.findViewById(R.id.comment_text);
            datetime = itemView.findViewById(R.id.comment_datetime);
        }
    }
}
