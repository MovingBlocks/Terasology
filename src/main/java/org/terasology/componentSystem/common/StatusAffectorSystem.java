package org.terasology.componentSystem.common;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.HealthComponent;
import org.terasology.components.PoisonedComponent;
import org.terasology.components.SpeedBoostComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.*;

import org.terasology.game.CoreRegistry;

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

            /*health.currentHealth -= 0.25;
            entity.saveComponent(health);   */

    }

    @ReceiveEvent(components = {PoisonedComponent.class})
    public void curePoisoned(CurePoisonEvent cureEvent, EntityRef entity){
        PoisonedComponent poisondEffect = entity.getComponent(PoisonedComponent.class);
        entity.removeComponent(PoisonedComponent.class);


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
    }
}

