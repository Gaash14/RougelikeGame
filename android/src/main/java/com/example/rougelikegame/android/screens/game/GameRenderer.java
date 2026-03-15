package com.example.rougelikegame.android.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.characters.BossEnemy;
import com.example.rougelikegame.android.models.characters.Enemy;
import com.example.rougelikegame.android.models.characters.GhostEnemy;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.input.GameInputController;
import com.example.rougelikegame.android.models.input.Joystick;
import com.example.rougelikegame.android.models.items.PassiveItem;
import com.example.rougelikegame.android.models.items.passives.BeamItem;
import com.example.rougelikegame.android.models.meta.RunStats;
import com.example.rougelikegame.android.models.world.Obstacle;
import com.example.rougelikegame.android.models.world.Pickup;
import com.example.rougelikegame.android.models.world.ProjectileSystem;
import com.example.rougelikegame.android.models.world.WaveManager;

public class GameRenderer {

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final GameAssets assets;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    public GameRenderer(SpriteBatch batch, BitmapFont font, GameAssets assets) {
        this.batch = batch;
        this.font = font;
        this.assets = assets;
    }

    public void render(
        Player player,
        Array<Enemy> enemies,
        Array<Pickup> pickups,
        Array<Obstacle> obstacles,
        ProjectileSystem projectileSystem,
        WaveManager waveManager,
        Joystick joystick,
        GameInputController inputController,
        Rectangle attackBtnBounds,
        Rectangle resumeBounds,
        Rectangle exitBounds,
        long runSeed,
        boolean dailyChallenge
    ) {
        batch.begin();

        // Background
        batch.draw(assets.backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // World
        player.draw(batch);

        for (Enemy e : enemies) {
            if (!(e instanceof GhostEnemy)) {
                e.draw(batch);
            }
        }

        drawBossHealthBar(enemies, player.debugPixel);

        for (Obstacle o : obstacles) {
            o.draw(batch);
        }

        // Ghosts drawn on top
        for (Enemy e : enemies) {
            if (e instanceof GhostEnemy) {
                e.draw(batch);
            }
        }

        for (Pickup p : pickups) {
            p.draw(batch);
        }

        projectileSystem.drawAll(batch);

        joystick.draw(batch);

        // Attack button
        batch.draw(assets.attackBtnTexture, attackBtnBounds.x, attackBtnBounds.y, attackBtnBounds.width, attackBtnBounds.height);

        drawDebugAttackHitbox(player);
        drawUI(player, waveManager);
        drawBeamChargeBar(player);

        if (inputController.isPaused()) {
            drawPauseOverlay(player, resumeBounds, exitBounds, runSeed, dailyChallenge);
        }

        batch.end();
    }

    private void drawUI(Player player, WaveManager waveManager) {
        final float panelX = 12f;
        final float panelPadding = 14f;
        final float panelTopMargin = 12f;
        final float iconSize = 64f;
        final float rowCount = 5f;
        final float lineHeight = 54f;
        final float textX = panelX + panelPadding + iconSize + 12f;
        final float panelWidth = 360f;
        final float panelHeight = panelPadding * 2f + (rowCount * lineHeight);
        final float panelY = Gdx.graphics.getHeight() - panelTopMargin - panelHeight;
        final float firstRowBaselineY = panelY + panelHeight - panelPadding - 6f;
        final float iconX = panelX + panelPadding;

        batch.setColor(0f, 0f, 0f, 0.55f);
        batch.draw(player.debugPixel, panelX, panelY, panelWidth, panelHeight);
        batch.setColor(Color.WHITE);

        float textY = firstRowBaselineY;
        float iconY = textY - iconSize + 6f;

        batch.draw(assets.hpIcon, iconX, iconY, iconSize, iconSize);
        font.draw(batch, "HP: " + player.health, textX, textY);

        textY -= lineHeight;
        iconY -= lineHeight;
        batch.draw(assets.waveIcon, iconX, iconY, iconSize, iconSize);
        font.draw(batch, "Wave: " + waveManager.getWave(), textX, textY);

        textY -= lineHeight;
        iconY -= lineHeight;
        batch.draw(assets.damageIcon, iconX, iconY, iconSize, iconSize);
        font.draw(batch, "Damage: " + player.getDisplayedDamage(), textX, textY);

        textY -= lineHeight;
        iconY -= lineHeight;
        batch.draw(assets.speedIcon, iconX, iconY, iconSize, iconSize);
        font.draw(batch, "Speed: " + player.speed, textX, textY);

        textY -= lineHeight;
        iconY -= lineHeight;
        batch.draw(assets.coinsIcon, iconX, iconY, iconSize, iconSize);
        font.draw(batch, "Coins: " + player.coins, textX, textY);
    }

    private void drawBossHealthBar(Array<Enemy> enemies, com.badlogic.gdx.graphics.Texture pixel) {
        BossEnemy boss = null;
        for (Enemy e : enemies) {
            if (e instanceof BossEnemy) {
                boss = (BossEnemy) e;
                break;
            }
        }
        if (boss == null) return;

        float barWidth = Gdx.graphics.getWidth() * 0.7f;
        float barHeight = 30f;
        float x = (Gdx.graphics.getWidth() - barWidth) / 2f;
        float y = Gdx.graphics.getHeight() - 60f;

        float healthPercent = (float) boss.getHealth() / boss.getMaxHealth();

        batch.setColor(0.3f, 0f, 0f, 1f);
        batch.draw(pixel, x, y, barWidth, barHeight);
        batch.setColor(1f, 0f, 0f, 1f);
        batch.draw(pixel, x, y, barWidth * healthPercent, barHeight);
        batch.setColor(Color.WHITE);
    }

    private void drawBeamChargeBar(Player player) {
        if (!hasBeamItemEquipped(player) && !player.isChargingBeam()) return;

        float progress = player.getBeamChargeProgress();
        float barWidth = 60f;
        float barHeight = 8f;
        float x = player.x + (player.width - barWidth) / 2f;
        float y = player.y + player.height + 12f;

        batch.setColor(0f, 0f, 0f, 0.7f);
        batch.draw(player.debugPixel, x, y, barWidth, barHeight);
        batch.setColor(0.3f, 0.85f, 1f, 0.95f);
        batch.draw(player.debugPixel, x, y, barWidth * progress, barHeight);
        batch.setColor(Color.WHITE);
    }

    private boolean hasBeamItemEquipped(Player player) {
        for (PassiveItem item : player.getPassiveItems()) {
            if (item.getItemId() == BeamItem.ID) return true;
        }
        return false;
    }

    private void drawPauseOverlay(Player player, Rectangle resumeBounds, Rectangle exitBounds, long runSeed, boolean dailyChallenge) {
        batch.setColor(0, 0, 0, 0.6f);
        batch.draw(player.debugPixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);

        glyphLayout.setText(font, "RESUME");
        font.draw(batch, glyphLayout, resumeBounds.x + (resumeBounds.width - glyphLayout.width) / 2f, resumeBounds.y + (resumeBounds.height + glyphLayout.height) / 2f);

        glyphLayout.setText(font, "EXIT");
        font.draw(batch, glyphLayout, exitBounds.x + (exitBounds.width - glyphLayout.width) / 2f, exitBounds.y + (exitBounds.height + glyphLayout.height) / 2f);

        if (dailyChallenge) {
            font.draw(batch, "DAILY CHALLENGE", 20, 60);
        } else {
            font.draw(batch, "Seed: " + runSeed, 20, 60);
        }
    }

    private void drawDebugAttackHitbox(Player player) {
        if (!player.attacking) return;
        batch.setColor(1, 0, 0, 0.4f);
        batch.draw(player.debugPixel, player.attackHitbox.x, player.attackHitbox.y, player.attackHitbox.width, player.attackHitbox.height);
        batch.setColor(Color.WHITE);
    }
}
