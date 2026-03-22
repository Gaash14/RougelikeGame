package com.example.rougelikegame.android.graphics;

import com.example.rougelikegame.android.models.meta.Skin;

import java.util.HashMap;
import java.util.Map;

public class SkinAnimationManager {

    private final Map<String, SkinAnimation> animations = new HashMap<>();

    public SkinAnimation getAnimation(Skin skin) {
        return animations.computeIfAbsent(
            skin.getId(),
            ignored -> new SkinAnimation(skin)); // create new animation if missing
    }

    public void dispose() {
        for (SkinAnimation animation : animations.values()) {
            if (animation != null) {
                animation.dispose();
            }
        }
        animations.clear(); //clear the map
    }
}
