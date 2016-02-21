/*
 * Copyright 2015 MovingBlocks
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

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.world.WorldRenderer;

/**
 */
@RegisterSystem(RegisterMode.CLIENT)
public class FirstPersonClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final int USEANIMATIONLENGTH = 200;

    @In
    private LocalPlayer localPlayer;
    @In
    private WorldRenderer worldRenderer;
    @In
    private EntityManager entityManager;
    @In
    private Time time;

    private EntityRef handEntity;
    private EntityRef currentHeldItem;
    private boolean relinkHeldItem;

    private EntityRef getHandEntity() {
        if (handEntity == null) {
            // create the hand entity
            EntityBuilder entityBuilder = entityManager.newBuilder("engine:hand");
            entityBuilder.setPersistent(false);
            handEntity = entityBuilder.build();
        }
        return handEntity;
    }

    @ReceiveEvent
    public void ensureClientSideEntityOnHeldItemMountPoint(OnActivatedComponent event, EntityRef camera,
                                                           FirstPersonHeldItemMountPointComponent firstPersonHeldItemMountPointComponent) {
        if (!firstPersonHeldItemMountPointComponent.mountPointEntity.exists()) {
            EntityBuilder builder = entityManager.newBuilder("engine:FirstPersonHeldItemMountPoint");
            builder.setPersistent(false);
            firstPersonHeldItemMountPointComponent.mountPointEntity = builder.build();
            camera.saveComponent(firstPersonHeldItemMountPointComponent);
        }

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

    @ReceiveEvent
    public void ensureHeldItemIsMountedOnLoad(OnChangedComponent event, EntityRef entityRef, ClientComponent clientComponent) {
        if (localPlayer.getClientEntity().equals(entityRef) && localPlayer.getCharacterEntity().exists() && localPlayer.getCameraEntity().exists()) {
            CharacterHeldItemComponent characterHeldItemComponent = localPlayer.getCharacterEntity().getComponent(CharacterHeldItemComponent.class);
            if (characterHeldItemComponent != null) {
                // special case of sending in null so that the initial load works
                linkHeldItemLocationForLocalPlayer(localPlayer.getCharacterEntity(), characterHeldItemComponent.selectedItem, null);
            }
        }
    }

    @Command(shortDescription = "Sets the held item mount point translation for the first person view")
    public void setFirstPersonheldItemMountPointTranslation(@CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z) {
        FirstPersonHeldItemMountPointComponent newComponent = localPlayer.getCameraEntity().getComponent(FirstPersonHeldItemMountPointComponent.class);
        if (newComponent != null) {
            newComponent.translate = new Vector3f(x, y, z);
            ensureClientSideEntityOnHeldItemMountPoint(OnActivatedComponent.newInstance(), localPlayer.getCameraEntity(), newComponent);
        }
    }

    @Command(shortDescription = "Sets the held item mount point rotation for the first person view")
    public void setFirstPersonheldItemMountPointRotation(@CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z) {
        FirstPersonHeldItemMountPointComponent newComponent = localPlayer.getCameraEntity().getComponent(FirstPersonHeldItemMountPointComponent.class);
        if (newComponent != null) {
            newComponent.rotateDegrees = new Vector3f(x, y, z);
            ensureClientSideEntityOnHeldItemMountPoint(OnActivatedComponent.newInstance(), localPlayer.getCameraEntity(), newComponent);
        }
    }

    @ReceiveEvent
    public void onHeldItemActivated(OnActivatedComponent event, EntityRef character, CharacterHeldItemComponent heldItemComponent, CharacterComponent characterComponents) {
        if (localPlayer.getCharacterEntity().equals(character)) {
            EntityRef oldHeldItem = currentHeldItem;
            currentHeldItem = heldItemComponent.selectedItem;
            linkHeldItemLocationForLocalPlayer(character, currentHeldItem, oldHeldItem);
        }
    }

    @ReceiveEvent
    public void onHeldItemChanged(OnChangedComponent event, EntityRef character, CharacterHeldItemComponent heldItemComponent, CharacterComponent characterComponents) {
        if (localPlayer.getCharacterEntity().equals(character)) {
            EntityRef oldHeldItem = currentHeldItem;
            currentHeldItem = heldItemComponent.selectedItem;
            linkHeldItemLocationForLocalPlayer(character, currentHeldItem, oldHeldItem);
        }
    }

    @ReceiveEvent(netFilter = RegisterMode.CLIENT)
    public void onLocationRemovedAddClientSideLocation(BeforeDeactivateComponent event, EntityRef entityRef, LocationComponent locationComponent) {
        // when in a remote client situation,  the remove LocationComponent happens after the CharacterHeldItemComponent is changed.
        // So this allows re-adding the client side LocationComponent after this
        if (entityRef.equals(currentHeldItem)) {
            relinkHeldItem = true;
        }
    }

    void linkHeldItemLocationForLocalPlayer(EntityRef character, EntityRef newItem, EntityRef oldItem) {
        if (!newItem.equals(oldItem)) {
            EntityRef camera = localPlayer.getCameraEntity();
            FirstPersonHeldItemMountPointComponent mountPointComponent = camera.getComponent(FirstPersonHeldItemMountPointComponent.class);
            if (mountPointComponent != null) {
                // remove the location from the old item
                if (oldItem != null && oldItem.exists()) {
                    Location.removeChild(mountPointComponent.mountPointEntity, oldItem);
                    oldItem.removeComponent(LocationComponent.class);
                    oldItem.removeComponent(ItemIsHeldComponent.class);
                } else {
                    Location.removeChild(mountPointComponent.mountPointEntity, getHandEntity());
                    getHandEntity().removeComponent(LocationComponent.class);
                    getHandEntity().removeComponent(ItemIsHeldComponent.class);
                }

                // use the hand if there is no new item
                EntityRef heldItem = newItem;
                if (!heldItem.exists()) {
                    heldItem = getHandEntity();
                }

                //ensure the item has a location
                heldItem.addOrSaveComponent(new LocationComponent());
                heldItem.addOrSaveComponent(new ItemIsHeldComponent());

                FirstPersonHeldItemTransformComponent heldItemTransformComponent = heldItem.getComponent(FirstPersonHeldItemTransformComponent.class);
                if (heldItemTransformComponent == null) {
                    heldItemTransformComponent = new FirstPersonHeldItemTransformComponent();
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

    /**
     * modifies the held item mount point to move the held item in first person view
     */
    @Override
    public void update(float delta) {
        // relink the held item if its location got removed from an item transition that removed its location
        if (relinkHeldItem) {
            linkHeldItemLocationForLocalPlayer(localPlayer.getCharacterEntity(), currentHeldItem, null);
            relinkHeldItem = false;
        }

        // ensure that there are no lingering items that are marked as still held. This situation happens with client side predicted items
        for (EntityRef entityRef : entityManager.getEntitiesWith(ItemIsHeldComponent.class)) {
            if (!entityRef.equals(currentHeldItem) && !entityRef.equals(handEntity)) {
                entityRef.removeComponent(ItemIsHeldComponent.class);

                // also remove the location if it is currently location linked to the mount point
                EntityRef camera = localPlayer.getCameraEntity();
                FirstPersonHeldItemMountPointComponent mountPointComponent = camera.getComponent(FirstPersonHeldItemMountPointComponent.class);
                LocationComponent locationComponent = entityRef.getComponent(LocationComponent.class);
                if (mountPointComponent != null && locationComponent != null && locationComponent.getParent().equals(mountPointComponent.mountPointEntity)) {
                    entityRef.removeComponent(LocationComponent.class);
                }
            }
        }

        // get the first person mount point and rotate it away from the camera
        CharacterHeldItemComponent characterHeldItemComponent = localPlayer.getCharacterEntity().getComponent(CharacterHeldItemComponent.class);
        FirstPersonHeldItemMountPointComponent mountPointComponent = localPlayer.getCameraEntity().getComponent(FirstPersonHeldItemMountPointComponent.class);
        if (characterHeldItemComponent == null
                || mountPointComponent == null) {
            return;
        }

        LocationComponent locationComponent = mountPointComponent.mountPointEntity.getComponent(LocationComponent.class);
        if (locationComponent == null) {
            return;
        }

        long timeElapsedSinceLastUsed = time.getGameTimeInMs() - characterHeldItemComponent.lastItemUsedTime;
        float animateAmount = 0f;
        if (timeElapsedSinceLastUsed < USEANIMATIONLENGTH) {
            // half way through the animation will be the maximum extent of rotation and translation
            animateAmount = 1f - Math.abs(((float) timeElapsedSinceLastUsed / (float) USEANIMATIONLENGTH) - 0.5f);
        }
        float addPitch = 15f * animateAmount;
        float addYaw = 10f * animateAmount;
        locationComponent.setLocalRotation(new Quat4f(
                TeraMath.DEG_TO_RAD * (mountPointComponent.rotateDegrees.y + addYaw),
                TeraMath.DEG_TO_RAD * (mountPointComponent.rotateDegrees.x + addPitch),
                TeraMath.DEG_TO_RAD * mountPointComponent.rotateDegrees.z));
        Vector3f offset = new Vector3f(0.25f * animateAmount, -0.12f * animateAmount, 0f);
        offset.add(mountPointComponent.translate);
        locationComponent.setLocalPosition(offset);

        mountPointComponent.mountPointEntity.saveComponent(locationComponent);
    }
}
