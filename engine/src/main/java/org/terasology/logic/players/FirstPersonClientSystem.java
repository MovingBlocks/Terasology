/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.players;

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.events.HeldItemChangedEvent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.ItemPickupSystem;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.items.BlockItemComponent;

/**
 */
@RegisterSystem(RegisterMode.CLIENT)
public class FirstPersonClientSystem extends BaseComponentSystem {

    @In
    private LocalPlayer localPlayer;
    @In
    private WorldRenderer worldRenderer;
    @In
    private EntityManager entityManager;

    private EntityRef handEntity;

    @ReceiveEvent
    public void ensureClientSideEntityOnHeldItemMountPoint(OnActivatedComponent event, EntityRef camera, FirstPersonHeldItemMountPointComponent firstPersonHeldItemMountPointComponent) {
        if (!firstPersonHeldItemMountPointComponent.mountPointEntity.exists()) {
            EntityBuilder builder = entityManager.newBuilder("engine:FirstPersonHeldItemMountPoint");
            builder.setPersistent(false);
            firstPersonHeldItemMountPointComponent.mountPointEntity = builder.build();
            camera.saveComponent(firstPersonHeldItemMountPointComponent);
        }
        if (camera.exists()) {
            // link the mount point entity to the camera
            Location.removeChild(camera, firstPersonHeldItemMountPointComponent.mountPointEntity);
            Location.attachChild(camera, firstPersonHeldItemMountPointComponent.mountPointEntity,
                    firstPersonHeldItemMountPointComponent.translate,
                    new Quat4f(
                            TeraMath.DEG_TO_RAD * firstPersonHeldItemMountPointComponent.rotateDegrees.y,
                            TeraMath.DEG_TO_RAD * firstPersonHeldItemMountPointComponent.rotateDegrees.x,
                            TeraMath.DEG_TO_RAD * firstPersonHeldItemMountPointComponent.rotateDegrees.z),
                    firstPersonHeldItemMountPointComponent.scale);
        }
    }

    @Command
    public void setFirstPersonheldItemMountPointTranslation(@CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z) {
        FirstPersonHeldItemMountPointComponent newComponent = localPlayer.getCameraEntity().getComponent(FirstPersonHeldItemMountPointComponent.class);
        if (newComponent != null) {
            newComponent.translate = new Vector3f(x, y, z);
            ensureClientSideEntityOnHeldItemMountPoint(OnActivatedComponent.newInstance(), localPlayer.getCameraEntity(), newComponent);
        }
    }

    @Command
    public void setFirstPersonheldItemMountPointRotation(@CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z) {
        FirstPersonHeldItemMountPointComponent newComponent = localPlayer.getCameraEntity().getComponent(FirstPersonHeldItemMountPointComponent.class);
        if (newComponent != null) {
            newComponent.rotateDegrees = new Vector3f(x, y, z);
            ensureClientSideEntityOnHeldItemMountPoint(OnActivatedComponent.newInstance(), localPlayer.getCameraEntity(), newComponent);
        }
    }

    @ReceiveEvent
    public void onHeldItemChanged(HeldItemChangedEvent event, EntityRef character, CharacterComponent characterComponents) {
        if (character.equals(localPlayer.getCharacterEntity()) && !event.getNewItem().equals(event.getOldItem())) {
            EntityRef camera = localPlayer.getCameraEntity();
            FirstPersonHeldItemMountPointComponent mountPointComponent = camera.getComponent(FirstPersonHeldItemMountPointComponent.class);
            if (mountPointComponent != null) {
                EntityRef heldItem = event.getNewItem();
                if (!heldItem.exists()) {
                    heldItem = handEntity;
                }

                FirstPersonHeldItemTransformComponent heldItemTransformComponent = heldItem.getComponent(FirstPersonHeldItemTransformComponent.class);

                //ensure the item has a location
                heldItem.addOrSaveComponent(new LocationComponent());

                // remove the location from the old item
                if (event.getOldItem().exists()) {
                    Location.removeChild(mountPointComponent.mountPointEntity, event.getOldItem());
                    event.getOldItem().removeComponent(LocationComponent.class);
                } else {
                    Location.removeChild(mountPointComponent.mountPointEntity, handEntity);
                    handEntity.removeComponent(LocationComponent.class);
                }

                Location.attachChild(mountPointComponent.mountPointEntity, heldItem,
                        heldItemTransformComponent.translate,
                        new Quat4f(
                                TeraMath.DEG_TO_RAD * heldItemTransformComponent.rotateDegrees.y,
                                TeraMath.DEG_TO_RAD * heldItemTransformComponent.rotateDegrees.x,
                                TeraMath.DEG_TO_RAD * heldItemTransformComponent.rotateDegrees.z),
                        heldItemTransformComponent.scale);
            }
        }
    }

    @Override
    public void postBegin() {
        // create the hand entity
        EntityBuilder entityBuilder = entityManager.newBuilder("engine:hand");
        entityBuilder.setPersistent(false);
        handEntity = entityBuilder.build();
    }

    @ReceiveEvent
    public void onItemComponentAdded(OnActivatedComponent event, EntityRef item, ItemComponent itemComponent) {
        ItemPickupSystem.addOrUpdateItemRendering(itemComponent, item);
    }

    @ReceiveEvent
    public void onItemComponentAdded(OnActivatedComponent event, EntityRef item, BlockItemComponent blockItemComponent) {
        ItemPickupSystem.addOrUpdateBlockRendering(blockItemComponent, item);
    }

    /**
     * modifies the held item mount point to move the held item in first person view
     */
    @ReceiveEvent
    public void animateHeldItemUse(OnChangedComponent event, EntityRef character, CharacterHeldItemComponent characterHeldItemComponent) {
        if (localPlayer.getCharacterEntity().equals(character)) {
            // get the first person mount point and rotate it away from the camera
            FirstPersonHeldItemMountPointComponent mountPointComponent = localPlayer.getCameraEntity().getComponent(FirstPersonHeldItemMountPointComponent.class);
            LocationComponent locationComponent = mountPointComponent.mountPointEntity.getComponent(LocationComponent.class);

            float addPitch = 0f;
            float addYaw = 0f;
            if (characterHeldItemComponent.handAnimation != 0) {
                addPitch = 15f;
                addYaw = 10f;
            }
            locationComponent.setLocalRotation(new Quat4f(
                    TeraMath.DEG_TO_RAD * (mountPointComponent.rotateDegrees.y + addYaw),
                    TeraMath.DEG_TO_RAD * (mountPointComponent.rotateDegrees.x + addPitch),
                    TeraMath.DEG_TO_RAD * mountPointComponent.rotateDegrees.z));

            Vector3f offset = new Vector3f();
            if (characterHeldItemComponent.handAnimation != 0) {
                offset = new Vector3f(0.25f, -0.12f, 0f);
            }
            offset.add(mountPointComponent.translate);
            locationComponent.setLocalPosition(offset);

            mountPointComponent.mountPointEntity.saveComponent(locationComponent);
        }
    }
}
