package com.Davor.humanify.Service;

import android.annotation.SuppressLint;
import android.content.Context;

import com.Davor.humanify.DTO.DiscussionRequest;
import com.Davor.humanify.DTO.EventResponse;
import com.Davor.humanify.Utils.AuthInterceptor;
import com.Davor.humanify.Utils.ByteArrayDeserializer;
import com.Davor.humanify.Utils.LocalDateDeserializer;
import com.Davor.humanify.Utils.LocalDateTimeDeserializer;
import com.Davor.humanify.Utils.LocalDateTimeSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DiscussionService {
    static String SERVER_URL = "http://192.168.1.147:8080/api/v1/discussions";
    static OkHttpClient client;
    @SuppressLint("NewApi")
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
            .registerTypeAdapter(byte[].class, new ByteArrayDeserializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .registerTypeAdapter(LocalDateTime.class,new LocalDateTimeSerializer())
            .create();

    public static void init(Context context) {
        client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .build();
    }

    public static void postDiscussion(String text, LocalDateTime dateTime, Integer eventId, PostDicsussionCallback callback){
        new Thread(()->{
        // Create the request body as a POJO
        DiscussionRequest discussionRequest = new DiscussionRequest(text, dateTime, eventId);

        // Convert the POJO into JSON using Gson
        String json = gson.toJson(discussionRequest);

        // Create the RequestBody with the JSON payload
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        // Create the POST request
        Request request = new Request.Builder()
                .url(SERVER_URL)
                .post(body)
                .build();

        // Make the network request in a separate thread to avoid blocking the UI thread

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // Notify success
                    callback.onSuccess();
                } else {
                    // Notify failure with error message
                    callback.onFailure("Failed to post discussion: " + response.message());
                }
            } catch (IOException e) {
                // Handle exception (e.g., network error)
                callback.onFailure("Network error: " + e.getMessage());
            }
        }).start();
    }
    public interface PostDicsussionCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
