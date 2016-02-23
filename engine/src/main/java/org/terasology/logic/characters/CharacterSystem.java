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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.input.binds.interaction.AttackButton;
import org.terasology.logic.characters.events.ActivationRequest;
import org.terasology.logic.characters.events.ActivationRequestDenied;
import org.terasology.logic.characters.events.AttackEvent;
import org.terasology.logic.characters.events.AttackRequest;
import org.terasology.logic.characters.events.DeathEvent;
import org.terasology.logic.characters.interactions.InteractionUtil;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.registry.In;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.ActAsBlockComponent;

/**
 */
@RegisterSystem
public class CharacterSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    public static final CollisionGroup[] DEFAULTPHYSICSFILTER = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD, StandardCollisionGroup.CHARACTER};
    private static final Logger logger = LoggerFactory.getLogger(CharacterSystem.class);

    @In
    private Physics physics;

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    @In
    private Time time;

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onDeath(DoDestroyEvent event, EntityRef entity) {
        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        character.controller.send(new DeathEvent());
        // TODO: Don't just destroy, ragdoll or create particle effect or something (possible allow another system to handle)
        //entity.removeComponent(CharacterComponent.class);
        //entity.removeComponent(CharacterMovementComponent.class);
    }


    @ReceiveEvent(components = {CharacterComponent.class}, netFilter = RegisterMode.CLIENT)
    public void onAttackRequest(AttackButton event, EntityRef entity, CharacterHeldItemComponent characterHeldItemComponent) {
        if (!event.isDown() || time.getGameTimeInMs() < characterHeldItemComponent.nextItemUseTime) {
            return;
        }

        EntityRef selectedItemEntity = characterHeldItemComponent.selectedItem;

        entity.send(new AttackRequest(selectedItemEntity));

        long currentTime = time.getGameTimeInMs();
        // TODO: send this data back to the server so that other players can visualize this attack
        // TODO: extract this into an event someplace so that this code does not have to exist both here and in LocalPlayerSystem
        characterHeldItemComponent.lastItemUsedTime = currentTime;
        characterHeldItemComponent.nextItemUseTime = currentTime;
        ItemComponent itemComponent = selectedItemEntity.getComponent(ItemComponent.class);
        if (itemComponent != null) {
            characterHeldItemComponent.nextItemUseTime += itemComponent.cooldownTime;
        } else {
            characterHeldItemComponent.nextItemUseTime += 200;
        }
        entity.saveComponent(characterHeldItemComponent);
        event.consume();
    }


    @ReceiveEvent(components = LocationComponent.class, netFilter = RegisterMode.AUTHORITY)
    public void onAttackRequest(AttackRequest event, EntityRef character) {
        // if an item is used,  make sure this entity is allowed to attack with it
        if (event.getItem().exists()) {
            if (!character.equals(event.getItem().getOwner())) {
                return;
            }
        }

        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        EntityRef gazeEntity = GazeAuthoritySystem.getGazeEntityForCharacter(character);
        LocationComponent gazeLocation = gazeEntity.getComponent(LocationComponent.class);
        Vector3f direction = gazeLocation.getWorldDirection();
        Vector3f originPos = gazeLocation.getWorldPosition();

        HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, Sets.newHashSet(character), DEFAULTPHYSICSFILTER);

        if (result.isHit()) {
            result.getEntity().send(new AttackEvent(character, event.getItem()));
        }
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
            if (event.getUsedOwnedEntity().exists()) {
                event.getUsedOwnedEntity().send(new ActivateEvent(event));
            } else {
                event.getTarget().send(new ActivateEvent(event));
            }
        } else {
            character.send(new ActivationRequestDenied(event.getActivationId()));
        }
    }

    private boolean vectorsAreAboutEqual(Vector3f v1, Vector3f v2) {
        Vector3f delta = new Vector3f();
        delta.add(v1);
        delta.sub(v2);
        float epsilon = 0.0001f;
        float deltaSquared = delta.lengthSquared();
        return deltaSquared < epsilon;
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
        Vector3f direction = location.getWorldDirection();
        if (!(vectorsAreAboutEqual(event.getDirection(), direction))) {
            logger.error("Direction at client {} was different than direction at server {}", event.getDirection(), direction);
        }
        // Assume the exact same value in case there are rounding mistakes:
        direction = event.getDirection();

        Vector3f originPos = location.getWorldPosition();
        if (!(vectorsAreAboutEqual(event.getOrigin(), originPos))) {
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

            if (!(vectorsAreAboutEqual(event.getHitPosition(), result.getHitPoint()))) {
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
            if (!(vectorsAreAboutEqual(event.getHitPosition(), originPos))) {
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
        Vector3f positionDelta = new Vector3f();
        positionDelta.add(characterLocation.getWorldPosition());
        positionDelta.sub(targetLocation.getWorldPosition());
        float interactionRangeSquared = positionDelta.lengthSquared();
        // add a small epsilon to have rounding mistakes be in favor of the player:
        float epsilon = 0.00001f;
        return interactionRangeSquared > maxInteractionRangeSquared + epsilon;
    }


}
