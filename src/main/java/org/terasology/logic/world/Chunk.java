/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.logic.generators.ChunkGenerator;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.AABB;
import org.terasology.model.structures.TeraArray;
import org.terasology.model.structures.TeraSmartArray;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;
import org.terasology.utilities.Helper;

import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

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
public class Chunk implements Comparable<Chunk>, Externalizable {
    public static final long serialVersionUID = 79881925217704826L;

    /* PUBLIC CONSTANT VALUES */
    public static final int CHUNK_DIMENSION_X = 16;
    public static final int CHUNK_DIMENSION_Y = 256;
    public static final int CHUNK_DIMENSION_Z = 16;
    public static final int VERTICAL_SEGMENTS = Config.getInstance().getVerticalChunkMeshSegments();

    private static final Logger logger = Logger.getLogger(Chunk.class.getName());

    private static final Vector3d[] LIGHT_DIRECTIONS = {
            new Vector3d(1, 0, 0), new Vector3d(-1, 0, 0),
            new Vector3d(0, 1, 0), new Vector3d(0, -1, 0),
            new Vector3d(0, 0, 1), new Vector3d(0, 0, -1)
    };

    public static int _statChunkMeshEmpty, _statChunkNotReady, _statRenderedTriangles;

    private int _id = 0;
    private final Vector3i _pos = new Vector3i();

    private final TeraArray _blocks;
    private final TeraSmartArray _sunlight, _light, _states;
    private final ChunkTessellator _tessellator;

    private boolean _dirty, _lightDirty, _fresh;
    private boolean _disposed = false;

    private FastRandom _random;
    private LocalWorldProvider _parent;
    private ChunkMesh _activeMeshes[];
    private ChunkMesh _newMeshes[];

    private AABB _aabb = null;
    private AABB[] _subMeshAABB = null;
    private RigidBody _rigidBody = null;
    private ReentrantLock _lock = new ReentrantLock();
    private ReentrantLock _lockRigidBody = new ReentrantLock();

    public boolean isAprox = false;

    public enum LIGHT_TYPE {
        BLOCK,
        SUN
    }

    public static String getChunkFileName(Vector3i v) {
        return Integer.toString(v.hashCode(),36);
    }

    public static String getChunkFileNameFromId(int id) {
        return Integer.toString(id,36);
    }

    public Chunk() {
        _tessellator = new ChunkTessellator(this);
        _blocks = new TeraArray(CHUNK_DIMENSION_X, CHUNK_DIMENSION_Y, CHUNK_DIMENSION_Z);
        _sunlight = new TeraSmartArray(CHUNK_DIMENSION_X, CHUNK_DIMENSION_Y, CHUNK_DIMENSION_Z);
        _light = new TeraSmartArray(CHUNK_DIMENSION_X, CHUNK_DIMENSION_Y, CHUNK_DIMENSION_Z);
        _states = new TeraSmartArray(CHUNK_DIMENSION_X, CHUNK_DIMENSION_Y, CHUNK_DIMENSION_Z);

        setLightDirty(true);
        setDirty(true);
        setFresh(true);

        _random = new FastRandom();
    }

    public Chunk(LocalWorldProvider p, int x, int y, int z) {
        this();
        _pos.x = x;
        _pos.y = y;
        _pos.z = z;
        _id = _pos.hashCode();
        _parent = p;
        _random = new FastRandom(_parent.getSeed().hashCode() + _pos.hashCode());
    }

    public boolean generate() {
        if (isFresh())
        {
            for (ChunkGenerator gen : _parent.getGeneratorManager().getChunkGenerators())
                gen.generate(this);
            generateSunlight();
            setFresh(false);
            return true;
        }
        return false;
    }

    public void updateLight() {
        if (isFresh() || !isLightDirty())
            return;
        for (int x = 0; x < CHUNK_DIMENSION_X; x++)
            for (int z = 0; z < CHUNK_DIMENSION_Z; z++)
                for (int y = CHUNK_DIMENSION_Y - 1; y >= 0; y--)
                {
                    byte blockValue = getBlock(x, y, z);
                    byte lightValue = getLight(x, y, z, LIGHT_TYPE.SUN);
                    if (!BlockManager.getInstance().getBlock(blockValue).isTranslucent())
                        continue;
                    // Spread the sunlight in translucent blocks with a light value greater than zero.
                    if (lightValue > 0)
                        spreadLight(x, y, z, lightValue, LIGHT_TYPE.SUN);
                }
        setLightDirty(false);
    }

