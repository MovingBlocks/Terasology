package org.terasology.componentSystem.action;

import org.terasology.components.HealthComponent;
import org.terasology.components.PotionComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bi0hax
 */
public class DrinkPotionAction implements EventHandlerSystem {
    private Logger _logger = Logger.getLogger(getClass().getName());

    public void initialise() {
        }
        public EntityRef entity;
        public PotionComponent potion;
    @ReceiveEvent(components = {PotionComponent.class})
        public void onActivate(ActivateEvent event, EntityRef entity) {
        potion = entity.getComponent(PotionComponent.class);
        HealthComponent health = event.getTarget().getComponent(HealthComponent.class);
        if (potion.type.equals(PotionComponent.PotionType.Red) && health != null) {
            health.currentHealth = health.maxHealth;
            event.getTarget().saveComponent(health);
            _logger.log(Level.SEVERE, "It does read the complete action");
        }
        _logger.log(Level.WARNING, "RECEIVED DRINK POTION");
        }
    }
