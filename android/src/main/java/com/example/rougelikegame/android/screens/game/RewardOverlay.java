package com.example.rougelikegame.android.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.rougelikegame.android.models.characters.Player;
import com.example.rougelikegame.android.models.items.ItemRegistry;
import com.example.rougelikegame.android.models.items.ItemTier;
import com.example.rougelikegame.android.models.items.PassiveItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RewardOverlay implements Disposable {

    public interface RewardCallback {
        void onRewardItemSelected(int itemId);
    }

    private static final int REWARD_REROLL_COST = 20;
    private static final int MAX_REWARD_REROLLS = 3;

    private final Player player;
    private final Random itemRnd;
    private final BitmapFont font;
    private final GameAssets assets;
    private final RewardCallback callback;

    private Stage stage;
    private RewardOption[] currentRewardOptions;
    private int rewardRerollsUsed = 0;
    private String statusMessage = "";
    private int rewardWave = -1;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private RewardRerollButtonActor rerollButton;

    public RewardOverlay(Player player, Random itemRnd, BitmapFont font, GameAssets assets, RewardCallback callback) {
        this.player = player;
        this.itemRnd = itemRnd;
        this.font = font;
        this.assets = assets;
        this.callback = callback;
    }

    public void show(int waveNumber) {
        rewardWave = waveNumber;
        rewardRerollsUsed = 0;
        statusMessage = "";
        
        if (stage != null) stage.dispose();
        stage = new Stage(new ScreenViewport());

        currentRewardOptions = pickRewardItemOptions();
        setupStage();

        Gdx.input.setInputProcessor(stage);
    }

    private void setupStage() {
        stage.clear();
        
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float cardW = Math.min(420f, screenW * 0.4f);
        float cardH = 180f;
        float gap = 80f;
        float totalW = (cardW * 2f) + gap;
        float startX = (screenW - totalW) / 2f;
        float y = (screenH - cardH) / 2f - 20f;

        RewardCardActor left = new RewardCardActor(0);
        left.setBounds(startX, y, cardW, cardH);
        left.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.onRewardItemSelected(currentRewardOptions[0].itemId);
            }
        });

        RewardCardActor right = new RewardCardActor(1);
        right.setBounds(startX + cardW + gap, y, cardW, cardH);
        right.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callback.onRewardItemSelected(currentRewardOptions[1].itemId);
            }
        });

        stage.addActor(left);
        stage.addActor(right);

        rerollButton = new RewardRerollButtonActor();
        rerollButton.setSize(320f, 72f);
        rerollButton.setPosition((screenW - rerollButton.getWidth()) / 2f, y - 100f);
        rerollButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!rerollButton.isDisabled()) {
                    onRewardRerollRequested();
                }
            }
        });
        updateRerollButtonState();
        stage.addActor(rerollButton);
    }

    private void onRewardRerollRequested() {
        if (rewardRerollsUsed >= MAX_REWARD_REROLLS) {
            statusMessage = "No rerolls left";
            updateRerollButtonState();
            return;
        }

        if (!player.spendCoins(REWARD_REROLL_COST)) {
            statusMessage = "Not enough coins";
            updateRerollButtonState();
            return;
        }

        rewardRerollsUsed++;
        statusMessage = "";
        currentRewardOptions = pickRewardItemOptions();
        updateRerollButtonState();
    }

    private void updateRerollButtonState() {
        if (rerollButton == null) return;

        boolean hasRerollsLeft = rewardRerollsUsed < MAX_REWARD_REROLLS;
        boolean canAfford = player.coins >= REWARD_REROLL_COST;
        rerollButton.setDisabled(!(hasRerollsLeft && canAfford));

        if (!hasRerollsLeft) {
            rerollButton.setLabel("Reroll (max used)");
        } else {
            rerollButton.setLabel("Reroll (20 coins)");
        }
    }

    private RewardOption[] pickRewardItemOptions() {
        Set<Integer> allIdsSet = ItemRegistry.getAllItemIds();
        List<Integer> allIds = new ArrayList<>(allIdsSet);
        List<Integer> candidateIds = new ArrayList<>();

        for (Integer id : allIds) {
            if (!player.hasItem(id)) {
                candidateIds.add(id);
            }
        }

        boolean ownsAllItems = candidateIds.isEmpty();
        if (ownsAllItems) {
            candidateIds.addAll(allIds);
        }

        int[] optionIds = new int[2];
        Set<Integer> pickedIds = new HashSet<>();
        for (int i = 0; i < optionIds.length; i++) {
            int pickedId = pickItemIdByTierWeight(candidateIds, pickedIds, ownsAllItems);
            optionIds[i] = pickedId;
            if (!ownsAllItems) {
                pickedIds.add(pickedId);
            }
        }

        RewardOption[] options = new RewardOption[2];
        for (int i = 0; i < 2; i++) {
            PassiveItem item = ItemRegistry.create(optionIds[i]);
            options[i] = new RewardOption(
                optionIds[i],
                item.getDisplayName(),
                item.getTier(),
                assets.getItemIconTexture(item.getIconPath())
            );
        }

        return options;
    }

    private int pickItemIdByTierWeight(List<Integer> candidateIds, Set<Integer> blockedIds, boolean allowBlocked) {
        boolean equalItemWeights = player.hasEqualItemWeights();
        int totalWeight = 0;
        for (int id : candidateIds) {
            if (!allowBlocked && blockedIds.contains(id)) {
                continue;
            }
            totalWeight += getItemSelectionWeight(id, equalItemWeights);
        }

        if (totalWeight <= 0) {
            return candidateIds.get(itemRnd.nextInt(candidateIds.size()));
        }

        int roll = itemRnd.nextInt(totalWeight);
        for (int id : candidateIds) {
            if (!allowBlocked && blockedIds.contains(id)) {
                continue;
            }

            roll -= getItemSelectionWeight(id, equalItemWeights);
            if (roll < 0) {
                return id;
            }
        }

        return candidateIds.get(candidateIds.size() - 1);
    }

    private int getItemSelectionWeight(int itemId, boolean equalItemWeights) {
        if (equalItemWeights) return 1;
        return ItemRegistry.create(itemId).getTier().getWeight();
    }

    public void update(float delta) {
        if (stage != null) stage.act(delta);
    }

    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        batch.begin();
        batch.setColor(0, 0, 0, 0.7f);
        batch.draw(player.debugPixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);

        glyphLayout.setText(font, "Choose a Passive Item");
        font.draw(batch, glyphLayout, (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, Gdx.graphics.getHeight() * 0.72f);

        glyphLayout.setText(font, "Wave " + rewardWave + " Reward");
        font.draw(batch, glyphLayout, (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, Gdx.graphics.getHeight() * 0.66f);

        glyphLayout.setText(font, "Rerolls: " + rewardRerollsUsed + "/" + MAX_REWARD_REROLLS);
        font.draw(batch, glyphLayout, (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, Gdx.graphics.getHeight() * 0.61f);

        if (!statusMessage.isEmpty()) {
            glyphLayout.setText(font, statusMessage);
            font.draw(batch, glyphLayout, (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, Gdx.graphics.getHeight() * 0.25f);
        }

        batch.end();
        if (stage != null) stage.draw();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }

    private static class RewardOption {
        private final int itemId;
        private final String displayName;
        private final ItemTier tier;
        private final Texture iconTexture;

        RewardOption(int itemId, String displayName, ItemTier tier, Texture iconTexture) {
            this.itemId = itemId;
            this.displayName = displayName;
            this.tier = tier;
            this.iconTexture = iconTexture;
        }
    }

    private class RewardCardActor extends Actor {
        private final int optionIndex;

        RewardCardActor(int optionIndex) {
            this.optionIndex = optionIndex;
        }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch actorBatch, float parentAlpha) {
            if (currentRewardOptions == null || optionIndex >= currentRewardOptions.length) return;
            RewardOption option = currentRewardOptions[optionIndex];

            actorBatch.setColor(0.2f, 0.2f, 0.2f, 1f);
            actorBatch.draw(player.debugPixel, getX(), getY(), getWidth(), getHeight());
            actorBatch.setColor(1f, 1f, 1f, 1f);

            float iconSize = Math.min(getWidth() * 0.4f, getHeight() * 0.55f);
            float iconX = getX() + (getWidth() - iconSize) / 2f;
            float iconY = getY() + getHeight() - iconSize - 24f;
            actorBatch.draw(option.iconTexture, iconX, iconY, iconSize, iconSize);

            glyphLayout.setText(font, option.displayName);
            font.draw(actorBatch, glyphLayout, getX() + (getWidth() - glyphLayout.width) / 2f, getY() + 80f);

            font.getData().setScale(0.8f);
            glyphLayout.setText(font, "Tier " + option.tier.name());
            font.draw(actorBatch, glyphLayout, getX() + (getWidth() - glyphLayout.width) / 2f, getY() + 30f);
            font.getData().setScale(1f);
        }
    }

    private class RewardRerollButtonActor extends Actor {
        private boolean disabled;
        private String label = "Reroll (20 coins)";

        boolean isDisabled() { return disabled; }
        void setDisabled(boolean disabled) { this.disabled = disabled; }
        void setLabel(String label) { this.label = label; }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch actorBatch, float parentAlpha) {
            if (disabled) {
                actorBatch.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            } else {
                actorBatch.setColor(0.18f, 0.38f, 0.22f, 1f);
            }
            actorBatch.draw(player.debugPixel, getX(), getY(), getWidth(), getHeight());

            actorBatch.setColor(1f, 1f, 1f, 1f);
            glyphLayout.setText(font, label);
            font.draw(actorBatch, glyphLayout, getX() + (getWidth() - glyphLayout.width) / 2f, getY() + (getHeight() + glyphLayout.height) / 2f);
        }
    }
}
