package com.example.rougelikegame.android.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.meta.Skin;

public class SkinAnimation {

    private static final int FRAME_COUNT = 3;
    private static final float WALK_FRAME_DURATION_SECONDS = 0.12f;

    private final Texture[] textures;
    private final TextureRegion idleFrame;
    private final Animation<TextureRegion> walkAnimation;

    public SkinAnimation(Skin skin) {
        this.textures = new Texture[FRAME_COUNT];
        Array<TextureRegion> frames = new Array<>(FRAME_COUNT);

        String baseTexturePath = skin.getTexturePath();
        for (int i = 0; i < FRAME_COUNT; i++) {
            Texture texture = new Texture(buildFramePath(baseTexturePath, i));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            textures[i] = texture;
            frames.add(new TextureRegion(texture));
        }

        idleFrame = frames.first();
        walkAnimation = new Animation<>(WALK_FRAME_DURATION_SECONDS, frames, Animation.PlayMode.LOOP);
    }

    public TextureRegion getFrame(float stateTime, boolean isMoving) {
        if (!isMoving) {
            return idleFrame;
        }
        return walkAnimation.getKeyFrame(stateTime, true);
    }

    public void dispose() {
        for (Texture texture : textures) {
            if (texture != null) {
                texture.dispose();
            }
        }
    }

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
