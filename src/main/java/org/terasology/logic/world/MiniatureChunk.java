/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.logic.world;

import com.bulletphysics.dynamics.RigidBody;
import com.google.common.base.Objects;
import org.terasology.logic.manager.Config;
import org.terasology.logic.world.chunks.Chunk;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.AABB;
import org.terasology.model.structures.TeraArray;
import org.terasology.model.structures.TeraSmartArray;
import org.terasology.rendering.primitives.ChunkMesh;

import javax.vecmath.Vector3d;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Chunks are the basic components of the world. Each chunk contains a fixed amount of blocks
 * determined by its dimensions. They are used to manage the world efficiently and
 * to reduce the batch count within the render loop.
 * <p/>
 * Chunks are tessellated on creation and saved to vertex arrays. From those VBOs are generated
 * which are then used for the actual rendering process.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class MiniatureChunk extends Chunk {
    public static final int SIZE_X = 512;
    public static final int SIZE_Y = 256;
    public static final int SIZE_Z = 512;
    public static final int INNER_CHUNK_POS_FILTER_X = TeraMath.ceilPowerOfTwo(SIZE_X) - 1;
    public static final int INNER_CHUNK_POS_FILTER_Z = TeraMath.ceilPowerOfTwo(SIZE_Z) - 1;
    public static final int POWER_X = TeraMath.sizeOfPower(SIZE_X);
    public static final int POWER_Z = TeraMath.sizeOfPower(SIZE_Z);
    public static final int VERTICAL_SEGMENTS = Config.getInstance().getVerticalChunkMeshSegments();
    public static final byte MAX_LIGHT = 0x0f;

    public static final Vector3i CHUNK_POWER = new Vector3i(POWER_X, 0, POWER_Z);
    public static final Vector3i CHUNK_SIZE = new Vector3i(SIZE_X, SIZE_Y, SIZE_Z);
    public static final Vector3i INNER_CHUNK_POS_FILTER = new Vector3i(INNER_CHUNK_POS_FILTER_X, 0, INNER_CHUNK_POS_FILTER_Z);

    public int getChunkSizeX() {
        return SIZE_X;
    }

    public int getChunkSizeY() {
        return SIZE_Y;
    }

    public int getChunkSizeZ() {
        return SIZE_Z;
    }
}
