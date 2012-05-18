package org.terasology.componentSystem.common;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.*;

import org.terasology.game.CoreRegistry;

/**
 * Status Affector System : Different Effect Handling [Affecting the player]
 */
public class StatusAffectorSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    protected EntityManager entityManager;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }
    @ReceiveEvent(components = {HealthComponent.class})
    public void giveHealth(BoostHpEvent boosthpEvent, EntityRef entity){
        HealthComponent health = entity.getComponent(HealthComponent.class);
        health.currentHealth = health.maxHealth;
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
    public void isPoisoned(PoisonedEvent poisonEvent, EntityRef entity){
        PoisonedComponent poisonedEffect = new PoisonedComponent();
        HealthComponent health = entity.getComponent(HealthComponent.class);
        entity.addComponent(poisonedEffect);
        entity.saveComponent(poisonedEffect);
    }

    @ReceiveEvent(components = {PoisonedComponent.class})
    public void curePoisoned(CurePoisonEvent cureEvent, EntityRef entity){
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

            }
        }
        for (EntityRef entity : entityManager.iteratorEntities(HealthComponent.class, PoisonedComponent.class)) {
            PoisonedComponent poisonedEffect = entity.getComponent(PoisonedComponent.class);
            HealthComponent health = entity.getComponent(HealthComponent.class);
            poisonedEffect.poisonDuration = poisonedEffect.poisonDuration - delta;
            //While POISONED:
            if (poisonedEffect.poisonDuration >=1) {
                health.currentHealth = Math.min(health.maxHealth, health.currentHealth - (int)poisonedEffect.poisonRate);
                entity.saveComponent(health);
                if (health.currentHealth <= 0){
                    entity.send(new NoHealthEvent(entity));
                }
            }
            //Remove POISONED Status
            if (poisonedEffect.poisonDuration <= 0) {
                entity.removeComponent(PoisonedComponent.class);
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


