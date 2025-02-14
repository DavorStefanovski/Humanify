package com.Davor.humanify.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.Davor.humanify.DTO.DiscussionResponse;
import com.Davor.humanify.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DiscussionAdapter extends BaseAdapter {
    private Context context;
    private List<DiscussionResponse> Discussions;
    private LayoutInflater inflater;

    public DiscussionAdapter(Context context, List<DiscussionResponse> Discussions) {
        this.context = context;
        this.Discussions = Discussions;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return Discussions.size();
    }

    @Override
    public Object getItem(int position) {
        return Discussions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.discussion_item, parent, false);
        }

        // Bind views
        CircleImageView imageView = convertView.findViewById(R.id.comment_profile_pic);
        TextView username = convertView.findViewById(R.id.comment_username);
        TextView text = convertView.findViewById(R.id.comment_text);
        TextView datetime = convertView.findViewById(R.id.comment_datetime);

        // Get current Discussion
        DiscussionResponse Discussion = Discussions.get(position);

        // Set data
        Bitmap bitmap = BitmapFactory.decodeByteArray(Discussions.get(position).getUser().getProfilePicture(), 0, Discussions.get(position).getUser().getProfilePicture().length);
        imageView.setImageBitmap(bitmap);
        username.setText(Discussions.get(position).getUser().getUsername());
        text.setText(Discussions.get(position).getText());
        datetime.setText(Discussions.get(position).getDateTime().toString());

        return convertView;
    }
}
