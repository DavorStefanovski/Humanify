package com.Davor.humanify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.Davor.humanify.DTO.UserResponse;
import com.Davor.humanify.Model.User;
import com.Davor.humanify.Service.AuthenticationService;
import com.Davor.humanify.Service.UserService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.Davor.humanify.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final long INTERVAL_MILLIS = 100000;
    private MutableLiveData<LatLng> locationLiveData = new MutableLiveData<>();

    public LiveData<LatLng> getLocationLiveData() {
        return locationLiveData;
    }

    private ActivityMainBinding binding;//Objekt za xml-ot
    private FusedLocationProviderClient fusedLocationClient;
    public UserResponse currentUser;
    FloatingActionButton floatingActionButton;
    TextView usernameT;
    CircleImageView imageT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        findViewById(R.id.btn_back).setVisibility(View.GONE);
        findViewById(R.id.btn_logout).setOnClickListener(v->{
            AuthenticationService.removeToken(getApplicationContext());
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        });
        usernameT = findViewById(R.id.username_toolbar);
        imageT = findViewById(R.id.profile_image);
        replaceFragment(new WorldFragment());
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.navigation_world){
                replaceFragment(new WorldFragment());
            }else if(item.getItemId() == R.id.navigation_events){
                replaceFragment(new EventsFragment());
            }

            return true;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
        UserService.init(getApplicationContext());
        UserService.getCurrentUser(getApplicationContext(), new UserService.CurrentUserCallback() {
            @Override
            public void onSuccess(UserResponse userResponse) {
                currentUser = userResponse;
                runOnUiThread(() -> {
                    usernameT.setText(userResponse.getUsername());
                    Bitmap bitmap = BitmapFactory.decodeByteArray(userResponse.getProfilePicture(), 0, userResponse.getProfilePicture().length);
                    imageT.setImageBitmap(bitmap);
                });

            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame,fragment);
        fragmentTransaction.commit();
    }
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            startLocationUpdates();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Log.e("log1", "Location permission denied.");
            }
        }
    }

    private void startLocationUpdates() {
//        LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(10000); // 10 seconds
//        locationRequest.setFastestInterval(5000);
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
                .setIntervalMillis(INTERVAL_MILLIS)             // Sets the interval for location updates
                .setMinUpdateIntervalMillis(INTERVAL_MILLIS/2)  // Sets the fastest allowed interval of location updates.
                .setWaitForAccurateLocation(false)              // Want Accurate location updates make it true or you get approximate updates
                .setMaxUpdateDelayMillis(100)                   // Sets the longest a location update may be delayed.
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(!locationLiveData.isInitialized()){
                    binding.floatingActionButton.setOnClickListener(v->{
                        Intent intent = new Intent(getApplicationContext(), NewEventActivity.class);
                        intent.putExtra("lat",locationLiveData.getValue().latitude);
                        intent.putExtra("lon",locationLiveData.getValue().longitude);
                        startActivity(intent);
                    });
                }
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        locationLiveData.setValue(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                }
            }
        }, null);
    }

}