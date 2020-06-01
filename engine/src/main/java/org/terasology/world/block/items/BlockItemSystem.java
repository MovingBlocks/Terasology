/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.world.block.items;

import org.joml.Vector2f;
import org.terasology.audio.AudioManager;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.KinematicCharacterMover;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.AABB;
import org.terasology.math.JomlUtil;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.telemetry.GamePlayStatsComponent;
import org.terasology.utilities.Assets;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.BlockPlacementData;

import java.util.Map;

// TODO: Predict placement client-side (and handle confirm/denial)
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockItemSystem extends BaseComponentSystem {

    /**
     * Margin and other allowed penetration is also 0.03 or 0.04.
     * Since precision is only float it needs to be that high.
     */
    private static final float ADDITIONAL_ALLOWED_PENETRATION = 0.4f;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private AudioManager audioManager;

    @In
    private NetworkSystem networkSystem;

    @ReceiveEvent(components = {BlockItemComponent.class, ItemComponent.class})
    public void onPlaceBlock(ActivateEvent event, EntityRef item) {
        if (!event.getTarget().exists()) {
            event.consume();
            return;
        }

        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
        BlockFamily blockFamily = blockItem.blockFamily;
        Side surfaceSide = Side.inDirection(event.getHitNormal());

        BlockComponent blockComponent = event.getTarget().getComponent(BlockComponent.class);
        if (blockComponent == null) {
            // If there is no block there (i.e. it's a BlockGroup, we don't allow placing block, try somewhere else)
            event.consume();
            return;
        }
        Vector3i targetBlock = new Vector3i(blockComponent.position);
        Vector3i placementPos = new Vector3i(targetBlock);
        placementPos.add(surfaceSide.getVector3i());

        Vector2f relativeAttachmentPosition = getRelativeAttachmentPosition(event);
        Block block = blockFamily.getBlockForPlacement(new BlockPlacementData(
                JomlUtil.from(placementPos), surfaceSide, JomlUtil.from(event.getDirection()), relativeAttachmentPosition
        ));

        if (canPlaceBlock(block, targetBlock, placementPos)) {
            // TODO: Fix this for changes.
            if (networkSystem.getMode().isAuthority()) {
                PlaceBlocks placeBlocks = new PlaceBlocks(placementPos, block, event.getInstigator());
                worldProvider.getWorldEntity().send(placeBlocks);
                if (!placeBlocks.isConsumed()) {
                    item.send(new OnBlockItemPlaced(placementPos, blockEntityRegistry.getBlockEntityAt(placementPos), event.getInstigator()));
                } else {
                    event.consume();
                }
            }
            recordBlockPlaced(event, blockFamily);
            event.getInstigator().send(new PlaySoundEvent(Assets.getSound("engine:PlaceBlock").get(), 0.5f));
        } else {
            event.consume();
        }
    }

    private Vector2f getRelativeAttachmentPosition(ActivateEvent event) {
        Vector3f targetPosition = event.getTargetLocation();
        if (event.getHitPosition() != null && targetPosition != null) {
            return getSideHitPosition(event.getHitPosition(), targetPosition);
        } else {
            return new Vector2f();
        }
    }

    /**
     * Returns the position at which the block side was hit, relative to the side.
     * <p/>
     * The specified hit position is expected to be on the surface of the cubic block at the specified position.
     * Example: The front side was hit right in the center.
     * The result will be (0.5, 0.5), representing the relative hit position on the side's surface.
     * @param hitPosition the hit position
     * @param blockPosition the block position relative to its center (block (0, 0, 0) has block position (0.5, 0.5, 0.5))
     * @return the 2D hit position relative to the side that was hit
     */
    private Vector2f getSideHitPosition(Vector3f hitPosition, Vector3f blockPosition) {
        float epsilon = 0.0001f;
        Vector3f relativeHitPosition = new Vector3f(hitPosition).sub(blockPosition);

        if (Math.abs(relativeHitPosition.x) > 0.5f - epsilon) {
            return new Vector2f(relativeHitPosition.z, relativeHitPosition.y).add(0.5f, 0.5f);
        } else if (Math.abs(relativeHitPosition.y) > 0.5f - epsilon) {
            return new Vector2f(relativeHitPosition.x, relativeHitPosition.z).add(0.5f, 0.5f);
        } else {
            return new Vector2f(relativeHitPosition.x, relativeHitPosition.y).add(0.5f, 0.5f);
        }
    }

    private void recordBlockPlaced(ActivateEvent event, BlockFamily block) {
        EntityRef instigator = event.getInstigator();
        String blockName = block.getDisplayName();
        if (instigator.hasComponent(GamePlayStatsComponent.class)) {
            GamePlayStatsComponent gamePlayStatsComponent = instigator.getComponent(GamePlayStatsComponent.class);
            Map<String, Integer> blockPlacedMap = gamePlayStatsComponent.blockPlacedMap;
            if (blockPlacedMap.containsKey(blockName)) {
                blockPlacedMap.put(blockName, blockPlacedMap.get(blockName) + 1);
            } else {
                blockPlacedMap.put(blockName, 1);
            }
            instigator.saveComponent(gamePlayStatsComponent);
        } else {
            GamePlayStatsComponent gamePlayStatsComponent = new GamePlayStatsComponent();
            Map<String, Integer> blockPlacedMap = gamePlayStatsComponent.blockPlacedMap;
            blockPlacedMap.put(blockName, 1);
            instigator.addOrSaveComponent(gamePlayStatsComponent);
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

        if (block.getBlockFamily().equals(adjBlock.getBlockFamily())) {
            return false;
        }

        // Prevent players from placing blocks inside their bounding boxes
        if (!block.isPenetrable()) {
            Physics physics = CoreRegistry.get(Physics.class);
            AABB blockBounds = block.getBounds(blockPos);
            Vector3f min = new Vector3f(blockBounds.getMin());
            Vector3f max = new Vector3f(blockBounds.getMax());

            /**
             * Characters can enter other solid objects/blocks for certain amount. This is does to detect collsion
             * start and end without noise. So if the user walked as close to a block as possible it is only natural
             * to let it place a block exactly above it even if that technically would mean a collision start.
             */
            min.x += KinematicCharacterMover.HORIZONTAL_PENETRATION;
            max.x -= KinematicCharacterMover.HORIZONTAL_PENETRATION;
            min.y += KinematicCharacterMover.VERTICAL_PENETRATION;
            max.y -= KinematicCharacterMover.VERTICAL_PENETRATION;
            min.z += KinematicCharacterMover.HORIZONTAL_PENETRATION;
            max.z -= KinematicCharacterMover.HORIZONTAL_PENETRATION;

            /*
             * Calculations aren't exact and in the corner cases it is better to let the user place the block.
             */
            min.add(ADDITIONAL_ALLOWED_PENETRATION, ADDITIONAL_ALLOWED_PENETRATION, ADDITIONAL_ALLOWED_PENETRATION);
            max.sub(ADDITIONAL_ALLOWED_PENETRATION, ADDITIONAL_ALLOWED_PENETRATION, ADDITIONAL_ALLOWED_PENETRATION);

            AABB newBounds = AABB.createMinMax(min, max);
            return physics.scanArea(newBounds, StandardCollisionGroup.DEFAULT, StandardCollisionGroup.CHARACTER).isEmpty();
        }
        return true;
    }
}
