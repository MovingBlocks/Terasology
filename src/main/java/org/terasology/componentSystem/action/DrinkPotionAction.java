package org.terasology.componentSystem.action;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.components.*;
import org.terasology.entitySystem.*;
import org.terasology.events.*;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.AudioManager;


/**
 * This Handles Potion Consumption
 * Applies effect and returns an empty vial.
 * @author bi0hax
 */
@RegisterComponentSystem
public class DrinkPotionAction implements EventHandlerSystem {

    public void initialise() {
        }


    @Override
    public void shutdown() {
    }

    public EntityRef entity;
    public PotionComponent potion;
    public PoisonedComponent poisoned;
    public EntityRef item;
    public CoreRegistry CoreRegister;


    @ReceiveEvent(components = {PotionComponent.class})
        public void onActivate(ActivateEvent event, EntityRef entity) {
        potion = entity.getComponent(PotionComponent.class);
        poisoned = entity.getComponent(PoisonedComponent.class);
        EntityManager entityManager = CoreRegister.get(EntityManager.class);

        HealthComponent health = event.getTarget().getComponent(HealthComponent.class);
        ItemComponent itemComp = entity.getComponent(ItemComponent.class);


        EntityRef item = entityManager.create("core:emptyVial");

        switch (potion.type) {
            case Red:
                //Max HP
                event.getInstigator().send(new BoostHpEvent());
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getInstigator().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }


            break;

            case Green:
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getInstigator().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                //Poison time!
                event.getInstigator().send(new PoisonedEvent());
               break;

            case Orange: //Cures the Poison.
                event.getInstigator().send(new CurePoisonEvent());
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getInstigator().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                break;

            case Purple:
                //Receive an Empty Vial (Destroy it if no inventory space available)
                event.getInstigator().send(new ReceiveItemEvent(item));
                if (itemComp != null && !itemComp.container.exists()) {
                    item.destroy();
                }
                //Speed time!
                event.getInstigator().send(new BoostSpeedEvent());
                break;

            default:
                break;
             }
        AudioManager.play(new AssetUri(AssetType.SOUND, "engine:drink"), 1.0f);


    }
    }
