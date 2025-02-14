package com.Davor.humanify.Service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import com.Davor.humanify.DTO.DiscussionRequest;
import com.Davor.humanify.DTO.LoginDTO;
import com.Davor.humanify.DTO.UserResponse;
import com.Davor.humanify.DTO.UserWithEventsResponse;
import com.Davor.humanify.Utils.AuthInterceptor;
import com.Davor.humanify.Utils.ByteArrayDeserializer;
import com.Davor.humanify.Utils.LocalDateDeserializer;
import com.Davor.humanify.Utils.LocalDateTimeDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserService {
    static String SERVER_URL = "http://192.168.1.147:8080/api/v1/users";
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

    public static void login(String username, String password, LoginCallback callback) {
        new Thread(() -> {
            LoginDTO loginDTO = new LoginDTO(username, password);
            String json = gson.toJson(loginDTO);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
            Request request = new Request.Builder().url(SERVER_URL + "/login").post(requestBody).build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure("Failed to log in");
                }
            } catch (Exception e) {
                callback.onFailure("Error: " + e.getMessage());
            }
        }).start();
    }
    public static void register(
            Context context,
            String email,
            String password,
            String username,
            String firstName,
            String lastName,
            LocalDate birthDate,
            Uri profilePictureUri,
            RegisterCallback callback
    ) {
        new Thread(() -> {
            try {
                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                // Add user details
                multipartBuilder.addFormDataPart("email", email);
                multipartBuilder.addFormDataPart("password", password);
                multipartBuilder.addFormDataPart("username", username);
                multipartBuilder.addFormDataPart("firstName", firstName);
                multipartBuilder.addFormDataPart("lastName", lastName);
                multipartBuilder.addFormDataPart("birthDate", birthDate.toString());

                // Add profile picture if provided
                if (profilePictureUri != null) {
                    InputStream inputStream = context.getContentResolver().openInputStream(profilePictureUri);
                    byte[] fileBytes = getBytes(inputStream);
                    RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse("image/*"));
                    multipartBuilder.addFormDataPart("profilePicture", "profile.jpg", fileBody);
                }

                RequestBody requestBody = multipartBuilder.build();

                Request request = new Request.Builder()
                        .url(SERVER_URL + "/register")
                        .post(requestBody)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();

                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    UserResponse userResponse = gson.fromJson(jsonResponse, UserResponse.class);
                    callback.onSuccess(userResponse);
                } else {
                    callback.onFailure("Registration failed with status code: " + response.code());
                }
            } catch (IOException e) {
                callback.onFailure("Error: " + e.getMessage());
            }
        }).start();
    }

    private static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    public static void getCurrentUser(Context context, CurrentUserCallback callback) {
        new Thread(() -> {
            Request request = new Request.Builder().url(SERVER_URL + "/currentuser").get().build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    UserResponse userResponse = gson.fromJson(jsonResponse, UserResponse.class);
                    callback.onSuccess(userResponse);
                } else {
                    callback.onFailure("Failed to retrieve user");
                }
            } catch (Exception e) {
                callback.onFailure("Error: " + e.getMessage());
            }
        }).start();
    }

    public static void participate(Integer eventId, ParticipateCallback callback) {
        new Thread(() -> {
            RequestBody emptyBody = RequestBody.create("", MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(SERVER_URL + "/participate/" + eventId)
                    .post(emptyBody)  // Use an empty JSON body
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Failed to participate: " + response.message());
                }
            } catch (IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
            }
        }).start();
    }
    public static void getCurrentUserWithEvents(UserWithEventsCallback callback) {
        new Thread(() -> {
            Request request = new Request.Builder().url(SERVER_URL + "/currentuserevents").get().build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    UserWithEventsResponse userResponse = gson.fromJson(jsonResponse, UserWithEventsResponse.class);
                    callback.onSuccess(userResponse);
                } else {
                    callback.onFailure("Failed to retrieve user");
                }
            } catch (Exception e) {
                callback.onFailure("Error: " + e.getMessage());
            }
        }).start();
    }
    public interface LoginCallback {
        void onSuccess(String response);
        void onFailure(String errorMessage);
    }

    public interface CurrentUserCallback {
        void onSuccess(UserResponse userResponse);
        void onFailure(String errorMessage);
    }
    public interface RegisterCallback {
        void onSuccess(UserResponse userResponse);
        void onFailure(String errorMessage);
    }
    public interface ParticipateCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
    public interface UserWithEventsCallback {
        void onSuccess(UserWithEventsResponse userWithEventsResponse);
        void onFailure(String errorMessage);
    }

}