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
package org.terasology.rendering.hudElement;

import javax.vecmath.Vector2f;

import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.logic.manager.HUDElement;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class HUDElementHearts extends UIDisplayContainer implements HUDElement {

    private static final int NUM_HEART_ICONS = 10;

    private final UIImage[] hearts;

    private LocalPlayer localPlayer;

    /**
     * Init. the HUD.
     */
    public HUDElementHearts() {
        setId("hearts");

        // Create hearts
        hearts = new UIImage[NUM_HEART_ICONS];
    }

    @Override
    public void update() {
        super.update();

        EntityRef characterEntity = localPlayer.getCharacterEntity();
		HealthComponent component = characterEntity.getComponent(HealthComponent.class);
		updateHealthBar(component);
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
        // Create hearts
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

        localPlayer = CoreRegistry.get(LocalPlayer.class);
	}

	@Override
	public void open() {
	}

	@Override
	public void willShutdown() {
	}

	@Override
	public void shutdown() {
	}

}
