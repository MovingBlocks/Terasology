package org.terasology.componentSystem.controllers;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.components.ItemComponent;
import org.terasology.components.utility.DroppedItemTypeComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.audio.AudioManager;
import org.terasology.physics.CollideEvent;


@RegisterSystem
public class ItemPickupSystem implements ComponentSystem {

    @Override
    public void initialise() {
    }

    @ReceiveEvent(components=DroppedItemTypeComponent.class)
    public void onBump(CollideEvent event, EntityRef entity) {
        DroppedItemTypeComponent droppedItem = entity.getComponent(DroppedItemTypeComponent.class);
        ReceiveItemEvent giveItemEvent = new ReceiveItemEvent(droppedItem.placedEntity);
        event.getOtherEntity().send(giveItemEvent);

        ItemComponent itemComp = droppedItem.placedEntity.getComponent(ItemComponent.class);

        if (itemComp != null && !itemComp.container.exists()) {
            droppedItem.placedEntity.destroy();
        } else {
            AudioManager.play(new AssetUri(AssetType.SOUND, "engine:Loot"));
            entity.destroy();
        }
    }

    @Override
    public void shutdown() {
    }
}
