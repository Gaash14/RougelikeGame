package com.example.rougelikegame.android.screens.menu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.managers.MusicManager;
import com.example.rougelikegame.android.managers.SoundManager;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.screens.launcher.AndroidLauncher;

/**
 * Activity that allows the player to configure their game session.
 * Options include selecting a player class (Melee or Ranged), choosing a difficulty level,
 * enabling the Daily Challenge mode, and adjusting sound/music settings.
 */
public class ChooseDifficultyActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_SFX_VOLUME = "sfx_volume";
    private static final String KEY_SFX_MUTED = "sfx_muted";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_MUSIC_MUTED = "music_muted";

    private RadioGroup classGroup;
    private RadioButton classMelee;
    private RadioButton classRanged;

    private Button startGameButton;
    private RadioGroup difficultyGroup;

    private Switch dailyChallengeSwitch;
    private boolean dailyChallenge = false;
    private TextView txtDailyReset;
    private final Handler handler = new Handler();
    private EditText seedInput;
    private Button openSoundSettingsButton;
    private SharedPreferences settingsPrefs;

    private Player.Difficulty selectedDifficulty = Player.Difficulty.NORMAL;
    private Player.PlayerClass selectedClass = Player.PlayerClass.MELEE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_difficulty);

        classGroup = findViewById(R.id.classGroup);
        classMelee = findViewById(R.id.classMelee);
        classRanged = findViewById(R.id.classRanged);

        txtDailyReset = findViewById(R.id.txtDailyReset);
        seedInput = findViewById(R.id.seedInput);

        openSoundSettingsButton = findViewById(R.id.openSoundSettingsButton);
        settingsPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ensureAudioLoaded();
        initializeAudioSettings();

        openSoundSettingsButton.setOnClickListener(v -> showSoundSettingsDialog());

        // Toggle between MELEE / RANGED
        classGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.classRanged) {
                selectedClass = Player.PlayerClass.RANGED;
            } else {
                selectedClass = Player.PlayerClass.MELEE;
            }
        });

        difficultyGroup = findViewById(R.id.difficultyGroup);
        difficultyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.diffEasy) {
                selectedDifficulty = Player.Difficulty.EASY;
            } else if (checkedId == R.id.diffNormal) {
                selectedDifficulty = Player.Difficulty.NORMAL;
            } else if (checkedId == R.id.diffHard) {
                selectedDifficulty = Player.Difficulty.HARD;
            }
        });

        dailyChallengeSwitch = findViewById(R.id.dailyChallengeSwitch);

        dailyChallengeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dailyChallenge = isChecked;

            if (isChecked) {
                // Lock difficulty to NORMAL
                selectedDifficulty = Player.Difficulty.NORMAL;
                difficultyGroup.check(R.id.diffNormal);

                for (int i = 0; i < difficultyGroup.getChildCount(); i++) {
                    difficultyGroup.getChildAt(i).setEnabled(false);
                }

                seedInput.setVisibility(View.GONE);
                seedInput.setText(""); // clear any custom seed
                txtDailyReset.setVisibility(View.VISIBLE);

                startDailyCountdown();

            } else {
                // Re-enable difficulty
                for (int i = 0; i < difficultyGroup.getChildCount(); i++) {
                    difficultyGroup.getChildAt(i).setEnabled(true);
                }

                txtDailyReset.setVisibility(View.GONE);
                handler.removeCallbacksAndMessages(null);

                seedInput.setVisibility(View.VISIBLE);
            }
        });

        startGameButton = findViewById(R.id.startGameButton);
        // Start game
        startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseDifficultyActivity.this, AndroidLauncher.class);
            intent.putExtra("PLAYER_CLASS", selectedClass.name());
            intent.putExtra("DIFFICULTY", selectedDifficulty.name());
            intent.putExtra("DAILY_CHALLENGE", dailyChallenge);

            String seedText = seedInput.getText().toString().trim();
            if (!seedText.isEmpty()) {
                try {
                    long customSeed = Long.parseLong(seedText);
                    intent.putExtra("CUSTOM_SEED", customSeed);
                } catch (NumberFormatException ignored) {
                    // invalid seed → ignore, fallback handled later
                }
            }

            startActivity(intent);
        });
    }

    /**
     * Ensures that audio managers are loaded before usage.
     */
    private void ensureAudioLoaded() {
        try {
            SoundManager.load();
        } catch (RuntimeException ignored) {
            // Safe fallback for cases where LibGDX audio isn't ready yet.
        }

        try {
            MusicManager.load();
        } catch (RuntimeException ignored) {
            // Safe fallback for cases where LibGDX audio isn't ready yet.
        }
    }

    /**
     * Initializes audio settings from SharedPreferences or defaults.
     */
    private void initializeAudioSettings() {
        int savedVolume = settingsPrefs.getInt(KEY_SFX_VOLUME, Math.round(SoundManager.getSfxVolume() * 100f));
        boolean savedMuted = settingsPrefs.getBoolean(KEY_SFX_MUTED, SoundManager.isMuted());
        int savedMusicVolume = settingsPrefs.getInt(KEY_MUSIC_VOLUME, Math.round(MusicManager.getMusicVolume() * 100f));
        boolean savedMusicMuted = settingsPrefs.getBoolean(KEY_MUSIC_MUTED, MusicManager.isMuted());

        int clampedVolume = Math.max(0, Math.min(savedVolume, 100));
        int clampedMusicVolume = Math.max(0, Math.min(savedMusicVolume, 100));

        applyAudioSettings(clampedVolume, savedMuted, clampedMusicVolume, savedMusicMuted);
    }

    /**
     * Displays a dialog for adjusting SFX and Music settings.
     */
    private void showSoundSettingsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sound_settings, null);

        TextView sfxVolumeLabel = dialogView.findViewById(R.id.sfxVolumeLabel);
        SeekBar sfxVolumeSeekBar = dialogView.findViewById(R.id.sfxVolumeSeekBar);
        Switch sfxMuteSwitch = dialogView.findViewById(R.id.sfxMuteSwitch);
        TextView musicVolumeLabel = dialogView.findViewById(R.id.musicVolumeLabel);
        SeekBar musicVolumeSeekBar = dialogView.findViewById(R.id.musicVolumeSeekBar);
        Switch musicMuteSwitch = dialogView.findViewById(R.id.musicMuteSwitch);

        int savedVolume = settingsPrefs.getInt(KEY_SFX_VOLUME, Math.round(SoundManager.getSfxVolume() * 100f));
        boolean savedMuted = settingsPrefs.getBoolean(KEY_SFX_MUTED, SoundManager.isMuted());
        int savedMusicVolume = settingsPrefs.getInt(KEY_MUSIC_VOLUME, Math.round(MusicManager.getMusicVolume() * 100f));
        boolean savedMusicMuted = settingsPrefs.getBoolean(KEY_MUSIC_MUTED, MusicManager.isMuted());
        int clampedVolume = Math.max(0, Math.min(savedVolume, 100));
        int clampedMusicVolume = Math.max(0, Math.min(savedMusicVolume, 100));

        sfxVolumeSeekBar.setMax(100);
        sfxVolumeSeekBar.setProgress(clampedVolume);
        updateSfxVolumeLabel(sfxVolumeLabel, clampedVolume);

        sfxMuteSwitch.setChecked(savedMuted);
        sfxVolumeSeekBar.setEnabled(!savedMuted);

        musicVolumeSeekBar.setMax(100);
        musicVolumeSeekBar.setProgress(clampedMusicVolume);
        updateMusicVolumeLabel(musicVolumeLabel, clampedMusicVolume);

        musicMuteSwitch.setChecked(savedMusicMuted);
        musicVolumeSeekBar.setEnabled(!savedMusicMuted);

        sfxVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSfxVolumeLabel(sfxVolumeLabel, progress);
                SoundManager.setSfxVolume(progress / 100f);
                persistAudioSettings(progress, sfxMuteSwitch.isChecked(),
                        musicVolumeSeekBar.getProgress(), musicMuteSwitch.isChecked());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // no-op
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // no-op
            }
        });

        sfxMuteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SoundManager.setMuted(isChecked);
            sfxVolumeSeekBar.setEnabled(!isChecked);
            persistAudioSettings(sfxVolumeSeekBar.getProgress(), isChecked,
                    musicVolumeSeekBar.getProgress(), musicMuteSwitch.isChecked());
        });

        musicVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateMusicVolumeLabel(musicVolumeLabel, progress);
                MusicManager.setMusicVolume(progress / 100f);
                persistAudioSettings(sfxVolumeSeekBar.getProgress(), sfxMuteSwitch.isChecked(),
                        progress, musicMuteSwitch.isChecked());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // no-op
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // no-op
            }
        });

        musicMuteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MusicManager.setMuted(isChecked);
            musicVolumeSeekBar.setEnabled(!isChecked);
            persistAudioSettings(sfxVolumeSeekBar.getProgress(), sfxMuteSwitch.isChecked(),
                    musicVolumeSeekBar.getProgress(), isChecked);
        });

        AlertDialog soundDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .create();

        soundDialog.show();
    }

    /**
     * Applies the given audio settings to the managers.
     *
     * @param sfxVolumePercent   SFX volume percentage (0-100)
     * @param sfxMuted           Whether SFX is muted
     * @param musicVolumePercent Music volume percentage (0-100)
     * @param musicMuted         Whether music is muted
     */
    private void applyAudioSettings(int sfxVolumePercent, boolean sfxMuted, int musicVolumePercent, boolean musicMuted) {
        SoundManager.setSfxVolume(sfxVolumePercent / 100f);
        SoundManager.setMuted(sfxMuted);

        MusicManager.setMusicVolume(musicVolumePercent / 100f);
        MusicManager.setMuted(musicMuted);
    }

    /**
     * Persists audio settings to SharedPreferences.
     *
     * @param sfxVolumePercent   SFX volume percentage
     * @param sfxMuted           Whether SFX is muted
     * @param musicVolumePercent Music volume percentage
     * @param musicMuted         Whether music is muted
     */
    private void persistAudioSettings(int sfxVolumePercent, boolean sfxMuted, int musicVolumePercent, boolean musicMuted) {
        settingsPrefs.edit()
                .putInt(KEY_SFX_VOLUME, sfxVolumePercent)
                .putBoolean(KEY_SFX_MUTED, sfxMuted)
                .putInt(KEY_MUSIC_VOLUME, musicVolumePercent)
                .putBoolean(KEY_MUSIC_MUTED, musicMuted)
                .apply();
    }

    /**
     * Updates the music volume label text.
     */
    private void updateMusicVolumeLabel(TextView volumeLabel, int volumePercent) {
        volumeLabel.setText("Music Volume: " + volumePercent + "%");
    }

    /**
     * Updates the SFX volume label text.
     */
    private void updateSfxVolumeLabel(TextView volumeLabel, int volumePercent) {
        volumeLabel.setText("SFX Volume: " + volumePercent + "%");
    }

    /**
     * Starts the countdown timer for the daily challenge reset.
     */
    private void startDailyCountdown() {
        txtDailyReset.setVisibility(View.VISIBLE);

        handler.post(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();

                java.time.LocalDateTime tomorrow =
                        java.time.LocalDate.now().plusDays(1).atStartOfDay();

                long millisUntilReset =
                        tomorrow.atZone(java.time.ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                                - now;

                long seconds = millisUntilReset / 1000;
                long h = seconds / 3600;
                long m = (seconds % 3600) / 60;
                long s = seconds % 60;

                txtDailyReset.setText(
                        String.format(
                                "⏱ Daily resets in %02d:%02d:%02d", h, m, s
                        )
                );

                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}

