package com.example.rougelikegame.android.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Static manager for handling sound effects.
 */
public final class SoundManager {
    private static final String TAG = "SoundManager";

    private static final Map<String, Sound> sounds = new HashMap<>();
    private static final Set<String> missingSoundKeysLogged = new HashSet<>();
    private static final Set<String> missingSoundFilesLogged = new HashSet<>();

    private static boolean initialized = false;
    private static float sfxVolume = 1f;
    private static boolean muted = false;

    private SoundManager() {
    }

    /**
     * Loads all sound effects from internal assets.
     */
    public static void load() {
        if (initialized) {
            return;
        }
        // sound taken from https://freesound.org/
        registerSound("shoot", "sounds/shoot.wav");
        registerSound("pickup", "sounds/pickup.wav");
        registerSound("hit", "sounds/hit.wav");
        registerSound("enemy_death", "sounds/enemy_death.wav");
        registerSound("player_hurt", "sounds/player_hurt.wav");
        registerSound("achievement", "sounds/achievement.wav");

        initialized = true;
    }

    /**
     * Plays a sound effect by its key with default volume.
     *
     * @param key the sound key
     */
    public static void play(String key) {
        play(key, 1f);
    }

    /**
     * Plays a sound effect by its key with specified volume.
     *
     * @param key the sound key
     * @param volume the volume multiplier (0.0 to 1.0)
     */
    public static void play(String key, float volume) {
        if (!initialized || muted) {
            return;
        }

        float finalVolume = MathUtils.clamp(volume, 0f, 1f) * sfxVolume;
        if (finalVolume <= 0f) {
            return;
        }

        Sound sound = sounds.get(key);
        if (sound == null) {
            if (!missingSoundKeysLogged.contains(key)) {
                Gdx.app.error(TAG, "Sound key not found: " + key);
                missingSoundKeysLogged.add(key);
            }
            return;
        }

        sound.play(finalVolume);
    }

    /**
     * Sets the volume level for all sound effects.
     *
     * @param volume the volume level (0.0 to 1.0)
     */
    public static void setSfxVolume(float volume) {
        sfxVolume = MathUtils.clamp(volume, 0f, 1f);
    }

    /**
     * Gets the current SFX volume level.
     *
     * @return the volume level
     */
    public static float getSfxVolume() {
        return sfxVolume;
    }

    /**
     * Mutes or unmutes sound effects.
     *
     * @param isMuted true to mute, false to unmute
     */
    public static void setMuted(boolean isMuted) {
        muted = isMuted;
    }

    /**
     * Checks if sound effects are currently muted.
     *
     * @return true if muted, false otherwise
     */
    public static boolean isMuted() {
        return muted;
    }

    /**
     * Disposes of all sound effects to free up memory.
     */
    public static void dispose() {
        for (Sound sound : sounds.values()) {
            if (sound != null) {
                sound.dispose();
            }
        }

        sounds.clear();
        missingSoundKeysLogged.clear();
        missingSoundFilesLogged.clear();
        initialized = false;
    }

    /**
     * Registers a new sound effect.
     *
     * @param key the sound key
     * @param path the internal path to the sound file
     */
    private static void registerSound(String key, String path) {
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) {
            if (!missingSoundFilesLogged.contains(path)) {
                Gdx.app.error(TAG, "Missing sound file: " + path);
                missingSoundFilesLogged.add(path);
            }
            return;
        }

        sounds.put(key, Gdx.audio.newSound(file));
    }
}
