/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.logic.location.LocationComponent;
import org.terasology.entityFactory.DroppedBlockFactory;
import org.terasology.entityFactory.DroppedItemFactory;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.engine.CoreRegistry;
import org.terasology.logic.characters.events.AttackRequest;
import org.terasology.logic.characters.events.DeathEvent;
import org.terasology.logic.characters.events.DropItemRequest;
import org.terasology.logic.characters.events.FrobRequest;
import org.terasology.logic.characters.events.UseItemRequest;
import org.terasology.logic.health.DamageEvent;
import org.terasology.logic.health.NoHealthEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.ImpulseEvent;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.entity.BlockComponent;
import org.terasology.world.block.entity.BlockItemComponent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterSystem()
public class CharacterSystem implements ComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(CharacterSystem.class);

    @In
    private BulletPhysics physics;

    @In
    private WorldProvider worldProvider;

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    private CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD};

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
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
            if (!character.equals(event.getItem().getComponent(NetworkComponent.class).owner)) {
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
            // Calculate damage from item
            ItemComponent item = event.getItem().getComponent(ItemComponent.class);
            if (item != null) {
                damage = item.baseDamage;

                BlockComponent blockComp = result.getEntity().getComponent(BlockComponent.class);
                if (blockComp != null) {
                    Block block = worldProvider.getBlock(blockComp.getPosition());
                    if (item.getPerBlockDamageBonus().containsKey(block.getBlockFamily().getURI().toString())) {
                        damage += item.getPerBlockDamageBonus().get(block.getBlockFamily().getURI().toString());
                    }
                }
            }

            result.getEntity().send(new DamageEvent(damage, character));
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

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class})
    public void onDropItemRequest(DropItemRequest event, EntityRef character) {

        //make sure we own the item and it exists
        if (!event.getItem().exists() || !networkSystem.getOwnerEntity(event.getItem()).equals(networkSystem.getOwnerEntity(character))) {
            return;
        }

        //drop the item
        EntityRef selectedItemEntity = event.getItem();
        Vector3f impulse = event.getImpulse();
        Vector3f newPosition = event.getNewPosition();
        ItemComponent item = selectedItemEntity.getComponent(ItemComponent.class);

        //don't perform actual drop on client side
        if (networkSystem.getMode().isAuthority()) {

            EntityManager entityManager = CoreRegistry.get(EntityManager.class);

            BlockItemComponent blockItem = selectedItemEntity.getComponent(BlockItemComponent.class);
            if (blockItem == null) {
                DroppedItemFactory droppedItemFactory = new DroppedItemFactory(entityManager);
                EntityRef droppedItem = droppedItemFactory.newInstance(new Vector3f(newPosition), item.icon, 200, selectedItemEntity);

                if (!droppedItem.equals(EntityRef.NULL)) {
                    droppedItem.send(new ImpulseEvent(new Vector3f(impulse)));
                }
            } else {
                DroppedBlockFactory droppedBlockFactory = new DroppedBlockFactory(entityManager);
                EntityRef droppedBlock = droppedBlockFactory.newInstance(new Vector3f(newPosition), blockItem.blockFamily, 20, blockItem.placedEntity);
                if (!droppedBlock.equals(EntityRef.NULL)) {
                    droppedBlock.send(new ImpulseEvent(new Vector3f(impulse)));
                    blockItem.placedEntity = EntityRef.NULL;
                    selectedItemEntity.saveComponent(blockItem);
                } else {
                    // Didn't create the dropped block, so don't decrement the stack
                    return;
                }
            }

        }
        // decrease item stack
        // note this occurs on both the client and server side
        InventoryManager inventoryManager = CoreRegistry.get(InventoryManager.class);
        int newStackSize = inventoryManager.getStackSize(selectedItemEntity) - 1;
        inventoryManager.setStackSize(selectedItemEntity, event.getInventoryEntity(), newStackSize);

    }

}
