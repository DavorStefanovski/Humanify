package com.Davor.humanify.Service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import com.Davor.humanify.DTO.EventResponse;
import com.Davor.humanify.DTO.UserResponse;
import com.Davor.humanify.Enum.Category;
import com.Davor.humanify.Utils.AuthInterceptor;
import com.Davor.humanify.Utils.ByteArrayDeserializer;
import com.Davor.humanify.Utils.LocalDateDeserializer;
import com.Davor.humanify.Utils.LocalDateTimeDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EventService {
    static String SERVER_URL = "http://192.168.1.147:8080/api/v1/events";
    static OkHttpClient client;
    @SuppressLint("NewApi")
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
            .registerTypeAdapter(byte[].class, new ByteArrayDeserializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .create();

    public static void init(Context context) {
        client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .build();
    }

    public static void getEvents(List<Category> category,String place,LocalDateTime until,Integer range,Double lat,Double lon, GetEventsCallback callback){
        new Thread(() -> {
            HttpUrl.Builder urlBuilder = HttpUrl.parse(SERVER_URL + "/bylocation").newBuilder();
            // Add parameters if they are not null
            if (category != null && !category.isEmpty()) {
                for (Category cat : category) {
                    urlBuilder.addQueryParameter("category", cat.name());
                }
            }
            if (place != null && !place.isEmpty()) {
                urlBuilder.addQueryParameter("place", place);
            }
            if (until != null) {
                urlBuilder.addQueryParameter("until", until.toString());
            }
            if (range != null) {
                urlBuilder.addQueryParameter("range", range.toString());
            }
            if (lat != null && lon != null) {
                urlBuilder.addQueryParameter("lat", lat.toString());
                urlBuilder.addQueryParameter("lon", lon.toString());
            }

            String url = urlBuilder.build().toString();

            Request request = new Request.Builder().url(url).get().build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    List<EventResponse> events = gson.fromJson(jsonResponse,
                            new com.google.gson.reflect.TypeToken<List<EventResponse>>() {}.getType()
                    );
                    callback.onSuccess(events);
                } else {
                    callback.onFailure("Failed to retrieve events: " + response.message());
                }
            } catch (Exception e) {
                callback.onFailure("Error: " + e.getMessage());
            }
        }).start();
    }

    public static void getEvent(Integer id, GetEventCallback callback) {
        new Thread(() -> {
            // Append the ID to the URL as a path variable
            String url = SERVER_URL + "/" + id;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    EventResponse event = gson.fromJson(jsonResponse, EventResponse.class);
                    callback.onSuccess(event);
                } else {
                    callback.onFailure("Failed to retrieve event: " + response.message());
                }
            } catch (Exception e) {
                callback.onFailure("Error: " + e.getMessage());
            }
        }).start();
    }

    public static void postEvent(
            Context context,
            String title,
            String description,
            String place,
            String category,
            LocalDateTime localDateTime,
            Double lat,
            Double lon,
            List<Uri> pictures,
            EventService.PostEventCallback callback
    ) {
        new Thread(() -> {
            try {
                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                // Add text fields
                multipartBuilder.addFormDataPart("title", title);
                multipartBuilder.addFormDataPart("description", description);
                multipartBuilder.addFormDataPart("place", place);
                multipartBuilder.addFormDataPart("category", category);
                multipartBuilder.addFormDataPart("dateTime", localDateTime.toString());
                multipartBuilder.addFormDataPart("lat", lat.toString());
                multipartBuilder.addFormDataPart("lon", lon.toString());

                // Add image files
                for (Uri imageUri : pictures) {
                    try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
                        if (inputStream != null) {
                            byte[] imageBytes = new byte[inputStream.available()];
                            inputStream.read(imageBytes);

                            RequestBody imageBody = RequestBody.create(imageBytes, MediaType.parse("image/*"));
                            multipartBuilder.addFormDataPart("pictures", "image.jpg", imageBody);
                        }
                    }
                }

                RequestBody requestBody = multipartBuilder.build();
                Request request = new Request.Builder()
                        .url(SERVER_URL)
                        .post(requestBody)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();

                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Post Event failed with status code: " + response.code());
                }
            } catch (IOException e) {
                callback.onFailure("Error: " + e.getMessage());
            }
        }).start();
    }


    public interface GetEventsCallback {
        void onSuccess(List<EventResponse> eventResponses);
        void onFailure(String errorMessage);
    }
    public interface GetEventCallback {
        void onSuccess(EventResponse eventResponse);
        void onFailure(String errorMessage);
    }
    public interface PostEventCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
