package com.example.rougelikegame.android.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.rougelikegame.android.models.meta.DailyRun;
import com.example.rougelikegame.android.models.meta.Guild;
import com.example.rougelikegame.android.models.meta.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Service class for Firebase Realtime Database operations.
 */
public class DatabaseService {

    private static final String TAG = "DatabaseService";
    private static final String USERS_PATH = "users";

    /**
     * Callback interface for asynchronous database operations.
     *
     * @param <T> the type of the result object
     */
    public interface DatabaseCallback<T> {
        void onCompleted(T object);
        void onFailed(Exception e);
    }

    private static DatabaseService instance;
    private final DatabaseReference databaseReference;

    private DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    /**
     * Gets the singleton instance of DatabaseService.
     *
     * @return the singleton instance
     */
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /**
     * Writes data to the specified path in the database.
     *
     * @param path the database path
     * @param data the object to write
     * @param callback the completion callback
     */
    private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        readData(path).setValue(data, (error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    /**
     * Deletes data at the specified path in the database.
     *
     * @param path the database path
     * @param callback the completion callback
     */
    private void deleteData(@NotNull final String path, @Nullable final DatabaseCallback<Void> callback) {
        readData(path).removeValue((error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    /**
     * Gets a DatabaseReference for the specified path.
     *
     * @param path the database path
     * @return the DatabaseReference
     */
    private DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }

    /**
     * Retrieves a single object from the specified path.
     *
     * @param path the database path
     * @param clazz the class of the object
     * @param callback the completion callback
     * @param <T> the type of the object
     */
    private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            T data = task.getResult().getValue(clazz);
            callback.onCompleted(data);
        });
    }

    /**
     * Retrieves a list of objects from the specified path.
     *
     * @param path the database path
     * @param clazz the class of the objects
     * @param callback the completion callback
     * @param <T> the type of the objects
     */
    private <T> void getDataList(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<List<T>> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<T> tList = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                T t = dataSnapshot.getValue(clazz);
                tList.add(t);
            });

