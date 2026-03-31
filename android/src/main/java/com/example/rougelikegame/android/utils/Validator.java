package com.example.rougelikegame.android.utils;

import android.util.Patterns;

import androidx.annotation.Nullable;

/**
 * Utility class for input validation.
 */
public class Validator {

    /**
     * Checks if the provided email address is valid according to standard patterns.
     *
     * @param email the email address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isEmailValid(@Nullable String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Checks if the provided password meets the minimum security requirements.
     *
     * @param password the password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isPasswordValid(@Nullable String password) {
        return password != null && password.length() >= 6;
    }


    /**
     * Checks if the provided phone number is valid according to standard patterns.
     *
     * @param phone the phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isPhoneValid(@Nullable String phone) {
        return phone != null && phone.length() >= 10 && Patterns.PHONE.matcher(phone).matches();
    }

    /**
     * Checks if the provided name meets the length requirements.
     *
     * @param name the name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNameValid(@Nullable String name) {
        return name != null && name.length() >= 3;
    }
}
