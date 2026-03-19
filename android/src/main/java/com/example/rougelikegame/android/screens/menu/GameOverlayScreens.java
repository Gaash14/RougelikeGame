package com.example.rougelikegame.android.screens.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.rougelikegame.android.models.items.ItemTier;

class GameOverlayScreens {
    interface OverlayCallbacks {
        void onRewardSelected(int itemId);
        boolean canAffordRewardReroll();
        RewardOption[] onRewardRerollRequested();
        void onVictoryMainMenuSelected();
        void onVictoryEndlessModeSelected();
        void onDeathMainMenuSelected();
    }

    static class RewardOption {
        final int itemId;
        final String displayName;
        final ItemTier tier;
        final Texture iconTexture;

        RewardOption(int itemId, String displayName, ItemTier tier, Texture iconTexture) {
            this.itemId = itemId;
            this.displayName = displayName;
            this.tier = tier;
            this.iconTexture = iconTexture;
        }
    }

    private static final int MAX_REWARD_REROLLS = 3;
    private static final int REWARD_REROLL_COST = 20;

    private final BitmapFont font;
    private final GlyphLayout glyphLayout;
    private final Texture panelTexture;
    private final OverlayCallbacks callbacks;

    private Stage rewardStage;
    private Stage victoryStage;
    private Stage deathStage;
    private RewardOption[] currentRewardOptions;
    private RewardRerollButtonActor rerollButton;
    private int rewardWave = -1;
    private int rewardRerollsUsed = 0;
    private String rewardStatusMessage = "";
    private boolean rewardScreenActive;
    private boolean victoryScreenActive;
    private boolean deathScreenActive;

    GameOverlayScreens(BitmapFont font, GlyphLayout glyphLayout, Texture panelTexture, OverlayCallbacks callbacks) {
        this.font = font;
        this.glyphLayout = glyphLayout;
        this.panelTexture = panelTexture;
        this.callbacks = callbacks;
    }

    boolean isAnyOverlayActive() {
        return rewardScreenActive || victoryScreenActive || deathScreenActive;
    }

    boolean isRewardScreenActive() {
        return rewardScreenActive;
    }

    boolean isVictoryScreenActive() {
        return victoryScreenActive;
    }

    boolean isDeathScreenActive() {
        return deathScreenActive;
    }

    int getRewardWave() {
        return rewardWave;
    }

    void act(float delta) {
        if (rewardScreenActive && rewardStage != null) {
            rewardStage.act(delta);
        } else if (victoryScreenActive && victoryStage != null) {
            victoryStage.act(delta);
        } else if (deathScreenActive && deathStage != null) {
            deathStage.act(delta);
        }
    }

    void drawOverlay(Batch batch) {
        if (rewardScreenActive) {
            drawRewardOverlay(batch);
        }
        if (victoryScreenActive) {
            drawMessageOverlay(batch, "You won!", "Choose what happens next.");
        }
        if (deathScreenActive) {
            drawMessageOverlay(batch, "You died", "Return to the main menu.");
        }
    }

    void drawStage() {
        if (rewardScreenActive && rewardStage != null) {
            rewardStage.draw();
        }
        if (victoryScreenActive && victoryStage != null) {
            victoryStage.draw();
        }
        if (deathScreenActive && deathStage != null) {
            deathStage.draw();
        }
    }

