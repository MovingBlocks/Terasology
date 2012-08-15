/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.components;

import javax.vecmath.Vector2f;

import org.terasology.asset.AssetManager;
import org.terasology.components.CuredComponent;
import org.terasology.components.HealthComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.PoisonedComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.HealthChangedEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

/**
 * Small health bar that visualizes the current amount of health points of the player
 * with ten small heart icons.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIHealthBar extends UIDisplayContainer implements EventHandlerSystem {
    private final UIGraphicsElement[] _hearts;
    protected EntityManager entityManager;

    public UIHealthBar() {
        setSize(new Vector2f(180f, 18f));

        _hearts = new UIGraphicsElement[10];

        // Create hearts
        for (int i = 0; i < 10; i++) {
            _hearts[i] = new UIGraphicsElement(AssetManager.loadTexture("engine:icons"));
            _hearts[i].setVisible(true);
            _hearts[i].getTextureSize().set(new Vector2f(9f / 256f, 9f / 256f));
            _hearts[i].getTextureOrigin().set(new Vector2f(52f / 256f, 0.0f)); //106f for poison
            _hearts[i].setSize(new Vector2f(18f, 18f));
            _hearts[i].setPosition(new Vector2f(18f * i, 18f));

            addDisplayElement(_hearts[i]);
        }
        
        CoreRegistry.get(EventSystem.class).registerEventHandler(this);
    }
    
	private void updateHealthBar(int currentHealth, int maxHealth) {
        float healthRatio = (float) currentHealth / maxHealth;

        // Show/Hide hearts relatively to the available health points of the player
        for (int i = 0; i < 10; i++) {

            if (i < healthRatio * 10f)
                _hearts[i].setVisible(true);
            else
                _hearts[i].setVisible(false);

            //Show Poisoned Status with Green Hearts:
            PoisonedComponent poisoned = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(PoisonedComponent.class);
            entityManager = CoreRegistry.get(EntityManager.class);
            for (EntityRef entity : entityManager.iteratorEntities(PoisonedComponent.class)) {
                if (poisoned.poisonDuration >= 1)
                    _hearts[i].getTextureOrigin().set(new Vector2f(106f / 256f, 0.0f));
                else
                	_hearts[i].getTextureOrigin().set(new Vector2f(52f / 256f, 0.0f));
            }
            
            for (EntityRef entity : entityManager.iteratorEntities(CuredComponent.class)) {
                //For fixing the Green > Red hearts when cured:
                CuredComponent cured = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(CuredComponent.class);
                entityManager = CoreRegistry.get(EntityManager.class);
                if (cured.cureDuration >= 1)
                    _hearts[i].getTextureOrigin().set(new Vector2f(52f / 256f, 0.0f));
                else
                	_hearts[i].getTextureOrigin().set(new Vector2f(52f / 256f, 0.0f));
            }
        }
	}

	@Override
	public void initialise() {
		entityManager = CoreRegistry.get(EntityManager.class);
	}

	@Override
	public void shutdown() {
		
	}
	
	@ReceiveEvent(components = {LocalPlayerComponent.class, HealthComponent.class})
    public void onHealthChange(HealthChangedEvent event, EntityRef entityref) {    	
		updateHealthBar(event.getCurrentHealth(), event.getMaxHealth());
    }
}

/*Blue Hearts:
_hearts[i].getTextureOrigin().set(new Vector2f(70f / 256f, 0.0f)); */
