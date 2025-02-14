package com.Davor.humanify;

import static com.Davor.humanify.R.id.toolbar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.Davor.humanify.DTO.EventResponse;
import com.Davor.humanify.Enum.Category;
import com.Davor.humanify.Service.EventService;
import com.Davor.humanify.Service.UserService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WorldFragment extends Fragment implements OnMapReadyCallback {
    private Marker currentMarker;
    private GoogleMap myMap;
    private List<EventResponse> events;

    public WorldFragment() {
        // Required empty public constructor
    }

    List<Category> categories;
    LocalDateTime until;
    String place;
    private LinearLayout categoryCheckboxContainer;
    private Button btnDateTimePicker;
    private EditText placeInput;
    private List<Marker> markers;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_world, container, false);
        EventService.init(getActivity().getApplicationContext());
        events = new ArrayList<>();
        // Set up the map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        } else {
            Log.e("WorldFragment", "SupportMapFragment is null");
        }
        categories = new ArrayList<>();
        btnDateTimePicker = view.findViewById(R.id.btnDateTimePicker);
        categoryCheckboxContainer = view.findViewById(R.id.categoryCheckboxContainer);
        btnDateTimePicker.setOnClickListener(v -> showDateTimePicker());
        placeInput = view.findViewById(R.id.editTextText3);
        placeInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                place = s.toString();
                loadEvents();
            }
        });
        addCategoryCheckboxes();
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        MainActivity parentActivity = (MainActivity) getActivity();
        myMap = googleMap;
        // Add the user location marker
        parentActivity.getLocationLiveData().observe(getViewLifecycleOwner(), location -> {
            if (currentMarker != null) {
                currentMarker.remove();
            }else{
                loadEvents();
            }
            currentMarker = myMap.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.mypin)).title("You"));
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));

        });

    }

    private void loadEvents() {
        EventService.getEvents(categories, place, until, null,
                ((MainActivity) getActivity()).getLocationLiveData().getValue().latitude,
                ((MainActivity) getActivity()).getLocationLiveData().getValue().longitude,
                new EventService.GetEventsCallback() {
                    @Override
                    public void onSuccess(List<EventResponse> eventResponses) {
                        events = eventResponses;
                        addEventMarkers();  // Add markers for the events after they are loaded
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e("WorldFragment", "Failed to load events: " + errorMessage);
                    }
                });
    }

    private void addEventMarkers() {
        getActivity().runOnUiThread(() -> {
            // Clear existing markers from the map
            if (markers != null) {
                for (Marker marker : markers) {
                    marker.remove();
                }
                markers.clear();
            } else {
                markers = new ArrayList<>();
            }

            for (EventResponse event : events) {
                LatLng eventLocation = new LatLng(event.getLat(), event.getLon());

                int iconResId = getMarkerIconForCategory(event.getCategory());

                Marker marker = myMap.addMarker(new MarkerOptions()
                        .position(eventLocation)
                        .title(event.getTitle())
                        .snippet(event.getDescription())
                        .icon(BitmapDescriptorFactory.fromResource(iconResId)));

                if (marker != null) {
                    marker.setTag(event.getId());  // Attach event data to the marker
                    markers.add(marker);
                }
            }

            myMap.setOnMarkerClickListener(marker -> {
                Integer clickedEventId = (Integer) marker.getTag();
                if (clickedEventId != null) {
                    Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
                    intent.putExtra("userId",((MainActivity) getActivity()).currentUser.getId());
                    intent.putExtra("eventId", clickedEventId);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
        });
    }

    private int getMarkerIconForCategory(Category category) {
        switch (category.name()) {
            case "MUSIC":
                return R.drawable.music;
            case "COOKING":
                return R.drawable.cooking;
            case "ART":
                return R.drawable.art;
            case "SPORT":
                return R.drawable.sport;
            case "LITERATURE":
                return R.drawable.literature;
            case "THEATRE":
                return R.drawable.theatre;
            case "KIDS":
                return R.drawable.kids;
            default:
                return R.drawable.other; // Default marker
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Show TimePickerDialog after date is selected
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            getActivity(),
                            (view1, selectedHour, selectedMinute) -> {
                                until = LocalDateTime.of(selectedYear, selectedMonth + 1, selectedDay, selectedHour, selectedMinute);
                                loadEvents();
                            },
                            hour, minute, true
                    );
                    timePickerDialog.show();
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void addCategoryCheckboxes() {
        for (Category category : Category.values()) {
            CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setText(category.name());
            checkBox.setTag(category);
            checkBox.setPadding(10, 0, 10, 0); // Add padding between checkboxes
            // Add a listener to handle checkbox state changes
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Category selectedCategory = (Category) buttonView.getTag();
                if (isChecked) {
                    // Add the category to the list if checked
                    if (!categories.contains(selectedCategory)) {
                        categories.add(selectedCategory);
                    }
                } else {
                    // Remove the category from the list if unchecked
                    categories.remove(selectedCategory);
                }

                // Reload events with the updated categories list
                loadEvents();
            });
            categoryCheckboxContainer.addView(checkBox);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(!events.isEmpty()) {
            loadEvents();
        }
    }
}
