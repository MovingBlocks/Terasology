package org.terasology.logic.systems;

import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.componentSystem.EventHandlerSystem;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockGroup;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.AABB;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ItemSystem implements EventHandlerSystem {
    private EntityManager entityManager;
    private IWorldProvider worldProvider;
    private BlockEntityLookup blockEntityLookup;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        blockEntityLookup = CoreRegistry.get(ComponentSystemManager.class).get(BlockEntityLookup.class);
    }

    /**
     * When an item is destroyed, remove it from its container.
     * @param event
     * @param itemEntity
     */
    @ReceiveEvent(components=ItemComponent.class)
    public void onDestroy(RemovedComponentEvent event, EntityRef itemEntity) {
        ItemComponent item = itemEntity.getComponent(ItemComponent.class);
        if (item.container != null) {
            InventoryComponent inventory = item.container.getComponent(InventoryComponent.class);
            int index = inventory.itemSlots.indexOf(itemEntity);
            if (index != -1) {
                inventory.itemSlots.set(index, null);
            }
        }
    }
    
    public void useItemOnBlock(EntityRef item, EntityRef user, Vector3i targetBlock, Side surfaceDirection, Side secondaryDirection) {
        if (item == null) return;
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null) return;

        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
        if (blockItem != null) {
            if (placeBlock(blockItem.blockGroup, targetBlock, surfaceDirection, secondaryDirection)) {
                itemComp.stackCount--;
                if (itemComp.stackCount == 0) {
                    item.destroy();
                }
            }
        } else {
            EntityRef targetEntity = blockEntityLookup.getOrCreateEntityAt(targetBlock);
            item.send(new ActivateEvent(targetEntity, user));
            checkConsumeItem(item, itemComp);
        }
    }

    public void useItem(EntityRef item, EntityRef user) {
        if (item == null) return;
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null) return;

        item.send(new ActivateEvent(user, user));
        checkConsumeItem(item, itemComp);
    }

    public void useItemOnEntity(EntityRef item, EntityRef target, EntityRef user) {
        if (item == null) return;
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null) return;

        item.send(new ActivateEvent(target, user));
        checkConsumeItem(item, itemComp);
    }

    public void useItemInDirection(EntityRef item, Vector3f location, Vector3f direction, EntityRef user) {
        if (item == null) return;
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null) return;

        item.send(new ActivateEvent(location, direction, user));
        checkConsumeItem(item, itemComp);
    }

    private void checkConsumeItem(EntityRef item, ItemComponent itemComp) {
        if (itemComp.consumedOnUse) {
            itemComp.stackCount--;
            if (itemComp.stackCount == 0) {
                item.destroy();
            }
        }
    }

    /**
     * Places a block of a given type in front of the player.
     *
     * @param type The type of the block
     * @return True if a block was placed
     */
    private boolean placeBlock(BlockGroup type, Vector3i targetBlock, Side surfaceDirection, Side secondaryDirection) {
        Vector3i placementPos = new Vector3i(targetBlock);
        placementPos.add(surfaceDirection.getVector3i());

        Block block = type.getBlockFor(surfaceDirection.reverse(), secondaryDirection);
        if (block == null)
            return false;

        if (canPlaceBlock(block, targetBlock, placementPos)) {
            worldProvider.setBlock(placementPos.x, placementPos.y, placementPos.z, block.getId(), true, true);
            AudioManager.play("PlaceBlock", 0.5f);
            return true;

            // TODO: Block change notification - should be handled by world?
            // Notify the world, that a block has been removed/placed
            /*int chunkPosX = TeraMath.calcChunkPosX(blockPos.x);
            int chunkPosZ = TeraMath.calcChunkPosZ(blockPos.z);

            if (type == 0) {
                _player.notifyObserversBlockRemoved(worldProvider.getChunkProvider().loadOrCreateChunk(chunkPosX, chunkPosZ), blockPos, update);
            } else {
                _player.notifyObserversBlockPlaced(worldProvider.getChunkProvider().loadOrCreateChunk(chunkPosX, chunkPosZ), blockPos, update);
            } */
        }
        return false;
    }
    
    private boolean canPlaceBlock(Block block, Vector3i targetBlock, Vector3i blockPos) {
        Block centerBlock = BlockManager.getInstance().getBlock(worldProvider.getBlock(targetBlock.x, targetBlock.y, targetBlock.z));

        if (!centerBlock.isAllowBlockAttachment()) {
            return false;
        }

        Block adjBlock = BlockManager.getInstance().getBlock(worldProvider.getBlock(blockPos.x, blockPos.y, blockPos.z));
        if (adjBlock != null && !adjBlock.isInvisible() && !adjBlock.isSelectionRayThrough()) {
            return false;
        }

        // Prevent players from placing blocks inside their bounding boxes
        if (!block.isPenetrable()) {
            for (EntityRef player : entityManager.iteratorEntities(PlayerComponent.class, AABBCollisionComponent.class, LocationComponent.class)) {
                LocationComponent location = player.getComponent(LocationComponent.class);
                AABBCollisionComponent collision = player.getComponent(AABBCollisionComponent.class);
                Vector3f worldPos = location.getWorldPosition();
                for (AABB blockAABB : block.getColliders(blockPos.x, blockPos.y, blockPos.z)) {
                    if (blockAABB.overlaps(new AABB(worldPos, collision.getExtents()))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
