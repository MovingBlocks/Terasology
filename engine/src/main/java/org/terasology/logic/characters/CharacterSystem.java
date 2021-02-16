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

package org.terasology.logic.characters;

import com.google.common.collect.Sets;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.input.binds.interaction.AttackButton;
import org.terasology.input.cameraTarget.PlayerTargetSystem;
import org.terasology.logic.characters.events.ActivationRequest;
import org.terasology.logic.characters.events.ActivationRequestDenied;
import org.terasology.logic.characters.events.AttackEvent;
import org.terasology.logic.characters.events.AttackRequest;
import org.terasology.logic.characters.events.DeathEvent;
import org.terasology.logic.characters.events.OnItemUseEvent;
import org.terasology.logic.characters.events.OnScaleEvent;
import org.terasology.logic.characters.events.PlayerDeathEvent;
import org.terasology.logic.characters.interactions.InteractionUtil;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.health.BeforeDestroyEvent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.recording.DirectionAndOriginPosRecorderList;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.recording.RecordAndReplayStatus;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.ActAsBlockComponent;

import java.util.Optional;

/**
 *
 */
@RegisterSystem
public class CharacterSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    public static final CollisionGroup[] DEFAULTPHYSICSFILTER = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD, StandardCollisionGroup.CHARACTER};
    private static final Logger logger = LoggerFactory.getLogger(CharacterSystem.class);

    @In
    private Physics physics;

    @In
    private PhysicsEngine physicsEngine;

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    @In
    private Time time;

    @In
    private PlayerTargetSystem targetSystem;

    @In
    private BlockEntityRegistry blockRegistry;

    @In
    private DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList;

    @In
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    @ReceiveEvent
    public void beforeDestroy(BeforeDestroyEvent event, EntityRef character, CharacterComponent characterComponent, AliveCharacterComponent aliveCharacterComponent) {
        if (character.hasComponent(PlayerCharacterComponent.class)) {
            // Consume the BeforeDestroyEvent so that the DoDestroy event is never sent for player entities
            event.consume();
            // PlayerDeathEvent only sent to the client for the player entity.
            PlayerDeathEvent playerDeathEvent = new PlayerDeathEvent();
            //Store the details of the death in the event for display on the death screen
            playerDeathEvent.damageTypeName = getDamageTypeName(event.getDamageType());
            playerDeathEvent.instigatorName = getInstigatorName(event.getInstigator());
            character.send(playerDeathEvent);
        }

        // DeathEvent sent to client for any character entity.
        DeathEvent deathEvent = new DeathEvent();
        deathEvent.damageTypeName = getDamageTypeName(event.getDamageType());
        deathEvent.instigatorName = getInstigatorName(event.getInstigator());
        characterComponent.controller.send(deathEvent);

        character.removeComponent(AliveCharacterComponent.class);
        // TODO: Don't just destroy, ragdoll or create particle effect or something (possible allow another system to handle)
        //entity.removeComponent(CharacterComponent.class);
        //entity.removeComponent(CharacterMovementComponent.class);
    }

    /**
     * Extracts the name from an entity.
     * If the entity is a character, then the display name from the {@link ClientComponent#clientInfo} is used.
     * Otherwise the entity itself is checked for a {@link DisplayNameComponent}.
     * In the last case, the prefab name of the entity is used, e.g. "engine:player" will be parsed to "Player".
     *
     * @param instigator The entity for which an instigator name is needed.
     * @return The instigator name.
     */
    public String getInstigatorName(EntityRef instigator) {
        if (instigator.hasComponent(CharacterComponent.class)) {
            EntityRef instigatorClient = instigator.getComponent(CharacterComponent.class).controller;
            EntityRef instigatorClientInfo = instigatorClient.getComponent(ClientComponent.class).clientInfo;
            DisplayNameComponent displayNameComponent = instigatorClientInfo.getComponent(DisplayNameComponent.class);
            return displayNameComponent.name;
        } else if (instigator.getParentPrefab() != null) {
            //A DisplayName can be specified in the entity prefab
            //Otherwise, the game will attempt to generate one from the name of that prefab
            Prefab parentPrefab = instigator.getParentPrefab();
            if (parentPrefab.hasComponent(DisplayNameComponent.class)) {
                DisplayNameComponent displayNameComponent = parentPrefab.getComponent(DisplayNameComponent.class);
                return displayNameComponent.name;
            } else {
                String instigatorName = parentPrefab.getName();
                //getParentPrefab.getName() returns a ResourceUrn String such as "engine:player"
                //The following calls change the damage type to be more readable
                //For instance, "engine:player" becomes "Player"
                instigatorName = instigatorName.replaceAll(".*:(.*)", "$1");
                instigatorName = Character.toUpperCase(instigatorName.charAt(0)) + instigatorName.substring(1);
                return instigatorName;
            }
        } else {
            return null;
        }

    }

    /**
     * Extracts the damage type name from a prefab. If the prefab has a {@link DisplayNameComponent}, it will be used.
     * Otherwise the damage type name is parsed, e.g. "engine:directDamage" will become "Direct Damage".
     *
     * @param damageType The damage type prefab.
     * @return A readable name for the damage type.
     */
    public String getDamageTypeName(Prefab damageType) {
        //A DisplayName can be specified in the damage type prefab
        //Otherwise, the game will attempt to generate one from the name of that prefab
        if (damageType.hasComponent(DisplayNameComponent.class)) {
            DisplayNameComponent displayNameComponent = damageType.getComponent(DisplayNameComponent.class);
            return displayNameComponent.name;
        } else {
            logger.info(String.format("%s is missing a readable DisplayName", damageType.getName()));
            String damageTypeName = damageType.getName();
            //damageType.getName() returns a ResourceUrn String such as "engine:directDamage"
            //The following calls change the damage type to be more readable
            //For instance, "engine:directDamage" becomes "Direct Damage"
            damageTypeName = damageTypeName.replaceAll(".*:(.*)", "$1");
            damageTypeName = damageTypeName.replaceAll("([A-Z])", " $1");
            damageTypeName = Character.toUpperCase(damageTypeName.charAt(0)) + damageTypeName.substring(1);
            return damageTypeName;
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class}, netFilter = RegisterMode.CLIENT)
    public void onAttackRequest(AttackButton event, EntityRef entity, CharacterHeldItemComponent characterHeldItemComponent) {
        if (!event.isDown()) {
            return;
        }

        boolean attackRequestIsValid;
        if (networkSystem.getMode().isAuthority()) {
            // Let the AttackRequest handler trigger the OnItemUseEvent if this is a local client
            attackRequestIsValid = true;
        } else {
            OnItemUseEvent onItemUseEvent = new OnItemUseEvent();
            entity.send(onItemUseEvent);
            attackRequestIsValid = !onItemUseEvent.isConsumed();
        }

        if (attackRequestIsValid) {
            EntityRef selectedItemEntity = characterHeldItemComponent.selectedItem;
            entity.send(new AttackRequest(selectedItemEntity));
            event.consume();
        }
    }

    @ReceiveEvent(components = LocationComponent.class, netFilter = RegisterMode.AUTHORITY)
    public void onAttackRequest(AttackRequest event, EntityRef character, CharacterComponent characterComponent) {
        // if an item is used,  make sure this entity is allowed to attack with it
        if (event.getItem().exists()) {
            if (!character.equals(event.getItem().getOwner())) {
                return;
            }
        }

        OnItemUseEvent onItemUseEvent = new OnItemUseEvent();
        character.send(onItemUseEvent);
        if (!onItemUseEvent.isConsumed()) {
            EntityRef gazeEntity = GazeAuthoritySystem.getGazeEntityForCharacter(character);
            LocationComponent gazeLocation = gazeEntity.getComponent(LocationComponent.class);
            org.joml.Vector3f direction = gazeLocation.getWorldDirection(new org.joml.Vector3f());
            org.joml.Vector3f originPos = gazeLocation.getWorldPosition(new org.joml.Vector3f());
            if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.RECORDING) {
                directionAndOriginPosRecorderList.getAttackEventDirectionAndOriginPosRecorder().add(direction,
                    originPos);
            } else if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.REPLAYING) {
                org.joml.Vector3f[] data =
                    directionAndOriginPosRecorderList.getAttackEventDirectionAndOriginPosRecorder().poll();
                direction = data[0];
                originPos = data[1];
            }

            HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange,
                Sets.newHashSet(character), DEFAULTPHYSICSFILTER);

            if (result.isHit()) {
                result.getEntity().send(new AttackEvent(character, event.getItem()));
            }
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onItemUse(OnItemUseEvent event, EntityRef entity, CharacterHeldItemComponent characterHeldItemComponent) {
        long currentTime = time.getGameTimeInMs();
        if (characterHeldItemComponent.nextItemUseTime > currentTime) {
            // this character is not yet ready to use another item,  they are still cooling down from last use
            event.consume();
            return;
        }

        EntityRef selectedItemEntity = characterHeldItemComponent.selectedItem;

        characterHeldItemComponent.lastItemUsedTime = currentTime;
        characterHeldItemComponent.nextItemUseTime = currentTime;
        ItemComponent itemComponent = selectedItemEntity.getComponent(ItemComponent.class);

        // Add the cooldown time for the next use of this item.
        if (itemComponent != null) {
            // Send out this event so other systems can alter the cooldown time.
            AffectItemUseCooldownTimeEvent affectItemUseCooldownTimeEvent = new AffectItemUseCooldownTimeEvent(itemComponent.cooldownTime);
            entity.send(affectItemUseCooldownTimeEvent);
            characterHeldItemComponent.nextItemUseTime += (long) affectItemUseCooldownTimeEvent.getResultValue();
        } else {
            characterHeldItemComponent.nextItemUseTime += 200;
        }

        entity.saveComponent(characterHeldItemComponent);
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL, netFilter = RegisterMode.AUTHORITY)
    public void onAttackBlock(AttackEvent event, EntityRef entityRef, BlockComponent blockComponent) {
        entityRef.send(new DestroyEvent(event.getInstigator(), event.getDirectCause(), EngineDamageTypes.PHYSICAL.get()));
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL, netFilter = RegisterMode.AUTHORITY)
    public void onAttackBlock(AttackEvent event, EntityRef entityRef, ActAsBlockComponent actAsBlockComponent) {
        entityRef.send(new DestroyEvent(event.getInstigator(), event.getDirectCause(), EngineDamageTypes.PHYSICAL.get()));
    }

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class}, netFilter = RegisterMode.AUTHORITY)
    public void onActivationRequest(ActivationRequest event, EntityRef character) {
        if (isPredictionOfEventCorrect(character, event)) {
            OnItemUseEvent onItemUseEvent = new OnItemUseEvent();
            event.getInstigator().send(onItemUseEvent);
            if (!onItemUseEvent.isConsumed()) {
                if (event.getUsedOwnedEntity().exists()) {
                    event.getUsedOwnedEntity().send(new ActivateEvent(event));
                } else {
                    event.getTarget().send(new ActivateEvent(event));
                }
            }
        } else {
            character.send(new ActivationRequestDenied(event.getActivationId()));
        }
    }

    private String getPlayerNameFromCharacter(EntityRef character) {
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            return "?";
        }
        EntityRef controller = characterComponent.controller;

        ClientComponent clientComponent = controller.getComponent(ClientComponent.class);
        EntityRef clientInfo = clientComponent.clientInfo;

        DisplayNameComponent displayNameComponent = clientInfo.getComponent(DisplayNameComponent.class);
        if (displayNameComponent == null) {
            return "?";
        }

        return displayNameComponent.name;
    }

    private boolean isPredictionOfEventCorrect(EntityRef character, ActivationRequest event) {
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        EntityRef camera = GazeAuthoritySystem.getGazeEntityForCharacter(character);
        LocationComponent location = camera.getComponent(LocationComponent.class);
        org.joml.Vector3f direction = location.getWorldDirection(new org.joml.Vector3f());
        if (!(event.getDirection().equals(direction, 0.0001f))) {
            logger.error("Direction at client {} was different than direction at server {}", event.getDirection(), direction);
        }
        // Assume the exact same value in case there are rounding mistakes:
        direction = event.getDirection();

        org.joml.Vector3f originPos = location.getWorldPosition(new org.joml.Vector3f());
        if (!(event.getOrigin().equals(originPos, 0.0001f))) {
            String msg = "Player {} seems to have cheated: It stated that it performed an action from {} but the predicted position is {}";
            logger.info(msg, getPlayerNameFromCharacter(character), event.getOrigin(), originPos);
            return false;
        }

        if (event.isOwnedEntityUsage()) {
            if (!event.getUsedOwnedEntity().exists()) {
                String msg = "Denied activation attempt by {} since the used entity does not exist on the authority";
                logger.info(msg, getPlayerNameFromCharacter(character));
                return false;
            }
            if (!networkSystem.getOwnerEntity(event.getUsedOwnedEntity()).equals(networkSystem.getOwnerEntity(character))) {
                String msg = "Denied activation attempt by {} since it does not own the entity at the authority";
                logger.info(msg, getPlayerNameFromCharacter(character));
                return false;
            }
        } else {
            // check for cheats so that data can later be trusted:
            if (event.getUsedOwnedEntity().exists()) {
                String msg = "Denied activation attempt by {} since it is not properly marked as owned entity usage";
                logger.info(msg, getPlayerNameFromCharacter(character));
                return false;
            }
        }

        if (event.isEventWithTarget()) {
            if (!event.getTarget().exists()) {
                String msg = "Denied activation attempt by {} since the target does not exist on the authority";
                logger.info(msg, getPlayerNameFromCharacter(character));
                return false; // can happen if target existed on client
            }

            HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, Sets.newHashSet(character), DEFAULTPHYSICSFILTER);
            if (!result.isHit()) {
                String msg = "Denied activation attempt by {} since at the authority there was nothing to activate at that place";
                logger.info(msg, getPlayerNameFromCharacter(character));
                return false;
            }
            EntityRef hitEntity = result.getEntity();
            if (!hitEntity.equals(event.getTarget())) {
                /**
                 * Tip for debugging this issue: Obtain the network id of hit entity and search it in both client and
                 * server entity dump. When certain fields don't get replicated, then wrong entity might get hin in the
                 * hit test.
                 */
                String msg = "Denied activation attempt by {} since at the authority another entity would have been activated";
                logger.info(msg, getPlayerNameFromCharacter(character));
                return false;
            }

            if (!(event.getHitPosition().equals(result.getHitPoint(), 0.0001f))) {
                String msg = "Denied activation attempt by {} since at the authority the object got hit at a differnt position";
                logger.info(msg, getPlayerNameFromCharacter(character));
                return false;
            }
        } else {
            // In order to trust the data later we need to verify it even if it should be correct if no one cheats:
            if (event.getTarget().exists()) {
                String msg = "Denied activation attempt by {} since the event was not properly labeled as having a target";
                logger.info(msg, getPlayerNameFromCharacter(character));
                return false;
            }
            if (!(event.getHitPosition().equals(originPos, 0.0001f))) {
                String msg = "Denied activation attempt by {} since the event was not properly labeled as having a hit postion";
                logger.info(msg, getPlayerNameFromCharacter(character));
                return false;
            }
        }
        return true;
    }

    @Override
    public void update(float delta) {
        Iterable<EntityRef> characterEntities = entityManager.getEntitiesWith(CharacterComponent.class, LocationComponent.class);
        for (EntityRef characterEntity : characterEntities) {
            CharacterComponent characterComponent = characterEntity.getComponent(CharacterComponent.class);
            if (characterComponent == null) {
                continue; // could have changed during events below
            }
            LocationComponent characterLocation = characterEntity.getComponent(LocationComponent.class);
            if (characterLocation == null) {
                continue; // could have changed during events below
            }
            EntityRef target = characterComponent.authorizedInteractionTarget;
            if (target.isActive()) {

                LocationComponent targetLocation = target.getComponent(LocationComponent.class);
                if (targetLocation == null) {
                    continue; // could have changed during events below
                }
                float maxInteractionRange = characterComponent.interactionRange;
                if (isDistanceToLarge(characterLocation, targetLocation, maxInteractionRange)) {
                    InteractionUtil.cancelInteractionAsServer(characterEntity);
                }
            }
        }
    }

    private boolean isDistanceToLarge(LocationComponent characterLocation, LocationComponent targetLocation, float maxInteractionRange) {
        float maxInteractionRangeSquared = maxInteractionRange * maxInteractionRange;
        Vector3f positionDelta = characterLocation.getWorldPosition(new Vector3f());
        positionDelta.sub(targetLocation.getWorldPosition(new Vector3f()));
        float interactionRangeSquared = positionDelta.lengthSquared();
        // add a small epsilon to have rounding mistakes be in favor of the player:
        float epsilon = 0.00001f;
        return interactionRangeSquared > maxInteractionRangeSquared + epsilon;
    }

    @ReceiveEvent
    public void onScaleCharacter(OnScaleEvent event, EntityRef entity, CharacterComponent character, CharacterMovementComponent movement) {
        //TODO: We should catch and consume this event somewhere in case there is no space for the character to grow

        Prefab parent = entity.getParentPrefab();

        // adjust movement parameters based on the default values defined by prefab
        CharacterMovementComponent defaultMovement =
                Optional.ofNullable(parent.getComponent(CharacterMovementComponent.class))
                        .orElse(new CharacterMovementComponent());

        final float factor = event.getFactor();

        movement.height = factor * movement.height;
        movement.jumpSpeed = getJumpSpeed(factor, defaultMovement.jumpSpeed);
        movement.stepHeight = factor * movement.stepHeight;
        movement.distanceBetweenFootsteps = factor * movement.distanceBetweenFootsteps;
        movement.runFactor = getRunFactor(factor, defaultMovement.runFactor);
        entity.saveComponent(movement);

        // adjust character parameters
        CharacterComponent defaultCharacter =
                Optional.ofNullable(parent.getComponent(CharacterComponent.class))
                        .orElse(new CharacterComponent());
        character.interactionRange = getInteractionRange(factor, defaultCharacter.interactionRange);
        entity.saveComponent(character);

        // refresh the entity collider - by retrieving the character collider after removing it we force recreation
        physicsEngine.removeCharacterCollider(entity);
        physicsEngine.getCharacterCollider(entity);

        // Scaling a character up will grow them into the ground. We would need to adjust the vertical position to be
        // safely above ground.
        Optional.ofNullable(entity.getComponent(LocationComponent.class))
                .map(k -> k.getWorldPosition(new org.joml.Vector3f()))
                .map(location -> location.add(0,(event.getNewValue() - event.getOldValue()) / 2f,0))
                .ifPresent(location -> entity.send(new CharacterTeleportEvent(location)));
    }

    private float getJumpSpeed(float ratio, float defaultValue) {
        return (float) Math.pow(ratio, 0.74f) * 0.4f * defaultValue + 0.6f * defaultValue;
    }

    private float getRunFactor(float ratio, float defaultValue) {
        return (float) Math.pow(ratio, 0.68f) * defaultValue;
    }

    private float getInteractionRange(float ratio, float defaultValue) {
        return (float) Math.pow(ratio, 0.62f) * defaultValue;
    }

}
