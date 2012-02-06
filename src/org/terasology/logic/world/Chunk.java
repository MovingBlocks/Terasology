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

import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import org.lwjgl.opengl.GL11;
import org.terasology.game.Terasology;
import org.terasology.logic.entities.StaticEntity;
import org.terasology.logic.generators.ChunkGenerator;
import org.terasology.logic.manager.ConfigurationManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockManager;
import org.terasology.model.structures.AABB;
import org.terasology.model.structures.TeraArray;
import org.terasology.model.structures.TeraSmartArray;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;
import org.terasology.utilities.Helper;
import org.terasology.utilities.MathHelper;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

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
public class Chunk extends StaticEntity implements Comparable<Chunk>, Externalizable {

    public static int _statChunkMeshEmpty, _statChunkNotReady, _statRenderedTriangles;

    public static final long serialVersionUID = 1L;

    /* CONSTANT VALUES */
    public static final int CHUNK_DIMENSION_X = 16;
    public static final int CHUNK_DIMENSION_Y = 256;
    public static final int CHUNK_DIMENSION_Z = 16;
    public static final int VERTICAL_SEGMENTS = (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.verticalChunkMeshSegments");
    private static final Vector3d[] LIGHT_DIRECTIONS = {new Vector3d(1, 0, 0), new Vector3d(-1, 0, 0), new Vector3d(0, 1, 0), new Vector3d(0, -1, 0), new Vector3d(0, 0, 1), new Vector3d(0, 0, -1)};

    protected FastRandom _random;
    /* ------ */
    protected boolean _dirty, _lightDirty, _fresh;
    /* ------ */
    protected LocalWorldProvider _parent;
    /* ------ */
    protected final TeraArray _blocks;
    protected final TeraSmartArray _sunlight, _light, _states;
    /* ------ */
    private ChunkMesh _activeMeshes[];
    private ChunkMesh _newMeshes[];
    /* ------ */
    private final ChunkTessellator _tessellator;
    /* ------ */
    private boolean _disposed = false;
    /* ----- */
    private AABB _aabb = null;
    private AABB[] _subMeshAABB = null;
    /* ----- */
    private RigidBody _rigidBody = null;
    /* ----- */
    private ReentrantLock _lock = new ReentrantLock();

    public enum LIGHT_TYPE {
        BLOCK,
        SUN
    }

    public static int getChunkIdForPosition(Vector3d position) {
        return MathHelper.cantorize(MathHelper.mapToPositive((int) position.x), MathHelper.mapToPositive((int) position.z));
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

    /**
     * Init. the chunk with a parent world and an absolute position.
     *
     * @param p        The parent world
     * @param position The absolute position of the chunk within the world
     */
    public Chunk(LocalWorldProvider p, Vector3d position) {
        this();

        setPosition(position);
        _parent = p;

        _random = new FastRandom((_parent.getSeed()).hashCode() + getChunkIdForPosition(position));
    }

    /**
     * Tries to load a chunk from disk. If the chunk is not present,
     * it is created from scratch.
     *
     * @return True if a generation has been executed
     */
    public boolean generate() {
        if (isFresh()) {
            for (ChunkGenerator gen : _parent.getGeneratorManager().getChunkGenerators()) {
                gen.generate(this);
            }

            generateSunlight();
            setFresh(false);

            return true;
        }
        return false;
    }

    /**
     * Updates the light of this chunk.
     */
    public void updateLight() {
        if (isFresh() || !isLightDirty())
            return;

        for (int x = 0; x < CHUNK_DIMENSION_X; x++) {
            for (int z = 0; z < CHUNK_DIMENSION_Z; z++) {
                for (int y = CHUNK_DIMENSION_Y - 1; y >= 0; y--) {
                    byte blockValue = getBlock(x, y, z);
                    byte lightValue = getLight(x, y, z, LIGHT_TYPE.SUN);

                    if (!BlockManager.getInstance().getBlock(blockValue).isTranslucent()) {
                        continue;
                    }

                    // Spread the sunlight in translucent blocks with a light value greater than zero.
                    if (lightValue > 0) {
                        spreadLight(x, y, z, lightValue, LIGHT_TYPE.SUN);
                    }
                }
            }
        }

        setLightDirty(false);
    }

    /**
     * Generates the initial sunlight.
     */
    private void generateSunlight() {
        for (int x = 0; x < CHUNK_DIMENSION_X; x++) {
            for (int z = 0; z < CHUNK_DIMENSION_Z; z++) {
                refreshSunlightAtLocalPos(x, z, false, false);
            }
        }
    }

    /**
     * Calculates the sunlight at a given column within the chunk.
     *
     * @param x               Local block position on the x-axis
     * @param z               Local block position on the z-axis
     * @param spreadLight     Spread light if a light value is greater than the old one
     * @param refreshSunlight Refreshes the sunlight using the surrounding chunks when the light value is lower than before
     */
    public void refreshSunlightAtLocalPos(int x, int z, boolean spreadLight, boolean refreshSunlight) {
        boolean covered = false;

        for (int y = CHUNK_DIMENSION_Y - 1; y >= 0; y--) {
            byte blockId = _blocks.get(x, y, z);
            Block b = BlockManager.getInstance().getBlock(blockId);

            // Remember if this "column" is covered
            if ((!b.isInvisible() && b.getBlockForm() != Block.BLOCK_FORM.BILLBOARD) && !covered) {
                covered = true;
            }

            byte oldValue = _sunlight.get(x, y, z);
            byte newValue;

            // If the column is not covered...
            if (!covered) {
                if (b.isInvisible() || b.getBlockForm() == Block.BLOCK_FORM.BILLBOARD)
                    _sunlight.set(x, y, z, (byte) 15);
                else
                    _sunlight.set(x, y, z, (byte) 0x0);

                newValue = _sunlight.get(x, y, z);

                // Otherwise the column is covered. Don't generate any light in the cells...
            } else {
                _sunlight.set(x, y, z, (byte) 0);

                // Update the sunlight at the current position (check the surrounding cells)
                if (refreshSunlight) {
                    refreshLightAtLocalPos(x, y, z, LIGHT_TYPE.SUN);
                }

                newValue = _sunlight.get(x, y, z);
            }


            if (spreadLight && oldValue > newValue)
                unspreadLight(x, y, z, oldValue, Chunk.LIGHT_TYPE.SUN);
            else if (spreadLight && oldValue < newValue) {
                /*
                * Spread sunlight if the new light value is more intense
                * than the old value.
                */
                spreadLight(x, y, z, newValue, LIGHT_TYPE.SUN);
            }
        }
    }

    /**
     * @param x    Local block position on the x-axis
     * @param y    Local block position on the y-axis
     * @param z    Local block position on the z-axis
     * @param type The type of the light
     */
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

            byte max = (byte) (Math.max(Math.max(Math.max(val1, val2), Math.max(val3, val4)), Math.max(val5, val6)) - 1);

            if (max < 0) {
                max = 0;
            }

            // Do nothing if the current light value is brighter
            byte res = (byte) Math.max(max, val);

            // Finally set the new light value
            setLight(x, y, z, res, type);
        }
    }