            callback.onCompleted(tList);
        });
    }

    /**
     * Generates a new unique ID at the specified path.
     *
     * @param path the database path
     * @return the generated ID
     */
    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    /**
     * Runs a transaction on the data at the specified path.
     *
     * @param path the database path
     * @param clazz the class of the object
     * @param function the transformation function
     * @param callback the completion callback
     * @param <T> the type of the object
     */
    private <T> void runTransaction(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull UnaryOperator<T> function, @NotNull final DatabaseCallback<T> callback) {
        readData(path).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                T currentValue = currentData.getValue(clazz);
                currentValue = function.apply(currentValue);
                currentData.setValue(currentValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed", error.toException());
                    callback.onFailed(error.toException());
                    return;
                }
                T result = currentData != null ? currentData.getValue(clazz) : null;
                callback.onCompleted(result);
            }
        });
    }

    /**
     * Generates a new user ID.
     *
     * @return the generated user ID
     */
    public String generateUserId() {
        return generateNewId(USERS_PATH);
    }

    /**
     * Creates a new user in the database.
     *
     * @param user the user to create
     * @param callback the completion callback
     */
    public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData(USERS_PATH + "/" + user.getUid(), user, callback);
    }

    /**
     * Retrieves a user by their UID.
     *
     * @param uid the user's UID
     * @param callback the completion callback
     */
    public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
        getData(USERS_PATH + "/" + uid, User.class, callback);
    }

    /**
     * Retrieves a list of all users.
     *
     * @param callback the completion callback
     */
    public void getUserList(@NotNull final DatabaseCallback<List<User>> callback) {
        getDataList(USERS_PATH, User.class, callback);
    }

    /**
     * Deletes a user by their UID.
     *
     * @param uid the user's UID
     * @param callback the completion callback
     */
    public void deleteUser(@NotNull final String uid, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(USERS_PATH + "/" + uid, callback);
    }

    /**
     * Retrieves a user by their email and password.
     *
     * @param email the user's email
     * @param password the user's password
     * @param callback the completion callback
     */
    public void getUserByEmailAndPassword(@NotNull final String email, @NotNull final String password, @NotNull final DatabaseCallback<User> callback) {
        readData(USERS_PATH).orderByChild("email").equalTo(email).get()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Error getting data", task.getException());
                    callback.onFailed(task.getException());
                    return;
                }
                if (task.getResult().getChildrenCount() == 0) {
                    callback.onFailed(new Exception("User not found"));
                    return;
                }
                for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user == null || !Objects.equals(user.getPassword(), password)) {
                        callback.onFailed(new Exception("Invalid email or password"));
                        return;
                    }

                    callback.onCompleted(user);
                    return;
                }
            });
    }

    /**
     * Checks if an email already exists in the database.
     *
     * @param email the email to check
     * @param callback the completion callback
     */
    public void checkIfEmailExists(@NotNull final String email, @NotNull final DatabaseCallback<Boolean> callback) {
        readData(USERS_PATH).orderByChild("email").equalTo(email).get()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Error getting data", task.getException());
                    callback.onFailed(task.getException());
                    return;
                }
                boolean exists = task.getResult().getChildrenCount() > 0;
                callback.onCompleted(exists);
            });
    }

    /**
     * Updates user data with merge logic to ensure progress is not lost.
     *
     * @param incomingUser the updated user data
     * @param isRunUpdate whether this update is from a completed game run
     * @param callback the completion callback
     */
    public void updateUser(
        @NotNull final User incomingUser,
        boolean isRunUpdate,
        @Nullable final DatabaseCallback<Void> callback
    ) {
        runTransaction(
            USERS_PATH + "/" + incomingUser.getUid(),
            User.class,
            currentUser -> {
                if (currentUser == null) {
                    return incomingUser;
                }

                if (incomingUser.getNumOfAttempts() < currentUser.getNumOfAttempts()) {
                    return currentUser;
                }

                currentUser.setHighestWave(Math.max(currentUser.getHighestWave(), incomingUser.getHighestWave()));

                if (incomingUser.getBestTime() > 0) {
                    int currentBest = currentUser.getBestTime();
                    if (currentBest == 0 || incomingUser.getBestTime() < currentBest) {
                        currentUser.setBestTime(incomingUser.getBestTime());
                    }
                }

                if (isRunUpdate) {
                    currentUser.setNumOfAttempts(currentUser.getNumOfAttempts() + 1);
                }

                if (incomingUser.getNumOfWins() > currentUser.getNumOfWins()) {
                    currentUser.setNumOfWins(currentUser.getNumOfWins() + 1);
                }

                if (incomingUser.getPickedRanged() > currentUser.getPickedRanged()) {
                    currentUser.setPickedRanged(currentUser.getPickedRanged() + 1);
                }

                currentUser.setCurrentStreak(incomingUser.getCurrentStreak());
                currentUser.setBestStreak(Math.max(currentUser.getBestStreak(), incomingUser.getBestStreak()));
                currentUser.setEnemiesKilled(Math.max(currentUser.getEnemiesKilled(), incomingUser.getEnemiesKilled()));
                currentUser.setPickupsPicked(Math.max(currentUser.getPickupsPicked(), incomingUser.getPickupsPicked()));
                currentUser.setItemsPicked(Math.max(currentUser.getItemsPicked(), incomingUser.getItemsPicked()));
                currentUser.setNumOfCoins(Math.max(currentUser.getNumOfCoins(), incomingUser.getNumOfCoins()));
                currentUser.setDailyChallengesCompleted(Math.max(currentUser.getDailyChallengesCompleted(), incomingUser.getDailyChallengesCompleted()));
                currentUser.setDailyStreak(Math.max(currentUser.getDailyStreak(), incomingUser.getDailyStreak()));
                currentUser.setBestDailyStreak(Math.max(currentUser.getBestDailyStreak(), incomingUser.getBestDailyStreak()));

                if (incomingUser.getLastDailyCompletionDate() != null) {
                    currentUser.setLastDailyCompletionDate(incomingUser.getLastDailyCompletionDate());
                }

                if (incomingUser.getProfileImage() != null) {
                    currentUser.setProfileImage(incomingUser.getProfileImage());
                }

                currentUser.setFirstName(incomingUser.getFirstName());
                currentUser.setLastName(incomingUser.getLastName());
                currentUser.setEmail(incomingUser.getEmail());
                currentUser.setPhone(incomingUser.getPhone());
                currentUser.setPassword(incomingUser.getPassword());

                return currentUser;
            },
            new DatabaseCallback<User>() {
                @Override
                public void onCompleted(User ignored) {
                    if (callback != null) {
                        callback.onCompleted(null);
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    if (callback != null) {
                        callback.onFailed(e);
                    }
                }
            }
        );
    }

    /**
     * Creates a new guild and links the owner user.
     *
     * @param guildName the name of the guild
     * @param user the owner user
     * @return the generated guild ID
     */
    public String createGuild(String guildName, User user) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        String guildId = rootRef.child("guilds").push().getKey();

        Guild guild = new Guild();
        guild.setGuildId(guildId);
        guild.setName(guildName);
        guild.setOwnerUid(user.getUid());

        Map<String, Boolean> members = new HashMap<>();
        members.put(user.getUid(), true);
        guild.setMembers(members);

        rootRef.child("guilds").child(guildId).setValue(guild);
        rootRef.child("users").child(user.getUid()).child("guildId").setValue(guildId);

        return guildId;
    }

    /**
     * Updates cumulative guild stats after a game run.
     *
     * @param user the user who completed the run
     * @param enemiesKilled number of enemies killed in the run
     * @param won whether the run was a victory
     */
    public void addGuildRunStats(User user, int enemiesKilled, boolean won) {
        if (user.getGuildId() == null) return;

        DatabaseReference guildRef = FirebaseDatabase.getInstance()
            .getReference("guilds")
            .child(user.getGuildId());

        incrementStat(guildRef.child("totalEnemiesKilled"), enemiesKilled);
        incrementStat(guildRef.child("totalAttempts"), 1);

        if (won) {
            incrementStat(guildRef.child("totalWins"), 1);
        }
    }

    /**
     * Atomically increments a numeric stat in the database.
     *
     * @param ref reference to the stat
     * @param amount amount to increment by
     */
    private void incrementStat(DatabaseReference ref, int amount) {
        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Integer value = currentData.getValue(Integer.class);
                currentData.setValue((value == null ? 0 : value) + amount);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {}
        });
    }

    /**
     * Retrieves a list of all available guilds.
     *
     * @param callback the completion callback
     */
    public void getGuildList(DatabaseCallback<List<Guild>> callback) {
        FirebaseDatabase.getInstance()
            .getReference("guilds")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<Guild> guilds = new ArrayList<>();
                    for (DataSnapshot guildSnap : snapshot.getChildren()) {
                        Guild guild = guildSnap.getValue(Guild.class);
                        if (guild != null) {
                            guild.setGuildId(guildSnap.getKey());
                            guilds.add(guild);
                        }
                    }
                    if (callback != null) {
                        callback.onCompleted(guilds);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if (callback != null) {
                        callback.onFailed(error.toException());
                    }
                }
            });
    }

    /**
     * Unlocks an achievement for a specific user.
     *
     * @param uid user UID
     * @param achievementId ID of the achievement to unlock
     */
    public void unlockAchievement(String uid, String achievementId) {
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .child("achievements")
            .child(achievementId)
            .setValue(true);
    }

    /**
     * Unlocks a skin for a specific user.
     *
     * @param uid user UID
     * @param skinId ID of the skin to unlock
     */
    public void unlockOwnedSkin(String uid, String skinId) {
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .child("ownedSkins")
            .child(skinId)
            .setValue(true);
    }

    /**
     * Sets the currently equipped skin for a user.
     *
     * @param uid user UID
     * @param skinId ID of the skin to equip
     */
    public void setEquippedSkin(String uid, String skinId) {
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .child("equippedSkinId")
            .setValue(skinId);
    }

    /**
     * Updates the coin count for a user.
     *
     * @param uid user UID
     * @param coins new coin total
     */
    public void setCoins(String uid, int coins) {
        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .child("numOfCoins")
            .setValue(coins);
    }

    /**
     * Saves a daily run entry.
     *
     * @param dateKey the date key (e.g., YYYY-MM-DD)
     * @param run the daily run data
     * @param callback the completion callback
     */
    public void saveDailyRun(String dateKey, DailyRun run, DatabaseCallback<Void> callback) {
        FirebaseDatabase.getInstance()
            .getReference("daily_runs")
            .child(dateKey)
            .child(run.uid)
            .setValue(run)
            .addOnSuccessListener(aVoid -> {
                if (callback != null) callback.onCompleted(null);
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailed(e);
            });
    }

    /**
     * Saves a daily run only if it is better than the existing entry for that day.
     *
     * @param dateKey the date key
     * @param run the daily run data
     * @param callback the completion callback
     */
    public void saveDailyRunIfBetter(String dateKey, DailyRun run, DatabaseCallback<Void> callback) {
        if (run == null || run.uid == null || run.uid.trim().isEmpty()) {
            android.util.Log.e("DatabaseService", "Cannot save daily run: invalid run or UID");
            if (callback != null) callback.onFailed(new Exception("Invalid run or UID"));
            return;
        }

        FirebaseDatabase.getInstance()
            .getReference("daily_runs")
            .child(dateKey)
            .child(run.uid)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    DailyRun existingRun = snapshot.getValue(DailyRun.class);
                    
                    // Logic for improvement: 
                    // 1. Higher wave is always better.
                    // 2. If same wave, a non-zero time (win) is better than zero time (loss).
                    // 3. If both have non-zero time, lower time is better.
                    boolean shouldSave = existingRun == null;
                    if (!shouldSave) {
                        if (run.wave > existingRun.wave) {
                            shouldSave = true;
                        } else if (run.wave == existingRun.wave) {
                            if (run.time > 0) {
                                if (existingRun.time <= 0 || run.time < existingRun.time) {
                                    shouldSave = true;
                                }
                            }
                        }
                    }

                    if (!shouldSave) {
                        android.util.Log.d("DatabaseService", "Daily run not better than existing. Wave: " + run.wave + " vs " + (existingRun != null ? existingRun.wave : "null"));
                        if (callback != null) callback.onCompleted(null);
                        return;
                    }

                    android.util.Log.d("DatabaseService", "Saving better daily run. Wave: " + run.wave + ", Time: " + run.time);
                    saveDailyRun(dateKey, run, callback);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    android.util.Log.e("DatabaseService", "Failed to check existing daily run", error.toException());
                    if (callback != null) callback.onFailed(error.toException());
                }
            });
    }

    /**
     * Retrieves all daily runs for a specific date.
     *
     * @param dateKey the date key
     * @param callback the completion callback
     */
    public void getDailyRuns(String dateKey, DatabaseCallback<List<DailyRun>> callback) {
        FirebaseDatabase.getInstance()
            .getReference("daily_runs")
            .child(dateKey)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<DailyRun> runs = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        DailyRun run = child.getValue(DailyRun.class);
                        if (run != null) {
                            runs.add(run);
                        }
                    }
                    callback.onCompleted(runs);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onFailed(error.toException());
                }
            });
    }
}
