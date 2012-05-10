package org.terasology.componentSystem.common;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.SpeedBoostComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.events.BoostSpeedEvent;
import org.terasology.game.CoreRegistry;

/**
 * Status Affector System : Different Effect Handling [Affecting the player]
 */
public class StatusAffectorSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    private EntityManager entityManager;
    private SpeedBoostComponent speedEffect;
    private CharacterMovementComponent charmov;
    private ActivateEvent event;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    //This would be the specific effect of the "Speed Boost" property (potion/spell)
    @ReceiveEvent(components = {SpeedBoostComponent.class})
    public void onActivate(BoostSpeedEvent boostevent, EntityRef entity, SpeedBoostComponent speedEffect) {
        CharacterMovementComponent charmov = event.getTarget().getComponent(CharacterMovementComponent.class);
        boostevent.getInstigator().exists();
        speedEffect.speedBoostDuration = 10f;
        charmov.runFactor = 8.0f;
        event.getTarget().saveComponent(charmov);
    }
    /*
     * The Effects Duration Countdown "timer"
     */
    public void update(float delta) {
         update(speedEffect.speedBoostDuration - delta);
        if (speedEffect.speedBoostDuration <= 0) {   //Returns to normal run speed.
            charmov.runFactor = 1.5f;
            event.getTarget().saveComponent(charmov);
        }
    }
}
