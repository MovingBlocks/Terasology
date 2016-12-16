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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.rendering.world.WorldRenderer;

/**
 */
@RegisterSystem(RegisterMode.CLIENT)
public class FirstPersonClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final int USEANIMATIONLENGTH = 200;

    /** Lists the component classes which are needed to render a held item */
    private static final Collection<Class<? extends Component>> NEEDED_COMPONENTS_FOR_RENDERING =
            Arrays.asList(
                MeshComponent.class,
                FirstPersonHeldItemTransformComponent.class);

    @In
    private LocalPlayer localPlayer;
    @In
    private WorldRenderer worldRenderer;
    @In
    private EntityManager entityManager;
    @In
    private Time time;

    private EntityRef handEntity;

    // the item from the inventory synchronized with the server
    private EntityRef currentHeldItem = EntityRef.NULL;

    // the client side entity representing the hold item
    private EntityRef clientHeldItem = EntityRef.NULL;

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

    /**
     * Changes held item entity.
     *
     * <p>Detaches old held item and removes it's components. Adds components to new held item and
     * attaches it to the mount point entity.</p>
     */
    private void linkHeldItemLocationForLocalPlayer(EntityRef character, EntityRef newItem, EntityRef oldItem) {
        if (!newItem.equals(oldItem)) {
            EntityRef camera = localPlayer.getCameraEntity();
            FirstPersonHeldItemMountPointComponent mountPointComponent = camera.getComponent(FirstPersonHeldItemMountPointComponent.class);
            if (mountPointComponent != null) {

                if (clientHeldItem.exists()) {
                    clientHeldItem.destroy();
                    clientHeldItem = EntityRef.NULL;
                }


                // remove the location from the old item
                if (oldItem != null && oldItem.exists()) {
                    oldItem.removeComponent(ItemIsHeldComponent.class);
                } else {
                    getHandEntity().removeComponent(ItemIsHeldComponent.class);
                }

                // use the hand if there is no new item
                EntityRef heldItem;
                if (!newItem.exists()) {
                    heldItem = getHandEntity();
                } else {
                    heldItem = newItem;
                }

                // create client side held item entity
                clientHeldItem = heldItem.copy();

                // remove unneeded/unused components for display only
                removeUnusedClientHeldComponents(clientHeldItem);

                clientHeldItem.addOrSaveComponent(new LocationComponent());

                heldItem.addOrSaveComponent(new ItemIsHeldComponent());

                FirstPersonHeldItemTransformComponent heldItemTransformComponent = clientHeldItem.getComponent(FirstPersonHeldItemTransformComponent.class);
                if (heldItemTransformComponent == null) {
                    heldItemTransformComponent = new FirstPersonHeldItemTransformComponent();
                    clientHeldItem.addComponent(heldItemTransformComponent);
                }
                Location.attachChild(mountPointComponent.mountPointEntity, clientHeldItem,
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
     * Removes all components from an entity which are not needed for client side rendering of the
     * held item.
     * @param heldItem The item from which to remove the components
     */
    private void removeUnusedClientHeldComponents(EntityRef heldItem) {
        Collection<Class<? extends Component>> componentsToRemove = new ArrayList<>();
        for (Component component : heldItem.iterateComponents()) {
            if (!NEEDED_COMPONENTS_FOR_RENDERING.contains(component.getClass())) {
                componentsToRemove.add(component.getClass());
            }
        }

        for (Class<? extends Component> componentToRemove : componentsToRemove) {
            heldItem.removeComponent(componentToRemove);
        }
    }

    /**
     * modifies the held item mount point to move the held item in first person view
     */
    @Override
    public void update(float delta) {

        // ensure empty hand is shown if no item is hold at the moment
        if (!currentHeldItem.exists() && clientHeldItem != getHandEntity()) {
            linkHeldItemLocationForLocalPlayer(localPlayer.getCharacterEntity(), currentHeldItem, null);
        }

        // ensure that there are no lingering items that are marked as still held. This situation happens with client side predicted items
        for (EntityRef entityRef : entityManager.getEntitiesWith(ItemIsHeldComponent.class)) {
            if (!entityRef.equals(currentHeldItem) && !entityRef.equals(handEntity)) {
                entityRef.removeComponent(ItemIsHeldComponent.class);
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
