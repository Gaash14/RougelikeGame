package com.example.rougelikegame.android.utils;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;

/**
 * Utility class for image processing and permission handling.
 */
public class ImageUtil {

    /**
     * Requests necessary permissions for camera and storage access.
     *
     * @param activity the activity requesting permissions
     */
    public static void requestPermission(@NotNull Activity activity) {
        ActivityCompat.requestPermissions(activity,
            new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
    }

    /**
     * Converts the drawable in an ImageView to a Base64 encoded string.
     *
     * @param postImage the ImageView containing the image to convert
     * @return the Base64 string, or null if the ImageView is empty
     */
    public static @Nullable String convertTo64Base(@NotNull final ImageView postImage) {
        if (postImage.getDrawable() == null) {
            return null;
        }
        Bitmap bitmap = ((BitmapDrawable) postImage.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Converts a Base64 encoded string back into a Bitmap.
     *
     * @param base64Code the Base64 string to decode
     * @return the decoded Bitmap, or null if the input is empty
     */
    public static @Nullable Bitmap convertFrom64base(@NotNull final String base64Code) {
        if (base64Code.isEmpty()) {
            return null;
        }
        byte[] decodedString = Base64.decode(base64Code, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
