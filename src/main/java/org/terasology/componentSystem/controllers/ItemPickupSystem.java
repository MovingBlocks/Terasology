package org.terasology.componentSystem.controllers;

import org.terasology.asset.Assets;
import org.terasology.audio.events.PlaySoundForOwnerEvent;
import org.terasology.components.utility.DroppedItemTypeComponent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.physics.CollideEvent;


@RegisterSystem
public class ItemPickupSystem implements ComponentSystem {

    @In
    private InventoryManager inventoryManager;

    @Override
    public void initialise() {
    }

    @ReceiveEvent(components = DroppedItemTypeComponent.class)
    public void onBump(CollideEvent event, EntityRef entity) {
        if (inventoryManager.giveItem(event.getOtherEntity(), entity)) {
            event.getOtherEntity().send(new PlaySoundForOwnerEvent(Assets.getSound("engine:Loot"), 1.0f));
        }
    }

    @Override
    public void shutdown() {
    }
}