    /**
     * Recursive light calculation.
     *
     * @param x          Local block position on the x-axis
     * @param y          Local block position on the y-axis
     * @param z          Local block position on the z-axis
     * @param lightValue The light value used to spread the light
     * @param type       The type of the light
     */
    public void unspreadLight(int x, int y, int z, byte lightValue, LIGHT_TYPE type) {
        ArrayList<Vector3d> brightSpots = new ArrayList<Vector3d>();
        unspreadLight(x, y, z, lightValue, 0, type, brightSpots);

        for (Vector3d pos : brightSpots) {
            getParent().spreadLight((int) pos.x, (int) pos.y, (int) pos.z, _parent.getLight((int) pos.x, (int) pos.y, (int) pos.z, type), 0, type);
        }
    }

    /**
     * Recursive light calculation.
     *
     * @param x           Local block position on the x-axis
     * @param y           Local block position on the y-axis
     * @param z           Local block position on the z-axis
     * @param lightValue  The light value used to spread the light
     * @param depth       Depth of the recursion
     * @param type        The type of the light
     * @param brightSpots Log of light spots, which where brighter than the current light node
     */
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

    /**
     * Recursive light calculation.
     *
     * @param x          Local block position on the x-axis
     * @param y          Local block position on the y-axis
     * @param z          Local block position on the z-axis
     * @param lightValue The light value used to spread the light
     * @param type       The type of the light
     */
    public void spreadLight(int x, int y, int z, byte lightValue, LIGHT_TYPE type) {
        spreadLight(x, y, z, lightValue, 0, type);
    }

    /**
     * Recursive light calculation.
     *
     * @param x          Local block position on the x-axis
     * @param y          Local block position on the y-axis
     * @param z          Local block position on the z-axis
     * @param lightValue The light value used to spread the light
     * @param depth      Depth of the recursion
     * @param type       The type of the light
     */
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

    /**
     * Returns the light intensity at a given local block position.
     *
     * @param x    Local block position on the x-axis
     * @param y    Local block position on the y-axis
     * @param z    Local block position on the z-axis
     * @param type The type of the light
     * @return The light intensity
     */
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

