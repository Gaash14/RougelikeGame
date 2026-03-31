package com.example.rougelikegame.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.rougelikegame.android.models.meta.User;
import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Utility class for SharedPreferences operations.
 */
public class SharedPreferencesUtil {

    private static final String PREF_NAME = "com.example.rougelikegame.PREFERENCE_FILE_KEY";

    /**
     * Saves a string value to SharedPreferences.
     *
     * @param context the context to use
     * @param key the key to save the string with
     * @param value the string value to save
     */
    private static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Retrieves a string value from SharedPreferences.
     *
     * @param context the context to use
     * @param key the key to retrieve the string with
     * @param defaultValue the default value if the key is not found
     * @return the retrieved string value
     */
    private static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Saves an integer value to SharedPreferences.
     *
     * @param context the context to use
     * @param key the key to save the integer with
     * @param value the integer value to save
     */
    private static void saveInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Retrieves an integer value from SharedPreferences.
     *
     * @param context the context to use
     * @param key the key to retrieve the integer with
     * @param defaultValue the default value if the key is not found
     * @return the retrieved integer value
     */
    private static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Clears all data from SharedPreferences.
     *
     * @param context the context to use
     */
    public static void clear(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Removes a specific key and its value from SharedPreferences.
     *
     * @param context the context to use
     * @param key the key to remove
     */
    private static void remove(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * Checks if SharedPreferences contains a specific key.
     *
     * @param context the context to use
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    private static boolean contains(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.contains(key);
    }

    /**
     * Serializes an object to JSON and saves it to SharedPreferences.
     *
     * @param context the context to use
     * @param key the key to save the object with
     * @param object the object to save
     * @param <T> the type of the object
     */
    private static <T> void saveObject(Context context, String key, T object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        saveString(context, key, json);
    }

    /**
     * Retrieves and deserializes an object from JSON in SharedPreferences.
     *
     * @param context the context to use
     * @param key the key to retrieve the object with
     * @param type the class of the object
     * @param <T> the type of the object
     * @return the deserialized object, or null if not found
     */
    private static <T> T getObject(Context context, String key, Class<T> type) {
        String json = getString(context, key, null);
        if (json == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    /**
     * Saves the current user object to local storage.
     *
     * @param context the context to use
     * @param user the user object to save
     */
    public static void saveUser(Context context, User user) {
        saveObject(context, "user", user);
    }

    /**
     * Retrieves the current logged-in user object from local storage.
     *
     * @param context the context to use
     * @return the user object, or null if no user is logged in
     */
    public static User getUser(Context context) {
        if (!isUserLoggedIn(context)) {
            return null;
        }

        User user = getObject(context, "user", User.class);

        if (user != null && user.getOwnedSkins() == null) {
            user.setOwnedSkins(new HashMap<>());
            saveUser(context, user);
        }

        return user;
    }

    /**
     * Signs out the current user by removing their data from local storage.
     *
     * @param context the context to use
     */
    public static void signOutUser(Context context) {
        remove(context, "user");
    }

    /**
     * Checks if a user is currently logged in locally.
     *
     * @param context the context to use
     * @return true if a user is logged in, false otherwise
     */
    public static boolean isUserLoggedIn(Context context) {
        return contains(context, "user");
    }

    /**
     * Gets the UID of the currently logged-in user.
     *
     * @param context the context to use
     * @return the user's UID, or null if not logged in
     */
    @Nullable
    public static String getUserId(Context context) {
        User user = getUser(context);
        if (user != null) {
            return user.getUid();
        }
        return null;
    }
}
