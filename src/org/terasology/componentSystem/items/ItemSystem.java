package org.terasology.componentSystem.items;

import org.terasology.components.*;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.AudioManager;
import org.terasology.componentSystem.block.BlockEntityLookup;
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

    public void useItemOnBlock(EntityRef item, EntityRef user, Vector3i targetBlock, Side surfaceDirection, Side secondaryDirection) {

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
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null) return;

        item.send(new ActivateEvent(user, user));
        checkConsumeItem(item, itemComp);
    }

    public void useItemOnEntity(EntityRef item, EntityRef target, EntityRef user) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null) return;

        item.send(new ActivateEvent(target, user));
        checkConsumeItem(item, itemComp);
    }

    public void useItemInDirection(EntityRef item, Vector3f location, Vector3f direction, EntityRef user) {
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
