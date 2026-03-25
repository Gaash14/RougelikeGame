package com.example.rougelikegame.android.graphics;

import java.util.HashMap;
import java.util.Map;

public class FrameAnimationManager {

    private final Map<String, FrameAnimation> animations = new HashMap<>();

    public FrameAnimation getAnimation(String baseTexturePath) {
        return animations.computeIfAbsent(baseTexturePath, FrameAnimation::new);
    }

    public void dispose() {
        for (FrameAnimation animation : animations.values()) {
            if (animation != null) {
                animation.dispose();
            }
        }
        animations.clear();
    }
}
