/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.block;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.block.items.BeforeBlockToItem;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockDropGrammarSystem implements ComponentSystem {
    @In
    private EntityManager entityManager;
    @In
    private BlockManager blockManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {

    }

    @ReceiveEvent(components = {BlockDropGrammarComponent.class})
    public void whenBlockDropped(BeforeBlockToItem event, EntityRef blockEntity) {
        BlockDropGrammarComponent blockDrop = blockEntity.getComponent(BlockDropGrammarComponent.class);

        // Remove the "default" block drop
        event.removeDefaultBlock();

        FastRandom rnd = new FastRandom();

        if (blockDrop.blockDrops != null) {
            for (String drop : blockDrop.blockDrops) {
                String dropResult = drop;
                boolean dropping = true;
                int pipeIndex = dropResult.indexOf('|');
                if (pipeIndex > -1) {
                    float chance = Float.parseFloat(dropResult.substring(0, pipeIndex));
                    if (rnd.nextFloat() >= chance) {
                        dropping = false;
                    }
                    dropResult = dropResult.substring(pipeIndex + 1);
                }
                if (dropping) {
                    DropParser dropParser = new DropParser(rnd, dropResult).invoke();
                    event.addBlockToGenerate(blockManager.getBlockFamily(dropParser.getDrop()), dropParser.getCount());
                }
            }
        }

        if (blockDrop.itemDrops != null) {
            for (String drop : blockDrop.itemDrops) {
                String dropResult = drop;
                boolean dropping = true;
                int pipeIndex = dropResult.indexOf('|');
                if (pipeIndex > -1) {
                    float chance = Float.parseFloat(dropResult.substring(0, pipeIndex));
                    if (rnd.nextFloat() >= chance) {
                        dropping = false;
                    }
                    dropResult = dropResult.substring(pipeIndex + 1);
                }
                if (dropping) {
                    DropParser dropParser = new DropParser(rnd, dropResult).invoke();
                    EntityRef entityRef = entityManager.create(dropParser.getDrop());
                    if (dropParser.getCount() > 1) {
                        ItemComponent itemComponent = entityRef.getComponent(ItemComponent.class);
                        itemComponent.stackCount = (byte) dropParser.getCount();
                    }
                    event.addItemToGenerate(entityRef);
                }
            }
        }
    }

    private class DropParser {
        private FastRandom rnd;
        private String drop;
        private int count;
        private String resultDrop;

        public DropParser(FastRandom rnd, String drop) {
            this.rnd = rnd;
            this.drop = drop;
        }

        public String getDrop() {
            return resultDrop;
        }

        public int getCount() {
            return count;
        }

        public DropParser invoke() {
            resultDrop = drop;
            int timesIndex = resultDrop.indexOf('*');
            int countMin = 1;
            int countMax = 1;
            if (timesIndex > -1) {
                String timesStr = resultDrop.substring(0, timesIndex);
                int minusIndex = timesStr.indexOf('-');
                if (minusIndex > -1) {
                    countMin = Integer.parseInt(timesStr.substring(0, minusIndex));
                    countMax = Integer.parseInt(timesStr.substring(minusIndex + 1));
                } else {
                    countMin = Integer.parseInt(timesStr);
                    countMax = countMin;
                }
                resultDrop = resultDrop.substring(timesIndex + 1);
            }
            count = rnd.nextInt(countMin, countMax);
            return this;
        }
    }
}
