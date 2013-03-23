/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Diamond3iIterator;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldView;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;

import java.util.Collection;
import java.util.List;

/**
 * @author Immortius
 */
public class LightPropagator {
    private static final Logger logger = LoggerFactory.getLogger(LightPropagator.class);

    private WorldView worldView;

    public LightPropagator(WorldView worldView) {
        this.worldView = worldView;
    }

    /**
     * Propagates light out of the central chunk of the world view, "connecting" it to the surrounding chunks
     * <p/>
     * This expects the light propagator to be set up with a 3x3 world view offset so the center chunk is accessed as(0,0,0)
     */
    public void propagateOutOfTargetChunk() {
        int maxX = Chunk.SIZE_X - 1;
        int maxZ = Chunk.SIZE_Z - 1;
        // Iterate over the blocks on the horizontal sides
        for (int y = 0; y < Chunk.SIZE_Y; y++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                propagateSunlightFrom(x, y, 0, Side.FRONT);
                propagateSunlightFrom(x, y, maxZ, Side.BACK);
                propagateLightFrom(x, y, 0, Side.FRONT);
                propagateLightFrom(x, y, maxZ, Side.BACK);
            }
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                propagateSunlightFrom(0, y, z, Side.LEFT);
                propagateSunlightFrom(maxX, y, z, Side.RIGHT);
                propagateLightFrom(0, y, z, Side.LEFT);
                propagateLightFrom(maxX, y, z, Side.RIGHT);
            }
        }
    }

    /**
     * Updates the lighting for a block change
     *
     * @param pos     The position of the block
     * @param type    The new block type
     * @param oldType The old block type
     * @return The region affected by the light update
     */
    public Region3i update(Vector3i pos, Block type, Block oldType) {
        return update(pos.x, pos.y, pos.z, type, oldType);
    }

    /**
     * Updates the lighting for a block change
     *
     * @param x
     * @param y
     * @param z
     * @param type    The new block type
     * @param oldType The old block type
     * @return The region affected by the light update
     */
    public Region3i update(int x, int y, int z, Block type, Block oldType) {
        return Region3i.createEncompassing(updateSunlight(x, y, z, type, oldType), updateLight(x, y, z, type, oldType));
    }

    private Region3i updateSunlight(int x, int y, int z, Block type, Block oldType) {
        PropagationComparison changeType = LightingUtil.compareLightingPropagation(type, oldType);
        switch (changeType) {
            case MORE_PERMISSIVE:
                byte light = pullSunlight(x, y, z, type);
                worldView.setSunlight(x, y, z, light);
                if (light > 1) {
                    return pushSunlight(x, y, z, light);
                }
                break;
            case MORE_RESTRICTED:
                return clearSunlight(x, y, z);
            default:
                return Region3i.EMPTY;
        }
        return Region3i.EMPTY;
    }

    private Region3i updateLight(int x, int y, int z, Block type, Block oldType) {
        byte currentLight = worldView.getLight(x, y, z);
        byte lum = type.getLuminance();

        PropagationComparison changeType = LightingUtil.compareLightingPropagation(type, oldType);

        if (changeType == PropagationComparison.MORE_RESTRICTED || (lum < oldType.getLuminance() && currentLight == oldType.getLuminance())) {
            // If some light might be blocked or reduced, do a full rebuild of the area
            return clearLight(x, y, z, currentLight, type);
        } else if (changeType == PropagationComparison.MORE_PERMISSIVE) {
            // Light level can only increase, pull in light and push it out
            byte newLight = pullLight(x, y, z, lum, type);
            worldView.setLight(x, y, z, newLight);
            return pushLight(x, y, z, newLight);
        } else if (lum > currentLight) {
            // Light level has increased, push out
            worldView.setLight(x, y, z, lum);
            return pushLight(x, y, z, lum);
        }


        // No change
        return Region3i.EMPTY;
    }

    private byte pullSunlight(int x, int y, int z, Block type) {
        byte light = 0;
        if (y == Chunk.SIZE_Y - 1 && LightingUtil.canSpreadLightInto(type, Side.TOP)) {
            light = Chunk.MAX_LIGHT;
        } else {
            for (Side side : Side.values()) {
                if (LightingUtil.canSpreadLightInto(type, side)) {
                    byte adjLight = worldView.getSunlight(x + side.getVector3i().x, y + side.getVector3i().y, z + side.getVector3i().z);
                    if (side != Side.TOP) {
                        adjLight -= 1;
                    }
                    Block otherType = worldView.getBlock(x + side.getVector3i().x, y + side.getVector3i().y, z + side.getVector3i().z);
                    if (adjLight > light && LightingUtil.canSpreadLightOutOf(otherType, side.reverse())) {
                        light = adjLight;
                    }
                }
            }
        }
        return light;
    }

    private byte pullLight(int x, int y, int z, byte newLight, Block type) {
        for (Side side : Side.values()) {
            if (LightingUtil.canSpreadLightInto(type, side)) {
                Vector3i adjDir = side.getVector3i();
                Block otherType = worldView.getBlock(x + adjDir.x, y + adjDir.y, z + adjDir.z);
                byte adjLight = (byte) (worldView.getLight(x + adjDir.x, y + adjDir.y, z + adjDir.z) - 1);
                if (adjLight > newLight && LightingUtil.canSpreadLightOutOf(otherType, side.reverse())) {
                    newLight = adjLight;
                }
            }
        }
        return newLight;
    }

    private Region3i pushSunlight(int x, int y, int z, byte lightLevel) {
        Collection<Vector3i> currentWave = Lists.newArrayList();
        Collection<Vector3i> nextWave = Lists.newArrayList();
        nextWave.add(new Vector3i(x, y, z));
        // First drop MAX_LIGHT until it is blocked
        if (lightLevel == Chunk.MAX_LIGHT && worldView.getSunlight(x, y - 1, z) < Chunk.MAX_LIGHT) {
            Block lastBlock = BlockManager.getAir();
            for (int columnY = y - 1; columnY >= 0; columnY--) {
                Block block = worldView.getBlock(x, columnY, z);
                if (LightingUtil.canSpreadLightOutOf(lastBlock, Side.BOTTOM) && LightingUtil.canSpreadLightInto(block, Side.TOP) && LightingUtil.doesSunlightRetainsFullStrengthIn(block)) {
                    worldView.setSunlight(x, columnY, z, lightLevel);
                    lastBlock = block;
                    nextWave.add(new Vector3i(x, columnY, z));
                } else {
                    break;
                }
            }
        }

        // Spread the sunlight
        Region3i affectedRegion = Region3i.createFromMinAndSize(new Vector3i(x, y, z), Vector3i.one());
        while (lightLevel > 1 && !nextWave.isEmpty()) {
            Collection<Vector3i> temp = currentWave;
            currentWave = nextWave;
            nextWave = temp;
            nextWave.clear();

            // Only move sunlight up if it is below max light
            if (lightLevel < Chunk.MAX_LIGHT) {
                for (Vector3i pos : currentWave) {
                    // Move sunlight up
                    if (pos.y < Chunk.SIZE_Y - 2) {
                        Vector3i adjPos = new Vector3i(pos.x, pos.y + 1, pos.z);
                        Block block = worldView.getBlock(pos);
                        Block adjBlock = worldView.getBlock(adjPos);
                        if (LightingUtil.canSpreadLightOutOf(block, Side.TOP) && LightingUtil.canSpreadLightInto(adjBlock, Side.BOTTOM)) {
                            byte adjLight = worldView.getSunlight(adjPos);
                            if (adjLight < lightLevel - 1) {
                                worldView.setSunlight(adjPos, (byte) (lightLevel - 1));
                                nextWave.add(adjPos);
                                affectedRegion = affectedRegion.expandToContain(adjPos);
                            }
                        }
                    }
                }
            }
            for (Vector3i pos : currentWave) {
                // Move sunlight down
                if (pos.y > 0) {
                    Vector3i adjPos = new Vector3i(pos.x, pos.y - 1, pos.z);
                    Block block = worldView.getBlock(pos);
                    Block adjBlock = worldView.getBlock(adjPos);
                    if (LightingUtil.canSpreadLightOutOf(block, Side.BOTTOM) && LightingUtil.canSpreadLightInto(adjBlock, Side.TOP)) {
                        byte adjLight = worldView.getSunlight(adjPos);
                        if (adjLight < lightLevel - 1) {
                            worldView.setSunlight(adjPos, (byte) (lightLevel - 1));
                            nextWave.add(adjPos);
                            affectedRegion = affectedRegion.expandToContain(adjPos);
                        }
                    }
                }
            }
            // Move sunlight sideways
            for (Vector3i pos : currentWave) {
                for (Side side : Side.horizontalSides()) {
                    Vector3i adjPos = new Vector3i(pos);
                    adjPos.add(side.getVector3i());

                    try {
                        Block block = worldView.getBlock(pos);
                        Block adjBlock = worldView.getBlock(adjPos);

                        if (LightingUtil.canSpreadLightOutOf(block, side) && LightingUtil.canSpreadLightInto(adjBlock, side.reverse())) {
                            byte adjLight = worldView.getSunlight(adjPos);
                            if (adjLight < lightLevel - 1) {
                                worldView.setSunlight(adjPos, (byte) (lightLevel - 1));
                                nextWave.add(adjPos);
                                affectedRegion = affectedRegion.expandToContain(adjPos);
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.error("Pushing Light {} {} {} failed", new Vector3i(x, y, z), lightLevel, worldView.getChunkRegion(), e);
                    }
                }
            }
            lightLevel--;
        }
        return affectedRegion;
    }

    private Region3i pushLight(int x, int y, int z, byte lightLevel) {
        Collection<Vector3i> currentWave = Lists.newArrayList();
        Collection<Vector3i> nextWave = Lists.newArrayList();
        nextWave.add(new Vector3i(x, y, z));

        Region3i affectedRegion = Region3i.createFromMinAndSize(new Vector3i(x, y, z), Vector3i.one());

        while (lightLevel > 1 && !nextWave.isEmpty()) {
            Collection<Vector3i> temp = currentWave;
            currentWave = nextWave;
            nextWave = temp;
            nextWave.clear();

            for (Vector3i pos : currentWave) {
                for (Side side : Side.values()) {
                    Vector3i adjPos = new Vector3i(pos);
                    adjPos.add(side.getVector3i());
                    if (adjPos.y < 0 || adjPos.y >= Chunk.SIZE_Y) {
                        continue;
                    }

                    Block block = worldView.getBlock(pos);
                    Block adjBlock = worldView.getBlock(adjPos);
                    if (LightingUtil.canSpreadLightOutOf(block, side) && LightingUtil.canSpreadLightInto(adjBlock, side.reverse())) {
                        byte adjLight = worldView.getLight(adjPos);
                        if (adjLight < lightLevel - 1) {
                            worldView.setLight(adjPos, (byte) (lightLevel - 1));
                            nextWave.add(adjPos);
                            affectedRegion = affectedRegion.expandToContain(adjPos);
                        }
                    }
                }
            }
            lightLevel--;
        }
        return affectedRegion;
    }

    private Region3i clearSunlight(int x, int y, int z) {
        byte oldSunlight = worldView.getSunlight(x, y, z);
        if (oldSunlight == Chunk.MAX_LIGHT) {
            worldView.setSunlight(x, y, z, (byte) 0);
            fullRecalculateSunlightAround(x, y, z);
            return Region3i.createFromMinAndSize(new Vector3i(x - Chunk.MAX_LIGHT + 1, 0, z - Chunk.MAX_LIGHT + 1), new Vector3i(2 * Chunk.MAX_LIGHT - 1, Chunk.SIZE_Y, 2 * Chunk.MAX_LIGHT - 1));
        } else if (oldSunlight > 1) {
            localRecalculateSunlightAround(x, y, z, oldSunlight);
            return Region3i.createFromCenterExtents(new Vector3i(x, y, z), oldSunlight - 1);
        } else if (oldSunlight > 0) {
            worldView.setSunlight(x, y, z, (byte) 0);
            return Region3i.createFromCenterExtents(new Vector3i(x, y, z), 0);
        }
        return Region3i.EMPTY;
    }

    private Region3i clearLight(int x, int y, int z, int oldLightLevel, Block type) {
        int checkExtent = (oldLightLevel > 0) ? oldLightLevel - 1 : 0;
        List<Vector3i> lightSources = Lists.newArrayList();
        Region3i region = Region3i.createFromCenterExtents(new Vector3i(x, y, z), checkExtent);

        // Clear old light, recording light sources
        for (Vector3i pos : Diamond3iIterator.iterate(new Vector3i(x, y, z), checkExtent + 1)) {
            byte lum = worldView.getBlock(pos).getLuminance();
            worldView.setLight(pos, lum);
            if (lum > 1) {
                lightSources.add(pos);
            }
        }
        // Apply light sources
        for (Vector3i pos : lightSources) {
            byte lightLevel = worldView.getLight(pos);
            if (lightLevel > 1) {
                region = Region3i.createEncompassing(region, pushLight(pos.x, pos.y, pos.z, lightLevel));
            }
        }
        // Draw in light from surrounding area
        for (Vector3i pos : Diamond3iIterator.iterateAtDistance(new Vector3i(x, y, z), checkExtent + 1)) {
            byte lightLevel = worldView.getLight(pos);
            lightLevel = pullLight(pos.x, pos.y, pos.z, lightLevel, type);
            if (lightLevel > 1) {
                pushLight(pos.x, pos.y, pos.z, lightLevel);
            }
        }
        return region;
    }

    private void localRecalculateSunlightAround(int x, int y, int z, int oldLightLevel) {
        // Clear old light, recording light sources
        for (Vector3i pos : Diamond3iIterator.iterate(new Vector3i(x, y, z), oldLightLevel)) {
            worldView.setSunlight(pos, (byte) 0);

        }
        // Draw in light from surrounding area
        for (Vector3i pos : Diamond3iIterator.iterateAtDistance(new Vector3i(x, y, z), oldLightLevel + 1)) {
            byte lightLevel = worldView.getSunlight(pos);
            if (lightLevel > 1) {
                pushSunlight(pos.x, pos.y, pos.z, lightLevel);
            }
        }
    }

    private void fullRecalculateSunlightAround(int blockX, int blockY, int blockZ) {
        int top = Math.min(Chunk.SIZE_Y - 2, blockY + Chunk.MAX_LIGHT - 2);
        Region3i region = Region3i.createFromMinMax(new Vector3i(blockX - Chunk.MAX_LIGHT + 1, 0, blockZ - Chunk.MAX_LIGHT + 1), new Vector3i(blockX + Chunk.MAX_LIGHT - 1, top, blockZ + Chunk.MAX_LIGHT - 1));
        short[] tops = new short[region.size().x * region.size().z];

        // Tunnel light down
        for (int x = 0; x < region.size().x; x++) {
            for (int z = 0; z < region.size().z; z++) {
                int y = top;
                byte aboveLight = worldView.getSunlight(x + region.min().x, y + 1, z + region.min().z);
                Block lastBlock = BlockManager.getAir();
                if (aboveLight == Chunk.MAX_LIGHT) {
                    for (; y >= 0; y--) {
                        Block block = worldView.getBlock(x + region.min().x, y, z + region.min().z);
                        if (LightingUtil.canSpreadLightOutOf(lastBlock, Side.BOTTOM) && LightingUtil.canSpreadLightInto(block, Side.TOP) && LightingUtil.doesSunlightRetainsFullStrengthIn(block)) {
                            worldView.setSunlight(x + region.min().x, y, z + region.min().z, Chunk.MAX_LIGHT);
                            lastBlock = block;
                        } else {
                            break;
                        }
                    }
                }

                tops[x + region.size().x * z] = (short) y;
                for (; y >= 0; y--) {
                    worldView.setSunlight(x + region.min().x, y, z + region.min().z, (byte) 0);
                }

            }
        }

        // Spread internal to the changed column
        for (int x = 0; x < region.size().x; x++) {
            for (int z = 0; z < region.size().z; z++) {
                // Pull light down
                if (tops[x + region.size().x * z] > 0) {
                    propagateSunlightFrom(region.min().x + x, tops[x + region.size().x * z] + 1, region.min().z + z, Side.BOTTOM);
                }
                for (int y = tops[x + region.size().x * z]; y >= 0; y--) {
                    if (x <= 0 || tops[(x - 1) + region.size().x * z] < y) {
                        propagateSunlightFrom(region.min().x + x - 1, y, region.min().z + z, Side.RIGHT);
                    }
                    if (x >= region.size().x - 1 || tops[(x + 1) + region.size().x * z] < y) {
                        propagateSunlightFrom(region.min().x + x + 1, y, region.min().z + z, Side.LEFT);
                    }
                    if (z <= 0 || tops[x + region.size().x * (z - 1)] < y) {
                        propagateSunlightFrom(region.min().x + x, y, region.min().z + z - 1, Side.BACK);
                    }
                    if (z >= region.size().z - 1 || tops[x + region.size().x * (z + 1)] < y) {
                        propagateSunlightFrom(region.min().x + x, y, region.min().z + z + 1, Side.FRONT);
                    }
                }
            }
        }
    }

    private void propagateSunlightFrom(int blockX, int blockY, int blockZ, Side side) {
        byte lightLevel = worldView.getSunlight(blockX, blockY, blockZ);
        Vector3i adjSide = new Vector3i(blockX, blockY, blockZ);
        adjSide.add(side.getVector3i());
        Block block = worldView.getBlock(blockX, blockY, blockZ);
        Block adjBlock = worldView.getBlock(adjSide);
        if (lightLevel > 1 && worldView.getSunlight(adjSide) < lightLevel - 1 && LightingUtil.canSpreadLightOutOf(block, side) && LightingUtil.canSpreadLightInto(adjBlock, side.reverse())) {
            worldView.setSunlight(adjSide, (byte) (lightLevel - 1));
            pushSunlight(adjSide.x, adjSide.y, adjSide.z, (byte) (lightLevel - 1));
        }
    }

    private void propagateLightFrom(int blockX, int blockY, int blockZ, Side side) {
        byte lightLevel = worldView.getLight(blockX, blockY, blockZ);
        Vector3i adjSide = new Vector3i(blockX, blockY, blockZ);
        adjSide.add(side.getVector3i());
        Block block = worldView.getBlock(blockX, blockY, blockZ);
        Block adjBlock = worldView.getBlock(adjSide);
        if (lightLevel > 1 && worldView.getLight(adjSide) < lightLevel - 1 && LightingUtil.canSpreadLightOutOf(block, side) && LightingUtil.canSpreadLightInto(adjBlock, side.reverse())) {
            worldView.setLight(adjSide, (byte) (lightLevel - 1));
            pushLight(adjSide.x, adjSide.y, adjSide.z, (byte) (lightLevel - 1));
        }
    }
}