    void showRewardScreen(int waveNumber, RewardOption[] options) {
        rewardScreenActive = true;
        rewardWave = waveNumber;
        rewardRerollsUsed = 0;
        rewardStatusMessage = "";
        currentRewardOptions = options;

        disposeRewardStage();
        rewardStage = new Stage(new ScreenViewport());

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
                callbacks.onRewardSelected(currentRewardOptions[0].itemId);
            }
        });

        RewardCardActor right = new RewardCardActor(1);
        right.setBounds(startX + cardW + gap, y, cardW, cardH);
        right.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callbacks.onRewardSelected(currentRewardOptions[1].itemId);
            }
        });

        rewardStage.addActor(left);
        rewardStage.addActor(right);

        rerollButton = new RewardRerollButtonActor();
        rerollButton.setSize(320f, 72f);
        rerollButton.setPosition((screenW - rerollButton.getWidth()) / 2f, y - 100f);
        rerollButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!rerollButton.isDisabled()) {
                    requestRewardReroll();
                }
            }
        });
        updateRerollButtonState();
        rewardStage.addActor(rerollButton);

        Gdx.input.setInputProcessor(rewardStage);
    }

    void hideRewardScreen() {
        rewardScreenActive = false;
        rewardWave = -1;
        disposeRewardStage();
    }

    void showVictoryScreen() {
        victoryScreenActive = true;
        disposeVictoryStage();
        victoryStage = createTwoButtonStage(
            "Main Menu",
            callbacks::onVictoryMainMenuSelected,
            "Continue Endless Mode",
            callbacks::onVictoryEndlessModeSelected
        );
        Gdx.input.setInputProcessor(victoryStage);
    }

    void hideVictoryScreen() {
        victoryScreenActive = false;
        disposeVictoryStage();
    }

    void showDeathScreen() {
        deathScreenActive = true;
        disposeDeathStage();
        deathStage = createSingleButtonStage(
            "Main Menu",
            callbacks::onDeathMainMenuSelected
        );
        Gdx.input.setInputProcessor(deathStage);
    }

    void hideDeathScreen() {
        deathScreenActive = false;
        disposeDeathStage();
    }

    void dispose() {
        disposeRewardStage();
        disposeVictoryStage();
        disposeDeathStage();
    }

    private void requestRewardReroll() {
        if (!rewardScreenActive) {
            return;
        }
        if (rewardRerollsUsed >= MAX_REWARD_REROLLS) {
            rewardStatusMessage = "No rerolls left";
            updateRerollButtonState();
            return;
        }

        RewardOption[] rerolledOptions = callbacks.onRewardRerollRequested();
        if (rerolledOptions == null) {
            rewardStatusMessage = "Not enough coins";
            updateRerollButtonState();
            return;
        }

        rewardRerollsUsed++;
        rewardStatusMessage = "";
        currentRewardOptions = rerolledOptions;
        updateRerollButtonState();
    }

    private void updateRerollButtonState() {
        if (rerollButton == null) {
            return;
        }

        boolean hasRerollsLeft = rewardRerollsUsed < MAX_REWARD_REROLLS;
        boolean canAfford = callbacks.canAffordRewardReroll();
        rerollButton.setDisabled(!(hasRerollsLeft && canAfford));
        rerollButton.setLabel(hasRerollsLeft ? "Reroll (20 coins)" : "Reroll (max used)");
    }

    private Stage createSingleButtonStage(String label, Runnable action) {
        Stage stage = new Stage(new ScreenViewport());
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float buttonWidth = Math.min(620f, screenW * 0.72f);
        float buttonHeight = 96f;
        float buttonX = (screenW - buttonWidth) / 2f;
        float buttonY = screenH * 0.38f;

        MenuButtonActor button = new MenuButtonActor(label);
        button.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });

        stage.addActor(button);
        return stage;
    }

    private Stage createTwoButtonStage(String firstLabel, Runnable firstAction, String secondLabel, Runnable secondAction) {
        Stage stage = new Stage(new ScreenViewport());
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float buttonWidth = Math.min(620f, screenW * 0.72f);
        float buttonHeight = 96f;
        float centerX = (screenW - buttonWidth) / 2f;
        float firstButtonY = screenH * 0.38f;
        float secondButtonY = firstButtonY - buttonHeight - 32f;

        MenuButtonActor firstButton = new MenuButtonActor(firstLabel);
        firstButton.setBounds(centerX, firstButtonY, buttonWidth, buttonHeight);
        firstButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                firstAction.run();
            }
        });

        MenuButtonActor secondButton = new MenuButtonActor(secondLabel);
        secondButton.setBounds(centerX, secondButtonY, buttonWidth, buttonHeight);
        secondButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                secondAction.run();
            }
        });

        stage.addActor(firstButton);
        stage.addActor(secondButton);
        return stage;
    }

    private void drawMessageOverlay(Batch batch, String title, String subtitle) {
        batch.setColor(0f, 0f, 0f, 0.78f);
        batch.draw(panelTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(1f, 1f, 1f, 1f);

        font.getData().setScale(1.45f);
        glyphLayout.setText(font, title);
        font.draw(batch, glyphLayout, (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, Gdx.graphics.getHeight() * 0.7f);

        font.getData().setScale(0.9f);
        glyphLayout.setText(font, subtitle);
        font.draw(batch, glyphLayout, (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, Gdx.graphics.getHeight() * 0.61f);
        font.getData().setScale(1f);
    }

    private void drawRewardOverlay(Batch batch) {
        batch.setColor(0, 0, 0, 0.7f);
        batch.draw(panelTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(1, 1, 1, 1);

        glyphLayout.setText(font, "Choose a Passive Item");
        font.draw(batch, glyphLayout, (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, Gdx.graphics.getHeight() * 0.72f);

        glyphLayout.setText(font, "Wave " + rewardWave + " Reward");
        font.draw(batch, glyphLayout, (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, Gdx.graphics.getHeight() * 0.66f);

        glyphLayout.setText(font, "Rerolls: " + rewardRerollsUsed + "/" + MAX_REWARD_REROLLS);
        font.draw(batch, glyphLayout, (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, Gdx.graphics.getHeight() * 0.61f);

        if (!rewardStatusMessage.isEmpty()) {
            glyphLayout.setText(font, rewardStatusMessage);
            font.draw(batch, glyphLayout, (Gdx.graphics.getWidth() - glyphLayout.width) / 2f, Gdx.graphics.getHeight() * 0.25f);
        }
    }

    private void disposeRewardStage() {
        if (rewardStage != null) {
            rewardStage.dispose();
            rewardStage = null;
        }
        rerollButton = null;
    }

    private void disposeVictoryStage() {
        if (victoryStage != null) {
            victoryStage.dispose();
            victoryStage = null;
        }
    }

    private void disposeDeathStage() {
        if (deathStage != null) {
            deathStage.dispose();
            deathStage = null;
        }
    }

    private class RewardCardActor extends Actor {
        private final int optionIndex;

        RewardCardActor(int optionIndex) {
            this.optionIndex = optionIndex;
        }

        @Override
        public void draw(Batch actorBatch, float parentAlpha) {
            if (currentRewardOptions == null || optionIndex >= currentRewardOptions.length) {
                return;
            }
            RewardOption option = currentRewardOptions[optionIndex];

            actorBatch.setColor(0.2f, 0.2f, 0.2f, 1f);
            actorBatch.draw(panelTexture, getX(), getY(), getWidth(), getHeight());
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

    private class MenuButtonActor extends Actor {
        private final String label;

        MenuButtonActor(String label) {
            this.label = label;
        }

        @Override
        public void draw(Batch actorBatch, float parentAlpha) {
            actorBatch.setColor(0.55f, 0.08f, 0.08f, 0.96f);
            actorBatch.draw(panelTexture, getX(), getY(), getWidth(), getHeight());
            actorBatch.setColor(1f, 1f, 1f, 1f);
            font.getData().setScale(0.9f);
            glyphLayout.setText(font, label);
            font.draw(actorBatch, glyphLayout, getX() + (getWidth() - glyphLayout.width) / 2f, getY() + (getHeight() + glyphLayout.height) / 2f);
            font.getData().setScale(1f);
        }
    }

    private class RewardRerollButtonActor extends Actor {
        private boolean disabled;
        private String label = "Reroll (20 coins)";

        boolean isDisabled() {
            return disabled;
        }

        void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        void setLabel(String label) {
            this.label = label;
        }

        @Override
        public void draw(Batch actorBatch, float parentAlpha) {
            if (disabled) {
                actorBatch.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            } else {
                actorBatch.setColor(0.18f, 0.38f, 0.22f, 1f);
            }
            actorBatch.draw(panelTexture, getX(), getY(), getWidth(), getHeight());
            actorBatch.setColor(Color.WHITE);
            glyphLayout.setText(font, label);
            font.draw(actorBatch, glyphLayout, getX() + (getWidth() - glyphLayout.width) / 2f, getY() + (getHeight() + glyphLayout.height) / 2f);
        }
    }
}
