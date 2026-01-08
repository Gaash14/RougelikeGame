package com.example.rougelikegame.android.models.meta;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {

    private String uid;
    private String email, password;
    private String firstName, lastName;
    private String fullName;
    private String phone;
    private boolean isAdmin;

    private String guildId;

    private Map<String, Boolean> ownedSkins = new HashMap<>();
    private String equippedSkinId = "default";

    private String profileImage;

    // Scores are stored directly on the User
    private int highestWave;
    private int bestTime; // in seconds (lower = better)
    private int numOfAttempts;
    private int numOfWins;
    private int currentStreak;
    private int bestStreak;
    private int pickedRanged;
    private int enemiesKilled;
    private int pickupsPicked;
    private int numOfCoins;

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
                int bestTime,
                int numOfAttempts,
                int numOfWins,
                int currentStreak,
                int bestStreak,
                int pickedRanged,
                int enemiesKilled,
                int pickupsPicked,
                int numOfCoins) {

        this.uid = uid;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.isAdmin = isAdmin;

        this.highestWave = highestWave;
        this.bestTime = bestTime;
        this.numOfAttempts = numOfAttempts;
        this.numOfWins = numOfWins;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
        this.pickedRanged = pickedRanged;
        this.enemiesKilled = enemiesKilled;
        this.pickupsPicked = pickupsPicked;
        this.numOfCoins = numOfCoins;

        this.ownedSkins = new HashMap<>();
        this.ownedSkins.put("default", true);
        this.equippedSkinId = "default";
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

    public String getProfileImage() {return profileImage;}
    public void setProfileImage(String profileImage) {this.profileImage = profileImage;}

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
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGuildId() {
        return guildId;
    }
    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    // --- shop/skin related ---
    public Map<String, Boolean> getOwnedSkins() {
        if (ownedSkins == null) {
            ownedSkins = new HashMap<>();
        }
        return ownedSkins;
    }
    public void setOwnedSkins(Map<String, Boolean> ownedSkins) { this.ownedSkins = ownedSkins;}
    public String getEquippedSkinId() { return equippedSkinId == null ? "default" : equippedSkinId;}
    public void setEquippedSkinId(String equippedSkinId) { this.equippedSkinId = equippedSkinId;}

    public int getNumOfCoins() { return numOfCoins;}
    public void setNumOfCoins(int numOfCoins) { this.numOfCoins = numOfCoins;}
    // --- score stuff ---
    // getters
    public int getHighestWave() {
        return highestWave;
    }
    public int getBestTime() {
        return bestTime;
    }
    public int getNumOfAttempts() { return numOfAttempts; }
    public int getNumOfWins() { return numOfWins; }
    public int getCurrentStreak() {return currentStreak;}
    public int getBestStreak() {return bestStreak;}
    public int getPickedRanged() { return pickedRanged; }
    public int getEnemiesKilled() { return enemiesKilled; }
    public int getPickupsPicked() { return pickupsPicked; }

    // setters
    public void setHighestWave(int highestWave) {
        this.highestWave = highestWave;
    }
    public void setBestTime(int bestTime) {
        this.bestTime = bestTime;
    }
    public void setNumOfAttempts(int numOfAttempts) { this.numOfAttempts = numOfAttempts;}
    public void setNumOfWins(int numOfWins) { this.numOfWins = numOfWins;}
    public void setCurrentStreak(int currentStreak) {this.currentStreak = currentStreak;}
    public void setBestStreak(int bestStreak) {this.bestStreak = bestStreak;}
    public void setPickedRanged(int pickedRanged) { this.pickedRanged = pickedRanged;}
    public void setEnemiesKilled(int enemiesKilled) { this.enemiesKilled = enemiesKilled;}
    public void setPickupsPicked(int pickupsPicked) { this.pickupsPicked = pickupsPicked;}
}
