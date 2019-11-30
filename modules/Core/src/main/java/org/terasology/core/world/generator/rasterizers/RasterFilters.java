/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.core.world.generator.rasterizers;

import com.google.common.base.Predicate;
import org.terasology.math.Side;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.facets.DensityFacet;

import java.util.Collection;

/**
 * A collection of filters that restrict the placement of objects based on block type.
 * Filters that rely on facets, not specific block data, can be found in {@link org.terasology.core.world.generator.facetProviders.PositionFilters}.
 */
public class RasterFilters {

    /**
     * Checks a list of valid block types against the block below the specified location.
     * @see #whiteListBlocks(CoreChunk, BaseVector3i, String...)
     */
    public static Predicate<Vector3i> whiteListBlocks(CoreChunk chunk, String... whiteList) {
        return whiteListBlocks(chunk, new Vector3i(0, -1, 0), whiteList);
    }

    /**
     * Checks a list of valid block types against the specified location.
     * @param chunk The generated chunk that is being decorated.
     * @param location The offset to be applied to the input.
     *                 Typically, it will be (x = 0, y = -1, z = 0): the block directly beneath the checked location.
     * @param whiteList A list of block names or block categories that are to be accepted.
     *                  While it is recommended to use block categories, full or partial block URIs are accepted as well.<br/>
     *                  If the whitelisted name includes a semicolon (<b>:</b>) it will be matched against the full block URI (<i>BlockModule:Name:ShapeModule:Shape.SUBSHAPE</i>).<br/>
     *                  Trailing or leading semicolons may be used to narrow the scope of matches.<br/>
     *                  - "<u>CoreBlocks:Dirt</u>" will match both "CoreBlocks:Dirt" and "CoreBlocks:DirtyWater".<br/>
     *                  - "<u>CoreBlocks:Dirt:</u>" will only match "CoreBlocks:Dirt" in any shape.<br/>
     *                  - "<u>CoreBlocks:Dirt:engine:cube</u>" will only match standard dirt blocks that are the full cube shape.<br/>
     *                  - "<u>:Dirt</u>" will match any block name starting with "Dirt", such as "CoreBlocks:Dirt", "OtherModule:Dirt", and "CoreBlocks:DirtyWater".<br/>
     *                  - "<u>Dirt:</u>" will match any block name ending in "Dirt", but also any block from a "FancyDirt" module.<br/>
     *                  - "<u>:Dirt:</u>" will match only blocks named "Dirt" from any module.<br/>
     *                  - "<u>Dirt:engine:cube</u>" will match any block name ending in "Dirt" that is a normal cube shape.<br/>
     *                  - "<u>engine:cube</u>" will match any full block. <u><i>This particular example is not recommended; use {@link org.terasology.core.world.generator.facetProviders.PositionFilters#density(DensityFacet) PositionFilters.density} or {@link #canSupport(CoreChunk, Collection<Side>) RasterFilters.canSupport} instead.</u></i><br/>
     *                  If the whitelisted name <b>does not</b> include a semicolon, it will only be matched against block categories.<br/>
     *                  - "<u>Wood</u>" will not find any block named "Wood" unless the block has the "Wood" category.<br/>
     *                  Block names and categories may be used interchangeably in the same whitelist.
     * @return True if the checked location contains a matching block.
     */
    public static Predicate<Vector3i> whiteListBlocks(CoreChunk chunk, BaseVector3i location, String... whiteList) {
        return input -> {
            assert input != null && whiteList.length > 0;
            Block block = chunk.getBlock(input.add(location));
            BlockFamily bF = block.getBlockFamily(); //the primary metadata about the block
            String bU = block.getURI().toString().toLowerCase(); //e.g. "CoreBlocks:Dirt:engine:stairs.LEFT"
            String bN = block.getURI().getIdentifier().toString().toLowerCase(); //e.g. "Dirt"
            for (String key : whiteList)
            {
                if (key.contains(":") && bU.contains(key.toLowerCase())) return true;
                if (bN.equalsIgnoreCase(key) || bF.hasCategory(key)) return true;
            }
            return false;
        };
    }

    /**
     * Checks a list of valid block types against the block below the specified location.
     * @see #whiteListBlocks(CoreChunk, BaseVector3i, String...)
     */
    public static Predicate<Vector3i> blackListBlocks(CoreChunk chunk, String... blackList) {
        return blackListBlocks(chunk, new Vector3i(0, -1, 0), blackList);
    }

    /**
     * Checks a list of specified blocks against the specified location.
     * @return true if the block does not match any of the blacklisted input.
     * @see #whiteListBlocks(CoreChunk, BaseVector3i, String...)
     */
    public static Predicate<Vector3i> blackListBlocks(CoreChunk chunk, BaseVector3i location, String... blackList) {
        return input -> !whiteListBlocks(chunk, location, blackList).apply(input);
    }

    /**
     * Checks to see if the given location can support an attached block at any of the given locations.
     * @param chunk The generated chunk that is being decorated.
     * @param sides The acceptable sides that can support this location, such as {@link Side#horizontalSides()}
     * @return True if any of the given sides return a positive for {@link Block#isAttachmentAllowed()}
     */
    public static Predicate<Vector3i> canSupport(CoreChunk chunk, Collection<Side> sides) {
        return input -> {
            for (Side side : sides)
            {
                if (canSupport(chunk, side).apply(input)) return true;
            }
            return false;
        };
    }

    /**
     * Checks to see if the given location can be supported by the block at the given side.
     * @param chunk The generated chunk being decorated.
     * @param side The side to check, relative to the input location, such as {@link Side#BOTTOM} for the block beneath the input location.
     * @return True if the block at the given side returns a positive for {@link Block#isAttachmentAllowed()}
     */
    public static Predicate<Vector3i> canSupport(CoreChunk chunk, Side side) {
        return input -> input != null && chunk.getBlock(input.add(side.getVector3i())).isAttachmentAllowed();
    }
}