    /**
     * Sets the light value at the given position.
     *
     * @param x         Local block position on the x-axis
     * @param y         Local block position on the y-axis
     * @param z         Local block position on the z-axis
     * @param intensity The light intensity
     * @param type      The type of the light
     */
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

    /**
     * Returns the block type at a given local block position.
     *
     * @param x Local block position on the x-axis
     * @param y Local block position on the y-axis
     * @param z Local block position on the z-axis
     * @return The block type
     */
    public byte getBlock(int x, int y, int z) {
        byte result = _blocks.get(x, y, z);

        if (result >= 0) {
            return result;
        }

        return 0;
    }

    /**
     * Returns the state at a given local block position.
     *
     * @param x Local block position on the x-axis
     * @param y Local block position on the y-axis
     * @param z Local block position on the z-axis
     * @return The block type
     */
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

    /**
     * Sets the block value at the given position.
     *
     * @param x    Local block position on the x-axis
     * @param y    Local block position on the y-axis
     * @param z    Local block position on the z-axis
     * @param type The block type
     */
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

    /**
     * Sets the state value at the given position.
     *
     * @param x    Local block position on the x-axis
     * @param y    Local block position on the y-axis
     * @param z    Local block position on the z-axis
     * @param type The block type
     */
    public void setState(int x, int y, int z, byte type) {
        _states.set(x, y, z, type);
    }

    /**
     * Calculates the distance of the chunk to the player.
     *
     * @return The distance of the chunk to the player
     */
    public double distanceToPlayer() {
        Vector3d result = new Vector3d(getPosition().x * CHUNK_DIMENSION_X, 0, getPosition().z * CHUNK_DIMENSION_Z);

        Vector3d playerPosition = Terasology.getInstance().getActivePlayer().getPosition();
        result.x -= playerPosition.x;
        result.z -= playerPosition.z;

        return result.length();
    }

    /**
     * Returns the neighbor chunks of this chunk.
     *
     * @return The adjacent chunks
     */
    public Chunk[] loadOrCreateNeighbors() {
        Chunk[] chunks = new Chunk[8];

        chunks[0] = getParent().getChunkProvider().loadOrCreateChunk((int) getPosition().x + 1, (int) getPosition().z);
        chunks[1] = getParent().getChunkProvider().loadOrCreateChunk((int) getPosition().x - 1, (int) getPosition().z);
        chunks[2] = getParent().getChunkProvider().loadOrCreateChunk((int) getPosition().x, (int) getPosition().z + 1);
        chunks[3] = getParent().getChunkProvider().loadOrCreateChunk((int) getPosition().x, (int) getPosition().z - 1);
        chunks[4] = getParent().getChunkProvider().loadOrCreateChunk((int) getPosition().x + 1, (int) getPosition().z + 1);
        chunks[5] = getParent().getChunkProvider().loadOrCreateChunk((int) getPosition().x - 1, (int) getPosition().z - 1);
        chunks[6] = getParent().getChunkProvider().loadOrCreateChunk((int) getPosition().x - 1, (int) getPosition().z + 1);
        chunks[7] = getParent().getChunkProvider().loadOrCreateChunk((int) getPosition().x + 1, (int) getPosition().z - 1);

        return chunks;
    }

