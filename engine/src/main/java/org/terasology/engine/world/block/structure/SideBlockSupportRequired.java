// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.structure;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.logic.destruction.DestroyEvent;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.math.geom.Vector3i;

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
        final SideBlockSupportRequiredComponent component = getComponent(location,
                Collections.emptyMap());
        if (component != null) {
            final boolean sufficientlySupported = isSufficientlySupported(location, sideChanged,
                    Collections.emptyMap(), component);
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
    public void checkForSupport(DelayedActionTriggeredEvent event, EntityRef entity, BlockComponent block,
                                SideBlockSupportRequiredComponent supportRequired) {
        if (event.getActionId().equals(SUPPORT_CHECK_ACTION_ID)) {
            if (!isSufficientlySupported(block.position, null, Collections.emptyMap(),
                    supportRequired)) {
                PrefabManager prefabManager = CoreRegistry.get(PrefabManager.class);
                entity.send(new DestroyEvent(entity, EntityRef.NULL, prefabManager.getPrefab("engine" +
                        ":supportRemovedDamage")));
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

    private boolean isSufficientlySupported(Vector3i location, Side sideChanged, Map<Vector3i, Block> blockOverrides,
                                            SideBlockSupportRequiredComponent supportComponent) {
        if (supportComponent != null) {
            if ((sideChanged == null || sideChanged.isHorizontal()) && supportComponent.sideAllowed
                    && !hasSupport(location, supportComponent, blockOverrides)) {
                return false;
            } else return (sideChanged != null && sideChanged.isHorizontal()) || (!supportComponent.topAllowed && !supportComponent.bottomAllowed)
                    || hasSupport(location, supportComponent, blockOverrides);
        }
        return true;
    }

    private boolean hasSupport(Vector3i blockPosition, SideBlockSupportRequiredComponent supportComponent,
                               Map<Vector3i, Block> blockOverrides) {
        if (supportComponent.bottomAllowed && hasSupportFromBlockOnSide(blockPosition, Side.BOTTOM, blockOverrides)) {
            return true;
        }
        if (supportComponent.topAllowed && hasSupportFromBlockOnSide(blockPosition, Side.TOP, blockOverrides)) {
            return true;
        }
        return supportComponent.sideAllowed && (hasSupportFromBlockOnSide(blockPosition, Side.LEFT, blockOverrides)
                || hasSupportFromBlockOnSide(blockPosition, Side.RIGHT, blockOverrides)
                || hasSupportFromBlockOnSide(blockPosition, Side.FRONT, blockOverrides)
                || hasSupportFromBlockOnSide(blockPosition, Side.BACK, blockOverrides));
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
