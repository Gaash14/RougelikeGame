package com.example.rougelikegame.android.utils;

import com.example.rougelikegame.android.models.meta.Skin;

import java.util.ArrayList;
import java.util.List;

public class SkinRegistry {

    public static List<Skin> getAllSkins() {
        List<Skin> skins = new ArrayList<>();

        // Default
        skins.add(new Skin("default", "Default",
            0, Skin.UnlockType.DEFAULT));

        // Shop skins
        skins.add(new Skin("red", "Red",
            5, Skin.UnlockType.SHOP));
        skins.add(new Skin("red_knight", "Red Knight",
            50, Skin.UnlockType.SHOP));
        skins.add(new Skin("shadow", "Shadow",
            100, Skin.UnlockType.SHOP));
        skins.add(new Skin("angel", "Angel",
            1000, Skin.UnlockType.SHOP));

        // Achievement skins
        skins.add(new Skin(
            "skin_wave_5",
            "Ruby Knight",
            Skin.UnlockType.ACHIEVEMENT,
            "wave_5"
        ));

        skins.add(new Skin(
            "skin_wave_10",
            "Ice Knight",
            Skin.UnlockType.ACHIEVEMENT,
            "wave_10"
        ));

        skins.add(new Skin(
            "skin_100_kills",
            "Slayer",
            Skin.UnlockType.ACHIEVEMENT,
            "kills_100"
        ));

        skins.add(new Skin(
            "skin_25_coins",
            "Collector",
            Skin.UnlockType.ACHIEVEMENT,
            "coins_25"
        ));

        skins.add(new Skin(
            "skin_first_win",
            "Champion",
            Skin.UnlockType.ACHIEVEMENT,
            "first_win"
        ));

        return skins;
    }
}
