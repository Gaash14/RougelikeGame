package com.example.rougelikegame.models;

import java.io.Serializable;

public class User implements Serializable {

    private String uid;
    private String email, password;
    private String firstName, lastName;
    private String phone;
    private boolean isAdmin;

    // 🔹 Scores are stored DIRECTLY on the User
    private int highestWave;
    private int bestTime; // in seconds (lower = better)

    // Required empty constructor for Firebase
    public User() {
    }

    public User(String uid,
                String email,
                String password,
                String firstName,
                String lastName,
                String phone,
                boolean isAdmin,
                int highestWave,
                int bestTime) {

        this.uid = uid;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.isAdmin = isAdmin;
        this.highestWave = highestWave;
        this.bestTime = bestTime;
    }

    // --- basic info ---

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    // --- score stuff ---

    // Old-style names you already use in code:
    public int GetHighestWave() {
        return highestWave;
    }

    public int GetBestTime() {
        return bestTime;
    }

    public void setHighestWave(int highestWave) {
        this.highestWave = highestWave;
    }

    public void setBestTime(int bestTime) {
        this.bestTime = bestTime;
    }

    // Also normal Java-style getters (nice for future use)
    public int getHighestWave() {
        return highestWave;
    }

    public int getBestTime() {
        return bestTime;
    }

    @Override
    public String toString() {
        return "User{" +
            "uid='" + uid + '\'' +
            ", email='" + email + '\'' +
            ", password='" + password + '\'' +
            ", firstName='" + firstName + '\'' +
            ", LastName='" + lastName + '\'' +
            ", phone='" + phone + '\'' +
            ", isAdmin=" + isAdmin +
            ", highestWave=" + highestWave +
            ", bestTime=" + bestTime +
            '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        User user = (User) object;
        return uid.equals(user.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
