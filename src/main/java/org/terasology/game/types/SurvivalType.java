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
package org.terasology.game.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.HealthComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.HealthChangedEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.rendering.gui.widgets.UIWindow;

public class SurvivalType extends BaseGameType{
    private final static Logger logger = LoggerFactory.getLogger(SurvivalType.class);

    public SurvivalType(){
        super(new GameTypeUri("core:survival"));
    }

    @Override
    public String name() {
        return "Survival";
    }

    @Override
    public void initialize() {
        logger.info(name()+" started");
    }

    @Override
    public void onPlayerDamageHook(EntityRef entity, HealthComponent health, int damageAmount, EntityRef instigator) {
        if (health.currentHealth <= 0) return;

        health.timeSinceLastDamage = 0;
        health.currentHealth -= damageAmount;
        if (health.currentHealth <= 0) {
            entity.send(new NoHealthEvent(instigator, health.maxHealth));
        } else {
            entity.send(new HealthChangedEvent(instigator, health.currentHealth, health.maxHealth));
        }
        entity.saveComponent(health);
    }

    @Override
    public void onCreateInventoryHook(UIWindow parent) {}
}
