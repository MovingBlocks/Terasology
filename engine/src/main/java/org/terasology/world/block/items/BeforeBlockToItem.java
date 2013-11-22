/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.block.items;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.entitySystem.event.Event;
import org.terasology.world.block.family.BlockFamily;

/**
 * @author Immortius
 */
public class BeforeBlockToItem extends AbstractConsumableEvent {

    private TObjectIntMap<BlockFamily> itemsToGenerate = new TObjectIntHashMap<>();

    public BeforeBlockToItem(BlockFamily blockFamily, int quantity) {
        itemsToGenerate.put(blockFamily, quantity);
    }

    public void addItemToGenerate(BlockFamily blockFamily, int quantity) {
        itemsToGenerate.adjustOrPutValue(blockFamily, quantity, quantity);
    }

    public void setItemToGenerate(BlockFamily blockFamily, int quantity) {
        itemsToGenerate.put(blockFamily, quantity);
    }

    public void removeItemFromGeneration(BlockFamily blockFamily) {
        itemsToGenerate.remove(blockFamily);
    }

    public Iterable<BlockFamily> getBlockItemsToGenerate() {
        return itemsToGenerate.keySet();
    }

    public int getQuanityForItem(BlockFamily blockFamily) {
        return itemsToGenerate.get(blockFamily);
    }

}
