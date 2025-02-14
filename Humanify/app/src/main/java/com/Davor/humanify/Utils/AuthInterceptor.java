package com.Davor.humanify.Utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.Davor.humanify.Service.AuthenticationService;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context context;
    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String token = null;

        try {
            token = AuthenticationService.getToken(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (token != null) {
            Request newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }

        return chain.proceed(originalRequest);
    }
}
