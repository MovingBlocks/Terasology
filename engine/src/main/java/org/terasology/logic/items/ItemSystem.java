/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.items.components.ItemComponent;
import org.terasology.logic.items.events.ItemDropEvent;
import org.terasology.logic.items.events.ItemGiveEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.events.CollideEvent;
import org.terasology.registry.In;
import org.terasology.rendering.iconmesh.IconMeshFactory;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.utilities.Assets;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemComponent;

@RegisterSystem()
public class ItemSystem extends BaseComponentSystem {
    Logger logger = LoggerFactory.getLogger(ItemSystem.class);

    @In
    LocalPlayer localPlayer;

    @In
    private EntitySystemLibrary library;

    @ReceiveEvent
    public void onItemCollision(CollideEvent event, EntityRef item, ItemComponent itemComponent) {
        if (event.getOtherEntity().hasComponent(CharacterComponent.class)) {
            ItemGiveEvent giveEvent = new ItemGiveEvent(event.getOtherEntity());
            item.send(giveEvent);
            if (giveEvent.wasSuccessful()) {
                for (Component component : itemComponent.onDroppedPrefab.iterateComponents()) {
                    item.removeComponent(component.getClass());
                }
                item.removeComponent(MeshComponent.class);
            }
        }
    }

    @ReceiveEvent
    public void onItemDrop(ItemDropEvent event, EntityRef item) {
        dropItem(
                event.getItem() == EntityRef.NULL ? item : event.getItem(),
                event.getPosition());
    }

    private void dropItem(EntityRef item, Vector3f position) {
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        for (Component component : itemComponent.onDroppedPrefab.iterateComponents()) {
            Component componentCopy = library.getComponentLibrary().copy(component);
            if (componentCopy instanceof LocationComponent) {
                ((LocationComponent) componentCopy).setWorldPosition(position);
            }
            item.addOrSaveComponent(componentCopy);
        }

        if (!item.hasComponent(LocationComponent.class)) {
            item.addComponent(new LocationComponent(position));
        }

        if (item.hasComponent(BlockItemComponent.class)) {
            item.addOrSaveComponent(getBlockMesh(item.getComponent(BlockItemComponent.class), item));
        } else {
            item.addOrSaveComponent(getItemMesh(itemComponent, item));
        }
        item.setOwner(localPlayer.getCharacterEntity());
    }


    private MeshComponent getItemMesh(ItemComponent itemComponent, EntityRef item) {
        MeshComponent meshComponent = item.hasComponent(MeshComponent.class) ? item.getComponent(MeshComponent.class) : new MeshComponent();
        meshComponent.material = Assets.getMaterial("engine:droppedItem").get();

        if (itemComponent.icon != null) {
            meshComponent.mesh = IconMeshFactory.getIconMesh(itemComponent.icon);
        }
        meshComponent.hideFromOwner = false;
        return meshComponent;
    }

    private MeshComponent getBlockMesh(BlockItemComponent itemComponent, EntityRef item) {
        MeshComponent meshComponent = item.hasComponent(MeshComponent.class) ? item.getComponent(MeshComponent.class) : new MeshComponent();
        BlockFamily blockFamily = itemComponent.blockFamily;
        if (blockFamily == null) {
            return null;
        }

        meshComponent.mesh = blockFamily.getArchetypeBlock().getMeshGenerator().getStandaloneMesh();
        meshComponent.material = Assets.getMaterial("engine:terrain").get();
        meshComponent.translucent = blockFamily.getArchetypeBlock().isTranslucent();

        float luminance = blockFamily.getArchetypeBlock().getLuminance() / 15f;
        meshComponent.selfLuminance = luminance;
        if (luminance > 0 && !item.hasComponent(LightComponent.class)) {
            LightComponent lightComponent = item.addComponent(new LightComponent());
            lightComponent.lightAttenuationRange *= luminance;
        }

        return meshComponent;
    }

}
