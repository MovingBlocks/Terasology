package org.terasology.logic.systems;

import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockGroup;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.AABB;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ItemSystem {
    private EntityManager entityManager;
    private IWorldProvider worldProvider;
    
    public ItemSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setWorldProvider(IWorldProvider worldProvider) {
        this.worldProvider = worldProvider;
    }
    
    
    public void useItemOnBlock(EntityRef item, EntityRef user, Vector3i targetBlock, Side surfaceDirection, Side secondaryDirection) {
        if (item == null) return;
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null) return;

        PlaceableBlockComponent placeableBlock = item.getComponent(PlaceableBlockComponent.class);
        if (placeableBlock != null) {
            if (placeBlock(placeableBlock.blockGroup, targetBlock, surfaceDirection, secondaryDirection)) {
                itemComp.stackCount--;
                if (itemComp.stackCount == 0) {
                    destroyItem(item, itemComp);
                }
            }
        }
        // TODO: Normal items

    }

    public void destroyItem(EntityRef item, ItemComponent itemComp) {
        if (itemComp.container != null) {
            InventoryComponent inventory = itemComp.container.getComponent(InventoryComponent.class);
            if (inventory == null)
                return;

            int slot = inventory.itemSlots.indexOf(item);
            if (slot != -1) {
                inventory.itemSlots.set(slot, null);
            }
        }
        item.destroy();
    }

    // useItem (no target)

    // useItemOnEntity

    // useItemInDirection


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

        if (canPlaceBlock(block, targetBlock, placementPos, surfaceDirection, secondaryDirection)) {
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
    
    private boolean canPlaceBlock(Block block, Vector3i targetBlock, Vector3i blockPoar, Side surfaceDirection, Side secondaryDirection) {
        Block centerBlock = BlockManager.getInstance().getBlock(worldProvider.getBlock(targetBlock.x, targetBlock.y, targetBlock.z));

        if (!centerBlock.isAllowBlockAttachment()) {
            return false;
        }

        Vector3i blockPos = new Vector3i(targetBlock);
        blockPos.add(surfaceDirection.getVector3i());
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
