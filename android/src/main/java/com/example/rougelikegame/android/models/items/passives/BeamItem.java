package com.example.rougelikegame.android.models.items.passives;

import com.example.rougelikegame.android.models.items.PassiveItem;

public class BeamItem implements PassiveItem {

    public static final int ID = 5;

    @Override
    public int getItemId() {
        return ID;
    }

    @Override
    public String getKey() {
        return "beam";
    }

    @Override
    public String getDisplayName() {
        return "Light Beam";
    }

    @Override
    public String getIconPath() {
        return "items/beam.png";
    }
}
