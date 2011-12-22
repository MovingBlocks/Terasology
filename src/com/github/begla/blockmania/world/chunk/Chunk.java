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
package com.github.begla.blockmania.world.chunk;

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.datastructures.AABB;
import com.github.begla.blockmania.datastructures.BlockmaniaArray;
import com.github.begla.blockmania.datastructures.BlockmaniaSmartArray;
import com.github.begla.blockmania.generators.ChunkGenerator;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.utilities.Helper;
import com.github.begla.blockmania.utilities.MathHelper;
import com.github.begla.blockmania.world.entity.StaticEntity;
import com.github.begla.blockmania.world.main.LocalWorldProvider;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import javax.vecmath.Vector3f;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

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

    /* CONSTANT VALUES */
    public static final int CHUNK_DIMENSION_X = 16;
    public static final int CHUNK_DIMENSION_Y = 256;
    public static final int CHUNK_DIMENSION_Z = 16;
    public static final int VERTICAL_SEGMENTS = (Integer) ConfigurationManager.getInstance().getConfig().get("Graphics.verticalChunkMeshSegments");
    private static final Vector3f[] LIGHT_DIRECTIONS = {new Vector3f(1, 0, 0), new Vector3f(-1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, -1, 0), new Vector3f(0, 0, 1), new Vector3f(0, 0, -1)};

    protected FastRandom _random;
    /* ------ */
    protected boolean _dirty, _lightDirty, _fresh;
    /* ------ */
    protected LocalWorldProvider _parent;
    /* ------ */
    protected final BlockmaniaArray _blocks;
    protected final BlockmaniaSmartArray _sunlight, _light, _states;
    /* ------ */
    private ChunkMesh _activeMeshes[];
    private ChunkMesh _newMeshes[];
    /* ------ */
    private final ChunkMeshGenerator _meshGenerator;
    /* ------ */
    private boolean[] _occlusionCulled = new boolean[VERTICAL_SEGMENTS];
    private boolean[] _subMeshCulled = new boolean[VERTICAL_SEGMENTS];
    private final int[] _queries = new int[VERTICAL_SEGMENTS];
    /* ------ */
    private boolean _disposed = false;
    /* ----- */
    private AABB _aabb = null;
    private AABB[] _subChunkAABB = null;

    public enum LIGHT_TYPE {
        BLOCK,
        SUN
    }

    public static int getChunkIdForPosition(Vector3f position) {
        return MathHelper.cantorize(MathHelper.mapToPositive((int) position.x), MathHelper.mapToPositive((int) position.z));
    }

    public Chunk() {
        _meshGenerator = new ChunkMeshGenerator(this);

        _blocks = new BlockmaniaArray(CHUNK_DIMENSION_X, CHUNK_DIMENSION_Y, CHUNK_DIMENSION_Z);
        _sunlight = new BlockmaniaSmartArray(CHUNK_DIMENSION_X, CHUNK_DIMENSION_Y, CHUNK_DIMENSION_Z);
        _light = new BlockmaniaSmartArray(CHUNK_DIMENSION_X, CHUNK_DIMENSION_Y, CHUNK_DIMENSION_Z);
        _states = new BlockmaniaSmartArray(CHUNK_DIMENSION_X, CHUNK_DIMENSION_Y, CHUNK_DIMENSION_Z);

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
    public Chunk(LocalWorldProvider p, Vector3f position) {
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
            setFresh(false);

            for (ChunkGenerator gen : _parent.getGeneratorManager().getChunkGenerators()) {
                gen.generate(this);
            }

            generateSunlight();
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
        ArrayList<Vector3f> brightSpots = new ArrayList<Vector3f>();
        unspreadLight(x, y, z, lightValue, 0, type, brightSpots);

        for (Vector3f pos : brightSpots) {
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
    public void unspreadLight(int x, int y, int z, byte lightValue, int depth, LIGHT_TYPE type, ArrayList<Vector3f> brightSpots) {
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
                brightSpots.add(new Vector3f(blockPosX + (int) LIGHT_DIRECTIONS[i].x, y + (int) LIGHT_DIRECTIONS[i].y, blockPosZ + (int) LIGHT_DIRECTIONS[i].z));
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

        if (type == Chunk.LIGHT_TYPE.SUN)
            return 15;
        else
            return 0;
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
        BlockmaniaSmartArray lSource;
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
        Vector3f result = new Vector3f(getPosition().x * CHUNK_DIMENSION_X, 0, getPosition().z * CHUNK_DIMENSION_Z);
        Vector3f referencePoint = new Vector3f(_parent.getRenderingReferencePoint().x, 0, _parent.getRenderingReferencePoint().z);
        result.sub(referencePoint);

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
            Vector3f dimensions = new Vector3f(CHUNK_DIMENSION_X / 2, CHUNK_DIMENSION_Y / 2, CHUNK_DIMENSION_Z / 2);
            Vector3f position = new Vector3f(getChunkWorldPosX() + dimensions.x - 0.5f, dimensions.y - 0.5f, getChunkWorldPosZ() + dimensions.z - 0.5f);
            _aabb = new AABB(position, dimensions);
        }

        return _aabb;
    }

    public AABB getSubMeshAABB(int subMesh) {
        if (_subChunkAABB == null) {
            _subChunkAABB = new AABB[VERTICAL_SEGMENTS];

            int heightHalf = CHUNK_DIMENSION_Y / VERTICAL_SEGMENTS / 2;

            for (int i = 0; i < _subChunkAABB.length; i++) {
                Vector3f dimensions = new Vector3f(8, CHUNK_DIMENSION_Y / VERTICAL_SEGMENTS / 2, 8);
                Vector3f position = new Vector3f(getChunkWorldPosX() + dimensions.x - 0.5f, (i * heightHalf * 2) + dimensions.y - 0.5f, getChunkWorldPosZ() + dimensions.z - 0.5f);
                _subChunkAABB[i] = new AABB(position, dimensions);
            }
        }

        return _subChunkAABB[subMesh];
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

        // The flags are stored within the first byte of the file...
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

        for (int i = 0; i < _blocks.size(); i++)
            _blocks.setRawByte(i, in.readByte());

        for (int i = 0; i < _sunlight.sizePacked(); i++)
            _sunlight.setRawByte(i, in.readByte());

        for (int i = 0; i < _light.sizePacked(); i++)
            _light.setRawByte(i, in.readByte());

        for (int i = 0; i < _states.sizePacked(); i++)
            _states.setRawByte(i, in.readByte());

        setFresh(false);
        setDirty(true);
    }

    /**
     * Generates the terrain mesh (creates the internal vertex arrays).
     */
    public void generateMeshes() {
        if (isFresh() || isLightDirty() || !isDirty())
            return;

        setDirty(false);
        ChunkMesh[] newMeshes = new ChunkMesh[VERTICAL_SEGMENTS];

        for (int i = 0; i < VERTICAL_SEGMENTS; i++) {
            newMeshes[i] = _meshGenerator.generateMesh(CHUNK_DIMENSION_Y / VERTICAL_SEGMENTS, i * (CHUNK_DIMENSION_Y / VERTICAL_SEGMENTS));
        }

        setNewMesh(newMeshes);
    }

    /**
     * Draws the opaque or translucent elements of a chunk.
     *
     * @param type The type of vertices to render
     */
    public boolean render(ChunkMesh.RENDER_TYPE type, int subMesh) {
        if (isReadyForRendering()) {
            if (!isSubMeshEmpty(subMesh)) {
                _activeMeshes[subMesh].render(type);
                return true;
            }
        }

        return false;
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

    public void executeOcclusionQuery() {
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);

        if (_activeMeshes != null) {
            for (int j = 0; j < VERTICAL_SEGMENTS; j++) {
                if (!isSubMeshEmpty(j)) {
                    if (_queries[j] == 0) {
                        _queries[j] = GL15.glGenQueries();

                        GL15.glBeginQuery(GL15.GL_SAMPLES_PASSED, _queries[j]);
                        getSubMeshAABB(j).renderSolid();
                        GL15.glEndQuery(GL15.GL_SAMPLES_PASSED);
                    }
                }
            }
        }

        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
    }

    public void applyOcclusionQueries() {
        for (int i = 0; i < VERTICAL_SEGMENTS; i++) {
            if (_queries[i] != 0) {
                int result = GL15.glGetQueryObjectui(_queries[i], GL15.GL_QUERY_RESULT_AVAILABLE);

                if (result != 0) {

                    result = GL15.glGetQueryObjectui(_queries[i], GL15.GL_QUERY_RESULT);

                    _occlusionCulled[i] = result <= 0;

                    GL15.glDeleteQueries(_queries[i]);
                    _queries[i] = 0;
                }
            }
        }
    }

    private void setNewMesh(ChunkMesh[] newMesh) {
        synchronized (this) {
            if (_disposed)
                return;

            ChunkMesh[] oldNewMesh = _newMeshes;
            _newMeshes = newMesh;

            if (oldNewMesh != null) {
                for (int i = 0; i < oldNewMesh.length; i++)
                    oldNewMesh[i].dispose();
            }
        }
    }

    private boolean swapActiveMesh() {
        synchronized (this) {
            if (_disposed)
                return false;

            if (_newMeshes != null) {
                if (_newMeshes[0].isDisposed() || !_newMeshes[0].isGenerated())
                    return false;

                ChunkMesh[] newMesh = _newMeshes;
                _newMeshes = null;

                ChunkMesh[] oldActiveMesh = _activeMeshes;
                _activeMeshes = newMesh;

                if (oldActiveMesh != null)
                    for (int i = 0; i < oldActiveMesh.length; i++)
                        oldActiveMesh[i].dispose();

                return true;
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

    public synchronized boolean isDirty() {
        return _dirty;
    }

    public synchronized boolean isFresh() {
        return _fresh;
    }

    public synchronized boolean isLightDirty() {
        return _lightDirty;
    }

    public synchronized void setFresh(boolean fresh) {
        _fresh = fresh;
    }

    public synchronized void setDirty(boolean dirty) {
        _dirty = dirty;
    }

    public synchronized void setLightDirty(boolean lightDirty) {
        _lightDirty = lightDirty;
    }

    public void setPosition(Vector3f position) {
        super.setPosition(position);
    }

    public void setParent(LocalWorldProvider parent) {
        _parent = parent;
    }

    public static String getChunkSavePathForPosition(Vector3f position) {
        String x36 = Integer.toString((int) position.x, 36);
        String z36 = Integer.toString((int) position.z, 36);

        return x36 + "/" + z36;
    }

    public static String getChunkFileNameForPosition(Vector3f position) {
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


    /**
     * Disposes this chunk. Can NOT be undone.
     */
    public void dispose() {
        synchronized (this) {
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
        }
    }

    public boolean isReadyForRendering() {
        return _activeMeshes != null;
    }

    public ChunkMesh getActiveSubMesh(int subMesh) {
        return _activeMeshes[subMesh];
    }

    public boolean isSubMeshOcclusionCulled(int subMesh) {
        return _occlusionCulled[subMesh];
    }

    public boolean isSubMeshCulled(int subMesh) {
        return _subMeshCulled[subMesh];
    }

    public void setSubMeshCulled(int subMesh, boolean culled) {
        _subMeshCulled[subMesh] = culled;
    }

    public void resetOcclusionCulled() {
        _occlusionCulled = new boolean[VERTICAL_SEGMENTS];
    }

    public void resetSubMeshCulled() {
        _subMeshCulled = new boolean[VERTICAL_SEGMENTS];
    }

    public boolean isSubMeshEmpty(int subMesh) {
        return _activeMeshes[subMesh].isEmpty();
    }

    public void renderAABBs(boolean solid) {
        if (isReadyForRendering()) {
            for (int i = 0; i < VERTICAL_SEGMENTS; i++) {
                if (!isSubMeshEmpty(i)) {
                    if (!solid)
                        getSubMeshAABB(i).render(2f);
                    else
                        getSubMeshAABB(i).renderSolid();
                }
            }
        }
    }

    public int triangleCount() {
        int result = 0;

        for (int i = 0; i < VERTICAL_SEGMENTS; i++) {
            result += _activeMeshes[i].triangleCount();
        }

        return result;
    }
}