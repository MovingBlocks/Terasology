/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.github.begla.blockmania;

import java.nio.channels.FileChannel;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import com.github.begla.blockmania.generators.ChunkGenerator;
import com.github.begla.blockmania.blocks.Block;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.newdawn.slick.util.ResourceLoader;
import org.lwjgl.util.vector.Vector4f;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.ArrayList;
import org.lwjgl.util.vector.Vector3f;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.opengl.Texture;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.TextureLoader;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import static org.lwjgl.opengl.GL11.*;

/**
 * Chunks are the basic components of the world. Each chunk contains a fixed amount of blocks
 * determined by the dimension of the chunk. Chunks are used to manage the world efficiently and
 * to reduce the batch count within the render loop.
 *
 * Chunks are tessellated on creation and saved to vertex arrays. From those display lists are generated
 * which are then used for the actual rendering process.
 *
 * The default size of one chunk is 16x128x16 (32768) blocks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Chunk extends RenderableObject implements Comparable<Chunk> {

    private boolean _dirty;
    private boolean _lightDirty;
    private static int _statVertexArrayUpdateCount = 0;
    /* ------ */
    private boolean _fresh = true;
    /* ------ */
    private int _chunkID = -1;
    private static Texture _textureMap;
    /* ------ */
    private final List<Float> _quadsTranslucent = new ArrayList<Float>();
    private final List<Float> _texTranslucent = new ArrayList<Float>();
    private final List<Float> _colorTranslucent = new ArrayList<Float>();
    private final List<Float> _quadsOpaque = new ArrayList<Float>();
    private final List<Float> _texOpaque = new ArrayList<Float>();
    private final List<Float> _colorOpaque = new ArrayList<Float>();
    private final List<Float> _quadsBillboard = new ArrayList<Float>();
    private final List<Float> _texBillboard = new ArrayList<Float>();
    private final List<Float> _colorBillboard = new ArrayList<Float>();
    /* ------ */
    private World _parent = null;

    /* ------ */
    private byte[][][] _blocks;
    private byte[][][] _sunlight;
    /* ------ */
    private int _displayListOpaque = -1;
    private int _displayListTranslucent = -1;
    private int _displayListBillboard = -1;
    /* ------ */
    private ArrayList<ChunkGenerator> _generators = new ArrayList<ChunkGenerator>();

    enum SIDE {

        LEFT, RIGHT, TOP, BOTTOM, FRONT, BACK;
    };

    /**
     * Init. the textures used within chunks.
     */
    public static void init() {
        try {
            Logger.getLogger(Chunk.class.getName()).log(Level.INFO, "Loading chunk textures...");
            _textureMap = TextureLoader.getTexture("png", ResourceLoader.getResource("com/github/begla/blockmania/images/terrain.png").openStream(), GL_NEAREST);
            Logger.getLogger(Chunk.class.getName()).log(Level.INFO, "Finished loading chunk textures!");
        } catch (IOException ex) {
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
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
    public Chunk(World p, Vector3f position, ArrayList<ChunkGenerator> g) {
        this._position = position;
        // Set the chunk ID
        _chunkID = Helper.getInstance().cantorize((int) _position.x, (int) _position.z);

        _parent = p;
        _blocks = new byte[(int) Configuration.CHUNK_DIMENSIONS.x][(int) Configuration.CHUNK_DIMENSIONS.y][(int) Configuration.CHUNK_DIMENSIONS.z];
        _sunlight = new byte[(int) Configuration.CHUNK_DIMENSIONS.x][(int) Configuration.CHUNK_DIMENSIONS.y][(int) Configuration.CHUNK_DIMENSIONS.z];

        _generators.addAll(g);

        _lightDirty = true;
        _dirty = true;
    }

    /**
     * Saves the chunk to disk and removes the display lists.
     */
    public void dispose() {
        writeChunkToDisk();

        try {
            glDeleteLists(_displayListOpaque, 1);
        } catch (Exception e) {
        }

        try {
            glDeleteLists(_displayListTranslucent, 1);
        } catch (Exception e) {
        }

        try {
            glDeleteLists(_displayListBillboard, 1);
        } catch (Exception e) {
        }
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
     * Tries to load a chunk from disk. If the chunk is not present
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
                setLightDirty(false);
                _fresh = false;
                Logger.getLogger(this.getClass().getName()).log(Level.FINEST, "Chunk ({1}) loaded from disk ({0}s).", new Object[]{(System.currentTimeMillis() - timeStart) / 1000d, this});
                return true;
            }

            for (ChunkGenerator g : _generators) {
                g.generate(this);
            }

            updateSunlight();
            _fresh = false;

            Logger.getLogger(this.getClass().getName()).log(Level.FINEST, "Chunk ({1}) generated ({0}s).", new Object[]{(System.currentTimeMillis() - timeStart) / 1000d, this});
            return true;
        }
        return false;
    }

    /**
     * Updates the light of this chunk.
     */
    public void updateLight() {
        // Light updates are only allowed if the initial sunlight
        // was calculated
        if (!_fresh) {
            for (int x = 0; x < (int) Configuration.CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < (int) Configuration.CHUNK_DIMENSIONS.z; z++) {
                    for (int y = 0; y < (int) Configuration.CHUNK_DIMENSIONS.y; y++) {
                        byte lightValue = getLight(x, y, z);
                        if (lightValue > 0 && Block.getBlockForType(getBlock(x, y, z)).isBlockTypeTranslucent()) {
                            spreadLight(x, y, z, lightValue);
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
        // Sunlight should only be applied
        // to fresh chunks
        if (_fresh) {
            for (int x = 0; x < (int) Configuration.CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < (int) Configuration.CHUNK_DIMENSIONS.z; z++) {
                    calcSunlightAtLocalPos(x, z, false);
                }
            }
        }
    }

    /**
     * Generates the vertex-, texture- and color-arrays.
     */
    public void generateVertexArrays() {
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

        List<Float> quads = new ArrayList<Float>();
        List<Float> tex = new ArrayList<Float>();
        List<Float> color = new ArrayList<Float>();

        /*
         * First side of the billboard
         */
        Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.FRONT);
        float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).x;
        float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).y;

        color.add(colorOffset.x * _parent.getDaylightAsFloat());
        color.add(colorOffset.y * _parent.getDaylightAsFloat());
        color.add(colorOffset.z * _parent.getDaylightAsFloat());
        color.add(colorOffset.w);

        tex.add(texOffsetX);
        tex.add(texOffsetY + 0.0624f);
        quads.add(-0.5f + x + offsetX);
        quads.add(-0.5f + y + offsetY);
        quads.add(z + offsetZ);

        color.add(colorOffset.x * _parent.getDaylightAsFloat());
        color.add(colorOffset.y * _parent.getDaylightAsFloat());
        color.add(colorOffset.z * _parent.getDaylightAsFloat());
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY + 0.0624f);
        quads.add(0.5f + x + offsetX);
        quads.add(-0.5f + y + offsetY);
        quads.add(z + offsetZ);

        color.add(colorOffset.x * _parent.getDaylightAsFloat());
        color.add(colorOffset.y * _parent.getDaylightAsFloat());
        color.add(colorOffset.z * _parent.getDaylightAsFloat());
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY);

        quads.add(0.5f + x + offsetX);
        quads.add(0.5f + y + offsetY);
        quads.add(z + offsetZ);

        color.add(colorOffset.x * _parent.getDaylightAsFloat());
        color.add(colorOffset.y * _parent.getDaylightAsFloat());
        color.add(colorOffset.z * _parent.getDaylightAsFloat());
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

        color.add(colorOffset.x * _parent.getDaylightAsFloat());
        color.add(colorOffset.y * _parent.getDaylightAsFloat());
        color.add(colorOffset.z * _parent.getDaylightAsFloat());
        color.add(colorOffset.w);

        tex.add(texOffsetX);
        tex.add(texOffsetY + 0.0624f);
        quads.add(x + offsetX);
        quads.add(-0.5f + y + offsetY);
        quads.add(-0.5f + z + offsetZ);

        color.add(colorOffset.x * _parent.getDaylightAsFloat());
        color.add(colorOffset.y * _parent.getDaylightAsFloat());
        color.add(colorOffset.z * _parent.getDaylightAsFloat());
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY + 0.0624f);
        quads.add(x + offsetX);
        quads.add(-0.5f + y + offsetY);
        quads.add(0.5f + z + offsetZ);

        color.add(colorOffset.x * _parent.getDaylightAsFloat());
        color.add(colorOffset.y * _parent.getDaylightAsFloat());
        color.add(colorOffset.z * _parent.getDaylightAsFloat());
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY);

        quads.add(x + offsetX);
        quads.add(0.5f + y + offsetY);
        quads.add(0.5f + z + offsetZ);

        color.add(colorOffset.x * _parent.getDaylightAsFloat());
        color.add(colorOffset.y * _parent.getDaylightAsFloat());
        color.add(colorOffset.z * _parent.getDaylightAsFloat());
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
        int blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y + 1), getBlockWorldPosZ(z));

        drawTop = isSideVisibleForBlockTypes(blockToCheck, block);

        List<Float> quads = new ArrayList<Float>();
        List<Float> tex = new ArrayList<Float>();
        List<Float> color = new ArrayList<Float>();

        if (drawTop) {
            Vector4f colorOffset = Block.getBlockForType(block).getColorOffsetFor(Block.SIDE.TOP);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y + 1), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x, y + 1, z)), Configuration.MIN_LIGHT) / 16f;

            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.TOP).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.TOP).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(-0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
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
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1)) * (calcSimpleOcclusionAmount(x, y, z - 1) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT) / 16f;

            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.FRONT).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(-0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
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
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1)) * (calcSimpleOcclusionAmount(x, y, z + 1) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT) / 16f;


            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BACK).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);

            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
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
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x - 1, y, z) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT) / 16f;

            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.LEFT).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.LEFT).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);

            quads.add(-0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
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
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x + 1, y, z) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT) / 16f;

            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.RIGHT).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.RIGHT).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offsetX);
            quads.add(0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
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
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y - 1), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x, y - 1, z)), Configuration.MIN_LIGHT) / 16f;

            float texOffsetX = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BOTTOM).x;
            float texOffsetY = Block.getBlockForType(block).getTextureOffsetFor(Block.SIDE.BOTTOM).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(-0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(-0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offsetX);
            quads.add(-0.5f + y + offsetY);
            quads.add(0.5f + z + offsetZ);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylightAsFloat());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylightAsFloat());
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
     * Generates the display lists from the precalculated arrays.
     */
    public void generateDisplayLists() {
        if (_colorOpaque.isEmpty() && _texOpaque.isEmpty() && _quadsOpaque.isEmpty() && _colorTranslucent.isEmpty() && _texTranslucent.isEmpty() && _quadsTranslucent.isEmpty()) {
            if (_colorBillboard.isEmpty() && _texBillboard.isEmpty() && _quadsBillboard.isEmpty()) {
                return;
            }
        }

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

        for (Float f : _quadsOpaque) {
            vb.put(f);
        }

        tb = BufferUtils.createFloatBuffer(_texOpaque.size());

        for (Float f : _texOpaque) {
            tb.put(f);
        }

        cb = BufferUtils.createFloatBuffer(_colorOpaque.size());

        for (Float f : _colorOpaque) {
            cb.put(f);
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

        _quadsOpaque.clear();
        _texOpaque.clear();
        _colorOpaque.clear();

        vb = BufferUtils.createFloatBuffer(_quadsTranslucent.size());

        for (Float f : _quadsTranslucent) {
            vb.put(f);
        }

        tb = BufferUtils.createFloatBuffer(_texTranslucent.size());

        for (Float f : _texTranslucent) {
            tb.put(f);
        }

        cb = BufferUtils.createFloatBuffer(_colorTranslucent.size());

        for (Float f : _colorTranslucent) {
            cb.put(f);
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

        _quadsTranslucent.clear();
        _texTranslucent.clear();
        _colorTranslucent.clear();

        vb = BufferUtils.createFloatBuffer(_quadsBillboard.size());

        for (Float f : _quadsBillboard) {
            vb.put(f);
        }

        tb = BufferUtils.createFloatBuffer(_texBillboard.size());

        for (Float f : _texBillboard) {
            tb.put(f);
        }

        cb = BufferUtils.createFloatBuffer(_colorBillboard.size());

        for (Float f : _colorBillboard) {
            cb.put(f);
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

        _quadsBillboard.clear();
        _texBillboard.clear();
        _colorBillboard.clear();
    }

    /**
     * Returns true if the block side is ajdacent to a translucent block or an air
     * block.
     *
     * NOTE: Air has to be handled separatly. Otherwise the water surface would not be displayed due to the tessellation process.
     */
    private boolean isSideVisibleForBlockTypes(int blockToCheck, int currentBlock) {
        return blockToCheck == 0x0 || blockToCheck == 0x6 || (Block.getBlockForType(blockToCheck).isBlockTypeTranslucent() && !Block.getBlockForType(currentBlock).isBlockTypeTranslucent());
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
     * @param refresh  
     */
    public void calcSunlightAtLocalPos(int x, int z, boolean refresh) {
        byte light = Configuration.MAX_LIGHT;
        boolean covered = false;

        for (int y = (int) Configuration.CHUNK_DIMENSIONS.y - 1; y >= 0; y--) {
            Block b = Block.getBlockForType(_blocks[x][y][z]);

            if (!b.isBlockTypeTranslucent()) {
                covered = true;
                continue;
            }

            if (b.isBlockTypeTranslucent() && !covered) {
                _sunlight[x][y][z] = light;

                // Reduce the sunlight with each passed block
                if (_blocks[x][y][z] != 0x0) {
                    light -= Configuration.LIGHT_ABSORPTION;
                    light = (byte) Math.max(0, light);
                }
            } else if (b.isBlockTypeTranslucent() && covered) {
                byte oldLightValue = _sunlight[x][y][z];
                _sunlight[x][y][z] = 0;
                if (refresh) {
                    refreshLightAtLocalPos(x, y, z);
                }
            }
        }
    }

    /**
     * 
     * @param x
     * @param y
     * @param z 
     */
    public void refreshLightAtLocalPos(int x, int y, int z) {
        int blockPosX = getBlockWorldPosX(x);
        int blockPosY = getBlockWorldPosY(y);
        int blockPosZ = getBlockWorldPosZ(z);

        byte val = getBlock(x, y, z);

        // If a block was placed, remove the light value at this point
        if (!Block.getBlockForType(val).isBlockTypeTranslucent()) {
            setSunlight(x, y, z, (byte) 0);
        } else {
            // If the block was removed: Find the brightest neighbor and
            // set the current block to this value - 1
            byte val1 = _parent.getLight(blockPosX + 1, blockPosY, blockPosZ);
            byte val2 = _parent.getLight(blockPosX - 1, blockPosY, blockPosZ);
            byte val3 = _parent.getLight(blockPosX, blockPosY, blockPosZ + 1);
            byte val4 = _parent.getLight(blockPosX, blockPosY, blockPosZ - 1);
            byte val5 = _parent.getLight(blockPosX, blockPosY + 1, blockPosZ);
            byte val6 = _parent.getLight(blockPosX, blockPosY - 1, blockPosZ);

            byte max = (byte) (Math.max(Math.max(Math.max(val1, val2), Math.max(val3, val4)), Math.max(val5, val6)) - 1);
            if (max < 0) {
                max = 0;
            }

            setSunlight(x, y, z, max);
        }
    }

    /**
     * TODO
     * 
     * @param x
     * @param y
     * @param z 
     * @param oldLightValue 
     */
    public void unspreadLight(int x, int y, int z, byte oldLightValue) {
        unspreadLight(x, y, z, oldLightValue, 0);
    }

    /**
     * TODO
     * 
     * @param x
     * @param y
     * @param z 
     * @param depth 
     * @param oldLightValue 
     */
    public void unspreadLight(int x, int y, int z, byte oldLightValue, int depth) {
        throw new NotImplementedException();
    }

    /**
     * Recursive light calculation.
     * 
     * @param x
     * @param y
     * @param z
     * @param lightValue 
     */
    public void spreadLight(int x, int y, int z, byte lightValue) {
        spreadLight(x, y, z, lightValue, 0);
    }

    /**
     * Recursive light calculation.
     * 
     * @param x
     * @param y
     * @param z 
     * @param lightValue
     * @param depth 
     */
    public void spreadLight(int x, int y, int z, byte lightValue, int depth) {
        if (depth > lightValue) {
            return;
        }

        int blockPosX = getBlockWorldPosX(x);
        int blockPosY = getBlockWorldPosY(y);
        int blockPosZ = getBlockWorldPosZ(z);

        byte val1 = _parent.getLight(blockPosX + 1, blockPosY, blockPosZ);
        byte type1 = _parent.getBlock(blockPosX + 1, blockPosY, blockPosZ);
        byte val2 = _parent.getLight(blockPosX - 1, blockPosY, blockPosZ);
        byte type2 = _parent.getBlock(blockPosX - 1, blockPosY, blockPosZ);
        byte val3 = _parent.getLight(blockPosX, blockPosY, blockPosZ + 1);
        byte type3 = _parent.getBlock(blockPosX, blockPosY, blockPosZ + 1);
        byte val4 = _parent.getLight(blockPosX, blockPosY, blockPosZ - 1);
        byte type4 = _parent.getBlock(blockPosX, blockPosY, blockPosZ - 1);
        byte val5 = _parent.getLight(blockPosX, blockPosY + 1, blockPosZ);
        byte type5 = _parent.getBlock(blockPosX, blockPosY + 1, blockPosZ);
        byte val6 = _parent.getLight(blockPosX, blockPosY - 1, blockPosZ);
        byte type6 = _parent.getBlock(blockPosX, blockPosY - 1, blockPosZ);

        byte newLightValue = 0;
        newLightValue = (byte) (lightValue - depth);

        _parent.setSunlight(blockPosX, blockPosY, blockPosZ, newLightValue);

        if (lightValue <= 0) {
            return;
        }

        if (val1 < newLightValue - 1 && Block.getBlockForType(type1).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX + 1, blockPosY, blockPosZ, lightValue, depth + 1);
        }


        if (val2 < newLightValue - 1 && Block.getBlockForType(type2).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX - 1, blockPosY, blockPosZ, lightValue, depth + 1);
        }


        if (val3 < newLightValue - 1 && Block.getBlockForType(type3).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX, blockPosY, blockPosZ + 1, lightValue, depth + 1);
        }


        if (val4 < newLightValue - 1 && Block.getBlockForType(type4).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX, blockPosY, blockPosZ - 1, lightValue, depth + 1);
        }


        if (val5 < newLightValue - 1 && Block.getBlockForType(type5).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX, blockPosY + 1, blockPosZ, lightValue, depth + 1);
        }


        if (val6 < newLightValue - 1 && Block.getBlockForType(type6).isBlockTypeTranslucent()) {
            _parent.spreadLight(blockPosX, blockPosY - 1, blockPosZ, lightValue, depth + 1);
        }
    }

    /**
     * Returns the amount of blocks with a value greater than zero.
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
     * @return The distance of the chunk to the player
     */
    public double calcDistanceToPlayer() {
        return Math.sqrt(Math.pow(_parent.getPlayer().getPosition().x - getChunkWorldPosX(), 2) + Math.pow(_parent.getPlayer().getPosition().z - getChunkWorldPosZ(), 2));
    }

    /**
     * Returns the light intensity at a given local block position.
     *
     * @param x Local block position on the x-axis
     * @param y Local block position on the y-axis
     * @param z Local block position on the z-axis
     * @return The light intensity
     */
    public byte getLight(int x, int y, int z) {
        try {
            return (byte) _sunlight[x][y][z];
            //return (byte) Math.max(_light[x][y][z], _sunlight[x][y][z]);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Sets the light value at the given position.
     * 
     * @param x Local block position on the x-axis
     * @param y Local block position on the y-axis
     * @param z Local block position on the z-axis
     * @param intens The light intensity to set
     */
    public void setSunlight(int x, int y, int z, byte intens) {
        if (isFresh()) {
            // Sunlight may not be changed within fresh blocks
            return;
        }

        try {
            Block b = Block.getBlockForType(getBlock(x, y, z));
            _sunlight[x][y][z] = intens;
            _dirty = true;
            // Mark the neighbors as dirty
            markNeighborsDirty(x, z);
        } catch (Exception e) {
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
        try {
            return _blocks[x][y][z];
        } catch (Exception e) {
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
        try {
            _blocks[x][y][z] = type;

            Block b = Block.getBlockForType(type);

            // If an opaque block was set, remove the light from this field
            if (!b.isBlockTypeTranslucent()) {
                _sunlight[x][y][z] = 0;
            }

            // Update vertex arrays and light
            setDirty(true);
            // Mark the neighbors as dirty
            markNeighborsDirty(x, z);
        } catch (Exception e) {
        }
    }

    /**
     * Calculates a simple occlusion value based on the amount of blocks
     * surrounding the given block position.
     * 
     * @param x Local block position on the x-axis
     * @param y Local block position on the y-axis
     * @param z Local block position on the z-axis
     * @return Occlusion amount
     */
    public float calcSimpleOcclusionAmount(int x, int y, int z) {
        float intens = 0f;
        if (Block.getBlockForType(_parent.getBlock(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlockForType(_parent.getBlock(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlockForType(_parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlockForType(_parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlockForType(_parent.getBlock(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlockForType(_parent.getBlock(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlockForType(_parent.getBlock(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlockForType(_parent.getBlock(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1))).isCastingShadows()) {
            intens++;
        }
        return 1f - (float) intens * Configuration.OCCLUSION_INTENS;
    }

    /**
     * Returns the neighbor chunks of this chunk.
     *
     * @return The adjacent chunks
     */
    public Chunk[] loadOrCreateNeighbors() {
        Chunk[] chunks = new Chunk[8];
        chunks[0] = _parent.loadOrCreateChunk((int) _position.x + 1, (int) _position.z);
        chunks[1] = _parent.loadOrCreateChunk((int) _position.x - 1, (int) _position.z);
        chunks[2] = _parent.loadOrCreateChunk((int) _position.x, (int) _position.z + 1);
        chunks[3] = _parent.loadOrCreateChunk((int) _position.x, (int) _position.z - 1);
        chunks[4] = _parent.loadOrCreateChunk((int) _position.x + 1, (int) _position.z + 1);
        chunks[5] = _parent.loadOrCreateChunk((int) _position.x - 1, (int) _position.z - 1);
        chunks[6] = _parent.loadOrCreateChunk((int) _position.x - 1, (int) _position.z + 1);
        chunks[7] = _parent.loadOrCreateChunk((int) _position.x + 1, (int) _position.z - 1);
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
            return new Double(calcDistanceToPlayer()).compareTo(o.calcDistanceToPlayer());
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
     * TODO: Uses a lot of memory... Precisely 524288 Bytes (0.5 MB) per Chunk.
     * TODO: Buggy with chunks that contain light from adjacent chunks but have not been
     * flooded yet
     * 
     * @return 
     */
    public boolean writeChunkToDisk() {
        // Don't save fresh chunks
        if (_fresh || _lightDirty) {
            return false;
        }

        ByteBuffer output = BufferUtils.createByteBuffer((int) Configuration.CHUNK_DIMENSIONS.x * (int) Configuration.CHUNK_DIMENSIONS.y * (int) Configuration.CHUNK_DIMENSIONS.z * 3);

        File dir1 = new File(String.format("SAVED_WORLDS", _parent.getTitle()));
        // Create directory
        if (!dir1.exists()) {
            dir1.mkdir();
        }

        // Create directory
        File dir2 = new File(String.format("SAVED_WORLDS/%s", _parent.getTitle()));
        if (!dir2.exists()) {
            dir2.mkdir();
        }

        File f = new File(String.format("SAVED_WORLDS/%s/%d.bc", _parent.getTitle(), Helper.getInstance().cantorize((int) _position.x, (int) _position.z)));

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                    output.put(_blocks[x][y][z]);
                    output.put(_sunlight[x][y][z]);
                }
            }
        }

        output.rewind();

        try {
            FileOutputStream oS = new FileOutputStream(f);
            FileChannel c = oS.getChannel();
            c.write(output);
            oS.close();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Loads this chunk from the disk (if present).
     * 
     * @return 
     */
    public boolean loadChunkFromFile() {
        ByteBuffer input = BufferUtils.createByteBuffer((int) Configuration.CHUNK_DIMENSIONS.x * (int) Configuration.CHUNK_DIMENSIONS.y * (int) Configuration.CHUNK_DIMENSIONS.z * 3);
        File f = new File(String.format("SAVED_WORLDS/%s/%d.bc", _parent.getTitle(), Helper.getInstance().cantorize((int) _position.x, (int) _position.z)));

        if (!f.exists()) {
            return false;
        }

        try {
            FileInputStream iS = new FileInputStream(f);
            FileChannel c = iS.getChannel();
            c.read(input);
            iS.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        input.rewind();

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                    _blocks[x][y][z] = input.get();
                    _sunlight[x][y][z] = input.get();
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
}
