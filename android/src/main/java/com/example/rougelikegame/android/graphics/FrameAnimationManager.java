package com.example.rougelikegame.android.graphics;

import com.badlogic.gdx.assets.AssetManager;
import java.util.HashMap;
import java.util.Map;

/**
 * FrameAnimationManager serves as a central registry for FrameAnimation instances.
 * it ensures that animations are cached and reused to optimize memory and performance.
 */
public class FrameAnimationManager {

    private final Map<String, FrameAnimation> animations = new HashMap<>();
    private final AssetManager assetManager;

    public FrameAnimationManager() {
        this.assetManager = new AssetManager();
    }

    /**
     * Retrieves an existing animation or creates a new one if it doesn't exist.
     *
     * @param baseTexturePath the base path to the animation textures
     * @return the FrameAnimation instance
     */
    public FrameAnimation getAnimation(String baseTexturePath) {
        return animations.computeIfAbsent(baseTexturePath, path -> new FrameAnimation(path, assetManager));
    }

    /**
     * Disposes of all managed animations and clears the internal map.
     */
    public void dispose() {
        for (FrameAnimation animation : animations.values()) {
            if (animation != null) {
                animation.dispose(assetManager);
            }
        }
        animations.clear();
        assetManager.dispose();
    }
}