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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetUri;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.input.binds.inventory.UseItemButton;
import org.terasology.logic.characters.events.*;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.interactions.InteractionUtil;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.inventory.*;
import org.terasology.logic.location.LocationComponent;
import org.terasology.network.ClientComponent;
import org.terasology.network.ClientInfoComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterSystem
public class CharacterSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private Logger logger = LoggerFactory.getLogger(CharacterSystem.class);

    @In
    private Physics physics;

    @In
    private WorldProvider worldProvider;

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private NUIManager nuiManager;


    private PickupBuilder pickupBuilder;

    private CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD};


    @Override
    public void initialise() {
        pickupBuilder = new PickupBuilder(entityManager);
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onDeath(DestroyEvent event, EntityRef entity) {
        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        character.controller.send(new DeathEvent());
        // TODO: Don't just destroy, ragdoll or create particle effect or something (possible allow another system to handle)
        //entity.removeComponent(CharacterComponent.class);
        //entity.removeComponent(CharacterMovementComponent.class);
        entity.destroy();
    }

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class})
    public void onAttackRequest(AttackRequest event, EntityRef character) {
        if (event.getItem().exists()) {
            if (!character.equals(event.getItem().getOwner())) {
                return;
            }
        }

        LocationComponent location = character.getComponent(LocationComponent.class);
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        Vector3f direction = characterComponent.getLookDirection();
        Vector3f originPos = location.getWorldPosition();
        originPos.y += characterComponent.eyeOffset;

        HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, filter);

        if (result.isHit()) {
            int damage = 1;
            Prefab damageType = EngineDamageTypes.PHYSICAL.get();
            // Calculate damage from item
            ItemComponent item = event.getItem().getComponent(ItemComponent.class);
            if (item != null) {
                damage = item.baseDamage;
                if (item.damageType != null) {
                    damageType = item.damageType;
                }
            }

            result.getEntity().send(new DoDamageEvent(damage, damageType, character, event.getItem()));
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class}, netFilter = RegisterMode.AUTHORITY)
    public void onActivationRequest(ActivationRequest event, EntityRef character) {
        if (isPredictionOfEventCorrect(character, event)) {
            if (event.getUsedItem().exists()) {
                event.getUsedItem().send(new ActivateEvent(event));
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

        ClientComponent clientComponent =  controller.getComponent(ClientComponent.class);
        if (characterComponent == null) {
            return "?";
        }
        EntityRef clientInfo = clientComponent.clientInfo;

        DisplayNameComponent displayNameComponent = clientInfo.getComponent(DisplayNameComponent.class);
        if (displayNameComponent == null) {
            return "?";
        }
        return displayNameComponent.name;
    }

    private boolean isPredictionOfEventCorrect(EntityRef character, ActivationRequest event) {
        LocationComponent location = character.getComponent(LocationComponent.class);
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        Vector3f direction = characterComponent.getLookDirection();
        if (!(vectorsAreAboutEqual(event.getDirection(), direction))) {
            logger.error("Direction at client {} was different than direction at server {}", event.getDirection(), direction);
        }
        // Assume the exact same value in case there are rounding mistakes:
        direction = event.getDirection();

        Vector3f originPos = location.getWorldPosition();
        originPos.y += characterComponent.eyeOffset;
        if (!(vectorsAreAboutEqual(event.getOrigin(), originPos))) {
            logger.info("Player {} seems to have cheated: It stated that it performed an action from {} but the predicted position is {}", getPlayerNameFromCharacter(character), event.getOrigin(), originPos);
            return false;
        }

        if (event.isItemUsage()) {
            if (!event.getUsedItem().exists()) {
                logger.info("Denied activation attempt by {} since the used item does not exist on the authority", getPlayerNameFromCharacter(character));
                return false;
            }
            if (!networkSystem.getOwnerEntity(event.getUsedItem()).equals(networkSystem.getOwnerEntity(character))) {
                logger.info("Denied activation attempt by {} since it does not own the item at the authority", getPlayerNameFromCharacter(character));
                return false;
            }
        } else {
            // check for cheats so that data can later be trusted:
            if (event.getUsedItem().exists()) {
                logger.info("Denied activation attempt by {} since it is not properly marked as item usage", getPlayerNameFromCharacter(character));
                return false;
            }
        }

        if (event.isEventWithTarget()) {
            if (!event.getTarget().exists()) {
                logger.info("Denied activation attempt by {} since the target does not exist on the authority", getPlayerNameFromCharacter(character));
                return false; // can happen if target existed on client
            }

            HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, filter);
            if (!result.isHit()) {
                logger.info("Denied activation attempt by {} since at the authority there was nothing to activate at that place", getPlayerNameFromCharacter(character));
                return false;
            }
            EntityRef hitEntity = result.getEntity();
            if (!hitEntity.equals(event.getTarget())) {
                /**
                 * Tip for debugging this issue: Obtain the network id of hit entity and search it in both client and
                 * server entity dump. When certain fields don't get replicated, then wrong entity might get hin in the
                 * hit test.
                 */
                logger.info("Denied activation attempt by {} since at the authority another entity would have been activated", getPlayerNameFromCharacter(character));
                return false;
            }

            if (!(vectorsAreAboutEqual(event.getHitPosition(), result.getHitPoint()))) {
                logger.info("Denied activation attempt by {} since at the authority the object got hit at a differnt position", getPlayerNameFromCharacter(character));
                return false;
            }
        } else {
            // In order to trust the data later we need to verify it even if it should be correct if no one cheats:
            if (event.getTarget().exists()) {
                logger.info("Denied activation attempt by {} since the event was not properly labeled as having a target", getPlayerNameFromCharacter(character));
                return false;
            }
            if (!(vectorsAreAboutEqual(event.getHitPosition(), originPos))) {
                logger.info("Denied activation attempt by {} since the event was not properly labeled as having a hit postion", getPlayerNameFromCharacter(character));
                return false;
            }
        }
        return true;
    }


    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class}, netFilter = RegisterMode.AUTHORITY)
    public void onDropItemRequest(DropItemRequest event, EntityRef character) {
        //make sure we own the item and it exists
        if (!event.getItem().exists() || !networkSystem.getOwnerEntity(event.getItem()).equals(networkSystem.getOwnerEntity(character))) {
            return;
        }

        EntityRef pickup = pickupBuilder.createPickupFor(event.getItem(), event.getNewPosition(), 200);
        pickup.send(new ImpulseEvent(event.getImpulse()));
    }

    @Override
    public void update(float delta) {
        Iterable<EntityRef> characterEntities = entityManager.getEntitiesWith(CharacterComponent.class, LocationComponent.class);
        for (EntityRef characterEntity: characterEntities) {
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
        float maxInteractionRangeSquared = maxInteractionRange*maxInteractionRange;
        Vector3f positionDelta = new Vector3f();
        positionDelta.add(characterLocation.getWorldPosition());
        positionDelta.sub(targetLocation.getWorldPosition());
        float interactionRangeSquared = positionDelta.lengthSquared();
        // add a small epsilon to have rounding mistakes be in favor of the player:
        float epsilon = 0.00001f;
        return interactionRangeSquared > maxInteractionRangeSquared + epsilon;
    }


}