    private void generateSunlight() {
        for (int x = 0; x < CHUNK_DIMENSION_X; x++)
            for (int z = 0; z < CHUNK_DIMENSION_Z; z++)
                refreshSunlightAtLocalPos(x, z, false, false);
    }

    public void refreshSunlightAtLocalPos(int x, int z, boolean spreadLight, boolean refreshSunlight) {
        boolean covered = false;

        for (int y = CHUNK_DIMENSION_Y - 1; y >= 0; y--) {
            byte blockId = _blocks.get(x, y, z);
            Block b = BlockManager.getInstance().getBlock(blockId);

            // Remember if this "column" is covered
            if (!b.isInvisible() && b.getBlockForm() != Block.BLOCK_FORM.BILLBOARD && !covered)
                covered = true;

            byte oldValue = _sunlight.get(x, y, z);
            byte newValue;

            // If the column is not covered...
            if (!covered) {
                if (b.isInvisible() || b.getBlockForm() == Block.BLOCK_FORM.BILLBOARD)
                    _sunlight.set(x, y, z, (byte) 15);
                else _sunlight.set(x, y, z, (byte) 0x0);
                newValue = _sunlight.get(x, y, z);
                // Otherwise the column is covered. Don't generate any light in the cells...
            }
            else {
                _sunlight.set(x, y, z, (byte) 0);
                // Update the sunlight at the current position (check the surrounding cells)
                if (refreshSunlight)
                    refreshLightAtLocalPos(x, y, z, LIGHT_TYPE.SUN);
                newValue = _sunlight.get(x, y, z);
            }
            if (spreadLight && oldValue > newValue)
                unspreadLight(x, y, z, oldValue, Chunk.LIGHT_TYPE.SUN);
            else if (spreadLight && oldValue < newValue)
                /*
                * Spread sunlight if the new light value is more intense
                * than the old value.
                */
                spreadLight(x, y, z, newValue, LIGHT_TYPE.SUN);
        }
    }

    public void refreshLightAtLocalPos(int x, int y, int z, LIGHT_TYPE type) {
        int blockPosX = getBlockWorldPosX(x);
        int blockPosZ = getBlockWorldPosZ(z);

        byte bType = getBlock(x, y, z);

        // If a block was just placed, remove the light value at this point
        if (!BlockManager.getInstance().getBlock(bType).isTranslucent()) {
            setLight(x, y, z, (byte) 0, type);
        } else {
            // If the block was removed: Find the brightest neighbor and
            // set the current light value to this value - 1
            byte val = getParent().getLight(blockPosX, y, blockPosZ, type);
            byte val1 = getParent().getLight(blockPosX + 1, y, blockPosZ, type);
            byte val2 = getParent().getLight(blockPosX - 1, y, blockPosZ, type);
            byte val3 = getParent().getLight(blockPosX, y, blockPosZ + 1, type);
            byte val4 = getParent().getLight(blockPosX, y, blockPosZ - 1, type);
            byte val5 = getParent().getLight(blockPosX, y + 1, blockPosZ, type);
            byte val6 = getParent().getLight(blockPosX, y - 1, blockPosZ, type);

            byte max = (byte) (java.lang.Math.max(java.lang.Math.max(java.lang.Math.max(val1, val2), java.lang.Math.max(val3, val4)), java.lang.Math.max(val5, val6)) - 1);

            if (max < 0) {
                max = 0;
            }

            // Do nothing if the current light value is brighter
            byte res = (byte) java.lang.Math.max(max, val);

            // Finally set the new light value
            setLight(x, y, z, res, type);
        }
    }

    public void unspreadLight(int x, int y, int z, byte lightValue, LIGHT_TYPE type) {
        ArrayList<Vector3d> brightSpots = new ArrayList<Vector3d>();
        unspreadLight(x, y, z, lightValue, 0, type, brightSpots);

        for (Vector3d pos : brightSpots) {
            getParent().spreadLight((int) pos.x, (int) pos.y, (int) pos.z, _parent.getLight((int) pos.x, (int) pos.y, (int) pos.z, type), 0, type);
        }
    }

