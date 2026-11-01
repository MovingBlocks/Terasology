// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import com.google.common.collect.Sets;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.characters.CharacterHeldItemComponent;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.location.Location;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.VisualComponent;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.util.Iterator;
import java.util.Set;

/**
 * This client system handles displaying held items for all remote players in multiplayer session.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ThirdPersonRemoteClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(ThirdPersonRemoteClientSystem.class);

    private static final int USEANIMATIONLENGTH = 200;

    @In
    private LocalPlayer localPlayer;

    @In
    private LocalPlayerSystem localPlayerSystem;

    @In
    private EntityManager entityManager;

    @In
    private Time time;

    /**
     * Ensures held item mount point entity exists, attaches it to the character and sets its transform.
     *
     * @param event the activation that triggered the need to consider changing a held item
     * @param character the character for which we need to consider the held item
     * @param remotePersonHeldItemMountPointComponent data for the mount point on the remote character
     */
    @ReceiveEvent
    public void ensureClientSideEntityOnHeldItemMountPoint(OnActivatedComponent event, EntityRef character,
                                                           RemotePersonHeldItemMountPointComponent remotePersonHeldItemMountPointComponent) {
        if (relatesToLocalPlayer(character)) {
            logger.debug("ensureClientSideEntityOnHeldItemMountPoint found its given character to relate to the local" +
                    " player, ignoring: {}", character);
            return;
        }

        // In case we haven't dealt with a given remote player yet set up a non-persistent mount point
        if (!remotePersonHeldItemMountPointComponent.mountPointEntity.exists()) {
            EntityBuilder builder = entityManager.newBuilder("engine:RemotePersonHeldItemMountPoint");
            builder.setPersistent(false);
            remotePersonHeldItemMountPointComponent.mountPointEntity = builder.build();
            character.saveComponent(remotePersonHeldItemMountPointComponent);
        }

        // Link the mount point entity to the camera
        Location.removeChild(character, remotePersonHeldItemMountPointComponent.mountPointEntity);
        Location.attachChild(character, remotePersonHeldItemMountPointComponent.mountPointEntity,
                remotePersonHeldItemMountPointComponent.translate,
                new Quaternionf().rotationYXZ(
                        Math.toRadians(remotePersonHeldItemMountPointComponent.rotateDegrees.y),
                        Math.toRadians(remotePersonHeldItemMountPointComponent.rotateDegrees.x),
                        Math.toRadians(remotePersonHeldItemMountPointComponent.rotateDegrees.z)),
                remotePersonHeldItemMountPointComponent.scale);

    }

    @ReceiveEvent
    public void ensureHeldItemIsMountedOnLoad(OnChangedComponent event, EntityRef clientEntity,
                                              ClientComponent clientComponent) {
        if (relatesToLocalPlayer(clientEntity)) {
            logger.debug("ensureHeldItemIsMountedOnLoad found its given clientEntity to relate to the local player, " +
                    "ignoring: {}", clientEntity);
            return;
        }

        if (clientEntity.exists() && clientComponent.character != EntityRef.NULL) {
            logger.debug("ensureHeldItemIsMountedOnLoad says a given clientEntity exists, has a character, and isn't " +
                    "related to the local player: {}", clientEntity);
            CharacterHeldItemComponent characterHeldItemComponent =
                    clientComponent.character.getComponent(CharacterHeldItemComponent.class);
            if (characterHeldItemComponent != null && !(clientComponent.character.equals(localPlayer.getCharacterEntity()))) {
                linkHeldItemLocationForRemotePlayer(characterHeldItemComponent.selectedItem, clientComponent.character);
            }
        } else {
            logger.debug("ensureHeldItemIsMountedOnLoad given a remote client, but one that didn't properly exist?");
        }
    }

    @Command(shortDescription = "Sets the held item mount point translation for remote characters")
    public void setRemotePlayersHeldItemMountPointTranslations(@CommandParam("x") float x, @CommandParam("y") float y, @CommandParam("z") float z) {
        for (EntityRef remotePlayer : entityManager.getEntitiesWith(RemotePersonHeldItemMountPointComponent.class)) {
            RemotePersonHeldItemMountPointComponent remoteMountPointComponent =
                    remotePlayer.getComponent(RemotePersonHeldItemMountPointComponent.class);
            remoteMountPointComponent.translate.set(x, y, z);
        }
    }

    @Command(shortDescription = "Sets the held item mount point rotation for remote characters")
    public void setRemotePlayersHeldItemMountPointRotations(@CommandParam("x") float x, @CommandParam("y") float y,
                                                            @CommandParam("z") float z) {
        for (EntityRef remotePlayer : entityManager.getEntitiesWith(RemotePersonHeldItemMountPointComponent.class)) {
            RemotePersonHeldItemMountPointComponent remoteMountPointComponent =
                    remotePlayer.getComponent(RemotePersonHeldItemMountPointComponent.class);
            remoteMountPointComponent.rotateDegrees.set(x, y, z);
        }
    }

    @ReceiveEvent
    public void onHeldItemActivated(OnActivatedComponent event, EntityRef player,
                                    CharacterHeldItemComponent heldItemComponent,
                                    CharacterComponent characterComponents) {
        if (relatesToLocalPlayer(player)) {
            logger.debug("onHeldItemActivated found its given player to relate to the local player, ignoring: {}",
                    player);
            return;
        }

        logger.debug("onHeldItemActivated says the given player is not the local player's character entity: {}",
                player);
        EntityRef newItem = heldItemComponent.selectedItem;
        linkHeldItemLocationForRemotePlayer(newItem, player);
    }

    @ReceiveEvent
    public void onHeldItemChanged(OnChangedComponent event, EntityRef character,
                                  CharacterHeldItemComponent heldItemComponent,
                                  CharacterComponent characterComponents) {
        if (relatesToLocalPlayer(character)) {
            logger.debug("onHeldItemChanged found its given character to relate to the local player, ignoring: {}",
                    character);
            return;
        }

        logger.debug("onHeldItemChanged says the given character is not the local player's character entity: {}",
                character);
        EntityRef newItem = heldItemComponent.selectedItem;
        linkHeldItemLocationForRemotePlayer(newItem, character);
    }

    /**
     * Changes held item entity.
     * <p>
     * Detaches old held item and removes its components. Adds components to new held item and attaches it to the mount
     * point entity.
     */
    private void linkHeldItemLocationForRemotePlayer(EntityRef newItem, EntityRef player) {
        if (relatesToLocalPlayer(player)) {
            logger.debug("linkHeldItemLocationForRemotePlayer called with an entity that relates to the local player," +
                    " ignoring{}", player);
            return;
        }

        // Find out if there is a current held item that maps to this player
        EntityRef currentHeldItem = EntityRef.NULL;
        for (EntityRef heldItemCandidate : entityManager.getEntitiesWith(ItemIsRemotelyHeldComponent.class)) {
            EntityRef remotePlayerCandidate =
                    heldItemCandidate.getComponent(ItemIsRemotelyHeldComponent.class).remotePlayer;
            logger.debug("For held item candidate {} got its player candidate as {}", heldItemCandidate,
                    remotePlayerCandidate);
            if (remotePlayerCandidate.equals(player)) {
                logger.debug("Thinking we found a match with player {} so counting this held item as relevant for " +
                        "processing", player);
                currentHeldItem = heldItemCandidate;
                // If we found an existing item yet the situation calls for emptying the players hand then we just
                // need to remove the old item
                if (newItem.equals(EntityRef.NULL)) {
                    logger.debug("Found an existing held item but the new request was to no longer hold anything so " +
                            "destroying {}", currentHeldItem);
                    currentHeldItem.destroy();
                    return;
                }
                break;
            }
        }

        // In the case of an actual change of item other than an empty hand we need to hook up a new held item entity
        if (newItem != null && !newItem.equals(EntityRef.NULL) && !newItem.equals(currentHeldItem)) {
            RemotePersonHeldItemMountPointComponent mountPointComponent =
                    player.getComponent(RemotePersonHeldItemMountPointComponent.class);
            if (mountPointComponent != null) {

                //currentHeldItem is at this point the old item
                if (currentHeldItem != EntityRef.NULL) {
                    currentHeldItem.destroy();
                }

                currentHeldItem = entityManager.create();
                logger.debug("linkHeldItemLocationForRemotePlayer is now creating a new held item {}", currentHeldItem);

                // add the visually relevant components
                for (Component component : newItem.iterateComponents()) {
                    if (component instanceof VisualComponent && !(component instanceof FirstPersonHeldItemTransformComponent)) {
                        currentHeldItem.addComponent(component);
                    }
                }

                // ensure world location is set
                currentHeldItem.addComponent(new LocationComponent());

                // Map this held item to the player it is held by
                ItemIsRemotelyHeldComponent itemIsRemotelyHeldComponent = new ItemIsRemotelyHeldComponent();
                itemIsRemotelyHeldComponent.remotePlayer = player;
                currentHeldItem.addComponent(itemIsRemotelyHeldComponent);

                RemotePersonHeldItemTransformComponent heldItemTransformComponent =
                        currentHeldItem.getComponent(RemotePersonHeldItemTransformComponent.class);
                if (heldItemTransformComponent == null) {
                    heldItemTransformComponent = new RemotePersonHeldItemTransformComponent();
                    currentHeldItem.addComponent(heldItemTransformComponent);
                }

                Location.attachChild(mountPointComponent.mountPointEntity, currentHeldItem,
                        heldItemTransformComponent.translate,
                        new Quaternionf().rotationYXZ(
                                Math.toRadians(heldItemTransformComponent.rotateDegrees.y),
                                Math.toRadians(heldItemTransformComponent.rotateDegrees.x),
                                Math.toRadians(heldItemTransformComponent.rotateDegrees.z)),
                        heldItemTransformComponent.scale);
            }
        } else {
            logger.info("Somehow ended up in the else during linkHeldItemLocationForRemotePlayer - current item was " +
                    "{} and new item {}", currentHeldItem, newItem);
        }
    }

    /**
     * Modifies the remote players' held item mount points to show and move their held items at their location. Clean up
     * no longer needed held item entities.
     * <p>
     * TODO: Also responsible for catching characters without current held item entities and then create them. Should be
     * moved elsewhere
     */
    @Override
    public void update(float delta) {

        // Make a set of all held items that exist so we can review them and later toss any no longer needed
        Set<EntityRef> heldItemsForReview =
                Sets.newHashSet(entityManager.getEntitiesWith(ItemIsRemotelyHeldComponent.class));

        // Note that the inclusion of PlayerCharacterComponent excludes "characters" like Gooey. In the future such
        // critters may also want held items
        for (EntityRef remotePlayer : entityManager.getEntitiesWith(CharacterComponent.class,
                PlayerCharacterComponent.class)) {
            if (relatesToLocalPlayer(remotePlayer)) {
                continue;
            }

            // Find the associated held item entity for this player, if one exists
            EntityRef currentHeldItem = EntityRef.NULL;
            Iterator<EntityRef> heldItermsIterator = heldItemsForReview.iterator();
            while (heldItermsIterator.hasNext()) {
                EntityRef heldItemCandidate = heldItermsIterator.next();
                ItemIsRemotelyHeldComponent itemIsRemotelyHeldComponent =
                        heldItemCandidate.getComponent(ItemIsRemotelyHeldComponent.class);
                if (itemIsRemotelyHeldComponent.remotePlayer.equals(remotePlayer)) {
                    currentHeldItem = heldItemCandidate;
                    heldItermsIterator.remove();
                    break;
                }
            }


            // If an associated held item entity does *not* exist yet, consider making one if the player has an item
            // selected
            if (currentHeldItem == EntityRef.NULL && remotePlayer.hasComponent(CharacterHeldItemComponent.class)) {
                CharacterHeldItemComponent characterHeldItemComponent =
                        remotePlayer.getComponent(CharacterHeldItemComponent.class);
                if (characterHeldItemComponent != null && !characterHeldItemComponent.selectedItem.equals(EntityRef.NULL)) {
                    linkHeldItemLocationForRemotePlayer(remotePlayer.getComponent(CharacterHeldItemComponent.class).selectedItem, remotePlayer);
                }
            }

            // get the remote person mount point
            CharacterHeldItemComponent characterHeldItemComponent =
                    remotePlayer.getComponent(CharacterHeldItemComponent.class);
            RemotePersonHeldItemMountPointComponent mountPointComponent =
                    remotePlayer.getComponent(RemotePersonHeldItemMountPointComponent.class);
            if (characterHeldItemComponent == null || mountPointComponent == null) {
                continue;
            }

            LocationComponent locationComponent =
                    mountPointComponent.mountPointEntity.getComponent(LocationComponent.class);
            if (locationComponent == null) {
                continue;
            }

            long timeElapsedSinceLastUsed = time.getGameTimeInMs() - characterHeldItemComponent.lastItemUsedTime;
            float animateAmount = 0f;
            if (timeElapsedSinceLastUsed < USEANIMATIONLENGTH) {
                //TODO add other easing functions into utilities and use here?
                // half way through the animation will be the maximum extent of rotation and translation
                animateAmount = 1f - Math.abs(((float) timeElapsedSinceLastUsed / (float) USEANIMATIONLENGTH) - 0.5f);
            }
            float addPitch = 15f * animateAmount;
            float addYaw = 10f * animateAmount;
            locationComponent.setLocalRotation(new Quaternionf().rotationYXZ(
                    Math.toRadians(mountPointComponent.rotateDegrees.y + addYaw),
                    Math.toRadians(mountPointComponent.rotateDegrees.x + addPitch),
                    Math.toRadians(mountPointComponent.rotateDegrees.z)));
            Vector3f offset = new Vector3f(0.05f * animateAmount, -0.24f * animateAmount, 0f);
            offset.add(mountPointComponent.translate);
            locationComponent.setLocalPosition(offset);

            mountPointComponent.mountPointEntity.saveComponent(locationComponent);
        }

        heldItemsForReview.forEach(remainingHeldItem -> {
            if (remainingHeldItem.exists()) {
                remainingHeldItem.destroy();
            }
        });
    }

    @Override
    public void postBegin() {
        /*
        // Go through all known remote players already present and make sure they have their currently equipped items
         defined
        // TODO: This catches the scenario in which a player logs in and can see other already-connected players'
            held items
        // But it doesn't cover when a new player then connects later. Only when that player takes an action causing
        an event handled in this System
        // The if (currentHeldItem == EntityRef.NULL) block in the update method catches BOTH scenarios, but is in a
        tick-loop
        // This snippet plus a separate fix that catches join events by other players should be able to do the work
        as two one-time events only
        for (EntityRef remotePlayer : entityManager.getEntitiesWith(CharacterComponent.class,
                                                                    PlayerCharacterComponent.class,
                                                                    CharacterHeldItemComponent.class,
                                                                    RemotePersonHeldItemMountPointComponent.class)) {
            if (!relatesToLocalPlayer(remotePlayer)) {
                logger.info("Found a remote player to process during postBegin, selected item is {}", remotePlayer
                .getComponent(CharacterHeldItemComponent.class).selectedItem);
                linkHeldItemLocationForRemotePlayer(remotePlayer.getComponent(CharacterHeldItemComponent.class)
                .selectedItem, remotePlayer);
            }
        }
        */
    }

    /**
     * Checks a given entity in a variety of ways to see if it is immediately related to a local player. TODO: Is a bit
     * of a shotgun blast approach to throwing out undesired player/client/character entities. Needs a more surgical
     * approach.
     *
     * @param entity the entity to check (probably a player, client, or character entity)
     * @return true if any such check passes, otherwise false
     */
    private boolean relatesToLocalPlayer(EntityRef entity) {
        if (entity == null || entity.equals(EntityRef.NULL)) {
            return false;
        }

        if (entity.equals(localPlayer.getClientEntity())) {
            return true;
        }

        if (entity.equals(localPlayer.getCharacterEntity())) {
            return true;
        }

        if (entity.equals(localPlayer.getClientInfoEntity())) {
            return true;
        }

        // In case we're in a scenario where localPlayer is unreliable this is an alternative way to check
        // This was needed in one case with headless + one client where an event triggered when localPlayer wasn't
        // set right
        EntityRef networkSystemProvidedClientEntity = localPlayerSystem.getClientEntityViaNetworkSystem();
        if (entity.equals(networkSystemProvidedClientEntity)) {
            logger.debug("checkForLocalPlayer found its entity to match the network system provided local client " +
                    "entity! {}", entity);
        }

        if (entity.hasComponent(CharacterComponent.class)) {
            EntityRef controller = entity.getComponent(CharacterComponent.class).controller;

            if (controller != null) {
                if (controller.equals(localPlayer.getClientEntity())) {
                    return true;
                }

                if (controller.equals(networkSystemProvidedClientEntity)) {
                    return true;
                }
            }
        }

        return false;
    }
}
