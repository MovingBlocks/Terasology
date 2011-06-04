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

import com.github.begla.blockmania.blocks.BlockLeaf;
import com.github.begla.blockmania.blocks.BlockGlass;
import com.github.begla.blockmania.blocks.BlockAir;
import java.util.ArrayList;
import com.github.begla.blockmania.Configuration;
import com.github.begla.blockmania.Helper;
import com.github.begla.blockmania.RenderableObject;
import javolution.util.FastList;
import java.nio.channels.FileChannel;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import com.github.begla.blockmania.generators.ChunkGenerator;
import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.utilities.VectorPool;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.list.TLinkable;
import gnu.trove.list.array.TFloatArrayList;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.newdawn.slick.util.ResourceLoader;
import org.lwjgl.util.vector.Vector4f;
import java.nio.FloatBuffer;
import org.lwjgl.util.vector.Vector3f;
import java.io.IOException;
import java.util.logging.Level;
import org.newdawn.slick.opengl.Texture;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.TextureLoader;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import static org.lwjgl.opengl.GL11.*;

/**
 * Chunks are the basic components of the world. Each chunk contains a fixed amount of blocks
 * determined its dimensions. Chunks are used to manage the world efficiently and
 * to reduce the batch count within the render loop.
 *
 * Chunks are tessellated on creation and saved to vertex arrays. From those display lists are generated
 * which are then used for the actual rendering process.
 *
 * The default size of one chunk is 16x128x16 (32768) blocks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Chunk extends RenderableObject implements Comparable<Chunk>, TLinkable<Chunk> {

    private Chunk _next, _prev;
    private boolean _dirty;
    private boolean _lightDirty;
    private static int _statVertexArrayUpdateCount = 0;
    /* ------ */
    private boolean _fresh = true;
    /* ------ */
    private int _chunkID = -1;
    private static Texture _textureMap;
    /* ------ */
    private TFloatArrayList _quadsTranslucent;
    private TFloatArrayList _texTranslucent;
    private TFloatArrayList _colorTranslucent;
    private TFloatArrayList _quadsOpaque;
    private TFloatArrayList _texOpaque;
    private TFloatArrayList _colorOpaque;
    private TFloatArrayList _quadsBillboard;
    private TFloatArrayList _texBillboard;
    private TFloatArrayList _colorBillboard;
    /* ------ */
    private World _parent = null;

    /* ------ */
    private byte[][][] _blocks;
    private byte[][][] _sunlight;
    private byte[][][] _light;
    /* ------ */
    private int _displayListOpaque = -1;
    private int _displayListTranslucent = -1;
    private int _displayListBillboard = -1;
    /* ------ */
    private FastList<ChunkGenerator> _generators = new FastList<ChunkGenerator>();

    /**
     * 
     */
    public enum SIDE {

        /**
         * 
         */
        LEFT,
        /**
         * 
         */
        RIGHT,
        /**
         * 
         */
        TOP,
        /**
         * 
         */
        BOTTOM,
        /**
         * 
         */
        FRONT,
        /**
         * 
         */
        BACK;
    }

    /**
     * 
     */
    public enum LIGHT_TYPE {

        /**
         * 
         */
        NONE,
        /**
         * 
         */
        BLOCK,
        /**
         * 
         */
        SUN
    }

    /**
     * Init. the textures used within chunks.
     */
    public static void init() {
        try {
            Helper.LOGGER.log(Level.FINE, "Loading chunk textures...");
            _textureMap = TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/images/terrain.png").openStream(), GL_NEAREST);
            Helper.LOGGER.log(Level.FINE, "Finished loading chunk textures!");
        } catch (IOException ex) {
            Helper.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Init. the chunk with a parent world, an absolute position and a list
     * of generators. The generators are applied when the chunk is generated.
     *
     * @param p The parent world
     * @param position The absolute position of the chunk within the world
     * @param g A list of generators which should be applied to this chunk
     */
    public Chunk(World p, Vector3f position, FastList<ChunkGenerator> g) {
        this._position = position;
        // Set the chunk ID
        _chunkID = Helper.getInstance().cantorize((int) _position.x, (int) _position.z);

        _parent = p;
        _blocks = new byte[(int) Configuration.CHUNK_DIMENSIONS.x][(int) Configuration.CHUNK_DIMENSIONS.y][(int) Configuration.CHUNK_DIMENSIONS.z];
        _sunlight = new byte[(int) Configuration.CHUNK_DIMENSIONS.x][(int) Configuration.CHUNK_DIMENSIONS.y][(int) Configuration.CHUNK_DIMENSIONS.z];
        _light = new byte[(int) Configuration.CHUNK_DIMENSIONS.x][(int) Configuration.CHUNK_DIMENSIONS.y][(int) Configuration.CHUNK_DIMENSIONS.z];

        _generators.addAll(g);

        _lightDirty = true;
        _dirty = true;
    }

    /**
     * Saves the chunk to disk and removes the display lists.
     */
    public void dispose() {
        writeChunkToDisk();
    }

    /**
     * Draws the opaque or translucent elements of a chunk.
     * 
     * @param translucent True if the translucent elements should be rendered
     */
    public void render(boolean translucent) {

        /*
         * Draws the outline of each chunk.
         */
        if (Configuration.getSettingBoolean("SHOW_CHUNK_OUTLINES")) {
            glLineWidth(2.0f);
            glColor3f(255.0f, 255.0f, 255.0f);

            glPushMatrix();
            glTranslatef(_position.x * (int) Configuration.CHUNK_DIMENSIONS.x, _position.y * (int) Configuration.CHUNK_DIMENSIONS.y, _position.z * (int) Configuration.CHUNK_DIMENSIONS.z);

            glBegin(GL_LINE_LOOP);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(Configuration.CHUNK_DIMENSIONS.x, 0.0f, 0.0f);
            glVertex3f(Configuration.CHUNK_DIMENSIONS.x, Configuration.CHUNK_DIMENSIONS.y, 0.0f);
            glVertex3f(0.0f, Configuration.CHUNK_DIMENSIONS.y, 0.0f);
            glEnd();

            glBegin(GL_LINE_LOOP);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(0.0f, 0.0f, Configuration.CHUNK_DIMENSIONS.z);
            glVertex3f(0.0f, Configuration.CHUNK_DIMENSIONS.y, Configuration.CHUNK_DIMENSIONS.z);
            glVertex3f(0.0f, Configuration.CHUNK_DIMENSIONS.y, 0.0f);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glEnd();

            glBegin(GL_LINE_LOOP);
            glVertex3f(0.0f, 0.0f, Configuration.CHUNK_DIMENSIONS.z);
            glVertex3f(Configuration.CHUNK_DIMENSIONS.x, 0.0f, Configuration.CHUNK_DIMENSIONS.z);
            glVertex3f(Configuration.CHUNK_DIMENSIONS.x, Configuration.CHUNK_DIMENSIONS.y, Configuration.CHUNK_DIMENSIONS.z);
            glVertex3f(0.0f, Configuration.CHUNK_DIMENSIONS.y, Configuration.CHUNK_DIMENSIONS.z);
            glVertex3f(0.0f, 0.0f, Configuration.CHUNK_DIMENSIONS.z);
            glEnd();

            glBegin(GL_LINE_LOOP);
            glVertex3f(Configuration.CHUNK_DIMENSIONS.x, 0.0f, 0.0f);
            glVertex3f(Configuration.CHUNK_DIMENSIONS.x, 0.0f, Configuration.CHUNK_DIMENSIONS.z);
            glVertex3f(Configuration.CHUNK_DIMENSIONS.x, Configuration.CHUNK_DIMENSIONS.y, Configuration.CHUNK_DIMENSIONS.z);
            glVertex3f(Configuration.CHUNK_DIMENSIONS.x, Configuration.CHUNK_DIMENSIONS.y, 0.0f);
            glVertex3f(Configuration.CHUNK_DIMENSIONS.x, 0.0f, 0.0f);
            glEnd();
            glPopMatrix();
        }

        _textureMap.bind();
        glEnable(GL_TEXTURE_2D);
        if (!translucent) {
            glCallList(_displayListOpaque);
        } else {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_ALPHA_TEST);
            glAlphaFunc(GL_GREATER, 0.1f);

            glCallList(_displayListTranslucent);

            glDisable(GL_CULL_FACE);
            glCallList(_displayListBillboard);
            glEnable(GL_CULL_FACE);

            glDisable(GL_BLEND);
            glDisable(GL_ALPHA_TEST);
        }
        glDisable(GL_TEXTURE_2D);
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
                Helper.LOGGER.log(Level.FINEST, "Chunk ({1}) loaded from disk ({0}s).", new Object[]{(System.currentTimeMillis() - timeStart) / 1000d, this});
                return true;
            }

            for (FastList.Node<ChunkGenerator> n = _generators.head(), end = _generators.tail(); (n = n.getNext()) != end;) {
                n.getValue().generate(this);
            }

            updateSunlight();
            _fresh = false;

            Helper.LOGGER.log(Level.FINEST, "Chunk ({1}) generated ({0}s).", new Object[]{(System.currentTimeMillis() - timeStart) / 1000d, this});
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
     * Updates the sunlight.
     */
    private void updateSunlight() {
        if (_fresh) {
            for (int x = 0; x < (int) Configuration.CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < (int) Configuration.CHUNK_DIMENSIONS.z; z++) {
                    refreshSunlightAtLocalPos(x, z, false, false);
                }
            }
        }
    }

    /**
     * Generates the vertex-, texture- and color-arrays.
     */
    public synchronized void generateVertexArrays() {
        _quadsTranslucent = new TFloatArrayList();
        _texTranslucent = new TFloatArrayList();
        _colorTranslucent = new TFloatArrayList();
        _quadsOpaque = new TFloatArrayList();
        _texOpaque = new TFloatArrayList();
        _colorOpaque = new TFloatArrayList();
        _quadsBillboard = new TFloatArrayList();
        _texBillboard = new TFloatArrayList();
        _colorBillboard = new TFloatArrayList();

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                    generateBlockVertices(x, y, z);
                    generateBillboardVertices(x, y, z);
                }
            }
        }

        setDirty(false);
        _statVertexArrayUpdateCount++;
    }

    /**
     * Generates the billboard vertices for a given local block position.
     *
     * @param x Local block position on the x-axis
     * @param y Local block position on the y-axis
     * @param z Local block position on the z-axis
     */
    private void generateBillboardVertices(int x, int y, int z) {
        byte block = _blocks[x][y][z];

        // Ignore invisible blocks like air and normal block!
        if (Block.getBlockForType(block).isBlockInvisible() || !Block.getBlockForType(block).isBlockBillboard()) {
            return;
        }

        float offsetX = _position.x * Configuration.CHUNK_DIMENSIONS.x;
        float offsetY = _position.y * Configuration.CHUNK_DIMENSIONS.y;
        float offsetZ = _position.z * Configuration.CHUNK_DIMENSIONS.z;

        FastList<Float> quads = new FastList<Float>();
        FastList<Float> tex = new FastList<Float>();
        FastList<Float> color = new FastList<Float>();

        /*
         * First side of the billboard
         */
        Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.FRONT);
        float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).x;
        float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).y;
        float lightIntens = getRenderingLightValue(x, y, z);

        color.add(colorOffset.x * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.y * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.z * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.w);

        tex.add(texOffsetX);
        tex.add(texOffsetY + 0.0624f);
        quads.add(-0.5f + x + offsetX);
        quads.add(-0.5f + y + offsetY);
        quads.add(z + offsetZ);

        color.add(colorOffset.x * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.y * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.z * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY + 0.0624f);
        quads.add(0.5f + x + offsetX);
        quads.add(-0.5f + y + offsetY);
        quads.add(z + offsetZ);

        color.add(colorOffset.x * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.y * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.z * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY);

        quads.add(0.5f + x + offsetX);
        quads.add(0.5f + y + offsetY);
        quads.add(z + offsetZ);

        color.add(colorOffset.x * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.y * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.z * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.w);

        tex.add(texOffsetX);
        tex.add(texOffsetY);

        quads.add(-0.5f + x + offsetX);
        quads.add(0.5f + y + offsetY);
        quads.add(z + offsetZ);

        /*
         * Second side of the billboard
         */
        colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.BACK);
        texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).x;
        texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).y;

        color.add(colorOffset.x * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.y * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.z * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.w);

        tex.add(texOffsetX);
        tex.add(texOffsetY + 0.0624f);
        quads.add(x + offsetX);
        quads.add(-0.5f + y + offsetY);
        quads.add(-0.5f + z + offsetZ);

        color.add(colorOffset.x * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.y * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.z * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY + 0.0624f);
        quads.add(x + offsetX);
        quads.add(-0.5f + y + offsetY);
        quads.add(0.5f + z + offsetZ);

        color.add(colorOffset.x * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.y * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.z * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY);

        quads.add(x + offsetX);
        quads.add(0.5f + y + offsetY);
        quads.add(0.5f + z + offsetZ);

        color.add(colorOffset.x * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.y * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.z * lightIntens * Configuration.BRIGHTNESS_FACTOR);
        color.add(colorOffset.w);

        tex.add(texOffsetX);
        tex.add(texOffsetY);

        quads.add(x + offsetX);
        quads.add(0.5f + y + offsetY);
        quads.add(-0.5f + z + offsetZ);

        // All billboards are translucent
        _quadsBillboard.addAll(quads);
        _texBillboard.addAll(tex);
        _colorBillboard.addAll(color);
    }

    private void generateBlockVertices(int x, int y, int z) {
        byte block = _blocks[x][y][z];

        // Ignore invisible blocks and billboards like air and flowers!
        if (Block.getBlockForType(block).isBlockInvisible() || Block.getBlockForType(block).isBlockBillboard()) {
            return;
        }

        float offsetX = _position.x * Configuration.CHUNK_DIMENSIONS.x;
        float offsetY = _position.y * Configuration.CHUNK_DIMENSIONS.y;
        float offsetZ = _position.z * Configuration.CHUNK_DIMENSIONS.z;

        boolean drawFront, drawBack, drawLeft, drawRight, drawTop, drawBottom;
        byte blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y + 1), getBlockWorldPosZ(z));

        drawTop = isSideVisibleForBlockTypes(blockToCheck, block);

        // Draw the chunk if the height map produced a too large number
        if (y == Configuration.CHUNK_DIMENSIONS.y - 1) {
            drawTop = true;
        }

        FastList<Float> quads = new FastList<Float>();
        FastList<Float> tex = new FastList<Float>();
        FastList<Float> color = new FastList<Float>();

        if (drawTop) {
            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.TOP);
            float shadowIntens = Math.max(_parent.getRenderingLightValue(getBlockWorldPosX(x), getBlockWorldPosY(y + 1), getBlockWorldPosZ(z)) * simpleOcclusionAmount(x, y, z, 0, 1, 0), 0);

            // If this block is 'is touching' the nil area: ignore light
            if (y == Configuration.CHUNK_DIMENSIONS.y - 1) {
                shadowIntens = 1.0f;
            }

            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.TOP).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.TOP).y;

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(-0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);


        }

        blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1));
        drawFront = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawFront) {
            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.FRONT);
            float shadowIntens = Math.max(_parent.getRenderingLightValue(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1)) * simpleOcclusionAmount(x, y, z, 0, 0, -1), 0);

            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).y;

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(-0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);


        }

        blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1));
        drawBack = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawBack) {
            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.BACK);
            float shadowIntens = Math.max(_parent.getRenderingLightValue(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1)) * simpleOcclusionAmount(x, y, z, 0, 0, 1), 0);


            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).y;

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);

            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);

            quads.add(-0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);


        }

        blockToCheck = _parent.getBlock(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z));
        drawLeft = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawLeft) {
            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.LEFT);
            float shadowIntens = Math.max(_parent.getRenderingLightValue(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) * (simpleOcclusionAmount(x, y, z, -1, 0, 0) - Configuration.BLOCK_SIDE_DIMMING), 0);

            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.LEFT).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.LEFT).y;

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);

            quads.add(-0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);

            quads.add(-0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

        }

        blockToCheck = _parent.getBlock(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z));
        drawRight = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawRight) {
            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.RIGHT);
            float shadowIntens = Math.max(_parent.getRenderingLightValue(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) * (simpleOcclusionAmount(x, y, z, 1, 0, 0) - Configuration.BLOCK_SIDE_DIMMING), 0);

            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.RIGHT).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.RIGHT).y;

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);


        }

        blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y - 1), getBlockWorldPosZ(z));
        drawBottom = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawBottom) {
            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.BOTTOM);
            float shadowIntens = Math.max(_parent.getRenderingLightValue(getBlockWorldPosX(x), getBlockWorldPosY(y - 1), getBlockWorldPosZ(z)), 0);

            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BOTTOM).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BOTTOM).y;

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(-0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.y * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.z * shadowIntens * Configuration.BRIGHTNESS_FACTOR);
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);


        }

        if (!Block.getBlockForType(block).isBlockTypeTranslucent()) {
            _quadsOpaque.addAll(quads);
            _texOpaque.addAll(tex);
            _colorOpaque.addAll(color);
        } else {
            _quadsTranslucent.addAll(quads);
            _texTranslucent.addAll(tex);
            _colorTranslucent.addAll(color);
        }

    }

    /**
     * Generates the display lists from the pre calculated arrays.
     */
    public synchronized void generateDisplayLists() {
        // Check if the vertex arrays are present
        if (_quadsOpaque == null) {
            return;
        }

        /*
         * Create the display lists if necessary.
         */
        if (_displayListOpaque == -1) {
            _displayListOpaque = glGenLists(1);
        }

        if (_displayListTranslucent == -1) {
            _displayListTranslucent = glGenLists(1);
        }

        if (_displayListBillboard == -1) {
            _displayListBillboard = glGenLists(1);
        }

        FloatBuffer cb = null;
        FloatBuffer tb = null;
        FloatBuffer vb = null;

        vb = BufferUtils.createFloatBuffer(_quadsOpaque.size());

        for (TFloatIterator it = _quadsOpaque.iterator(); it.hasNext();) {
            vb.put(it.next());
        }

        tb = BufferUtils.createFloatBuffer(_texOpaque.size());

        for (TFloatIterator it = _texOpaque.iterator(); it.hasNext();) {
            tb.put(it.next());
        }

        cb = BufferUtils.createFloatBuffer(_colorOpaque.size());

        for (TFloatIterator it = _colorOpaque.iterator(); it.hasNext();) {
            cb.put(it.next());
        }

        vb.flip();
        tb.flip();
        cb.flip();

        glNewList(_displayListOpaque, GL_COMPILE);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glTexCoordPointer(2, 0, tb);
        glColorPointer(4, 0, cb);
        glVertexPointer(3, 0, vb);
        glDrawArrays(GL_QUADS, 0, _quadsOpaque.size() / 3);
        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
        glEndList();

        vb = BufferUtils.createFloatBuffer(_quadsTranslucent.size());

        for (TFloatIterator it = _quadsTranslucent.iterator(); it.hasNext();) {
            vb.put(it.next());
        }

        tb = BufferUtils.createFloatBuffer(_texTranslucent.size());

        for (TFloatIterator it = _texTranslucent.iterator(); it.hasNext();) {
            tb.put(it.next());
        }

        cb = BufferUtils.createFloatBuffer(_colorTranslucent.size());

        for (TFloatIterator it = _colorTranslucent.iterator(); it.hasNext();) {
            cb.put(it.next());
        }

        vb.flip();
        tb.flip();
        cb.flip();

        glNewList(_displayListTranslucent, GL_COMPILE);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glTexCoordPointer(2, 0, tb);
        glColorPointer(4, 0, cb);
        glVertexPointer(3, 0, vb);
        glDrawArrays(GL_QUADS, 0, _quadsTranslucent.size() / 3);
        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
        glEndList();

        vb = BufferUtils.createFloatBuffer(_quadsBillboard.size());

        for (TFloatIterator it = _quadsBillboard.iterator(); it.hasNext();) {
            vb.put(it.next());
        }


        tb = BufferUtils.createFloatBuffer(_texBillboard.size());

        for (TFloatIterator it = _texBillboard.iterator(); it.hasNext();) {
            tb.put(it.next());
        }


        cb = BufferUtils.createFloatBuffer(_colorBillboard.size());

        for (TFloatIterator it = _colorBillboard.iterator(); it.hasNext();) {
            cb.put(it.next());
        }

        vb.flip();
        tb.flip();
        cb.flip();

        glNewList(_displayListBillboard, GL_COMPILE);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glTexCoordPointer(2, 0, tb);
        glColorPointer(4, 0, cb);
        glVertexPointer(3, 0, vb);
        glDrawArrays(GL_QUADS, 0, _quadsBillboard.size() / 3);
        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
        glEndList();

        _quadsTranslucent = null;
        _texTranslucent = null;
        _colorTranslucent = null;
        _quadsOpaque = null;
        _texOpaque = null;
        _colorOpaque = null;
        _quadsBillboard = null;
        _texBillboard = null;
        _colorBillboard = null;
    }

    /**
     * Returns true if the block side is adjacent to a translucent block or an air
     * block.
     *
     * NOTE: Air and leafs have to be handled separately. Otherwise the water surface would not be displayed due to the tessellation process.
     */
    private boolean isSideVisibleForBlockTypes(byte blockToCheck, byte currentBlock) {
        Block bCheck = Block.getBlockForType(blockToCheck);
        Block cBlock = Block.getBlockForType(currentBlock);

        return bCheck.getClass() == BlockAir.class || bCheck.getClass() == BlockLeaf.class || bCheck.getClass() == BlockGlass.class || Block.getBlockForType(blockToCheck).isBlockBillboard() || (Block.getBlockForType(blockToCheck).isBlockTypeTranslucent() && !Block.getBlockForType(currentBlock).isBlockTypeTranslucent());
    }

    /**
     * Returns the position of the chunk within the world.
     * @return
     */
    public int getChunkWorldPosX() {
        return (int) _position.x * (int) Configuration.CHUNK_DIMENSIONS.x;
    }

    /**
     * Returns the position of the chunk within the world.
     * @return
     */
    public int getChunkWorldPosY() {
        return (int) _position.y * (int) Configuration.CHUNK_DIMENSIONS.y;
    }

    /**
     * Returns the position of the chunk within the world.
     * @return
     */
    public int getChunkWorldPosZ() {
        return (int) _position.z * (int) Configuration.CHUNK_DIMENSIONS.z;
    }

    /**
     * Returns the position of block within the world.
     * @param x
     * @return
     */
    public int getBlockWorldPosX(int x) {
        return x + getChunkWorldPosX();
    }

    /**
     * Returns the position of block within the world.
     * @param y 
     * @return
     */
    public int getBlockWorldPosY(int y) {
        return y + getChunkWorldPosY();
    }

    /**
     * Returns the position of block within the world.
     * @param z 
     * @return
     */
    public int getBlockWorldPosZ(int z) {
        return z + getChunkWorldPosZ();
    }

    /**
     * Calculates the sunlight at a given column within the chunk.
     * 
     * @param x Local block position on the x-axis
     * @param z Local block position on the z-axis
     * @param spreadLight  
     */
    public void refreshSunlightAtLocalPos(int x, int z, boolean spreadLight, boolean refreshSunlight) {
        boolean covered = false;

        for (int y = (int) Configuration.CHUNK_DIMENSIONS.y - 1; y >= 0; y--) {
            Block b = Block.getBlockForType(_blocks[x][y][z]);

            if ((b.getClass() != BlockAir.class && !b.isBlockBillboard())) {
                covered = true;
                continue;
            }

            if ((b.getClass() == BlockAir.class || b.isBlockBillboard()) && !covered) {
                byte oldValue = _sunlight[x][y][z];
                _sunlight[x][y][z] = Configuration.MAX_LIGHT;
                byte newValue = _sunlight[x][y][z];

                /*
                 * Spread sunlight if the new light value is more intense
                 * than the old value.
                 */
                if (spreadLight && oldValue < newValue) {
                    spreadLight(x, y, z, newValue, LIGHT_TYPE.SUN);
                }
            } else if ((b.getClass() == BlockAir.class || b.isBlockBillboard()) && covered) {
                _sunlight[x][y][z] = 0;

                if (refreshSunlight) {
                    refreshLightAtLocalPos(x, y, z, LIGHT_TYPE.SUN);
                }
            }
        }
    }

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @param type  
     */
    public void refreshLightAtLocalPos(int x, int y, int z, LIGHT_TYPE type) {
        int blockPosX = getBlockWorldPosX(x);
        int blockPosY = getBlockWorldPosY(y);
        int blockPosZ = getBlockWorldPosZ(z);

        byte bType = getBlock(x, y, z);

        // If a block was placed, remove the light value at this point
        if (!Block.getBlockForType(bType).isBlockTypeTranslucent()) {
            setLight(x, y, z, (byte) 0, type);
        } else {
            // If the block was removed: Find the brightest neighbor and
            // set the current block to this value - 1
            byte val = _parent.getLight(blockPosX, blockPosY, blockPosZ, type);
            byte val1 = _parent.getLight(blockPosX + 1, blockPosY, blockPosZ, type);
            byte val2 = _parent.getLight(blockPosX - 1, blockPosY, blockPosZ, type);
            byte val3 = _parent.getLight(blockPosX, blockPosY, blockPosZ + 1, type);
            byte val4 = _parent.getLight(blockPosX, blockPosY, blockPosZ - 1, type);
            byte val5 = _parent.getLight(blockPosX, blockPosY + 1, blockPosZ, type);
            byte val6 = _parent.getLight(blockPosX, blockPosY - 1, blockPosZ, type);

            byte max = (byte) (Math.max(Math.max(Math.max(val1, val2), Math.max(val3, val4)), Math.max(val5, val6)) - 1);

            if (max < 0) {
                max = 0;
            }

            // Do nothing if the current light value is brighter
            byte res = (byte) Math.max(max, val);

            setLight(x, y, z, res, type);
        }
    }

    /**
     * TODO: Implement "removal" of light
     * 
     * @param x
     * @param y
     * @param z 
     * @param oldLightValue
     * @param type  
     */
    public void unspreadLight(int x, int y, int z, byte oldLightValue, LIGHT_TYPE type) {
        unspreadLight(x, y, z, oldLightValue, 0, type);
    }

    /**
     * TODO: Implement "removal" of light
     * 
     * @param x
     * @param y
     * @param z 
     * @param depth 
     * @param oldLightValue
     * @param type  
     */
    public void unspreadLight(int x, int y, int z, byte oldLightValue, int depth, LIGHT_TYPE type) {
        throw new NotImplementedException();
    }

    /**
     * Recursive light calculation.
     * 
     * @param x
     * @param y
     * @param z
     * @param lightValue
     * @param type  
     */
    public void spreadLight(int x, int y, int z, byte lightValue, LIGHT_TYPE type) {
        spreadLight(x, y, z, lightValue, 0, type);
    }

    /**
     * Recursive light calculation.
     * 
     * @param x
     * @param y
     * @param z 
     * @param lightValue
     * @param depth
     * @param type  
     */
    public void spreadLight(int x, int y, int z, byte lightValue, int depth, LIGHT_TYPE type) {
        if (depth > lightValue) {
            return;
        }

        int blockPosX = getBlockWorldPosX(x);
        int blockPosY = getBlockWorldPosY(y);
        int blockPosZ = getBlockWorldPosZ(z);

        byte val1 = _parent.getLight(blockPosX + 1, blockPosY, blockPosZ, type);
        byte type1 = _parent.getBlock(blockPosX + 1, blockPosY, blockPosZ);
        byte val2 = _parent.getLight(blockPosX - 1, blockPosY, blockPosZ, type);
        byte type2 = _parent.getBlock(blockPosX - 1, blockPosY, blockPosZ);
        byte val3 = _parent.getLight(blockPosX, blockPosY, blockPosZ + 1, type);
        byte type3 = _parent.getBlock(blockPosX, blockPosY, blockPosZ + 1);
        byte val4 = _parent.getLight(blockPosX, blockPosY, blockPosZ - 1, type);
        byte type4 = _parent.getBlock(blockPosX, blockPosY, blockPosZ - 1);
        byte val5 = _parent.getLight(blockPosX, blockPosY + 1, blockPosZ, type);
        byte type5 = _parent.getBlock(blockPosX, blockPosY + 1, blockPosZ);
        byte val6 = _parent.getLight(blockPosX, blockPosY - 1, blockPosZ, type);
        byte type6 = _parent.getBlock(blockPosX, blockPosY - 1, blockPosZ);

        byte newLightValue = 0;
        newLightValue = (byte) (lightValue - depth);

        _parent.setLight(blockPosX, blockPosY, blockPosZ, newLightValue, type);

        if (lightValue <= 0) {
            return;
        }

        if (val1 < newLightValue - 1 && Block.getBlockForType(type1).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX + 1, blockPosY, blockPosZ, lightValue, depth + 1, type);
        }


        if (val2 < newLightValue - 1 && Block.getBlockForType(type2).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX - 1, blockPosY, blockPosZ, lightValue, depth + 1, type);
        }


        if (val3 < newLightValue - 1 && Block.getBlockForType(type3).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX, blockPosY, blockPosZ + 1, lightValue, depth + 1, type);
        }


        if (val4 < newLightValue - 1 && Block.getBlockForType(type4).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX, blockPosY, blockPosZ - 1, lightValue, depth + 1, type);
        }


        if (val5 < newLightValue - 1 && Block.getBlockForType(type5).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX, blockPosY + 1, blockPosZ, lightValue, depth + 1, type);
        }


        if (val6 < newLightValue - 1 && Block.getBlockForType(type6).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX, blockPosY - 1, blockPosZ, lightValue, depth + 1, type);
        }
    }

    /**
     * Returns the amount of blocks within this chunk.
     *
     * @return The amount of blocks
     */
    public int blockCount() {
        int counter = 0;

        for (int x = 0; x < (int) Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < (int) Configuration.CHUNK_DIMENSIONS.z; z++) {
                for (int y = 0; y < (int) Configuration.CHUNK_DIMENSIONS.y; y++) {
                    if (_blocks[x][y][z] > 0) {
                        counter++;
                    }
                }
            }
        }
        return counter;
    }

    /**
     * Calculates the distance of the chunk to the player.
     * 
     * @return The distance of the chunk to the player
     */
    public double distanceToPlayer() {
        return Math.sqrt(Math.pow(_parent.getPlayer().getPosition().x - getChunkWorldPosX(), 2) + Math.pow(_parent.getPlayer().getPosition().z - getChunkWorldPosZ(), 2));
    }

    /**
     * Returns the light intensity at a given local block position.
     *
     * @param x Local block position on the x-axis
     * @param y Local block position on the y-axis
     * @param z Local block position on the z-axis
     * @param type 
     * @return The light intensity
     */
    public byte getLight(int x, int y, int z, LIGHT_TYPE type) {
        if (Helper.getInstance().checkBounds3D(x, y, z, _sunlight)) {
            if (type == LIGHT_TYPE.SUN) {
                return _sunlight[x][y][z];
            } else {
                return _light[x][y][z];
            }
        }

        return -1;
    }

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public float getRenderingLightValue(int x, int y, int z) {
        if (Helper.getInstance().checkBounds3D(x, y, z, _sunlight)) {
            float sunlight = (float) Math.pow(0.8, 15 - _sunlight[x][y][z]) * _parent.getDaylight();
            float light = (float) Math.pow(0.8, 15 - _light[x][y][z]);
            if (sunlight > light) {
                return sunlight;
            } else {
                return light;
            }
        }

        return -1;
    }

    /**
     * Sets the light value at the given position.
     * 
     * @param x Local block position on the x-axis
     * @param y Local block position on the y-axis
     * @param z Local block position on the z-axis
     * @param intens 
     * @param type  
     */
    public void setLight(int x, int y, int z, byte intens, LIGHT_TYPE type) {
        if (isFresh()) {
            // Sunlight should not be changed within fresh blocks
            return;
        }

        byte[][][] lSource = null;
        if (type == LIGHT_TYPE.SUN) {
            lSource = _sunlight;
        } else if (type == LIGHT_TYPE.BLOCK) {
            lSource = _light;
        } else {
            return;
        }

        if (Helper.getInstance().checkBounds3D(x, y, z, _sunlight)) {
            Block b = Block.getBlockForType(getBlock(x, y, z));
            byte oldValue = lSource[x][y][z];
            lSource[x][y][z] = intens;

            if (oldValue != intens) {
                setDirty(true);
                // Mark the neighbors as dirty
                markNeighborsDirty(x, z);
            }
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
        if (Helper.getInstance().checkBounds3D(x, y, z, _blocks)) {
            return _blocks[x][y][z];
        } else {
            return -1;
        }
    }

    /**
     * Sets the block value at the given position.
     * 
     * @param x Local block position on the x-axis
     * @param y Local block position on the y-axis
     * @param z Local block position on the z-axis
     * @param type The block type
     */
    public void setBlock(int x, int y, int z, byte type) {
        if (Helper.getInstance().checkBounds3D(x, y, z, _blocks)) {
            byte oldValue = _blocks[x][y][z];
            _blocks[x][y][z] = type;

            Block b = Block.getBlockForType(type);

            // If an opaque block was set, remove the light from this field
            if (!b.isBlockTypeTranslucent()) {
                _sunlight[x][y][z] = 0;
            }

            if (oldValue != type) {
                // Update vertex arrays and light
                setDirty(true);
                // Mark the neighbors as dirty
                markNeighborsDirty(x, z);
            }
        }
    }

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @return 
     */
    public boolean canBlockSeeTheSky(int x, int y, int z) {
        while (y < Configuration.CHUNK_DIMENSIONS.y) {
            if (!Block.getBlockForType(getBlock(x, y, z)).isBlockTypeTranslucent()) {
                return false;
            }
            y++;
        }
        return true;
    }

    /**
     * Calculates a simple occlusion value based on the amount of blocks
     * surrounding the given block position.
     * 
     * @param x Local block position on the x-axis
     * @param y Local block position on the y-axis
     * @param z Local block position on the z-axis
     * @param dirX 
     * @param dirY 
     * @param dirZ 
     * @return Occlusion amount
     */
    public float simpleOcclusionAmount(int x, int y, int z, int dirX, int dirY, int dirZ) {
        float intens = 0f;

        ArrayList<Vector3f> positions = new ArrayList<Vector3f>();
        positions.add(VectorPool.getVector(x + dirX + 1, y + dirY, z + dirZ));
        positions.add(VectorPool.getVector(x + dirX - 1, y + dirY, z + dirZ));
        positions.add(VectorPool.getVector(x + dirX, y + dirY, z + dirZ + 1));
        positions.add(VectorPool.getVector(x + dirX, y + dirY, z + dirZ - 1));
//        positions.add(VectorPool.getVector(x + dirX + 1, y + dirY, z + dirZ + 1));
//        positions.add(VectorPool.getVector(x + dirX + 1, y + dirY, z + dirZ - 1));
//        positions.add(VectorPool.getVector(x + dirX - 1, y + dirY, z + dirZ - 1));
//        positions.add(VectorPool.getVector(x + dirX - 1, y + dirY, z + dirZ + 1));

        for (Vector3f p : positions) {
            if (Block.getBlockForType(_parent.getBlock(getBlockWorldPosX((int) p.x), getBlockWorldPosY((int) p.y), getBlockWorldPosZ((int) p.z))).isCastingShadows()) {
                intens += 1f / 15f;
            }

            VectorPool.putVector(p);
        }

        return 1f - (intens * Configuration.OCCLUSION_INTENS);
    }

    /**
     * Returns the neighbor chunks of this chunk.
     *
     * @return The adjacent chunks
     */
    public Chunk[] loadOrCreateNeighbors() {
        Chunk[] chunks = new Chunk[8];
        chunks[0] = _parent.getChunkCache().loadOrCreateChunk((int) _position.x + 1, (int) _position.z);
        chunks[1] = _parent.getChunkCache().loadOrCreateChunk((int) _position.x - 1, (int) _position.z);
        chunks[2] = _parent.getChunkCache().loadOrCreateChunk((int) _position.x, (int) _position.z + 1);
        chunks[3] = _parent.getChunkCache().loadOrCreateChunk((int) _position.x, (int) _position.z - 1);
        chunks[4] = _parent.getChunkCache().loadOrCreateChunk((int) _position.x + 1, (int) _position.z + 1);
        chunks[5] = _parent.getChunkCache().loadOrCreateChunk((int) _position.x - 1, (int) _position.z - 1);
        chunks[6] = _parent.getChunkCache().loadOrCreateChunk((int) _position.x - 1, (int) _position.z + 1);
        chunks[7] = _parent.getChunkCache().loadOrCreateChunk((int) _position.x + 1, (int) _position.z - 1);
        return chunks;
    }

    /**
     * Marks the neighbors of this chunks as dirty.
     */
    public void markNeighborsDirty() {
        Chunk[] neighbors = loadOrCreateNeighbors();

        if (neighbors[1] != null) {
            neighbors[1]._dirty = true;
        }
        if (neighbors[0] != null) {
            neighbors[0]._dirty = true;
        }
        if (neighbors[3] != null) {
            neighbors[3]._dirty = true;
        }
        if (neighbors[2] != null) {
            neighbors[2]._dirty = true;
        }
    }

    /**
     * Marks those neighbors of a chunk dirty, that are adjacent to
     * the given block coordinate.
     * 
     * @param x Local block position on the x-axis
     * @param z Local block position on the z-axis
     */
    public void markNeighborsDirty(int x, int z) {
        Chunk[] neighbors = loadOrCreateNeighbors();

        if (x == 0 && neighbors[1] != null) {
            neighbors[1]._dirty = true;
        }

        if (x == Configuration.CHUNK_DIMENSIONS.x - 1 && neighbors[0] != null) {
            neighbors[0]._dirty = true;
        }

        if (z == 0 && neighbors[3] != null) {
            neighbors[3]._dirty = true;
        }

        if (z == Configuration.CHUNK_DIMENSIONS.z - 1 && neighbors[2] != null) {
            neighbors[2]._dirty = true;
        }
    }

    /**
     * Returns the parent world.
     * 
     * @return
     */
    public World getParent() {
        return _parent;
    }

    /**
     * Returns the amount of executed vertex array updates.
     * 
     * @return The amount of updates
     */
    public static int getVertexArrayUpdateCount() {
        return _statVertexArrayUpdateCount;
    }

    /**
     * Chunks are comparable by the relative distance to the player.
     *
     * @param o The chunk to compare to
     * @return
     */
    @Override
    public int compareTo(Chunk o) {
        if (_parent.getPlayer() != null) {
            return new Double(distanceToPlayer()).compareTo(o.distanceToPlayer());
        }

        return new Integer(o.getChunkID()).compareTo(new Integer(getChunkID()));
    }

    /**
     * Returns some information about the chunk as a string.
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("Chunk (%d) at %s.", _chunkID, _position);
    }

    /**
     * Saves this chunk to disk.
     * 
     * TODO: Chunks use a lot of memory...
     * 
     * @return 
     */
    public boolean writeChunkToDisk() {
        // Don't save fresh chunks
        if (_fresh) {
            return false;
        }

        ByteBuffer output = BufferUtils.createByteBuffer((int) Configuration.CHUNK_DIMENSIONS.x * (int) Configuration.CHUNK_DIMENSIONS.y * (int) Configuration.CHUNK_DIMENSIONS.z * 3 + 1);
        File f = new File(String.format("%s/%d.bc", _parent.getWorldSavePath().toString(), Helper.getInstance().cantorize((int) _position.x, (int) _position.z)));

        // Save flags...
        byte flags = 0x0;
        if (_lightDirty) {
            flags = Helper.getInstance().setFlag(flags, (short) 0);
        }

        // The flags are stored within the first byte of the file...
        output.put(flags);

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                    output.put(_blocks[x][y][z]);
                    output.put(_sunlight[x][y][z]);
                    output.put(_light[x][y][z]);
                }
            }
        }

        output.rewind();

        try {
            FileOutputStream oS = new FileOutputStream(f);
            FileChannel c = oS.getChannel();
            c.write(output);
            Helper.LOGGER.log(Level.FINE, "Wrote chunk {0} to disk.", this);
            oS.close();
        } catch (FileNotFoundException ex) {
            Helper.LOGGER.log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Helper.LOGGER.log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    /**
     * Loads this chunk from the disk (if present).
     * 
     * @return 
     */
    public boolean loadChunkFromFile() {
        ByteBuffer input = BufferUtils.createByteBuffer((int) Configuration.CHUNK_DIMENSIONS.x * (int) Configuration.CHUNK_DIMENSIONS.y * (int) Configuration.CHUNK_DIMENSIONS.z * 3 + 1);
        File f = new File(String.format("%s/%d.bc", _parent.getWorldSavePath(), Helper.getInstance().cantorize((int) _position.x, (int) _position.z)));

        if (!f.exists()) {
            return false;
        }

        try {
            FileInputStream iS = new FileInputStream(f);
            FileChannel c = iS.getChannel();
            c.read(input);
            Helper.LOGGER.log(Level.FINE, "Loaded chunk {0} from disk.", this);
            iS.close();
        } catch (FileNotFoundException ex) {
            Helper.LOGGER.log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Helper.LOGGER.log(Level.SEVERE, null, ex);
            return false;
        }

        input.rewind();

        // The first byte contains the flags...
        byte flags = input.get();
        // Parse the flags...
        _lightDirty = Helper.getInstance().isFlagSet(flags, (short) 0);

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                    _blocks[x][y][z] = input.get();
                    _sunlight[x][y][z] = input.get();
                    _light[x][y][z] = input.get();
                }
            }
        }

        return true;
    }

    /**
     * 
     * @return 
     */
    public boolean isDirty() {
        return _dirty;
    }

    /**
     * 
     * @return 
     */
    public boolean isFresh() {
        return _fresh;
    }

    /**
     * 
     * @return 
     */
    public boolean isLightDirty() {
        return _lightDirty;
    }

    /**
     * 
     * @param _dirty 
     */
    public void setDirty(boolean _dirty) {
        this._dirty = _dirty;
    }

    /**
     * 
     * @param _lightDirty 
     */
    public void setLightDirty(boolean _lightDirty) {
        this._lightDirty = _lightDirty;
    }

    /**
     * 
     * @return 
     */
    public int getChunkID() {
        return _chunkID;
    }

    /**
     * 
     * @return
     */
    @Override
    public Chunk getNext() {
        return _next;
    }

    /**
     * 
     * @return
     */
    @Override
    public Chunk getPrevious() {
        return _prev;
    }

    /**
     * 
     * @param t
     */
    @Override
    public void setNext(Chunk t) {
        _next = t;
    }

    /**
     * 
     * @param t
     */
    @Override
    public void setPrevious(Chunk t) {
        _prev = t;
    }
}