    public void unspreadLight(int x, int y, int z, byte lightValue, int depth, LIGHT_TYPE type, ArrayList<Vector3d> brightSpots) {
        int blockPosX = getBlockWorldPosX(x);
        int blockPosZ = getBlockWorldPosZ(z);

        // Remove the light at this point
        getParent().setLight(blockPosX, y, blockPosZ, (byte) 0x0, type);

        for (int i = 0; i < 6; i++) {

            byte neighborValue = getParent().getLight(blockPosX + (int) LIGHT_DIRECTIONS[i].x, y + (int) LIGHT_DIRECTIONS[i].y, blockPosZ + (int) LIGHT_DIRECTIONS[i].z, type);
            byte neighborType = getParent().getBlock(blockPosX + (int) LIGHT_DIRECTIONS[i].x, y + (int) LIGHT_DIRECTIONS[i].y, blockPosZ + (int) LIGHT_DIRECTIONS[i].z);

            if (neighborValue < lightValue && neighborValue > 0 && BlockManager.getInstance().getBlock(neighborType).isTranslucent()) {
                getParent().unspreadLight(blockPosX + (int) LIGHT_DIRECTIONS[i].x, y + (int) LIGHT_DIRECTIONS[i].y, blockPosZ + (int) LIGHT_DIRECTIONS[i].z, (byte) (lightValue - 1), depth + 1, type, brightSpots);
            } else if (neighborValue >= lightValue) {
                brightSpots.add(new Vector3d(blockPosX + (int) LIGHT_DIRECTIONS[i].x, y + (int) LIGHT_DIRECTIONS[i].y, blockPosZ + (int) LIGHT_DIRECTIONS[i].z));
            }
        }
    }

    public void spreadLight(int x, int y, int z, byte lightValue, LIGHT_TYPE type) {
        spreadLight(x, y, z, lightValue, 0, type);
    }

    public void spreadLight(int x, int y, int z, byte lightValue, int depth, LIGHT_TYPE type) {
        if (depth > lightValue || lightValue - depth < 1) {
            return;
        }

        int blockPosX = getBlockWorldPosX(x);
        int blockPosZ = getBlockWorldPosZ(z);

        byte newLightValue;
        newLightValue = (byte) (lightValue - depth);

        getParent().setLight(blockPosX, y, blockPosZ, newLightValue, type);

        for (int i = 0; i < 6; i++) {
            byte neighborValue = getParent().getLight(blockPosX + (int) LIGHT_DIRECTIONS[i].x, y + (int) LIGHT_DIRECTIONS[i].y, blockPosZ + (int) LIGHT_DIRECTIONS[i].z, type);
            byte neighborType = getParent().getBlock(blockPosX + (int) LIGHT_DIRECTIONS[i].x, y + (int) LIGHT_DIRECTIONS[i].y, blockPosZ + (int) LIGHT_DIRECTIONS[i].z);

            if (neighborValue < newLightValue - 1 && BlockManager.getInstance().getBlock(neighborType).isTranslucent()) {
                getParent().spreadLight(blockPosX + (int) LIGHT_DIRECTIONS[i].x, y + (int) LIGHT_DIRECTIONS[i].y, blockPosZ + (int) LIGHT_DIRECTIONS[i].z, lightValue, depth + 1, type);
            }
        }
    }

    public byte getLight(int x, int y, int z, LIGHT_TYPE type) {
        byte result;

        if (type == LIGHT_TYPE.SUN) {
            result = _sunlight.get(x, y, z);
        } else {
            result = _light.get(x, y, z);
        }

        if (result >= 0) {
            return result;
        }

        return 15;
    }

    public void setLight(int x, int y, int z, byte intensity, LIGHT_TYPE type) {
        TeraSmartArray lSource;
        if (type == LIGHT_TYPE.SUN) {
            lSource = _sunlight;
        } else if (type == LIGHT_TYPE.BLOCK) {
            lSource = _light;
        } else {
            return;
        }

        byte oldValue = lSource.get(x, y, z);
        lSource.set(x, y, z, intensity);

        if (oldValue != intensity) {
            setDirty(true);
            // Mark the neighbors as dirty
            markNeighborsDirty(x, z);
        }
    }

