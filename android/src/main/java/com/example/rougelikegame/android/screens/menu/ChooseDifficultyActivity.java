package com.example.rougelikegame.android.screens.menu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rougelikegame.R;
import com.example.rougelikegame.android.managers.SoundManager;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.screens.launcher.AndroidLauncher;

public class ChooseDifficultyActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_SFX_VOLUME = "sfx_volume";
    private static final String KEY_SFX_MUTED = "sfx_muted";

    private Switch classSwitch;
    private TextView classLabel;
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

        classSwitch = findViewById(R.id.classSwitch);
        classLabel = findViewById(R.id.classLabel);

        txtDailyReset = findViewById(R.id.txtDailyReset);
        seedInput = findViewById(R.id.seedInput);

        openSoundSettingsButton = findViewById(R.id.openSoundSettingsButton);
        settingsPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ensureSoundLoaded();
        initializeSoundSettings();

        openSoundSettingsButton.setOnClickListener(v -> showSoundSettingsDialog());

        // Toggle between MELEE / RANGED
        classSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedClass = Player.PlayerClass.RANGED;
                classLabel.setText("Class: RANGED");
            } else {
                selectedClass = Player.PlayerClass.MELEE;
                classLabel.setText("Class: MELEE");
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

    private void ensureSoundLoaded() {
        try {
            SoundManager.load();
        } catch (RuntimeException ignored) {
            // Safe fallback for cases where LibGDX audio isn't ready yet.
        }
    }

    private void initializeSoundSettings() {
        int savedVolume = settingsPrefs.getInt(KEY_SFX_VOLUME, Math.round(SoundManager.getSfxVolume() * 100f));
        boolean savedMuted = settingsPrefs.getBoolean(KEY_SFX_MUTED, SoundManager.isMuted());

        int clampedVolume = Math.max(0, Math.min(savedVolume, 100));

        applySoundSettings(clampedVolume, savedMuted);
    }

    private void showSoundSettingsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sound_settings, null);

        TextView volumeLabel = dialogView.findViewById(R.id.sfxVolumeLabel);
        SeekBar volumeSeekBar = dialogView.findViewById(R.id.sfxVolumeSeekBar);
        Switch muteSwitch = dialogView.findViewById(R.id.sfxMuteSwitch);

        int savedVolume = settingsPrefs.getInt(KEY_SFX_VOLUME, Math.round(SoundManager.getSfxVolume() * 100f));
        boolean savedMuted = settingsPrefs.getBoolean(KEY_SFX_MUTED, SoundManager.isMuted());
        int clampedVolume = Math.max(0, Math.min(savedVolume, 100));

        volumeSeekBar.setMax(100);
        volumeSeekBar.setProgress(clampedVolume);
        updateSfxVolumeLabel(volumeLabel, clampedVolume);

        muteSwitch.setChecked(savedMuted);
        volumeSeekBar.setEnabled(!savedMuted);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSfxVolumeLabel(volumeLabel, progress);
                SoundManager.setSfxVolume(progress / 100f);
                persistSoundSettings(progress, muteSwitch.isChecked());
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

        muteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SoundManager.setMuted(isChecked);
            volumeSeekBar.setEnabled(!isChecked);
            persistSoundSettings(volumeSeekBar.getProgress(), isChecked);
        });

        AlertDialog soundDialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create();

        soundDialog.show();
    }

    private void applySoundSettings(int volumePercent, boolean muted) {
        SoundManager.setSfxVolume(volumePercent / 100f);
        SoundManager.setMuted(muted);
    }

    private void persistSoundSettings(int volumePercent, boolean muted) {
        settingsPrefs.edit()
            .putInt(KEY_SFX_VOLUME, volumePercent)
            .putBoolean(KEY_SFX_MUTED, muted)
            .apply();
    }

    private void updateSfxVolumeLabel(TextView volumeLabel, int volumePercent) {
        volumeLabel.setText("SFX Volume: " + volumePercent + "%");
    }


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
