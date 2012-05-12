package org.terasology.componentSystem.action;

import org.terasology.componentSystem.common.StatusAffectorSystem;
import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.events.BoostSpeedEvent;
import org.terasology.events.PoisonedEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;


/**
 * This Handles Potion Consumption
 * Applies effect and returns an empty vial.
 * @author bi0hax
 */
public class DrinkPotionAction implements EventHandlerSystem {

    public void initialise() {
        }
        public EntityRef entity;
        public PotionComponent potion;
        //public EntityRef item;
        public CoreRegistry CoreRegister;


    @ReceiveEvent(components = {PotionComponent.class})
        public void onActivate(ActivateEvent event, EntityRef entity) {
        potion = entity.getComponent(PotionComponent.class);
        EntityManager entityManager = CoreRegister.get(EntityManager.class);

        HealthComponent health = event.getTarget().getComponent(HealthComponent.class);
        ItemComponent itemComp = entity.getComponent(ItemComponent.class);


        EntityRef item = entityManager.create("core:emptyVial");

        switch (potion.type) {
            case Red:
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getTarget().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                //Max HP
                health.currentHealth = health.maxHealth;
                event.getTarget().saveComponent(health);
                break;

            case Green:
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getTarget().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                //Poison time!
                event.getTarget().send(new PoisonedEvent());//Receive an Empty Vial
               break;

            case Purple:
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getTarget().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                //Speed time!
                event.getInstigator().send(new BoostSpeedEvent());
                break;

            default:
                break;
             }

        }
    }
