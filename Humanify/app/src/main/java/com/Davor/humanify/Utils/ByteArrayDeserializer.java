package com.Davor.humanify.Utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.Base64;

public class ByteArrayDeserializer implements JsonDeserializer<byte[]> {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Base64.getDecoder().decode(json.getAsString());
    }
}
