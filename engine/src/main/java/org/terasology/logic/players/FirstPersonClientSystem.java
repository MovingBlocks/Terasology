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
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
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
import org.terasology.rendering.logic.VisualComponent;
import org.terasology.rendering.world.WorldRenderer;

@RegisterSystem(RegisterMode.CLIENT)
public class FirstPersonClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final int USEANIMATIONLENGTH = 200;

    @In
    private LocalPlayer localPlayer;
    @In
    private EntityManager entityManager;
    @In
    private Time time;

    private EntityRef handEntity;

    // the item from the inventory synchronized with the server
    private EntityRef currentHeldItem = EntityRef.NULL;

    private EntityRef getHandEntity() {
        if (handEntity == null) {
            // create the hand entity
            EntityBuilder entityBuilder = entityManager.newBuilder("engine:hand");
            entityBuilder.setPersistent(false);
            handEntity = entityBuilder.build();
        }
        return handEntity;
    }

    // ensures held item mount point entity exists, attaches it to the camera and sets its transform
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
                linkHeldItemLocationForLocalPlayer(characterHeldItemComponent.selectedItem);
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
            EntityRef newItem = heldItemComponent.selectedItem;
            linkHeldItemLocationForLocalPlayer(newItem);
        }
    }

    @ReceiveEvent
    public void onHeldItemChanged(OnChangedComponent event, EntityRef character, CharacterHeldItemComponent heldItemComponent, CharacterComponent characterComponents) {
        if (localPlayer.getCharacterEntity().equals(character)) {
            EntityRef newItem = heldItemComponent.selectedItem;
            linkHeldItemLocationForLocalPlayer(newItem);
        }
    }

    /**
     * Changes held item entity.
     *
     * <p>Detaches old held item and removes it's components. Adds components to new held item and
     * attaches it to the mount point entity.</p>
     */
    private void linkHeldItemLocationForLocalPlayer(EntityRef newItem) {
        if (!newItem.equals(currentHeldItem)) {
            EntityRef camera = localPlayer.getCameraEntity();
            FirstPersonHeldItemMountPointComponent mountPointComponent = camera.getComponent(FirstPersonHeldItemMountPointComponent.class);
            if (mountPointComponent != null) {

                //currentHeldItem is at this point the old item
                if (currentHeldItem != EntityRef.NULL) {
                    currentHeldItem.destroy();
                }

                // use the hand if there is no new item
                EntityRef newHeldItem;
                if (newItem == EntityRef.NULL) {
                    newHeldItem = getHandEntity();
                } else {
                    newHeldItem = newItem;
                }

                // create client side held item entity
                currentHeldItem = entityManager.create();

                // add the visually relevant components
                for (Component component : newHeldItem.iterateComponents()) {
                    if (component instanceof VisualComponent) {
                        currentHeldItem.addComponent(component);
                    }
                }

                // ensure world location is set
                currentHeldItem.addComponent(new LocationComponent());
                currentHeldItem.addComponent(new ItemIsHeldComponent());

                FirstPersonHeldItemTransformComponent heldItemTransformComponent = currentHeldItem.getComponent(FirstPersonHeldItemTransformComponent.class);
                if (heldItemTransformComponent == null) {
                    heldItemTransformComponent = new FirstPersonHeldItemTransformComponent();
                    currentHeldItem.addComponent(heldItemTransformComponent);
                }

                Location.attachChild(mountPointComponent.mountPointEntity, currentHeldItem,
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

        // ensure empty hand is shown if no item is hold at the moment
        if (!currentHeldItem.exists() && currentHeldItem != getHandEntity()) {
            linkHeldItemLocationForLocalPlayer(getHandEntity());
        }

        // ensure that there are no lingering items that are marked as still held. This situation happens with client side predicted items
        for (EntityRef entityRef : entityManager.getEntitiesWith(ItemIsHeldComponent.class)) {
            if (!entityRef.equals(currentHeldItem) && !entityRef.equals(handEntity)) {
                entityRef.destroy();
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

    @Override
    public void preSave() {
        if (currentHeldItem != EntityRef.NULL) {
            currentHeldItem.destroy();
        }
    }
}
