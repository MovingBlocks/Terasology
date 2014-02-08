/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.gui.windows;

import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.engine.GameEngine;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.drowning.DrowningComponent;
import org.terasology.logic.drowning.DrownsComponent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.util.Locale;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 *         <p/>
 *         TODO clean up -> remove debug stuff, move to debug window together with metrics
 */
public class UIScreenHUD extends UIWindow implements ComponentSystem {

    private static final int NUM_HEART_ICONS = 10;
    private static final int NUM_BUBBLE_ICONS = 10;

    protected EntityManager entityManager;
    private Time time;

    /* DISPLAY ELEMENTS */
    private final UIImage[] hearts;
    private final UIImage[] breathBubbles;
    private final UIImage crosshair;

    private final Config config = CoreRegistry.get(Config.class);

    private LocalPlayer localPlayer;

    /**
     * Init. the HUD.
     */
    public UIScreenHUD() {
        setId("hud");
        maximize();
        time = CoreRegistry.get(Time.class);

        // Create hearts
        hearts = new UIImage[NUM_HEART_ICONS];
        for (int i = 0; i < NUM_HEART_ICONS; i++) {
            hearts[i] = new UIImage(Assets.getTexture("engine:icons"));
            hearts[i].setVisible(true);
            hearts[i].setTextureSize(new Vector2f(9f, 9f));
            hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f)); //106f for poison
            hearts[i].setSize(new Vector2f(18f, 18f));
            hearts[i].setVerticalAlign(EVerticalAlign.BOTTOM);
            hearts[i].setHorizontalAlign(EHorizontalAlign.CENTER);
            hearts[i].setPosition(new Vector2f(18f * i - 212f, -52f));

            addDisplayElement(hearts[i]);
        }

        breathBubbles = new UIImage[NUM_BUBBLE_ICONS];
        for (int i = 0; i < NUM_BUBBLE_ICONS; ++i) {
            breathBubbles[i] = new UIImage(Assets.getTexture("engine:icons"));
            breathBubbles[i].setVisible(true);
            breathBubbles[i].setTextureSize(new Vector2f(9f, 9f));
            breathBubbles[i].setTextureOrigin(new Vector2f(16f, 18f));
            breathBubbles[i].setSize(new Vector2f(18f, 18f));
            breathBubbles[i].setVerticalAlign(EVerticalAlign.BOTTOM);
            breathBubbles[i].setHorizontalAlign(EHorizontalAlign.CENTER);
            breathBubbles[i].setPosition(new Vector2f(-18f * i + 210f, -52f));

            addDisplayElement(breathBubbles[i]);
        }

        crosshair = new UIImage(Assets.getTexture("engine:gui"));
        crosshair.setId("crosshair");
        crosshair.setTextureSize(new Vector2f(20f, 20f));
        crosshair.setTextureOrigin(new Vector2f(24f, 24f));
        crosshair.setSize(new Vector2f(40f, 40f));
        crosshair.setHorizontalAlign(EHorizontalAlign.CENTER);
        crosshair.setVerticalAlign(EVerticalAlign.CENTER);
        crosshair.setVisible(true);

        addDisplayElement(crosshair);

        localPlayer = CoreRegistry.get(LocalPlayer.class);

        update();
        layout();
    }

    @Override
    public void update() {
        super.update();

        updateHealthBar(localPlayer.getCharacterEntity().getComponent(HealthComponent.class));
        updateBreathBar(localPlayer.getCharacterEntity().getComponent(DrownsComponent.class), localPlayer.getCharacterEntity().getComponent(DrowningComponent.class));
    }

    private void updateBreathBar(DrownsComponent drownsComponent, DrowningComponent drowningComponent) {
        if (drownsComponent != null && drowningComponent != null) {
            float breath = drowningComponent.getPercentageBreath(time.getGameTimeInMs());
            if (breath <= 0) {
                for (UIImage breathBubble : breathBubbles) {
                    breathBubble.setVisible(true);
                    breathBubble.setTextureOrigin(new Vector2f(25f, 18f));
                }
            } else {
                breath *= NUM_BUBBLE_ICONS;
                for (int i = 0; i < breathBubbles.length; ++i) {
                    breathBubbles[i].setVisible(true);
                    if (NUM_BUBBLE_ICONS - i - 1 < breath) {
                        breathBubbles[i].setTextureOrigin(new Vector2f(16f, 18f));
                    } else {
                        breathBubbles[i].setTextureOrigin(new Vector2f(25f, 18f));
                    }
                }
            }
        } else {
            for (UIImage breathBubble : breathBubbles) {
                breathBubble.setVisible(false);
            }
        }
    }

    private void updateHealthBar(HealthComponent health) {
        float healthRatio = 0;
        if (health != null) {
            healthRatio = (float) health.currentHealth / health.maxHealth;
        }

        // Show/Hide hearts relatively to the available health points of the player
        for (int i = 0; i < NUM_HEART_ICONS; i++) {

            if (i < healthRatio * 10f) {
                hearts[i].setVisible(true);
            } else {
                hearts[i].setVisible(false);
            }

            // TODO: Need to reimplement this in some way, maybe expose a method to change the health icon
            //Show Poisoned Status with Green Hearts:
            /*PoisonedComponent poisoned = CoreRegistry.get(LocalPlayer.class).getCharacterEntity().getComponent(PoisonedComponent.class);
            entityManager = CoreRegistry.get(EntityManager.class);
            for (EntityRef entity : entityManager.listEntities(PoisonedComponent.class)) {
                if (poisoned.poisonDuration >= 1)
                    hearts[i].setTextureOrigin(new Vector2f(106f, 0.0f));
                else
                    hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f));
            }
            
            for (EntityRef entity : entityManager.listEntities(CuredComponent.class)) {
                //For fixing the Green > Red hearts when cured:
                CuredComponent cured = CoreRegistry.get(LocalPlayer.class).getCharacterEntity().getComponent(CuredComponent.class);
                entityManager = CoreRegistry.get(EntityManager.class);
                if (cured.cureDuration >= 1)
                    hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f));
                else
                    hearts[i].setTextureOrigin(new Vector2f(52f, 0.0f));
            }*/
        }
    }

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {

    }

}
