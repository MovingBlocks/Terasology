/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.lighting;

import com.google.common.collect.Maps;
import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.Map;

/**
 * @author Immortius
 */
public class StubLightingWorldView implements LightingWorldView {
    private TObjectByteMap<Vector3i> lightData = new TObjectByteHashMap<>();
    private Map<Vector3i, Block> blockData = Maps.newHashMap();

    @Override
    public byte getLuminanceAt(Vector3i pos) {
        return lightData.get(pos);
    }

    @Override
    public void setLuminanceAt(Vector3i pos, byte value) {
        lightData.put(pos, value);
    }

    @Override
    public Block getBlockAt(Vector3i pos) {
        Block result =blockData.get(pos);
        if (result == null) {
            return BlockManager.getAir();
        }
        return result;
    }

    @Override
    public boolean isInBounds(Vector3i pos) {
        return true;
    }

    public void setBlockAt(Vector3i pos, Block block) {
        blockData.put(pos, block);
    }
}
