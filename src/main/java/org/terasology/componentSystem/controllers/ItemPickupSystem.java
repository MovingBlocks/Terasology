package org.terasology.componentSystem.controllers;

import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.components.ItemComponent;
import org.terasology.components.utility.DroppedItemTypeComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.physics.CollideEvent;


@RegisterComponentSystem
public class ItemPickupSystem implements EventHandlerSystem {
    @In
    private AudioManager audioManager;

    @Override
    public void initialise() {
    }

    @ReceiveEvent(components = DroppedItemTypeComponent.class)
    public void onBump(CollideEvent event, EntityRef entity) {
        DroppedItemTypeComponent droppedItem = entity.getComponent(DroppedItemTypeComponent.class);
        ReceiveItemEvent giveItemEvent = new ReceiveItemEvent(droppedItem.placedEntity);
        event.getOtherEntity().send(giveItemEvent);

        ItemComponent itemComp = droppedItem.placedEntity.getComponent(ItemComponent.class);

        if (itemComp != null && !itemComp.container.exists()) {
            droppedItem.placedEntity.destroy();
        } else {
            audioManager.playSound(Assets.getSound("engine:Loot"));
            entity.destroy();
        }
    }

    @Override
    public void shutdown() {
    }
}
