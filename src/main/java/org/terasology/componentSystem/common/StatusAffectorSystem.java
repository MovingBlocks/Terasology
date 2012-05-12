package org.terasology.componentSystem.common;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.HealthComponent;
import org.terasology.components.SpeedBoostComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.BoostSpeedEvent;
import org.terasology.components.PoisonedComponent;
import org.terasology.events.PoisonedEvent;
import org.terasology.game.CoreRegistry;

import java.util.logging.Logger;

/**
 * Status Affector System : Different Effect Handling [Affecting the player]
 */
public class StatusAffectorSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    protected EntityManager entityManager;
    private Logger logger = Logger.getLogger(getClass().getName());


    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
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
    public  void isPoisoned(PoisonedEvent poisonEvent, EntityRef entity) {
        PoisonedComponent poisonedEffect = new PoisonedComponent();
        HealthComponent health = entity.getComponent(HealthComponent.class);
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
                health.currentHealth -= 1;
                entity.saveComponent(health);
            }
            //Remove POISONED Status
            if (poisonedEffect.poisonDuration <= 0) {
                entity.removeComponent(PoisonedComponent.class);
            }
        }
    }
}

