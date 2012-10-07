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
package org.terasology.potions;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.HealthComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.HealthChangedEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.physics.character.CharacterMovementComponent;

/**
 * Status Affector System : Different Effect Handling [Affecting the player]
 */
@RegisterComponentSystem
public class StatusAffectorSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    protected EntityManager entityManager;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {HealthComponent.class})
    public void giveHealth(BoostHpEvent boosthpEvent, EntityRef entity) {
        HealthComponent health = entity.getComponent(HealthComponent.class);
        health.currentHealth = health.maxHealth;
        entity.send(new HealthChangedEvent(entity, health.currentHealth, health.maxHealth));
        entity.saveComponent(health);
    }

    @ReceiveEvent(components = {CharacterMovementComponent.class})
    public void onSpeed(BoostSpeedEvent speedEvent, EntityRef entity) {
        SpeedBoostComponent speedEffect = new SpeedBoostComponent();
        CharacterMovementComponent charmov = entity.getComponent(CharacterMovementComponent.class);
        entity.addComponent(speedEffect);
        charmov.runFactor = 8f;
        entity.saveComponent(charmov);
    }

    @ReceiveEvent(components = {HealthComponent.class})
    public void isPoisoned(PoisonedEvent poisonEvent, EntityRef entity) {
        PoisonedComponent poisonedEffect = new PoisonedComponent();
        HealthComponent health = entity.getComponent(HealthComponent.class);
        entity.addComponent(poisonedEffect);
        entity.saveComponent(poisonedEffect);
    }

    @ReceiveEvent(components = {PoisonedComponent.class})
    public void curePoisoned(CurePoisonEvent cureEvent, EntityRef entity) {
        CuredComponent curedEffect = new CuredComponent();
        PoisonedComponent poisoned = entity.getComponent(PoisonedComponent.class);
        entity.removeComponent(PoisonedComponent.class);
        entity.addComponent(curedEffect);
        entity.saveComponent(curedEffect);
    }

    /*
     * The Effects Duration Countdown "timer"
     */
    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(CharacterMovementComponent.class, SpeedBoostComponent.class)) {
            SpeedBoostComponent speedEffect = entity.getComponent(SpeedBoostComponent.class);
            CharacterMovementComponent charmov = entity.getComponent(CharacterMovementComponent.class);
            speedEffect.speedBoostDuration = speedEffect.speedBoostDuration - delta;
            //Returns to normal run speed
            if (speedEffect.speedBoostDuration <= 0) {
                charmov.runFactor = 1.5f;
                entity.saveComponent(charmov);
                entity.removeComponent(SpeedBoostComponent.class);

            } else {
                entity.saveComponent(speedEffect);
            }
        }
        for (EntityRef entity : entityManager.iteratorEntities(HealthComponent.class, PoisonedComponent.class)) {
            PoisonedComponent poisonedEffect = entity.getComponent(PoisonedComponent.class);
            HealthComponent health = entity.getComponent(HealthComponent.class);
            poisonedEffect.poisonDuration = poisonedEffect.poisonDuration - delta;
            //While POISONED:
            if (poisonedEffect.poisonDuration >= 1) {
                health.currentHealth = Math.min(health.maxHealth, health.currentHealth - (int) poisonedEffect.poisonRate);
                entity.saveComponent(health);
                if (health.currentHealth <= 0) {
                    entity.send(new NoHealthEvent(entity, health.maxHealth));
                } else {
                    entity.send(new HealthChangedEvent(entity, health.currentHealth, health.maxHealth));
                }
            }
            //Remove POISONED Status
            if (poisonedEffect.poisonDuration <= 0) {
                entity.removeComponent(PoisonedComponent.class);
            } else {
                entity.saveComponent(poisonedEffect);
            }
        }
        for (EntityRef entity : entityManager.iteratorEntities(CuredComponent.class)) {
            CuredComponent curedEffect = entity.getComponent(CuredComponent.class);
            PoisonedComponent poisonedEffect = entity.getComponent(PoisonedComponent.class);
            curedEffect.cureDuration = curedEffect.cureDuration - delta;
            //While Immune:
            if (curedEffect.cureDuration >= 1) {
                entity.saveComponent(curedEffect);
            }
            //Remove Poison Immunity Status
            else entity.removeComponent(CuredComponent.class);
        }
    }
}


