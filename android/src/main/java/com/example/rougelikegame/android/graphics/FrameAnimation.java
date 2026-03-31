package com.example.rougelikegame.android.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * FrameAnimation handles the loading and playback of frame-based animations for game entities.
 * It supports fallback textures if specific animation frames are missing.
 */
public class FrameAnimation {

    private static final int DEFAULT_FRAME_COUNT = 3;
    private static final float DEFAULT_FRAME_DURATION_SECONDS = 0.12f;
    private static final String DEFAULT_FALLBACK_TEXTURE_PATH = "skins/player_default.png";

    private final Texture[] textures;
    private final TextureRegion idleFrame;
    private final Animation<TextureRegion> loopAnimation;

    /**
     * Constructs a FrameAnimation with default settings.
     *
     * @param baseTexturePath the base path to the texture files
     */
    public FrameAnimation(String baseTexturePath) {
        this(baseTexturePath, DEFAULT_FRAME_COUNT, DEFAULT_FRAME_DURATION_SECONDS, DEFAULT_FALLBACK_TEXTURE_PATH);
    }

    /**
     * Constructs a FrameAnimation with specified settings.
     *
     * @param baseTexturePath the base path to the texture files
     * @param frameCount the number of frames in the animation
     * @param frameDurationSeconds the duration of each frame in seconds
     * @param fallbackTexturePath the path to a fallback texture if frames are missing
     */
    public FrameAnimation(
        String baseTexturePath,
        int frameCount,
        float frameDurationSeconds,
        String fallbackTexturePath
    ) {
        this.textures = new Texture[frameCount];
        Array<TextureRegion> frames = new Array<>(frameCount);

        for (int i = 0; i < frameCount; i++) {
            String texturePath = resolveTexturePath(baseTexturePath, fallbackTexturePath, i);
            Texture texture = new Texture(texturePath);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            textures[i] = texture;
            frames.add(new TextureRegion(texture));
        }

        idleFrame = frames.first();
        loopAnimation = new Animation<>(frameDurationSeconds, frames, Animation.PlayMode.LOOP);
    }

    /**
     * Gets the appropriate texture region for the current state.
     *
     * @param stateTime the elapsed time since the animation started
     * @param isMoving whether the entity is currently moving
     * @return the TextureRegion for the current frame
     */
    public TextureRegion getFrame(float stateTime, boolean isMoving) {
        if (!isMoving) {
            return idleFrame;
        }
        return loopAnimation.getKeyFrame(stateTime, true);
    }

    /**
     * Disposes of all textures used by this animation to free up memory.
     */
    public void dispose() {
        for (Texture texture : textures) {
            if (texture != null) {
                texture.dispose();
            }
        }
    }

    /**
     * Resolves the actual file path for a specific animation frame.
     *
     * @param baseTexturePath the base texture path
     * @param fallbackTexturePath the fallback texture path
     * @param frameIndex the index of the frame
     * @return the resolved texture path
     */
    private String resolveTexturePath(String baseTexturePath, String fallbackTexturePath, int frameIndex) {
        String frameTexturePath = buildFramePath(baseTexturePath, frameIndex);
        if (fileExists(frameTexturePath)) {
            return frameTexturePath;
        }
        if (fileExists(baseTexturePath)) {
            Gdx.app.error("FrameAnimation", "Missing animation frame, falling back to base texture: " + frameTexturePath);
            return baseTexturePath;
        }
        Gdx.app.error("FrameAnimation", "Missing base texture, falling back to default texture: " + baseTexturePath);
        return fallbackTexturePath;
    }

    /**
     * Checks if a file exists in the internal assets.
     *
     * @param texturePath the path to check
     * @return true if the file exists, false otherwise
     */
    private boolean fileExists(String texturePath) {
        FileHandle fileHandle = Gdx.files.internal(texturePath);
        return fileHandle.exists();
    }

    /**
     * Builds the path for a specific frame by inserting the index before the file extension.
     *
     * @param baseTexturePath the base texture path
     * @param frameIndex the index of the frame
     * @return the constructed frame path
     */
    private String buildFramePath(String baseTexturePath, int frameIndex) {
        int extensionIndex = baseTexturePath.lastIndexOf('.');
        if (extensionIndex < 0) {
            return baseTexturePath + "_" + frameIndex;
        }
        return baseTexturePath.substring(0, extensionIndex)
            + "_" + frameIndex
            + baseTexturePath.substring(extensionIndex);
    }
}
