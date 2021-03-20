// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.health;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.telemetry.GamePlayStatsComponent;
import org.terasology.engine.world.block.BlockComponent;

import java.util.Map;

@RegisterSystem(RegisterMode.AUTHORITY)
public class EntityDestructionAuthoritySystem extends BaseComponentSystem {
    @ReceiveEvent
    public void onDestroy(DestroyEvent event, EntityRef entity) {
        recordDestroyed(event, entity);
        BeforeDestroyEvent destroyCheck = new BeforeDestroyEvent(event.getInstigator(), event.getDirectCause(), event.getDamageType());
        entity.send(destroyCheck);
        if (!destroyCheck.isConsumed()) {
            entity.send(new DoDestroyEvent(event.getInstigator(), event.getDirectCause(), event.getDamageType()));
            entity.destroy();
        }
    }

    private void recordDestroyed(DestroyEvent event, EntityRef entityRef) {
        EntityRef instigator = event.getInstigator();
        if (instigator != null) {
            if (entityRef.hasComponent(BlockComponent.class)) {
                BlockComponent blockComponent = entityRef.getComponent(BlockComponent.class);
                String blockName = blockComponent.getBlock().getDisplayName();
                if (instigator.hasComponent(GamePlayStatsComponent.class)) {
                    GamePlayStatsComponent gamePlayStatsComponent = instigator.getComponent(GamePlayStatsComponent.class);
                    Map<String, Integer> blockDestroyedMap = gamePlayStatsComponent.blockDestroyedMap;
                    if (blockDestroyedMap.containsKey(blockName)) {
                        blockDestroyedMap.put(blockName, blockDestroyedMap.get(blockName) + 1);
                    } else {
                        blockDestroyedMap.put(blockName, 1);
                    }
                    instigator.saveComponent(gamePlayStatsComponent);
                } else {
                    GamePlayStatsComponent gamePlayStatsComponent = new GamePlayStatsComponent();
                    Map<String, Integer> blockDestroyedMap = gamePlayStatsComponent.blockDestroyedMap;
                    blockDestroyedMap.put(blockName, 1);
                    instigator.addOrSaveComponent(gamePlayStatsComponent);
                }
            } else if (entityRef.hasComponent(CharacterComponent.class)) {
                String creatureName = entityRef.getParentPrefab().getName();
                if (instigator.hasComponent(GamePlayStatsComponent.class)) {
                    GamePlayStatsComponent gamePlayStatsComponent = instigator.getComponent(GamePlayStatsComponent.class);
                    Map<String, Integer> creatureKilled = gamePlayStatsComponent.creatureKilled;
                    if (creatureKilled.containsKey(creatureName)) {
                        creatureKilled.put(creatureName, creatureKilled.get(creatureName) + 1);
                    } else {
                        creatureKilled.put(creatureName, 1);
                    }
                    instigator.saveComponent(gamePlayStatsComponent);
                } else {
                    GamePlayStatsComponent gamePlayStatsComponent = new GamePlayStatsComponent();
                    Map<String, Integer> creatureKilled = gamePlayStatsComponent.creatureKilled;
                    creatureKilled.put(creatureName, 1);
                    instigator.addOrSaveComponent(gamePlayStatsComponent);
                }
            }
        }
    }
}
