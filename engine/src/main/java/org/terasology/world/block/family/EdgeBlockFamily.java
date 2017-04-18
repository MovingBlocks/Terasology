/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.BlockHitDetector;
import org.terasology.math.ChunkMath;
import org.terasology.math.Edge;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Block group for blocks that can be oriented on 12 edges.
 *
 */
public class EdgeBlockFamily extends AbstractBlockFamily {

    Logger logger = LoggerFactory.getLogger(EdgeBlockFamily.class); //fixme delete...
    private EnumMap<Edge, Block> blocks;
    private Edge archetypeEdge;

    public EdgeBlockFamily(BlockUri uri, Edge archetypeEdge, EnumMap<Edge, Block> blocks, Iterable<String> categories) {
        super(uri, categories);
        this.archetypeEdge = archetypeEdge;
        this.blocks = blocks;
        for( Edge edge : Edge.values() ) {
            Block block = blocks.get(edge);
            if (block == null) {
                throw new IllegalArgumentException("Missing block for edge: " + edge.toString());
            }
            block.setBlockFamily(this);
        }
    }

    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Vector3f direction, Vector3f hitNormal, Vector3f hitPosition) {
        Edge edge = BlockHitDetector.detectEdge(hitPosition, location);
        logger.info( "orientation "+  edge ); //fixme delete...

        Block b = blocks.get( edge );
        if( b == null ) {
            b = getArchetypeBlock();
        }
        return b;
    }

    @Override
    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        //FIXME throw Exception?
        return getArchetypeBlock();
    }

    @Override
    public Block getArchetypeBlock() {
        return blocks.get(archetypeEdge);
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                Edge edge = Edge.of(blockUri.getIdentifier());
                return blocks.get(edge);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return blocks.values();
    }

}
