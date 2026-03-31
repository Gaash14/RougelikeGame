package com.example.rougelikegame.android.models.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.example.rougelikegame.android.models.world.Obstacle;
import com.example.rougelikegame.android.models.world.Projectile;
import com.example.rougelikegame.android.screens.menu.MainActivity;

/**
 * This class represents a boss enemy in the game. It features a unique 'Inferno' ability
 * that damages the player if they are outside a safe zone, and spawns reinforcements
 * when reaching half health.
 */
public class BossEnemy extends Enemy {

    private final float shootCooldown = 2f;
    private float shootTimer = 0f;
    private static final int PROJECTILE_DAMAGE = 4;

    private static final float HALF_HEALTH_THRESHOLD = 0.5f;
    private static final float SPECIAL_MIN_COOLDOWN_SECONDS = 30f;
    private static final float SPECIAL_MAX_COOLDOWN_SECONDS = 60f;
    private static final float SPECIAL_DURATION_SECONDS = 10f;
    private static final float SPECIAL_SAFE_ZONE_SIZE = 640f;
    private static final float INFERNO_TICK_INTERVAL = 1f;
    private static final int INFERNO_DAMAGE_PER_TICK = 1;
    private static final float INFERNO_OVERLAY_ALPHA = 0.4f;
    private static final float INFERNO_BORDER_THICKNESS = 8f;

    private boolean reinforcementsSummoned = false;
    private final int maxHealth;
    private final MainActivity game;

    private final Array<Projectile> projectiles;
    private final Rectangle infernoSafeZone = new Rectangle();
    private float infernoCooldownTimer = randomInfernoCooldown();
    private float infernoActiveTimer = 0f;
    private float infernoDamageTimer = 0f;

    /**
     * Constructs a new BossEnemy.
     *
     * @param texture     the boss texture
     * @param startX      starting X position
     * @param startY      starting Y position
     * @param projectiles array of projectiles to add boss bullets to
     * @param health      initial health
     * @param damage      base contact damage
     * @param game        reference to the main activity for summoning reinforcements
     */
    public BossEnemy(Texture texture, float startX, float startY, Array<Projectile> projectiles,
                     int health, int damage, MainActivity game) {
        super(texture, startX, startY, 70f, 256f, 256f, health, damage);

        this.projectiles = projectiles;
        this.isBoss = true;
        this.maxHealth = health;
        this.game = game;
    }

    @Override
    public void update(float delta, float playerX, float playerY) {
        super.update(delta * 0.75f, playerX, playerY);

        updateInferno(delta, playerX, playerY);
        if (!reinforcementsSummoned && health <= maxHealth / 2) {
            reinforcementsSummoned = true;
            game.spawnBossReinforcements();
        }

        shootTimer -= delta;

        if (shootTimer <= 0) {
            shootTimer = shootCooldown;

            float originX = x + width / 2f;
            float originY = y + height / 2f;
            float dirX = playerX - originX;
            float dirY = playerY - originY;
            float spreadAngle = 30f;

            for (float angleOffset : new float[]{-spreadAngle, 0f, spreadAngle}) {
                float rotatedDirX = dirX * MathUtils.cosDeg(angleOffset) - dirY * MathUtils.sinDeg(angleOffset);
                float rotatedDirY = dirX * MathUtils.sinDeg(angleOffset) + dirY * MathUtils.cosDeg(angleOffset);

                projectiles.add(
                    new Projectile(
                        originX,
                        originY,
                        rotatedDirX,
                        rotatedDirY,
                        PROJECTILE_DAMAGE
                    )
                );
            }
        }

        // clamp to screen
        x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth() - width);
        y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - height);
        bounds.setPosition(x, y);
    }

    private void updateInferno(float delta, float playerX, float playerY) {
        if (health > maxHealth * HALF_HEALTH_THRESHOLD) {
            infernoDamageTimer = 0f;
            return;
        }

        if (infernoActiveTimer > 0f) {
            infernoActiveTimer -= delta;
            if (infernoActiveTimer <= 0f) {
                infernoActiveTimer = 0f;
                infernoDamageTimer = 0f;
                infernoCooldownTimer = randomInfernoCooldown();
            }
            return;
        }

        infernoCooldownTimer -= delta;
        if (infernoCooldownTimer > 0f) {
            return;
        }

        infernoActiveTimer = SPECIAL_DURATION_SECONDS;
        infernoCooldownTimer = 0f;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float safeZoneX = MathUtils.clamp(
            playerX + 64f - SPECIAL_SAFE_ZONE_SIZE / 2f,
            0f,
            Math.max(0f, screenWidth - SPECIAL_SAFE_ZONE_SIZE)
        );
        float safeZoneY = MathUtils.clamp(
            playerY + 64f - SPECIAL_SAFE_ZONE_SIZE / 2f,
            0f,
            Math.max(0f, screenHeight - SPECIAL_SAFE_ZONE_SIZE)
        );
        infernoSafeZone.set(safeZoneX, safeZoneY, SPECIAL_SAFE_ZONE_SIZE, SPECIAL_SAFE_ZONE_SIZE);
    }

    private float randomInfernoCooldown() {
        return MathUtils.random(SPECIAL_MIN_COOLDOWN_SECONDS, SPECIAL_MAX_COOLDOWN_SECONDS);
    }

    /**
     * Applies damage to the player if they are outside the inferno safe zone.
     *
     * @param player the player to damage
     * @param delta  time since last update
     * @return the total damage taken this tick
     */
    public int applyInfernoDamage(Player player, float delta) {
        if (!isInfernoActive()) {
            infernoDamageTimer = 0f;
            return 0;
        }

        if (infernoSafeZone.contains(
            player.x + player.width / 2f,
            player.y + player.height / 2f
        )) {
            infernoDamageTimer = 0f;
            return 0;
        }

        infernoDamageTimer += delta;
        int totalDamageTaken = 0;
        while (infernoDamageTimer >= INFERNO_TICK_INTERVAL) {
            infernoDamageTimer -= INFERNO_TICK_INTERVAL;
            totalDamageTaken += player.applyIncomingDamage(INFERNO_DAMAGE_PER_TICK);
        }

        return totalDamageTaken;
    }

    public float getInfernoOverlayAlpha() {
        return INFERNO_OVERLAY_ALPHA;
    }

    public float getInfernoBorderThickness() {
        return INFERNO_BORDER_THICKNESS;
    }

    public boolean isInfernoActive() {
        return infernoActiveTimer > 0f;
    }

    public Rectangle getInfernoSafeZone() {
        return infernoSafeZone;
    }

    public float getInfernoTimeRemaining() {
        return infernoActiveTimer;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public void handleObstacleCollision(Array<Obstacle> obstacles) {
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            if (!bounds.overlaps(obstacle.bounds)) {
                continue;
            }

            obstacle.dispose();
            obstacles.removeIndex(i);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (isEnraged()) {
            float pulse = 0.08f * (1f + MathUtils.sin(shootTimer * 6f));
            batch.setColor(1f, 0.35f + pulse, 0.35f + pulse, 1f);
            TextureRegion frame = getCurrentFrame();
            if (frame != null) {
                drawCurrentFrame(batch, frame);
            } else {
                drawCurrentTexture(batch);
            }
            batch.setColor(Color.WHITE);
            return;
        }

        super.draw(batch);
    }

    private boolean isEnraged() {
        return reinforcementsSummoned || health <= maxHealth / 2;
    }
}
