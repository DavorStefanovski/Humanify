package com.Davor.humanify;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Davor.humanify.Adapters.DiscussionAdapter;
import com.Davor.humanify.Adapters.EventDiscussionsAdapter;
import com.Davor.humanify.Adapters.EventImagesAdapter;
import com.Davor.humanify.DTO.EventResponse;
import com.Davor.humanify.Model.User;
import com.Davor.humanify.Service.DiscussionService;
import com.Davor.humanify.Service.EventService;
import com.Davor.humanify.Service.UserService;
import com.Davor.humanify.databinding.ActivityEventDetailsBinding;
import com.Davor.humanify.databinding.ActivityMainBinding;

import java.time.LocalDateTime;

public class EventDetailsActivity extends AppCompatActivity {

    EventResponse event;
    private ActivityEventDetailsBinding binding;
    EventImagesAdapter eventImagesAdapter;
    EventDiscussionsAdapter eventDiscussionsAdapter;
    RecyclerView recyclerViewImages, recyclerViewDiscussions;
    ListView listView;
    Boolean participates = false;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        TextView titleActivity = findViewById(R.id.username_toolbar);
//        titleActivity.setText("Be the organizer!");
//        findViewById(R.id.btn_logout).setVisibility(View.GONE);
//        findViewById(R.id.profile_image).setVisibility(View.GONE);
        Integer eventId = getIntent().getIntExtra("eventId",0);
        Integer userId = getIntent().getIntExtra("userId",0);
        EventService.init(getApplicationContext());
        DiscussionService.init(getApplicationContext());
        UserService.init(getApplicationContext());
        EventService.getEvent(eventId, new EventService.GetEventCallback() {
            @Override
            public void onSuccess(EventResponse eventResponse) {
                runOnUiThread(() -> {
                    binding.eventTitle.setText(eventResponse.getTitle());

                    byte[] profilePicBytes = eventResponse.getUser().getProfilePicture();
                    if (profilePicBytes != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(profilePicBytes, 0, profilePicBytes.length);
                        binding.organizerProfilePic.setImageBitmap(bitmap);
                    } else {
                        // Set a default image if the byte array is empty or null
                        binding.organizerProfilePic.setImageResource(R.drawable.kids);
                    }
                    binding.organizerUsername.setText(eventResponse.getUser().getUsername());
                    binding.eventDate.setText(eventResponse.getDateTime().toString());
                    binding.eventDescription.setText(eventResponse.getDescription());
                    binding.participantCountTextView.setText(String.valueOf(eventResponse.getParticipants().size()));

                    for(User user : eventResponse.getParticipants()){
                        if(userId == user.getId()) {
                            participates = true;
                            binding.participateButton.setText("Participating");
                        }
                    }

                    if(!eventResponse.getPictures().isEmpty()) {
                    recyclerViewImages = binding.eventPicturesRecyclerView;
                    recyclerViewImages.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
//                    recyclerViewImages.setHasFixedSize(false);
//                    recyclerViewImages.setNestedScrollingEnabled(false);

                        eventImagesAdapter = new EventImagesAdapter(getApplicationContext(), eventResponse.getPictures());
                        recyclerViewImages.setAdapter(eventImagesAdapter);
                    }else{
                        binding.eventPicturesRecyclerView.setVisibility(View.GONE);
                    }

                    recyclerViewDiscussions = binding.commentsRecyclerView;
                    recyclerViewDiscussions.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//                    recyclerViewDiscussions.setHasFixedSize(false);
//                    recyclerViewDiscussions.setNestedScrollingEnabled(false);
                    eventDiscussionsAdapter = new EventDiscussionsAdapter(getApplicationContext(),eventResponse.getDiscussions());
                    recyclerViewDiscussions.setAdapter(eventDiscussionsAdapter);
//                    DiscussionAdapter adapter = new DiscussionAdapter(getApplicationContext(), eventResponse.getDiscussions());
//                    binding.commentsListView.setAdapter(adapter);
                });
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
        binding.sendButton.setOnClickListener(v->{
            String text = binding.commentInput.getText().toString();
            DiscussionService.postDiscussion(text, LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS), eventId, new DiscussionService.PostDicsussionCallback() {
                @Override
                public void onSuccess() {
                    finish();
                    startActivity(getIntent());
                }

                @Override
                public void onFailure(String errorMessage) {

                }
            });
        });
        binding.participateButton.setOnClickListener(v->{
            // participate the event and refresh
            if(!participates) {
                UserService.participate(eventId, new UserService.ParticipateCallback() {
                    @Override
                    public void onSuccess() {
                        finish();
                        startActivity(getIntent());
                    }

                    @Override
                    public void onFailure(String errorMessage) {

                    }
                });
            }
        });
    }
}