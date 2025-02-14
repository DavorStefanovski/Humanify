package com.Davor.humanify.Service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.Davor.humanify.DTO.UserResponse;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import com.Davor.humanify.Utils.KeystoreHelper;

public class AuthenticationService {
    private static final String PREFS_NAME = "secure_prefs";
    private static final String TIMESTAMP_KEY = "token_timestamp";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // This is the default length for AES-GCM IV
    private static final long TOKEN_VALIDITY = 1800000; // 30 minutes in milliseconds

    public static void saveToken(Context context, String token) throws Exception {
        SecretKey secretKey = KeystoreHelper.getSecretKey();  // Use the secure key
        Cipher cipher = Cipher.getInstance(AES_MODE);

        // Initialize cipher in ENCRYPT_MODE (automatic IV generation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Retrieve the IV used during encryption (this is automatically generated)
        byte[] iv = cipher.getIV();

        byte[] encrypted = cipher.doFinal(token.getBytes());

        // Combine IV and encrypted token
        String encryptedToken = Base64.encodeToString(iv, Base64.DEFAULT) +
                ":" +
                Base64.encodeToString(encrypted, Base64.DEFAULT);

        // Save the encrypted token and current timestamp
        long currentTime = System.currentTimeMillis(); // Save the current timestamp
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(TOKEN_KEY, encryptedToken)
                .putLong(TIMESTAMP_KEY, currentTime)
                .apply();
    }

    // Returns null if the token is not existent or older than 30 minutes
    public static String getToken(Context context) throws Exception {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String encryptedToken = prefs.getString(TOKEN_KEY, null);
        long savedTime = prefs.getLong(TIMESTAMP_KEY, 0);
        long currentTime = System.currentTimeMillis();

        if (encryptedToken == null || (currentTime - savedTime > TOKEN_VALIDITY)) return null;

        // Split the stored token into IV and encrypted parts
        String[] parts = encryptedToken.split(":");
        byte[] iv = Base64.decode(parts[0], Base64.DEFAULT);
        byte[] encrypted = Base64.decode(parts[1], Base64.DEFAULT);

        SecretKey secretKey = KeystoreHelper.getSecretKey();  // Retrieve the same key
        Cipher cipher = Cipher.getInstance(AES_MODE);

        // Initialize Cipher for decryption with the same IV and GCMParameterSpec
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);  // 128-bit authentication tag length
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted);
    }
    public static void removeToken(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Remove the token and timestamp
        editor.remove(TOKEN_KEY);
        editor.remove(TIMESTAMP_KEY);

        // Commit the changes
        editor.apply();
    }
}