    public byte getBlock(int x, int y, int z) {
        byte result = _blocks.get(x, y, z);

        if (result >= 0) {
            return result;
        }

        return 0;
    }

    public byte getState(int x, int y, int z) {
        return _states.get(x, y, z);
    }

    public boolean canBlockSeeTheSky(int x, int y, int z) {
        for (int y1 = y + 1; y1 < CHUNK_DIMENSION_Y; y1++) {
            if (!BlockManager.getInstance().getBlock(getBlock(x, y1, z)).isTranslucent())
                return false;
        }

        return true;
    }

    public void setBlock(int x, int y, int z, byte type) {
        byte oldValue = _blocks.get(x, y, z);
        _blocks.set(x, y, z, type);

        if (oldValue != type) {
            // Update vertex arrays and light
            setDirty(true);
            // Mark the neighbors as dirty
            markNeighborsDirty(x, z);
        }
    }

    public void setState(int x, int y, int z, byte type) {
        _states.set(x, y, z, type);
    }

    public double distanceToCamera() {
        Vector3d result = new Vector3d(_pos.x * CHUNK_DIMENSION_X, _pos.y * CHUNK_DIMENSION_Y, _pos.z * CHUNK_DIMENSION_Z);

        Vector3d cameraPos = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
        result.x -= cameraPos.x;
        result.z -= cameraPos.z;

        return result.length();
    }

    public Chunk[] loadOrCreateNeighbors() {
        Chunk[] chunks = new Chunk[8];

        chunks[0] = getParent().getChunkProvider().getChunk(_pos.x + 1, 0, _pos.z);
        chunks[1] = getParent().getChunkProvider().getChunk(_pos.x - 1, 0, _pos.z);
        chunks[2] = getParent().getChunkProvider().getChunk(_pos.x, 0, _pos.z + 1);
        chunks[3] = getParent().getChunkProvider().getChunk(_pos.x, 0, _pos.z - 1);
        chunks[4] = getParent().getChunkProvider().getChunk(_pos.x + 1, 0, _pos.z + 1);
        chunks[5] = getParent().getChunkProvider().getChunk(_pos.x - 1, 0, _pos.z - 1);
        chunks[6] = getParent().getChunkProvider().getChunk(_pos.x - 1, 0, _pos.z + 1);
        chunks[7] = getParent().getChunkProvider().getChunk(_pos.x + 1, 0, _pos.z - 1);

        return chunks;
    }

    private void markNeighborsDirty(int x, int z) {
        Chunk[] neighbors = loadOrCreateNeighbors();

        if (x == 0 && neighbors[1] != null) {
            neighbors[1].setDirty(true);
        }

        if (x == CHUNK_DIMENSION_X - 1 && neighbors[0] != null) {
            neighbors[0].setDirty(true);
        }

        if (z == 0 && neighbors[3] != null) {
            neighbors[3].setDirty(true);
        }

        if (z == CHUNK_DIMENSION_Z - 1 && neighbors[2] != null) {
            neighbors[2].setDirty(true);
        }

        if (x == CHUNK_DIMENSION_X - 1 && z == 0 && neighbors[7] != null) {
            neighbors[7].setDirty(true);
        }

        if (x == 0 && z == CHUNK_DIMENSION_Z - 1 && neighbors[6] != null) {
            neighbors[6].setDirty(true);
        }

        if (x == 0 && z == 0 && neighbors[5] != null) {
            neighbors[5].setDirty(true);
        }

        if (x == CHUNK_DIMENSION_X - 1 && z == CHUNK_DIMENSION_Z - 1 && neighbors[4] != null) {
            neighbors[4].setDirty(true);
        }
    }

    @Override
    public String toString() {
        return String.format("Chunk at %s.", _pos.toString());
    }

    /**
     * Chunks are comparable by their relative distance to the player.
     *
     * @param o The chunk to compare to
     * @return The comparison value
     */
    @Override
    public int compareTo(Chunk o) {
        if (o == null) {
            return 0;
        }

        double distance = distanceToCamera();
        double distance2 = o.distanceToCamera();

        if (distance == distance2)
            return 0;

        return distance2 > distance ? -1 : 1;
    }

