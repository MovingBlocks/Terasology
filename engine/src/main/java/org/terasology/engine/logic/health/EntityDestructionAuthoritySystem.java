/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.logic.health;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.telemetry.GamePlayStatsComponent;
import org.terasology.world.block.BlockComponent;

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
                String blockName = blockComponent.block.getDisplayName();
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
