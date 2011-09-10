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
package com.github.begla.blockmania.world;

import com.github.begla.blockmania.Configuration;
import com.github.begla.blockmania.Game;
import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.datastructures.AABB;
import com.github.begla.blockmania.datastructures.BlockmaniaArray;
import com.github.begla.blockmania.datastructures.BlockmaniaSmartArray;
import com.github.begla.blockmania.generators.ChunkGenerator;
import com.github.begla.blockmania.rendering.VectorPool;
import com.github.begla.blockmania.utilities.Helper;
import com.github.begla.blockmania.utilities.MathHelper;
import javolution.util.FastList;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;

/**
 * Chunks are the basic components of the world. Each chunk contains a fixed amount of blocks
 * determined its dimensions. Chunks are used to manage the world efficiently and
 * to reduce the batch count within the render loop.
 * <p/>
 * Chunks are tessellated on creation and saved to vertex arrays. From those display lists are generated
 * which are then used for the actual rendering process.
 * <p/>
 * The default size of one chunk is 16x128x16 (32768) blocks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Chunk extends RenderableObject implements Comparable<Chunk> {

    private static final
    Vector3f[] _lightDirections = {new Vector3f(1, 0, 0), new Vector3f(-1, 0, 0), VectorPool.getVector(0, 1, 0), new Vector3f(0, -1, 0), new Vector3f(0, 0, 1), new Vector3f(0, 0, -1)};
    /* ------ */
    private static int _statVertexArrayUpdateCount = 0;
    /* ------ */
    private boolean _dirty;
    private boolean _lightDirty;
    private boolean _fresh = true;
    private boolean _cached = false;
    /* ------ */
    private Integer _chunkId = -1;
    /* ------ */
    private ChunkMesh _activeMesh;
    private ChunkMesh _newMesh;
    /* ------ */
    private final World _parent;
    /* ------ */
    private final BlockmaniaArray _blocks;
    private final BlockmaniaSmartArray _sunlight;
    private final BlockmaniaSmartArray _light;
    /* ------ */
    private final FastList<ChunkGenerator> _generators = new FastList<ChunkGenerator>();
    /* ------ */
    private final ChunkMeshGenerator _meshGenerator;
    /* ------ */
    private AABB _aabb;

    public enum LIGHT_TYPE {
        BLOCK,
        SUN
    }

    /**
     * Init. the chunk with a parent world, an absolute position and a list
     * of generators. The generators are applied when the chunk is generated.
     *
     * @param p        The parent world
     * @param position The absolute position of the chunk within the world
     * @param g        A list of generators which will be used to generate this chunk
     */
    public Chunk(World p, Vector3f position, FastList<ChunkGenerator> g) {
        this._position = position;
        // Set the chunk ID
        _chunkId = Integer.valueOf(MathHelper.cantorize((int) _position.x, (int) _position.z));

        _parent = p;
        _blocks = new BlockmaniaArray((int) Configuration.CHUNK_DIMENSIONS.x, (int) Configuration.CHUNK_DIMENSIONS.y, (int) Configuration.CHUNK_DIMENSIONS.z);
        _sunlight = new BlockmaniaSmartArray((int) Configuration.CHUNK_DIMENSIONS.x, (int) Configuration.CHUNK_DIMENSIONS.y, (int) Configuration.CHUNK_DIMENSIONS.z);
        _light = new BlockmaniaSmartArray((int) Configuration.CHUNK_DIMENSIONS.x, (int) Configuration.CHUNK_DIMENSIONS.y, (int) Configuration.CHUNK_DIMENSIONS.z);

        _meshGenerator = new ChunkMeshGenerator(this);

        _generators.addAll(g);

        _lightDirty = true;
        _dirty = true;
    }

    @Override
    public void render() {
        render(true);
        render(false);
    }

    @Override
    public void update() {
        if (_newMesh != null) {
            // Do not update the mesh if one of the VISIBLE neighbors is dirty
            for (Chunk nc : loadOrCreateNeighbors())
                if (nc.isDirty() && nc.isChunkInFrustum())
                    return;

            if (_newMesh.isGenerated()) {
                ChunkMesh oldMesh = _activeMesh;

                if (oldMesh != null)
                    oldMesh.disposeMesh();

                _activeMesh = _newMesh;
                _newMesh = null;
            }
        }
    }

    /**
     * Draws the opaque or translucent elements of a chunk.
     *
     * @param translucent True if the translucent elements should be rendered
     */
    public void render(boolean translucent) {
        // Render the generated chunk mesh
        if (_activeMesh != null) {
            _activeMesh.render(translucent);
        }
    }

    /**
     * Tries to load a chunk from disk. If the chunk is not present,
     * it is created from scratch.
     *
     * @return True if a generation has been executed
     */
    public boolean generate() {
        if (_fresh) {
            // Apply all generators to this chunk
            long timeStart = System.currentTimeMillis();

            // Try to load the chunk from disk
            if (loadChunkFromFile()) {
                _fresh = false;
                Game.getInstance().getLogger().log(Level.FINEST, "Chunk ({1}) loaded from disk ({0}s).", new Object[]{(System.currentTimeMillis() - timeStart) / 1000d, this});
                return true;
            }

            for (FastList.Node<ChunkGenerator> n = _generators.head(), end = _generators.tail(); (n = n.getNext()) != end; ) {
                n.getValue().generate(this);
            }

            generateSunlight();
            _fresh = false;

            Game.getInstance().getLogger().log(Level.FINEST, "Chunk ({1}) generated ({0}s).", new Object[]{(System.currentTimeMillis() - timeStart) / 1000d, this});
            return true;
        }
        return false;
    }

    /**
     * Updates the light of this chunk.
     */
    public void updateLight() {
        if (!_fresh) { // Do NOT update fresh chunks
            for (int x = 0; x < (int) Configuration.CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < (int) Configuration.CHUNK_DIMENSIONS.z; z++) {
                    for (int y = 0; y < (int) Configuration.CHUNK_DIMENSIONS.y; y++) {
                        byte lightValue = getLight(x, y, z, LIGHT_TYPE.SUN);

                        // Spread the sunlight in translucent blocks with a light value greater than zero.
                        if (lightValue > 0 && Block.getBlockForType(getBlock(x, y, z)).isBlockTypeTranslucent()) {
                            spreadLight(x, y, z, lightValue, LIGHT_TYPE.SUN);
                        }
                    }
                }
            }
            setLightDirty(false);
        }
    }

    /**
     * Generates the initial sunlight.
     */
    private void generateSunlight() {
        if (_fresh) {
            for (int x = 0; x < (int) Configuration.CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < (int) Configuration.CHUNK_DIMENSIONS.z; z++) {
                    refreshSunlightAtLocalPos(x, z, false, false);
                }
            }
        }
    }

    /**
     * Generates the terrain mesh (creates the internal vertex arrays).
     */
    public void generateMesh() {
        if (!_fresh) {
            _newMesh = _meshGenerator.generateMesh();

            setDirty(false);
            _statVertexArrayUpdateCount++;
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

        for (int y = (int) Configuration.CHUNK_DIMENSIONS.y - 1; y >= 0; y--) {
            Block b = Block.getBlockForType(_blocks.get(x, y, z));

            // Remember if this "column" is covered
            if ((!b.isBlockInvisible() && b.getBlockForm() != Block.BLOCK_FORM.BILLBOARD) && !covered) {
                covered = true;
            }

            byte oldValue = _sunlight.get(x, y, z);
            byte newValue;

            // If the column is not covered...
            if (!covered) {
                if (b.isBlockInvisible() || b.getBlockForm() == Block.BLOCK_FORM.BILLBOARD)
                    _sunlight.set(x, y, z, Configuration.MAX_LIGHT);
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
        int blockPosY = getBlockWorldPosY(y);
        int blockPosZ = getBlockWorldPosZ(z);

        byte bType = getBlock(x, y, z);

        // If a block was just placed, remove the light value at this point
        if (!Block.getBlockForType(bType).isBlockTypeTranslucent()) {
            setLight(x, y, z, (byte) 0, type);
        } else {
            // If the block was removed: Find the brightest neighbor and
            // set the current light value to this value - 1
            byte val = getParent().getLight(blockPosX, blockPosY, blockPosZ, type);
            byte val1 = getParent().getLight(blockPosX + 1, blockPosY, blockPosZ, type);
            byte val2 = getParent().getLight(blockPosX - 1, blockPosY, blockPosZ, type);
            byte val3 = getParent().getLight(blockPosX, blockPosY, blockPosZ + 1, type);
            byte val4 = getParent().getLight(blockPosX, blockPosY, blockPosZ - 1, type);
            byte val5 = getParent().getLight(blockPosX, blockPosY + 1, blockPosZ, type);
            byte val6 = getParent().getLight(blockPosX, blockPosY - 1, blockPosZ, type);

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
        FastList<Vector3f> brightSpots = new FastList<Vector3f>();
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
    public void unspreadLight(int x, int y, int z, byte lightValue, int depth, LIGHT_TYPE type, FastList<Vector3f> brightSpots) {
        int blockPosX = getBlockWorldPosX(x);
        int blockPosY = getBlockWorldPosY(y);
        int blockPosZ = getBlockWorldPosZ(z);

        // Remove the light at this point
        getParent().setLight(blockPosX, blockPosY, blockPosZ, (byte) 0x0, type);

        for (int i = 0; i < 6; i++) {

            byte neighborValue = getParent().getLight(blockPosX + (int) _lightDirections[i].x, blockPosY + (int) _lightDirections[i].y, blockPosZ + (int) _lightDirections[i].z, type);
            byte neighborType = getParent().getBlock(blockPosX + (int) _lightDirections[i].x, blockPosY + (int) _lightDirections[i].y, blockPosZ + (int) _lightDirections[i].z);

            if (neighborValue < lightValue && neighborValue > 0 && Block.getBlockForType(neighborType).isBlockTypeTranslucent()) {
                getParent().unspreadLight(blockPosX + (int) _lightDirections[i].x, blockPosY + (int) _lightDirections[i].y, blockPosZ + (int) _lightDirections[i].z, (byte) (lightValue - 1), depth + 1, type, brightSpots);
            } else if (neighborValue >= lightValue) {
                brightSpots.add(VectorPool.getVector(blockPosX + (int) _lightDirections[i].x, blockPosY + (int) _lightDirections[i].y, blockPosZ + (int) _lightDirections[i].z));
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
        if (depth > lightValue) {
            return;
        }

        int blockPosX = getBlockWorldPosX(x);
        int blockPosY = getBlockWorldPosY(y);
        int blockPosZ = getBlockWorldPosZ(z);

        byte newLightValue;
        newLightValue = (byte) (lightValue - depth);

        getParent().setLight(blockPosX, blockPosY, blockPosZ, newLightValue, type);

        for (int i = 0; i < 6; i++) {
            byte neighborValue = getParent().getLight(blockPosX + (int) _lightDirections[i].x, blockPosY + (int) _lightDirections[i].y, blockPosZ + (int) _lightDirections[i].z, type);
            byte neighborType = getParent().getBlock(blockPosX + (int) _lightDirections[i].x, blockPosY + (int) _lightDirections[i].y, blockPosZ + (int) _lightDirections[i].z);

            if (neighborValue < newLightValue - 1 && Block.getBlockForType(neighborType).isBlockTypeTranslucent()) {
                getParent().spreadLight(blockPosX + (int) _lightDirections[i].x, blockPosY + (int) _lightDirections[i].y, blockPosZ + (int) _lightDirections[i].z, lightValue, depth + 1, type);
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
        if (isFresh()) {
            // Sunlight should not be changed within fresh blocks
            return;
        }

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
     * Calculates the distance of the chunk to the player.
     *
     * @return The distance of the chunk to the player
     */
    public double distanceToPlayer() {
        return Math.sqrt(Math.pow(getParent().getPlayer().getPosition().x - getChunkWorldPosX(), 2) + Math.pow(getParent().getPlayer().getPosition().z - getChunkWorldPosZ(), 2));
    }

    /**
     * Returns the neighbor chunks of this chunk.
     *
     * @return The adjacent chunks
     */
    public Chunk[] loadOrCreateNeighbors() {
        Chunk[] chunks = new Chunk[8];
        chunks[0] = getParent().getChunkCache().loadOrCreateChunk((int) _position.x + 1, (int) _position.z);
        chunks[1] = getParent().getChunkCache().loadOrCreateChunk((int) _position.x - 1, (int) _position.z);
        chunks[2] = getParent().getChunkCache().loadOrCreateChunk((int) _position.x, (int) _position.z + 1);
        chunks[3] = getParent().getChunkCache().loadOrCreateChunk((int) _position.x, (int) _position.z - 1);
        chunks[4] = getParent().getChunkCache().loadOrCreateChunk((int) _position.x + 1, (int) _position.z + 1);
        chunks[5] = getParent().getChunkCache().loadOrCreateChunk((int) _position.x - 1, (int) _position.z - 1);
        chunks[6] = getParent().getChunkCache().loadOrCreateChunk((int) _position.x - 1, (int) _position.z + 1);
        chunks[7] = getParent().getChunkCache().loadOrCreateChunk((int) _position.x + 1, (int) _position.z - 1);
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

        if (x == Configuration.CHUNK_DIMENSIONS.x - 1 && neighbors[0] != null) {
            neighbors[0].setDirty(true);
        }

        if (z == 0 && neighbors[3] != null) {
            neighbors[3].setDirty(true);
        }

        if (z == Configuration.CHUNK_DIMENSIONS.z - 1 && neighbors[2] != null) {
            neighbors[2].setDirty(true);
        }

        if (x == Configuration.CHUNK_DIMENSIONS.x - 1 && z == 0 && neighbors[7] != null) {
            neighbors[7].setDirty(true);
        }

        if (x == 0 && z == Configuration.CHUNK_DIMENSIONS.z - 1 && neighbors[6] != null) {
            neighbors[6].setDirty(true);
        }

        if (x == 0 && z == 0 && neighbors[5] != null) {
            neighbors[5].setDirty(true);
        }

        if (x == Configuration.CHUNK_DIMENSIONS.x - 1 && z == Configuration.CHUNK_DIMENSIONS.z - 1 && neighbors[4] != null) {
            neighbors[4].setDirty(true);
        }
    }

    /**
     * Saves this chunk to disk.
     *
     * @return True if the chunk was successfully written to the disk
     */
    public boolean writeChunkToDisk() {
        // Don't save fresh chunks
        if (_fresh) {
            return false;
        }

        if (Game.getInstance().isSandboxed()) {
            return false;
        }
        // Generate the save directory if needed
        File dir = new File(_parent.getWorldSavePath());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Game.getInstance().getLogger().log(Level.SEVERE, "Could not create save directory.");
                return false;
            }
        }

        ByteBuffer output = BufferUtils.createByteBuffer(_blocks.getSize() + _sunlight.getPackedSize() + _light.getPackedSize() + 1);
        File f = new File(String.format("%s/%d.bc", getParent().getWorldSavePath(), getChunkId()));

        // Save flags...
        byte flags = 0x0;
        if (_lightDirty) {
            flags = Helper.setFlag(flags, (short) 0);
        }

        // The flags are stored within the first byte of the file...
        output.put(flags);


        for (int i = 0; i < _blocks.getSize(); i++)
            output.put(_blocks.getRawByte(i));

        for (int i = 0; i < _sunlight.getPackedSize(); i++)
            output.put(_sunlight.getRawByte(i));

        for (int i = 0; i < _light.getPackedSize(); i++)
            output.put(_light.getRawByte(i));

        output.rewind();

        try {
            FileOutputStream oS = new FileOutputStream(f);
            FileChannel c = oS.getChannel();
            c.write(output);
            Game.getInstance().getLogger().log(Level.FINE, "Wrote chunk {0} to disk.", this);
            oS.close();
        } catch (FileNotFoundException ex) {
            Game.getInstance().getLogger().log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Game.getInstance().getLogger().log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    /**
     * Loads this chunk from the disk (if present).
     *
     * @return True if the chunk was successfully loaded
     */
    public boolean loadChunkFromFile() {
        if (Game.getInstance().isSandboxed()) {
            return false;
        }

        ByteBuffer input = BufferUtils.createByteBuffer(_blocks.getSize() + _sunlight.getPackedSize() + _light.getPackedSize() + 1);
        File f = new File(String.format("%s/%d.bc", getParent().getWorldSavePath(), getChunkId()));

        if (!f.exists()) {
            return false;
        }

        try {
            FileInputStream iS = new FileInputStream(f);
            FileChannel c = iS.getChannel();
            c.read(input);
            Game.getInstance().getLogger().log(Level.FINE, "Loaded chunk {0} from disk.", this);
            iS.close();
        } catch (FileNotFoundException ex) {
            Game.getInstance().getLogger().log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Game.getInstance().getLogger().log(Level.SEVERE, null, ex);
            return false;
        }

        input.rewind();

        // The first byte contains the flags...
        byte flags = input.get();
        // Parse the flags...
        _lightDirty = Helper.isFlagSet(flags, (short) 0);

        for (int i = 0; i < _blocks.getSize(); i++)
            _blocks.setRawByte(i, input.get());

        for (int i = 0; i < _sunlight.getPackedSize(); i++)
            _sunlight.setRawByte(i, input.get());

        for (int i = 0; i < _light.getPackedSize(); i++)
            _light.setRawByte(i, input.get());

        return true;
    }

    public void disposeChunk() {
        if (_activeMesh != null)
            _activeMesh.disposeMesh();
    }

    @Override
    public String toString() {
        return String.format("Chunk (%d) at %s.", getChunkId(), _position);
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

        if (getParent().getPlayer() != null) {
            double distance = distanceToPlayer();
            double distance2 = o.distanceToPlayer();

            if (distance == distance2)
                return 0;

            return distance2 > distance ? -1 : 1;
        }

        return getChunkId().compareTo(o.getChunkId());
    }

    public AABB getAABB() {
        if (_aabb == null) {
            Vector3f dimensions = VectorPool.getVector(Configuration.CHUNK_DIMENSIONS.x / 2, Configuration.CHUNK_DIMENSIONS.y / 2, Configuration.CHUNK_DIMENSIONS.z / 2);
            Vector3f position = VectorPool.getVector(getChunkWorldPosX() + dimensions.getX(), getChunkWorldPosY() + dimensions.getY(), getChunkWorldPosZ() + dimensions.getZ());
            _aabb = new AABB(position, dimensions);
        }
        return _aabb;
    }


    /**
     * Generates the display lists and swaps the old mesh with the current mesh.
     */
    public void generateVBOs() {
        if (_newMesh != null) {
            _newMesh.generateVBOs();
        }
    }

    /**
     * Returns the position of the chunk within the world.
     *
     * @return The world position
     */
    int getChunkWorldPosX() {
        return (int) _position.x * (int) Configuration.CHUNK_DIMENSIONS.x;
    }

    /**
     * Returns the position of the chunk within the world.
     *
     * @return The world  position
     */
    int getChunkWorldPosY() {
        return (int) _position.y * (int) Configuration.CHUNK_DIMENSIONS.y;
    }

    /**
     * Returns the position of the chunk within the world.
     *
     * @return Thew world position
     */
    int getChunkWorldPosZ() {
        return (int) _position.z * (int) Configuration.CHUNK_DIMENSIONS.z;
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
     * @param y The local position
     * @return The world position
     */
    public int getBlockWorldPosY(int y) {
        return y + getChunkWorldPosY();
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

    public World getParent() {
        return _parent;
    }

    public static int getVertexArrayUpdateCount() {
        return _statVertexArrayUpdateCount;
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

    void setDirty(boolean _dirty) {
        this._dirty = _dirty;
    }

    void setLightDirty(boolean _lightDirty) {
        this._lightDirty = _lightDirty;
    }

    public Integer getChunkId() {
        return _chunkId;
    }

    public void setCached(boolean b) {
        _cached = b;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public boolean isCached() {
        return _cached;
    }

    public boolean isChunkInFrustum() {
        return _parent.getPlayer().getViewFrustum().intersects(getAABB());
    }
}