    public AABB getAABB() {
        if (_aabb == null) {
            // TODO: The AABBs are currently a bit larger than the actual chunk is
            Vector3d dimensions = new Vector3d(CHUNK_DIMENSION_X / 1.75, CHUNK_DIMENSION_Y / 1.75, CHUNK_DIMENSION_Z / 1.75);
            Vector3d position = new Vector3d(getChunkWorldPosX() + dimensions.x - 0.5f, dimensions.y - 0.5f, getChunkWorldPosZ() + dimensions.z - 0.5f);
            _aabb = new AABB(position, dimensions);
        }

        return _aabb;
    }

    public AABB getSubMeshAABB(int subMesh) {
        if (_subMeshAABB == null) {
            _subMeshAABB = new AABB[VERTICAL_SEGMENTS];

            int heightHalf = CHUNK_DIMENSION_Y / VERTICAL_SEGMENTS / 2;

            for (int i = 0; i < _subMeshAABB.length; i++) {
                Vector3d dimensions = new Vector3d(8, heightHalf, 8);
                Vector3d position = new Vector3d(getChunkWorldPosX() + dimensions.x - 0.5f, i * heightHalf * 2 + dimensions.y - 0.5f, getChunkWorldPosZ() + dimensions.z - 0.5f);
                _subMeshAABB[i] = new AABB(position, dimensions);
            }
        }

        return _subMeshAABB[subMesh];
    }

    public void processChunk() {
        /*
        * Generate the chunk...
        */
        generate();

        /*
        * ... and fetch its neighbors...
        */
        Chunk[] neighbors = loadOrCreateNeighbors();

        /*
        * Before starting the illumination process, make sure that the neighbor chunks
        * are present and fully generated.
        */
        for (Chunk neighbor : neighbors)
        {
            if (neighbor != null) {
                neighbor.generate();
            }
        }

        // Finally update the light and generate the meshes
        updateLight();
        generateMeshes();
    }

    /**
     * Generates the terrain mesh (creates the internal vertex arrays).
     */
    public void generateMeshes() {
        if (isFresh() || isLightDirty() || !isDirty())
            return;

        ChunkMesh[] newMeshes = new ChunkMesh[VERTICAL_SEGMENTS];

        for (int i = 0; i < VERTICAL_SEGMENTS; i++) {
            newMeshes[i] = _tessellator.generateMesh(CHUNK_DIMENSION_Y / VERTICAL_SEGMENTS, i * (CHUNK_DIMENSION_Y / VERTICAL_SEGMENTS));
        }

        setNewMesh(newMeshes);
        setDirty(false);
    }

    /**
     * Draws the opaque or translucent elements of a chunk.
     *
     * @param type The type of vertices to render
     */
    public void render(ChunkMesh.RENDER_PHASE type) {
        if (isReadyForRendering()) {
            ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("chunk");
            // Transfer the world offset of the chunk to the shader for various effects
            shader.setFloat3("chunkOffset", (_pos.x * Chunk.CHUNK_DIMENSION_X), (_pos.y * Chunk.CHUNK_DIMENSION_Y), (_pos.z * Chunk.CHUNK_DIMENSION_Z));

            GL11.glPushMatrix();

            Vector3d cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            GL11.glTranslated(_pos.x * Chunk.CHUNK_DIMENSION_X - cameraPosition.x, _pos.y * Chunk.CHUNK_DIMENSION_Y - cameraPosition.y, _pos.z * Chunk.CHUNK_DIMENSION_Z - cameraPosition.z);

            for (int i = 0; i < VERTICAL_SEGMENTS; i++) {
                if (!isSubMeshEmpty(i)) {
                    if (Config.getInstance().isRenderChunkBoundingBoxes()) {
                        getSubMeshAABB(i).renderLocally(1f);
                        _statRenderedTriangles += 12;
                    }

                    shader.enable();
                    _activeMeshes[i].render(type);
                    _statRenderedTriangles += _activeMeshes[i].triangleCount();
                }
            }

            GL11.glPopMatrix();
        } else {
            _statChunkNotReady++;
        }
    }

    public boolean generateVBOs() {
        if (_newMeshes != null) {
            for (ChunkMesh _newMeshe : _newMeshes)
            {
                _newMeshe.generateVBOs();
            }

            return true;
        }

        return false;
    }

    public void update() {
        generateVBOs();
        swapActiveMesh();
    }

