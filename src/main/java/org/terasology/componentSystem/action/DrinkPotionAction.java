package org.terasology.componentSystem.action;

import org.terasology.componentSystem.common.StatusAffectorSystem;
import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.events.BoostSpeedEvent;
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
        public EntityRef item;
        public CoreRegistry CoreRegister;


    @ReceiveEvent(components = {PotionComponent.class})
        public void onActivate(ActivateEvent event, EntityRef entity, StatusAffectorSystem stat) {
        potion = entity.getComponent(PotionComponent.class);
        EntityManager entityManager = CoreRegister.get(EntityManager.class);

        HealthComponent health = event.getTarget().getComponent(HealthComponent.class);
        ItemComponent itemComp = item.getComponent(ItemComponent.class);

        /*Required for the OLDSpeedPotion
        CharacterMovementComponent charmov = event.getTarget().getComponent(CharacterMovementComponent.class);
        */

        EntityRef item = entityManager.create("core:emptyVial");

        switch (potion.type) {
            case Red:
                //Set HP to Max HP
                health.currentHealth = health.maxHealth;
                event.getTarget().saveComponent(health);
                //Receive an Empty Vial
                event.getTarget().send(new ReceiveItemEvent(item));
                //Destroy Vial if no space in players inventory
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                break;

            /* THE OLD SPEED POTION
            case Cerulean:
                charmov.runFactor = 8.0f;
                event.getTarget().saveComponent(charmov);
                //Empty Vial:
                event.getTarget().send(new ReceiveItemEvent(item));
                //Destroy Vial if no space in players inventory
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
             */

            case Black:
                //This activates the Speed Boost effect through the StatusAffectorSystem
                event.getInstigator().send(new BoostSpeedEvent());
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getTarget().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                break;

            default:
                break;
             }
        }
    }
