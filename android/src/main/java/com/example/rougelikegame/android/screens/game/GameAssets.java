package com.example.rougelikegame.android.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.example.rougelikegame.android.models.meta.Skin;
import com.example.rougelikegame.android.utils.SkinRegistry;

import java.util.HashMap;
import java.util.Map;

public class GameAssets implements Disposable {

    public final Texture enemyTexture;
    public final Texture ghostTexture;
    public final Texture bossTexture;
    public final Texture playerTexture;
    public final Texture fallbackItemTexture;
    public final Texture backgroundTexture;
    public final Texture hpIcon;
    public final Texture waveIcon;
    public final Texture damageIcon;
    public final Texture speedIcon;
    public final Texture coinsIcon;
    public final Texture attackBtnTexture;

    private final Map<String, Texture> itemIconCache = new HashMap<>();

    public GameAssets(String skinId) {
        enemyTexture = new Texture("enemies/enemy.png");
        ghostTexture = new Texture("enemies/ghost_enemy.png");
        bossTexture = new Texture("enemies/boss.png");
        
        Skin skin = SkinRegistry.getSkinById(skinId);
        playerTexture = new Texture(skin.getTexturePath());

        fallbackItemTexture = new Texture("items/error.png");
        backgroundTexture = new Texture("backgrounds/abyss_cave_bg.png");

        hpIcon = new Texture("ui/hp_icon.png");
        waveIcon = new Texture("ui/wave_icon.png");
        damageIcon = new Texture("ui/damage_icon.png");
        speedIcon = new Texture("ui/speed_icon.png");
        coinsIcon = new Texture("ui/coin_icon.png");
        
        attackBtnTexture = new Texture("inputs/attack_icon.png");

        setupFilters(hpIcon, waveIcon, damageIcon, speedIcon, coinsIcon);
    }

    private void setupFilters(Texture... textures) {
        for (Texture tex : textures) {
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
    }

    public Texture getItemIconTexture(String iconPath) {
        if (iconPath == null || iconPath.trim().isEmpty()) {
            return fallbackItemTexture;
        }

        if (itemIconCache.containsKey(iconPath)) {
            return itemIconCache.get(iconPath);
        }

        Texture texture;
        try {
            texture = new Texture(iconPath);
        } catch (Exception ex) {
            Gdx.app.error("GameAssets", "Failed to load icon: " + iconPath + ", using fallback", ex);
            texture = fallbackItemTexture;
        }

        itemIconCache.put(iconPath, texture);
        return texture;
    }

    @Override
    public void dispose() {
        enemyTexture.dispose();
        ghostTexture.dispose();
        bossTexture.dispose();
        playerTexture.dispose();
        fallbackItemTexture.dispose();
        backgroundTexture.dispose();
        hpIcon.dispose();
        waveIcon.dispose();
        damageIcon.dispose();
        speedIcon.dispose();
        coinsIcon.dispose();
        attackBtnTexture.dispose();

        for (Texture tex : itemIconCache.values()) {
            if (tex != null && tex != fallbackItemTexture) {
                tex.dispose();
            }
        }
        itemIconCache.clear();
    }
}
