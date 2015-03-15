/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.block.structure;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;

import java.util.Collections;
import java.util.Map;

public class SideBlockSupportRequired implements BlockStructuralSupport {
    private static final String SUPPORT_CHECK_ACTION_ID = "Engine:SideBlockSupportCheck";

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public boolean shouldBeRemovedDueToChange(Vector3i location, Side sideChanged) {
        final SideBlockSupportRequiredComponent component = getComponent(location, Collections.<Vector3i, Block>emptyMap());
        if (component != null) {
            final boolean sufficientlySupported = isSufficientlySupported(location, sideChanged, Collections.<Vector3i, Block>emptyMap(), component);
            if (!sufficientlySupported) {
                if (component.dropDelay <= 0) {
                    return true;
                } else {
                    DelayManager delayManager = CoreRegistry.get(DelayManager.class);
                    final EntityRef blockEntity = getBlockEntityRegistry().getEntityAt(location);
                    if (!delayManager.hasDelayedAction(blockEntity, SUPPORT_CHECK_ACTION_ID)) {
                        delayManager.addDelayedAction(blockEntity, SUPPORT_CHECK_ACTION_ID, component.dropDelay);
                    }
                }
            }
        }
        return false;
    }

    @ReceiveEvent
    public void checkForSupport(DelayedActionTriggeredEvent event, EntityRef entity, BlockComponent block, SideBlockSupportRequiredComponent supportRequired) {
        if (event.getActionId().equals(SUPPORT_CHECK_ACTION_ID)) {
            if (!isSufficientlySupported(block.getPosition(), null, Collections.<Vector3i, Block>emptyMap(), supportRequired)) {
                PrefabManager prefabManager = CoreRegistry.get(PrefabManager.class);
                entity.send(new DestroyEvent(entity, EntityRef.NULL, prefabManager.getPrefab("engine:supportRemovedDamage")));
            }
        }
    }

    @Override
    public boolean isSufficientlySupported(Vector3i location, Map<Vector3i, Block> blockOverrides) {
        final SideBlockSupportRequiredComponent component = getComponent(location, blockOverrides);
        if (component != null) {
            return isSufficientlySupported(location, null, blockOverrides, component);
        }
        return true;
    }

    private EntityRef getEntity(Vector3i location, Map<Vector3i, Block> blockOverrides) {
        final Block overwrittenBlock = blockOverrides.get(location);
        if (overwrittenBlock != null) {
            return overwrittenBlock.getEntity();
        }
        EntityRef blockEntity = getBlockEntityRegistry().getExistingBlockEntityAt(location);
        if (blockEntity.exists()) {
            return blockEntity;
        } else {
            return getWorldProvider().getBlock(location).getEntity();
        }
    }

    private SideBlockSupportRequiredComponent getComponent(Vector3i location, Map<Vector3i, Block> blockOverrides) {
        return getEntity(location, blockOverrides).getComponent(SideBlockSupportRequiredComponent.class);
    }

    private boolean isSufficientlySupported(Vector3i location, Side sideChanged, Map<Vector3i, Block> blockOverrides, SideBlockSupportRequiredComponent supportComponent) {
        if (supportComponent != null) {
            if ((sideChanged == null || sideChanged.isHorizontal()) && supportComponent.sideAllowed
                    && !hasSupport(location, supportComponent, blockOverrides)) {
                return false;
            } else if ((sideChanged == null || !sideChanged.isHorizontal()) && (supportComponent.topAllowed || supportComponent.bottomAllowed)
                    && !hasSupport(location, supportComponent, blockOverrides)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasSupport(Vector3i blockPosition, SideBlockSupportRequiredComponent supportComponent, Map<Vector3i, Block> blockOverrides) {
        if (supportComponent.bottomAllowed && hasSupportFromBlockOnSide(blockPosition, Side.BOTTOM, blockOverrides)) {
            return true;
        }
        if (supportComponent.topAllowed && hasSupportFromBlockOnSide(blockPosition, Side.TOP, blockOverrides)) {
            return true;
        }
        if (supportComponent.sideAllowed && (hasSupportFromBlockOnSide(blockPosition, Side.LEFT, blockOverrides)
                || hasSupportFromBlockOnSide(blockPosition, Side.RIGHT, blockOverrides)
                || hasSupportFromBlockOnSide(blockPosition, Side.FRONT, blockOverrides)
                || hasSupportFromBlockOnSide(blockPosition, Side.BACK, blockOverrides))) {
            return true;
        }
        return false;
    }

    private boolean hasSupportFromBlockOnSide(Vector3i blockPosition, Side side, Map<Vector3i, Block> blockOverrides) {
        final Vector3i sideBlockPosition = side.getAdjacentPos(blockPosition);
        if (!getWorldProvider().isBlockRelevant(sideBlockPosition)) {
            return true;
        }
        return getBlockWithOverrides(sideBlockPosition, blockOverrides).canAttachTo(side.reverse());
    }

    private Block getBlockWithOverrides(Vector3i location, Map<Vector3i, Block> blockOverrides) {
        final Block blockFromOverride = blockOverrides.get(location);
        if (blockFromOverride != null) {
            return blockFromOverride;
        }
        return getWorldProvider().getBlock(location);
    }

    private BlockEntityRegistry getBlockEntityRegistry() {
        return CoreRegistry.get(BlockEntityRegistry.class);
    }

    private WorldProvider getWorldProvider() {
        return CoreRegistry.get(WorldProvider.class);
    }
}
