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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.*;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.location.LocationComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;
import java.util.List;

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
    public void onUseItem(UseItemRequest event, EntityRef character) {

        if (!event.getItem().exists() || !networkSystem.getOwnerEntity(event.getItem()).equals(networkSystem.getOwnerEntity(character))) {
            return;
        }

        LocationComponent location = character.getComponent(LocationComponent.class);
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        Vector3f direction = characterComponent.getLookDirection();
        Vector3f originPos = location.getWorldPosition();
        originPos.y += characterComponent.eyeOffset;

        HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, filter);

        if (result.isHit()) {
            event.getItem().send(new ActivateEvent(result.getEntity(), character, originPos, direction, result.getHitPoint(), result.getHitNormal()));
        } else {
            originPos.y += characterComponent.eyeOffset;
            event.getItem().send(new ActivateEvent(character, originPos, direction));
        }

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

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class})
    public void onFrob(FrobRequest event, EntityRef character) {
        LocationComponent location = character.getComponent(LocationComponent.class);
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        Vector3f direction = characterComponent.getLookDirection();
        Vector3f originPos = location.getWorldPosition();
        originPos.y += characterComponent.eyeOffset;

        HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, filter);
        if (result.isHit()) {
            result.getEntity().send(new ActivateEvent(character, character, originPos, direction, result.getHitPoint(), result.getHitNormal()));
        }
    }

    @ReceiveEvent(components = {}, netFilter = RegisterMode.AUTHORITY)
    public void onInteractionStartRequest(InteractionStartRequest request, EntityRef instigator) {
        EntityRef target = request.getTarget();
        target.send(new InteractionStartEvent(instigator));
    }


    /**
     *
     * Sets interactionTarget to the specified value with the highest priority so that the field is updated when
     * other handlers of this event run.
     */
    @ReceiveEvent(components = {}, netFilter = RegisterMode.ALWAYS, priority = EventPriority.PRIORITY_CRITICAL)
    public void onInteractionStartEvent(InteractionStartEvent event, EntityRef target) {
        EntityRef instigator = event.getInstigator();
        CharacterComponent characterComponent = instigator.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Interaction start request instigator has no character component");
            return;
        }
        if (characterComponent.interactionTarget.exists()) {
            logger.error("Interaction wasn't finished at start of next interaction");
            target.send(new InteractionEndEvent(instigator));
        }

        characterComponent.interactionTarget = target;
        instigator.saveComponent(characterComponent);
    }

    @ReceiveEvent(components = {}, netFilter = RegisterMode.AUTHORITY)
    public void onInteractionEndRequest(InteractionEndRequest request, EntityRef instigator) {
        EntityRef target = request.getTarget();
        target.send(new InteractionEndEvent(instigator));
    }

    /**
     * Sets interactionTarget to NULL with a low priorty so that all handlers of this event are finished when this event
     * gets processed.
     */
    @ReceiveEvent(components = {}, netFilter = RegisterMode.ALWAYS, priority = EventPriority.PRIORITY_TRIVIAL)
    public void onInteractionEndEent(InteractionEndEvent event, EntityRef target) {
        EntityRef instigator = event.getInstigator();
        CharacterComponent characterComponent = instigator.getComponent(CharacterComponent.class);
        if (characterComponent == null) {
            logger.error("Interaction end request instigator has no character component");
            return;
        }

        characterComponent.interactionTarget = EntityRef.NULL;
        instigator.saveComponent(characterComponent);
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
            EntityRef target = characterComponent.interactionTarget;
            if (target.isActive()) {

                LocationComponent targetLocation = target.getComponent(LocationComponent.class);
                if (targetLocation == null) {
                    continue; // could have changed during events below
                }
                float maxInteractionRange = characterComponent.interactionRange;
                if (isDistanceToLarge(characterLocation, targetLocation, maxInteractionRange)) {
                    CharacterUtil.setInteractionTarget(characterEntity, EntityRef.NULL);
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
