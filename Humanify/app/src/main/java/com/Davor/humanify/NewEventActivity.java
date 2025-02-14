package com.Davor.humanify;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.Davor.humanify.Enum.Category;
import com.Davor.humanify.Service.EventService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class NewEventActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int IMAGE_PICK_REQUEST = 1;
    private EditText etTitle, etDescription, etPlace, etDateTime, etLat, etLon;
    private Spinner spinnerCategory;
    private Button btnPickDateTime, btnPickImages, btnCreateEvent;
    private MapView mapView;
    private GoogleMap gMap;
    private Marker eventMarker;
    private List<Uri> selectedImages = new ArrayList<>();
    LatLng curr;
    LatLng selected;
    LocalDateTime localDateTime;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView titleActivity = findViewById(R.id.username_toolbar);
        titleActivity.setText("Be the organizer!");
        findViewById(R.id.btn_logout).setVisibility(View.GONE);
        findViewById(R.id.profile_image).setVisibility(View.GONE);
        findViewById(R.id.btn_back).setOnClickListener(v->{
            finish();
        });
        EventService.init(getApplicationContext());
        Double lat = getIntent().getDoubleExtra("lat",0);
        Double lon = getIntent().getDoubleExtra("lon",0);
        curr = new LatLng(lat,lon);
        // Initialize UI Elements
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPlace = findViewById(R.id.etPlace);
        etDateTime = findViewById(R.id.etDateTime);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnPickDateTime = findViewById(R.id.btnPickDateTime);
        btnPickImages = findViewById(R.id.btnPickImages);
        btnCreateEvent = findViewById(R.id.btnMakeEvent);
        mapView = findViewById(R.id.mapView);

        String[] categories = Arrays.stream(Category.values())
                .map(Enum::name)
                .toArray(String[]::new);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);

        spinnerCategory.setAdapter(adapter);
        // Setup Google Map
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // DateTime Picker Logic
        btnPickDateTime.setOnClickListener(v -> showDateTimePicker());

        // Image Picker
        btnPickImages.setOnClickListener(v -> selectImages());

        // Handle Event Creation (Example Toast)
        btnCreateEvent.setOnClickListener(v -> saveEvent());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        eventMarker = gMap.addMarker(new MarkerOptions().position(curr).title("Select Event Location"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curr, 10));

        gMap.setOnMapClickListener(latLng -> {
            eventMarker.setPosition(latLng);
            selected = latLng;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {
            TimePickerDialog timePicker = new TimePickerDialog(this, (view1, hour, minute) -> {
                localDateTime = LocalDateTime.of(year,month,day,hour,minute);
                etDateTime.setText(localDateTime.toString());
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePicker.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }
    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, IMAGE_PICK_REQUEST);
    }

    // Handle Image Selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) { // Multiple Images
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImages.add(imageUri);
                }
            } else if (data.getData() != null) { // Single Image
                selectedImages.add(data.getData());
            }
        }
    }

    private void saveEvent() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String place = etPlace.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        EventService.postEvent(getApplicationContext(), title, description, place, category, localDateTime, selected.latitude, selected.longitude, selectedImages, new EventService.PostEventCallback() {
            @Override
            public void onSuccess() {
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }

}