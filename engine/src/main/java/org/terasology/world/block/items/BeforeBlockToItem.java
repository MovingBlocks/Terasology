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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.world.block.family.BlockFamily;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Immortius
 */
public class BeforeBlockToItem extends AbstractConsumableEvent {

    private Prefab damageType;
    private EntityRef instigator;
    private EntityRef tool;
    private TObjectIntMap<BlockFamily> itemsToGenerate = new TObjectIntHashMap<>();
    private List<EntityRef> itemsToDrop = new LinkedList<>();

    public BeforeBlockToItem(Prefab damageType, EntityRef instigator, EntityRef tool, BlockFamily blockFamily, int quantity) {
        this.damageType = damageType;
        this.instigator = instigator;
        this.tool = tool;
        itemsToGenerate.put(blockFamily, quantity);
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getTool() {
        return tool;
    }

    public void addItemToGenerate(EntityRef entityRef) {
        itemsToDrop.add(entityRef);
    }

    public void addBlockToGenerate(BlockFamily blockFamily, int quantity) {
        itemsToGenerate.adjustOrPutValue(blockFamily, quantity, quantity);
    }

    public void setBlockToGenerate(BlockFamily blockFamily, int quantity) {
        itemsToGenerate.put(blockFamily, quantity);
    }

    public void removeBlockFromGeneration(BlockFamily blockFamily) {
        itemsToGenerate.remove(blockFamily);
    }

    public Iterable<BlockFamily> getBlockItemsToGenerate() {
        return itemsToGenerate.keySet();
    }

    public Iterable<EntityRef> getItemsToDrop() {
        return itemsToDrop;
    }

    public int getQuanityForBlock(BlockFamily blockFamily) {
        return itemsToGenerate.get(blockFamily);
    }

    public Prefab getDamageType() {
        return damageType;
    }
}
