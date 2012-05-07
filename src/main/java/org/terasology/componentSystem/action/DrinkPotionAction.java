package org.terasology.componentSystem.action;

import org.terasology.components.HealthComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.PotionComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;


/**
 *
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
        public void onActivate(ActivateEvent event, EntityRef entity) {
        potion = entity.getComponent(PotionComponent.class);
        EntityManager entityManager = CoreRegister.get(EntityManager.class);
        EntityRef item = entityManager.create("core:emptyVial");
        HealthComponent health = event.getTarget().getComponent(HealthComponent.class);
        switch (potion.type) {
            case Red:
                //Set HP to Max HP
                health.currentHealth = health.maxHealth;
                event.getTarget().saveComponent(health);
                //Receive an Empty Vial
                event.getTarget().send(new ReceiveItemEvent(item));
                ItemComponent itemComp = item.getComponent(ItemComponent.class);
                //Destroy Vial if no space in players inventory
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                break;
            //Add more cases
            default:
                break;//nothing happens
             }
        }
    }