    private void setNewMesh(ChunkMesh[] newMesh) {
        if (_lock.tryLock()) {
            try {
                if (!_disposed) {
                    ChunkMesh[] oldNewMesh = _newMeshes;
                    _newMeshes = newMesh;

                    if (oldNewMesh != null) {
                        for (ChunkMesh element : oldNewMesh)
                            element.dispose();
                    }
                }
            } finally {
                _lock.unlock();
            }
        }
    }

    private boolean swapActiveMesh() {
        if (_lock.tryLock()) {
            try {
                if (!_disposed) {
                    if (_newMeshes != null) {
                        if (!_newMeshes[0].isDisposed() && _newMeshes[0].isGenerated()) {

                            ChunkMesh[] newMesh = _newMeshes;
                            _newMeshes = null;

                            ChunkMesh[] oldActiveMesh = _activeMeshes;
                            _activeMeshes = newMesh;
                            _rigidBody = null;

                            if (oldActiveMesh != null) {
                                for (ChunkMesh element : oldActiveMesh)
                                {
                                    element.dispose();
                                }
                            }

                            return true;
                        }
                    }
                }
            } finally {
                _lock.unlock();
            }
        }

        return false;
    }

    /**
     * Returns the position of the chunk within the world.
     *
     * @return The world position
     */

    public int getChunkWorldPosX() {
        return _pos.x * CHUNK_DIMENSION_X;
    }

    public int getChunkWorldPosY() {
        return _pos.y * CHUNK_DIMENSION_Y;
    }

    public int getChunkWorldPosZ() {
        return _pos.z * CHUNK_DIMENSION_Z;
    }

    public int getBlockWorldPosX(int x) {
        return x + getChunkWorldPosX();
    }

    public int getBlockWorldPosY(int y) {
        return y + getChunkWorldPosY();
    }

    public int getBlockWorldPosZ(int z) {
        return z + getChunkWorldPosZ();
    }

    public LocalWorldProvider getParent() {
        return _parent;
    }

    public boolean isDirty() {
        return _dirty;
    }

    public boolean isFresh() {
        return _fresh;
    }

    public boolean isLightDirty() {
        return _lightDirty;
    }

    public void setFresh(boolean fresh) {
        _fresh = fresh;
    }

    public void setDirty(boolean dirty) {
        _dirty = dirty;
    }

    public void setLightDirty(boolean lightDirty) {
        _lightDirty = lightDirty;
    }

    public void setParent(LocalWorldProvider parent) {
        _parent = parent;
    }

    public String getChunkFileName() {
        return getChunkFileName(_pos);
    }

    public FastRandom getRandom() {
        return _random;
    }

    public void clearMeshes() {
        _lock.lock();

        try {
            if (_disposed)
                return;

            if (_activeMeshes != null) for (ChunkMesh _activeMeshe : _activeMeshes)
                _activeMeshe.dispose();
            if (_newMeshes != null) {
                for (ChunkMesh _newMeshe : _newMeshes)
                    _newMeshe.dispose();
            }

            _activeMeshes = null;
            _newMeshes = null;
            setDirty(true);

        } finally {
            _lock.unlock();
        }
    }

    /**
     * Disposes this chunk. Can NOT be undone.
     */
    public void dispose() {
        _lock.lock();

        try {
            if (_disposed)
                return;

            if (_activeMeshes != null) for (ChunkMesh _activeMeshe : _activeMeshes)
                _activeMeshe.dispose();
            if (_newMeshes != null) {
                for (ChunkMesh _newMeshe : _newMeshes)
                    _newMeshe.dispose();
            }

            _disposed = true;
            _activeMeshes = null;
            _newMeshes = null;

        } finally {
            _lock.unlock();
        }
    }

    public boolean isReadyForRendering() {
        return _activeMeshes != null;
    }

    public boolean isSubMeshEmpty(int subMesh) {
        if (isReadyForRendering()) {
            return _activeMeshes[subMesh].isEmpty();
        } else {
            return true;
        }
    }

    public void updateRigidBody() {
        updateRigidBody(_activeMeshes);
    }

