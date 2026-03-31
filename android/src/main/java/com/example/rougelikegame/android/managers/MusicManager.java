package com.example.rougelikegame.android.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Static manager for handling background music tracks.
 */
public final class MusicManager {
    private static final String TAG = "MusicManager";
    private static final String BGM_KEY = "bgm";
    private static final String BOSS_KEY = "boss";
    private static final String WIN_KEY = "win";
    private static final String DEATH_KEY = "death";

    private static final Map<String, Music> tracks = new HashMap<>();
    private static final Set<String> missingMusicKeysLogged = new HashSet<>();
    private static final Set<String> missingMusicFilesLogged = new HashSet<>();

    private static boolean initialized = false;
    private static float musicVolume = 1f;
    private static boolean muted = false;
    private static String currentTrackKey = null;

    private MusicManager() {
    }

    /**
     * Loads all music tracks from internal assets.
     */
    public static void load() {
        if (initialized) {
            return;
        }
        // music taken from https://opengameart.org/
        registerMusic(BGM_KEY, "music/background_bgm.mp3");
        registerMusic(BOSS_KEY, "music/boss_bgm.mp3");
        registerMusic(WIN_KEY, "music/win_bgm.mp3");
        registerMusic(DEATH_KEY, "music/death_bgm.ogg");

        initialized = true;
    }

    /**
     * Plays the standard background music.
     */
    public static void playBackgroundMusic() {
        play(BGM_KEY, true);
    }

    /**
     * Plays the boss fight background music.
     */
    public static void playBossMusic() {
        play(BOSS_KEY, true);
    }

    /**
     * Plays the victory theme.
     */
    public static void playWinMusic() {
        play(WIN_KEY, true);
    }

    /**
     * Plays the death theme.
     */
    public static void playDeathMusic() {
        play(DEATH_KEY, true);
    }

    /**
     * Checks if the boss music is currently active.
     *
     * @return true if boss music is active, false otherwise
     */
    public static boolean isBossMusicActive() {
        return BOSS_KEY.equals(currentTrackKey);
    }

    /**
     * Plays a specific music track by its key.
     *
     * @param key the track key
     * @param looping whether the track should loop
     */
    public static void play(String key, boolean looping) {
        if (!initialized) {
            return;
        }

        Music music = tracks.get(key);
        if (music == null) {
            if (!missingMusicKeysLogged.contains(key)) {
                Gdx.app.error(TAG, "Music key not found: " + key);
                missingMusicKeysLogged.add(key);
            }
            return;
        }

        if (key.equals(currentTrackKey)) {
            music.setLooping(looping);
            applyVolume(music);
            if (!muted && !music.isPlaying()) {
                music.play();
            }
            return;
        }

        stopCurrent();

        currentTrackKey = key;
        music.setLooping(looping);
        applyVolume(music);

        if (!muted) {
            music.play();
        }
    }

    /**
     * Stops the currently playing music.
     */
    public static void stop() {
        stopCurrent();
        currentTrackKey = null;
    }

    /**
     * Sets the volume for all music tracks.
     *
     * @param volume the volume level (0.0 to 1.0)
     */
    public static void setMusicVolume(float volume) {
        musicVolume = MathUtils.clamp(volume, 0f, 1f);
        Music currentMusic = getCurrentMusic();
        if (currentMusic != null) {
            applyVolume(currentMusic);
        }
    }

    /**
     * Gets the current music volume level.
     *
     * @return the volume level
     */
    public static float getMusicVolume() {
        return musicVolume;
    }

    /**
     * Mutes or unmutes the music.
     *
     * @param isMuted true to mute, false to unmute
     */
    public static void setMuted(boolean isMuted) {
        muted = isMuted;

        Music currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            return;
        }

        applyVolume(currentMusic);
        if (muted) {
            currentMusic.pause();
        } else if (currentTrackKey != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    /**
     * Checks if the music is currently muted.
     *
     * @return true if muted, false otherwise
     */
    public static boolean isMuted() {
        return muted;
    }

    /**
     * Disposes of all music tracks to free up memory.
     */
    public static void dispose() {
        stop();

        for (Music music : tracks.values()) {
            if (music != null) {
                music.dispose();
            }
        }

        tracks.clear();
        missingMusicKeysLogged.clear();
        missingMusicFilesLogged.clear();
        currentTrackKey = null;
        initialized = false;
    }

    /**
     * Registers a new music track.
     *
     * @param key the track key
     * @param path the internal path to the music file
     */
    private static void registerMusic(String key, String path) {
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) {
            if (!missingMusicFilesLogged.contains(path)) {
                Gdx.app.error(TAG, "Missing music file: " + path);
                missingMusicFilesLogged.add(path);
            }
            return;
        }

        tracks.put(key, Gdx.audio.newMusic(file));
    }

    /**
     * Gets the currently active Music instance.
     *
     * @return the active Music, or null if none
     */
    private static Music getCurrentMusic() {
        return currentTrackKey == null ? null : tracks.get(currentTrackKey);
    }

    /**
     * Stops the currently active track if it exists.
     */
    private static void stopCurrent() {
        Music currentMusic = getCurrentMusic();
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    /**
     * Applies the current volume/mute settings to a specific music track.
     *
     * @param music the music track to apply settings to
     */
    private static void applyVolume(Music music) {
        music.setVolume(muted ? 0f : musicVolume);
    }
}