    /**
     * Marks those neighbors of a chunk dirty, that are adjacent to
     * the given block coordinate.
     *
     * @param x Local block position on the x-axis
     * @param z Local block position on the z-axis
     */
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
        return String.format("Chunk at %s.", getPosition());
    }

    /**
     * Chunks are comparable by their relative distance to the player.
     *
     * @param o The chunk to compare to
     * @return The comparison value
     */
    public int compareTo(Chunk o) {
        if (o == null) {
            return 0;
        }

        double distance = distanceToPlayer();
        double distance2 = o.distanceToPlayer();

        if (distance == distance2)
            return 0;

        return distance2 > distance ? -1 : 1;
    }

    public AABB getAABB() {
        if (_aabb == null) {
            Vector3d dimensions = new Vector3d(CHUNK_DIMENSION_X / 2.0, CHUNK_DIMENSION_Y / 2.0, CHUNK_DIMENSION_Z / 2.0);
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
                Vector3d position = new Vector3d(getChunkWorldPosX() + dimensions.x - 0.5f, (i * heightHalf * 2) + dimensions.y - 0.5f, getChunkWorldPosZ() + dimensions.z - 0.5f);
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
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null) {
                neighbors[i].generate();
            }
        }

        // Finally update the light and generate the meshes
        updateLight();
        generateMeshes();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt((int) getPosition().x);
        out.writeInt((int) getPosition().z);

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

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        getPosition().x = in.readInt();
        getPosition().z = in.readInt();

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
     * @return True if rendered
     */
    public void render(ChunkMesh.RENDER_TYPE type) {
        if (isReadyForRendering()) {
            GL11.glPushMatrix();
            
            Vector3d playerPosition = Terasology.getInstance().getActivePlayer().getPosition();
            GL11.glTranslated(getPosition().x * Chunk.CHUNK_DIMENSION_X - playerPosition.x, getPosition().y * Chunk.CHUNK_DIMENSION_Y - playerPosition.y, getPosition().z * Chunk.CHUNK_DIMENSION_Z - playerPosition.z);

            for (int i = 0; i < VERTICAL_SEGMENTS; i++) {
                if (!isSubMeshEmpty(i)) {
                    if (WorldRenderer.BOUNDING_BOXES_ENABLED) {
                        ShaderManager.getInstance().enableShader(null);
                        getSubMeshAABB(i).renderLocally(2f);
                        _statRenderedTriangles += 12;
                        ShaderManager.getInstance().enableShader("chunk");
                    }

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
            for (int i = 0; i < _newMeshes.length; i++) {
                _newMeshes[i].generateVBOs();
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
                        for (int i = 0; i < oldNewMesh.length; i++)
                            oldNewMesh[i].dispose();
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
                                for (int i = 0; i < oldActiveMesh.length; i++) {
                                    oldActiveMesh[i].dispose();
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
        return (int) getPosition().x * CHUNK_DIMENSION_X;
    }

    /**
     * Returns the position of the chunk within the world.
     *
     * @return Thew world position
     */
    public int getChunkWorldPosZ() {
        return (int) getPosition().z * CHUNK_DIMENSION_Z;
    }

    /**
     * Returns the position of block within the world.
     *
     * @param x The local position
     * @return The world position
     */
    public int getBlockWorldPosX(int x) {
        return x + getChunkWorldPosX();
    }

    /**
     * Returns the position of block within the world.
     *
     * @param z The local position
     * @return The world position
     */
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

    public void setPosition(Vector3d position) {
        super.setPosition(position);
    }

    public void setParent(LocalWorldProvider parent) {
        _parent = parent;
    }

    public static String getChunkSavePathForPosition(Vector3d position) {
        String x36 = Integer.toString((int) position.x, 36);
        String z36 = Integer.toString((int) position.z, 36);

        return x36 + "/" + z36;
    }

    public static String getChunkFileNameForPosition(Vector3d position) {
        String x36 = Integer.toString((int) position.x, 36);
        String z36 = Integer.toString((int) position.z, 36);

        return "bc_" + x36 + "." + z36;
    }

    public String getChunkSavePath() {
        return Chunk.getChunkSavePathForPosition(getPosition());
    }

    public String getChunkFileName() {
        return Chunk.getChunkFileNameForPosition(getPosition());
    }

    public FastRandom getRandom() {
        return _random;
    }

    public void clearMeshes() {
        _lock.lock();

        try {
            if (_disposed)
                return;

            if (_activeMeshes != null)
                for (int i = 0; i < _activeMeshes.length; i++)
                    _activeMeshes[i].dispose();
            if (_newMeshes != null) {
                for (int i = 0; i < _newMeshes.length; i++)
                    _newMeshes[i].dispose();
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

            if (_activeMeshes != null)
                for (int i = 0; i < _activeMeshes.length; i++)
                    _activeMeshes[i].dispose();
            if (_newMeshes != null) {
                for (int i = 0; i < _newMeshes.length; i++)
                    _newMeshes[i].dispose();
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
        if (_rigidBody != null)
            return;

        if (meshes == null)
            return;

        if (meshes.length < VERTICAL_SEGMENTS)
            return;

        Terasology.getInstance().submitTask("Update Chunk Collision", new Runnable() {
            public void run() {
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

                        DefaultMotionState blockMotionState = new DefaultMotionState(new Transform(new Matrix4f(rot, new Vector3f((float) getPosition().x * Chunk.CHUNK_DIMENSION_X, (float) getPosition().y * Chunk.CHUNK_DIMENSION_Y, (float) getPosition().z * Chunk.CHUNK_DIMENSION_Z), 1.0f)));

                        RigidBodyConstructionInfo blockConsInf = new RigidBodyConstructionInfo(0, blockMotionState, shape, new Vector3f());
                        _rigidBody = new RigidBody(blockConsInf);

                    } catch (Exception e) {
                        Terasology.getInstance().getLogger().log(Level.WARNING, "Chunk failed to create rigid body.", e);
                    }
                }

            }
        });
    }

    public RigidBody getRigidBody() {
        return _rigidBody;
    }

    public static void resetStats() {
        _statChunkMeshEmpty = 0;
        _statChunkNotReady = 0;
        _statRenderedTriangles = 0;
    }
}
