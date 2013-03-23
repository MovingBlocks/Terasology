package org.terasology.world.block.entity;

import com.google.common.collect.Lists;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;

/**
 * @author Immortius
 */
// TODO: Predict placement client-side (and handle confirm/denial)
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockItemSystem implements ComponentSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private AudioManager audioManager;

    @In
    private NetworkSystem networkSystem;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = BlockItemComponent.class)
    public void onDestroyed(RemovedComponentEvent event, EntityRef entity) {
        entity.getComponent(BlockItemComponent.class).placedEntity.destroy();
    }

    @ReceiveEvent(components = {BlockItemComponent.class, ItemComponent.class})
    public void onPlaceBlock(ActivateEvent event, EntityRef item) {
        if (!event.getTarget().exists()) {
            event.cancel();
            return;
        }

        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
        BlockFamily type = blockItem.blockFamily;
        Side surfaceSide = Side.inDirection(event.getHitNormal());
        Side secondaryDirection = TeraMath.getSecondaryPlacementDirection(event.getDirection(), event.getHitNormal());
        Block block = type.getBlockFor(surfaceSide, secondaryDirection);
        Vector3i targetBlock = event.getTarget().getComponent(BlockComponent.class).getPosition();
        Vector3i placementPos = new Vector3i(targetBlock);
        placementPos.add(surfaceSide.getVector3i());

        if (canPlaceBlock(block, targetBlock, placementPos)) {
            if (networkSystem.getMode().isAuthority()) {
                if (blockEntityRegistry.setBlock(placementPos, block, worldProvider.getBlock(placementPos), blockItem.placedEntity)) {
                    if (blockItem.placedEntity.exists()) {
                        blockItem.placedEntity = EntityRef.NULL;
                    }
                } else {
                    // Something changed the block on another thread, cancel
                    event.cancel();
                    return;
                }
            }
            event.getInstigator().send(new PlaySoundEvent(event.getInstigator(), Assets.getSound("engine:PlaceBlock"), 0.5f));
        } else {
            event.cancel();
        }
    }

    private boolean canPlaceBlock(Block block, Vector3i targetBlock, Vector3i blockPos) {
        if (block == null) {
            return false;
        }

        Block centerBlock = worldProvider.getBlock(targetBlock.x, targetBlock.y, targetBlock.z);
        if (!centerBlock.isAttachmentAllowed()) {
            return false;
        }

        Block adjBlock = worldProvider.getBlock(blockPos.x, blockPos.y, blockPos.z);
        if (!adjBlock.isReplacementAllowed() || adjBlock.isTargetable()) {
            return false;
        }

        // Prevent players from placing blocks inside their bounding boxes
        if (!block.isPenetrable()) {
            return !CoreRegistry.get(BulletPhysics.class).scanArea(block.getBounds(blockPos), Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.DEFAULT, StandardCollisionGroup.CHARACTER)).iterator().hasNext();
        }
        return true;
    }
}
