package org.terasology.componentSystem.items;

import org.terasology.componentSystem.block.BlockEntityRegistry;
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
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockFamily;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.AABB;

import javax.print.attribute.standard.Sides;
import javax.vecmath.Vector3f;

/**
 * TODO: Refactor use methods into events? Usage should become a separate component
 * @author Immortius <immortius@gmail.com>
 */
public class ItemSystem implements EventHandlerSystem {
    private EntityManager entityManager;
    private IWorldProvider worldProvider;
    private BlockEntityRegistry blockEntityRegistry;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
    }

    @ReceiveEvent(components=BlockItemComponent.class)
    public void onDestroyed(RemovedComponentEvent event, EntityRef entity) {
        entity.getComponent(BlockItemComponent.class).placedEntity.destroy();
    }

    @ReceiveEvent(components={BlockItemComponent.class, ItemComponent.class})
    public void onPlaceBlock(ActivateEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);


        Side surfaceDir = Side.inDirection(event.getNormal());

        Vector3f attachDir = surfaceDir.reverse().getVector3i().toVector3f();
        Vector3f rawDirection = new Vector3f(event.getDirection());
        float dot = rawDirection.dot(attachDir);
        rawDirection.sub(new Vector3f(dot * attachDir.x, dot * attachDir.y, dot * attachDir.z));
        Side secondaryDirection = Side.inDirection(rawDirection.x, rawDirection.y, rawDirection.z).reverse();

        if (!placeBlock(blockItem.blockFamily, event.getTarget().getComponent(BlockComponent.class).getPosition(), surfaceDir, secondaryDirection, blockItem)) {
            event.cancel();
        }
    }

    @ReceiveEvent(components=ItemComponent.class,priority = ReceiveEvent.PRIORITY_CRITICAL)
    public void checkCanUseItem(ActivateEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        switch (itemComp.usage) {
            case None:
                event.cancel();
                break;
            case OnBlock:
                if (event.getTarget().getComponent(BlockComponent.class) == null) {
                    event.cancel();
                }
                break;
            case OnEntity:
                if (event.getTarget().getComponent(BlockComponent.class) != null) {
                    event.cancel();
                }
                break;
        }
    }

    @ReceiveEvent(components=ItemComponent.class,priority = ReceiveEvent.PRIORITY_TRIVIAL)
    public void usedItem(ActivateEvent event, EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp.consumedOnUse) {
            itemComp.stackCount--;
            if (itemComp.stackCount == 0) {
                item.destroy();
            }
            else {
                item.saveComponent(itemComp);
            }
        }
    }
    /**
     * Places a block of a given type in front of the player.
     *
     * @param type The type of the block
     * @return True if a block was placed
     */
    private boolean placeBlock(BlockFamily type, Vector3i targetBlock, Side surfaceDirection, Side secondaryDirection, BlockItemComponent blockItem) {
        Vector3i placementPos = new Vector3i(targetBlock);
        placementPos.add(surfaceDirection.getVector3i());

        Block block = type.getBlockFor(surfaceDirection, secondaryDirection);
        if (block == null)
            return false;

        if (canPlaceBlock(block, targetBlock, placementPos)) {
            worldProvider.setBlock(placementPos.x, placementPos.y, placementPos.z, block.getId(), true, true);
            AudioManager.play("PlaceBlock", 0.5f);
            if (blockItem.placedEntity.exists()) {
                // Establish a block entity
                blockItem.placedEntity.addComponent(new BlockComponent(placementPos, false));
                // TODO: Get regen and wait from block config?
                blockItem.placedEntity.addComponent(new HealthComponent(type.getArchetypeBlock().getHardness(), 2.0f,1.0f));
                blockItem.placedEntity = EntityRef.NULL;
            }
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