    private void updateRigidBody(final ChunkMesh[] meshes) {
        if (_rigidBody != null || meshes == null || meshes.length < VERTICAL_SEGMENTS)
            return;

        CoreRegistry.get(GameEngine.class).submitTask("Update Chunk Collision", new Runnable() {
            @Override
            public void run() {
                try {
                    _lockRigidBody.lock();

                    TriangleIndexVertexArray vertexArray = new TriangleIndexVertexArray();

                    int counter = 0;
                    for (int k = 0; k < Chunk.VERTICAL_SEGMENTS; k++) {
                        ChunkMesh mesh = meshes[k];

                        if (mesh != null) {
                            IndexedMesh indexedMesh = mesh._indexedMesh;

                            if (indexedMesh != null) {
                                vertexArray.addIndexedMesh(indexedMesh);
                                counter++;
                            }

                            mesh._indexedMesh = null;
                        }
                    }

                    if (counter == VERTICAL_SEGMENTS) {
                        try {
                            BvhTriangleMeshShape shape = new BvhTriangleMeshShape(vertexArray, true);

                            Matrix3f rot = new Matrix3f();
                            rot.setIdentity();

                            DefaultMotionState blockMotionState = new DefaultMotionState(new Transform(new Matrix4f(rot, new Vector3f((float) _pos.x * Chunk.CHUNK_DIMENSION_X, (float) _pos.y * Chunk.CHUNK_DIMENSION_Y, (float) _pos.z * Chunk.CHUNK_DIMENSION_Z), 1.0f)));

                            RigidBodyConstructionInfo blockConsInf = new RigidBodyConstructionInfo(0, blockMotionState, shape, new Vector3f());
                            _rigidBody = new RigidBody(blockConsInf);

                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Chunk failed to create rigid body.", e);
                        }
                    }
                } finally {
                    _lockRigidBody.unlock();
                }
            }
        });
    }

    public int triangleCount(ChunkMesh.RENDER_PHASE type) {
        int count = 0;

        if (isReadyForRendering())
            for (int i = 0; i < VERTICAL_SEGMENTS; i++)
                count += _activeMeshes[i].triangleCount(type);

        return count;
    }

    public RigidBody getRigidBody() {
        return _rigidBody;
    }

    public int getId() {
        return _id;
    }

    public Vector3i getPos() {
        return _pos;
    }

    public static void resetStats() {
        _statChunkMeshEmpty = 0;
        _statChunkNotReady = 0;
        _statRenderedTriangles = 0;
    }

    //Externalizable Interface
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(_id);
        out.writeInt(_pos.x);
        out.writeInt(_pos.y);
        out.writeInt(_pos.z);

        // Save flags...
        byte flags = 0x0;
        if (isLightDirty()) {
            flags = Helper.setFlag(flags, (short) 0);
        }
        if (isFresh()) {
            flags = Helper.setFlag(flags, (short) 1);
        }

        // The flags are stored in the first byte of the file...
        out.writeByte(flags);

        for (int i = 0; i < _blocks.size(); i++)
            out.writeByte(_blocks.getRawByte(i));

        for (int i = 0; i < _sunlight.sizePacked(); i++)
            out.writeByte(_sunlight.getRawByte(i));

        for (int i = 0; i < _light.sizePacked(); i++)
            out.writeByte(_light.getRawByte(i));

        for (int i = 0; i < _states.sizePacked(); i++)
            out.writeByte(_states.getRawByte(i));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _id = in.readInt();
        _pos.x = in.readInt();
        _pos.y = in.readInt();
        _pos.z = in.readInt();

        // The first byte contains the flags...
        byte flags = in.readByte();
        // Parse the flags...
        setLightDirty(Helper.isFlagSet(flags, (short) 0));
        setFresh(Helper.isFlagSet(flags, (short) 1));

        for (int i = 0; i < _blocks.size(); i++)
            _blocks.setRawByte(i, in.readByte());

        for (int i = 0; i < _sunlight.sizePacked(); i++)
            _sunlight.setRawByte(i, in.readByte());

        for (int i = 0; i < _light.sizePacked(); i++)
            _light.setRawByte(i, in.readByte());

        for (int i = 0; i < _states.sizePacked(); i++)
            _states.setRawByte(i, in.readByte());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Chunk)) {
            return false;
        }

        if (this == o) {
            return true;
        }

        Chunk comp = (Chunk) o;
        return getPos().equals(comp.getPos());
    }
}
