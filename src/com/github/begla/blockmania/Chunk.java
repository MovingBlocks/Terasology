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
import com.github.begla.blockmania.utilities.Helper;
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
import static org.lwjgl.opengl.GL11.*;

/**
 * Chunks are the basic components of the world. Each chunk contains a fixed amount of blocks
 * determined by the dimension of the chunk. Chunks are used to manage the world efficiently and
 * reduce the batch count within the render loop.
 *
 * Chunks are tessellated on creation and saved to vertex arrays. From those display lists are generated
 * which are then used for the actual rendering process.
 *
 * The default size of chunk is 16x128x16 (32768) blocks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Chunk extends RenderableObject implements Comparable<Chunk> {

    /**
     * True if a block value was changed within the chunk.
     */
    public boolean _dirty = true;
    /**
     * True if a light value was changed within the chunk.
     */
    public boolean _lightDirty = true;
    private static int _statVertexArrayUpdateCount = 0;
    /* ------ */
    private boolean _fresh = true;
    /* ------ */
    private static int _maxChunkID = 0;
    private int _chunkID = 0;
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
    private byte[][][] _light;
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
     * of generators. The genrators are applied when the chunk is generated.
     *
     * @param p The parent world
     * @param position The absolute position of the chunk within the world
     * @param g A list of generators which should be applied to this chunk
     */
    public Chunk(World p, Vector3f position, ArrayList<ChunkGenerator> g) {
        this._position = position;
        _parent = p;
        _blocks = new byte[(int) Configuration.CHUNK_DIMENSIONS.x][(int) Configuration.CHUNK_DIMENSIONS.y][(int) Configuration.CHUNK_DIMENSIONS.z];
        _sunlight = new byte[(int) Configuration.CHUNK_DIMENSIONS.x][(int) Configuration.CHUNK_DIMENSIONS.y][(int) Configuration.CHUNK_DIMENSIONS.z];
        _light = new byte[(int) Configuration.CHUNK_DIMENSIONS.x][(int) Configuration.CHUNK_DIMENSIONS.y][(int) Configuration.CHUNK_DIMENSIONS.z];

        _chunkID = _maxChunkID + 1;
        _maxChunkID++;

        _generators.addAll(g);
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
     * Applies the genrators and updates the light if the chunk is
     * marked as fresh.
     *
     * @return True if a generation has been executes
     */
    public boolean generate() {
        if (_fresh) {
            // Apply all generators to this chunk
            long timeStart = System.currentTimeMillis();

            if (loadChunkFromFile()) {
                _lightDirty = false;
                _fresh = false;
                markNeighborsDirty();
                Logger.getLogger(this.getClass().getName()).log(Level.FINEST, "Chunk ({1}) loaded from disk ({0}s).", new Object[]{(System.currentTimeMillis() - timeStart) / 1000d, this});
                return true;
            }

            for (ChunkGenerator g : _generators) {
                g.generate(this);
            }
            calcSunlight();
            calcLight();
            _lightDirty = false;
            _fresh = false;
            writeChunkToDisk();

            Logger.getLogger(this.getClass().getName()).log(Level.FINEST, "Chunk ({1}) generated ({0}s).", new Object[]{(System.currentTimeMillis() - timeStart) / 1000d, this});
            return true;
        }
        return false;
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

        _dirty = false;
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
        if (Block.getBlock(block).isBlockInvisible() || !Block.getBlock(block).isBlockBillboard()) {
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
        Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.FRONT);
        float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.FRONT).x;
        float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.FRONT).y;

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
        colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.BACK);
        texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BACK).x;
        texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BACK).y;

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

        // Ignore invisible blocks like air and billboards like flowers!
        if (Block.getBlock(block).isBlockInvisible() || Block.getBlock(block).isBlockBillboard()) {
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
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.TOP);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y + 1), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x, y + 1, z)), Configuration.MIN_LIGHT) / 16f;

            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.TOP).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.TOP).y;

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
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.FRONT);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1)) * (calcSimpleOcclusionAmount(x, y, z - 1) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT) / 16f;

            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.FRONT).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.FRONT).y;

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
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.BACK);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1)) * (calcSimpleOcclusionAmount(x, y, z + 1) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT) / 16f;


            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BACK).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BACK).y;

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
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.LEFT);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x - 1, y, z) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT) / 16f;

            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.LEFT).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.LEFT).y;

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
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.RIGHT);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x + 1, y, z) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT) / 16f;

            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.RIGHT).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.RIGHT).y;

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
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.BOTTOM);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y - 1), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x, y - 1, z)), Configuration.MIN_LIGHT) / 16f;

            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BOTTOM).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BOTTOM).y;

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

        if (!Block.getBlock(block).isBlockTypeTranslucent()) {
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

        if (glIsList(_displayListOpaque)) {
            glDeleteLists(_displayListOpaque, 1);
        }

        if (glIsList(_displayListTranslucent)) {
            glDeleteLists(_displayListTranslucent, 1);
        }

        if (glIsList(_displayListBillboard)) {
            glDeleteLists(_displayListBillboard, 1);
        }

        _displayListOpaque = glGenLists(1);
        _displayListTranslucent = glGenLists(1);
        _displayListBillboard = glGenLists(1);

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
     * Returns true, if the block side is ajdacent to a translucent block or an air
     * block.
     *
     * NOTE: Air has to be handled separatly. Otherwise the water surface would be ignored in the tessellation progress!
     */
    private boolean isSideVisibleForBlockTypes(int blockToCheck, int currentBlock) {
        return blockToCheck == 0x0 || blockToCheck == 0x6 || (Block.getBlock(blockToCheck).isBlockTypeTranslucent() && !Block.getBlock(currentBlock).isBlockTypeTranslucent());
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
     * Calculates the sunlight.
     */
    private void calcSunlight() {
        for (int x = 0; x < (int) Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < (int) Configuration.CHUNK_DIMENSIONS.z; z++) {
                calcSunlightAtLocalPos(x, z);
            }
        }
    }

    /**
     * Calculates the sunlight at a given column within the chunk.
     * 
     * @param x Local block position on the x-axis
     * @param z Local block position on the z-axis
     */
    public void calcSunlightAtLocalPos(int x, int z) {
        byte light = Configuration.MAX_LIGHT;
        boolean covered = false;

        for (int y = (int) Configuration.CHUNK_DIMENSIONS.y - 1; y >= 0; y--) {
            if (Block.getBlock(_blocks[x][y][z]).isBlockTypeTranslucent() && !covered) {
                setSunlight(x, y, z, light);

                // Reduce the sunlight with each passed block
                if (_blocks[x][y][z] != 0x0) {
                    light -= Configuration.LIGHT_ABSORPTION;
                    light = (byte) Math.max(0, light);
                }
            } else if (Block.getBlock(_blocks[x][y][z]).isBlockTypeTranslucent() && covered) {
                setSunlight(x, y, z, (byte) 0);
            } else {
                setSunlight(x, y, z, (byte) 0);
                covered = true;
            }
        }

        _lightDirty = true;
    }

    /**
     * Update the light of this chunk.
     *
     * TODO: Update this entry.
     * TODO: Not working perfectly.
     */
    public void calcLight() {
        for (int x = 0; x < (int) Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < (int) Configuration.CHUNK_DIMENSIONS.z; z++) {
                for (int y = (int) Configuration.CHUNK_DIMENSIONS.y - 1; y > 0; y--) {
                    byte lightValue = getLight(x, y, z);
                    if (Block.getBlock(getBlock(x, y, z)).isBlockTypeTranslucent() && lightValue > 0) {
                        propagateLight(x, y, z, lightValue, 0);
                    }

                }
            }
        }

        _lightDirty = false;
    }

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @param oldLightValue 
     */
    public void unpropagateLight(int x, int y, int z, byte oldLightValue) {
        unpropagateLight(x, y, z, oldLightValue, 0);
    }

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @param dirX
     * @param dirY
     * @param dirZ
     * @param oldLightValue 
     */
    public void unpropagateLight(int x, int y, int z, byte oldLightValue, int depth) {
        if (depth >= 16) {
            return;
        }
        
        int blockPosX = getBlockWorldPosX(x);
        int blockPosY = getBlockWorldPosY(y);
        int blockPosZ = getBlockWorldPosZ(z);

        byte blockType = _parent.getBlock(blockPosX, blockPosY, blockPosZ);

        if ((!Block.getBlock(blockType).isBlockTypeTranslucent() && depth > 0)) {
            return;
        }

        if (oldLightValue <= 0) {
            return;
        }

        _parent.setSunlight(blockPosX, blockPosY, blockPosZ, (byte) 0);

        byte val1 = _parent.getLight(blockPosX + 1, blockPosY, blockPosZ);

        if (val1 < oldLightValue && val1 > 0) {
            _parent.unpropagateLight(blockPosX + 1, blockPosY, blockPosZ, oldLightValue, ++depth);
        }

        byte val2 = _parent.getLight(blockPosX - 1, blockPosY, blockPosZ);

        if (val2 < oldLightValue && val2 > 0) {
            _parent.unpropagateLight(blockPosX - 1, blockPosY, blockPosZ, oldLightValue, ++depth);
        }

        byte val3 = _parent.getLight(blockPosX, blockPosY, blockPosZ + 1);

        if (val3 < oldLightValue && val3 > 0) {
            _parent.unpropagateLight(blockPosX, blockPosY, blockPosZ + 1, oldLightValue, ++depth);
        }

        byte val4 = _parent.getLight(blockPosX, blockPosY, blockPosZ - 1);

        if (val4 < oldLightValue && val4 > 0) {
            _parent.unpropagateLight(blockPosX, blockPosY, blockPosZ - 1, oldLightValue, ++depth);
        }

        byte val5 = _parent.getLight(blockPosX, blockPosY + 1, blockPosZ);

        if (val5 < oldLightValue && val5 > 0) {
            _parent.unpropagateLight(blockPosX, blockPosY + 1, blockPosZ, oldLightValue, ++depth);
        }

        byte val6 = _parent.getLight(blockPosX, blockPosY - 1, blockPosZ);

        if (val6 < oldLightValue && val6 > 0) {
            _parent.unpropagateLight(blockPosX, blockPosY - 1, blockPosZ, oldLightValue, ++depth);
        }

        _lightDirty = true;
    }

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @param lightValue 
     */
    public void propagateLight(int x, int y, int z, byte lightValue) {
        propagateLight(x, y, z, lightValue, 0);
    }

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @param lightValue
     * @param depth 
     */
    public void propagateLight(int x, int y, int z, byte lightValue, int depth) {
        if (depth >= 4) {
            return;
        }

        int blockPosX = getBlockWorldPosX(x);
        int blockPosY = getBlockWorldPosY(y);
        int blockPosZ = getBlockWorldPosZ(z);

        // Ignore solid blocks
        byte blockType = _parent.getBlock(blockPosX, blockPosY, blockPosZ);
        if (!Block.getBlock(blockType).isBlockTypeTranslucent()) {
            return;
        }

        // If the new light value is less than 0 we are done!
        byte newLightValue = (byte) (lightValue - depth);
        if (newLightValue < 0) {
            return;
        }

        _parent.setSunlight(blockPosX, blockPosY, blockPosZ, newLightValue);

        byte val1 = _parent.getLight(blockPosX + 1, blockPosY, blockPosZ);
        if (val1 < newLightValue - 1) {
            _parent.propagateLight(blockPosX + 1, blockPosY, blockPosZ, lightValue, ++depth);
        }

        byte val2 = _parent.getLight(blockPosX - 1, blockPosY, blockPosZ);
        if (val2 < newLightValue - 1) {
            _parent.propagateLight(blockPosX - 1, blockPosY, blockPosZ, lightValue, ++depth);
        }

        byte val3 = _parent.getLight(blockPosX, blockPosY, blockPosZ + 1);
        if (val3 < newLightValue - 1) {
            _parent.propagateLight(blockPosX, blockPosY, blockPosZ + 1, lightValue, ++depth);
        }

        byte val4 = _parent.getLight(blockPosX, blockPosY, blockPosZ - 1);
        if (val4 < newLightValue - 1) {
            _parent.propagateLight(blockPosX, blockPosY, blockPosZ - 1, lightValue, ++depth);
        }

        byte val5 = _parent.getLight(blockPosX, blockPosY + 1, blockPosZ);
        if (val5 < newLightValue - 1) {
            _parent.propagateLight(blockPosX, blockPosY + 1, blockPosZ, lightValue, ++depth);
        }

        byte val6 = _parent.getLight(blockPosX, blockPosY - 1, blockPosZ);
        if (val6 < newLightValue - 1) {
            _parent.propagateLight(blockPosX, blockPosY - 1, blockPosZ, lightValue, ++depth);
        }

        _lightDirty = true;
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
        double distance = Math.sqrt(Math.pow(_parent.getPlayer().getPosition().x - getChunkWorldPosX(), 2) + Math.pow(_parent.getPlayer().getPosition().z - getChunkWorldPosZ(), 2));

        // Update the chunks in direction of the player first
//        double weight = Vector3f.dot(_position.normalise(null), _parent.getPlayer().getViewDirection().normalise(null));
//
//        if (weight <= 0d) {
//            distance = Double.MAX_VALUE;
//        } else {
//            distance /= weight;
//        }

        return distance;
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
            return (byte) Math.max(_light[x][y][z], _sunlight[x][y][z]);
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
        try {
            _sunlight[x][y][z] = intens;
            _dirty = true;
            _lightDirty = true;
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
            // Update vertex arrays and light
            _dirty = true;
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
        if (Block.getBlock(_parent.getBlock(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlock(_parent.getBlock(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlock(_parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlock(_parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlock(_parent.getBlock(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlock(_parent.getBlock(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlock(_parent.getBlock(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1))).isCastingShadows()) {
            intens++;
        }
        if (Block.getBlock(_parent.getBlock(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1))).isCastingShadows()) {
            intens++;
        }
        return 1f - (float) intens * Configuration.OCCLUSION_INTENS;
    }

    /**
     * Returns the neighbor chunks of this chunk.
     *
     * @return The adjacent chunks
     */
    public Chunk[] getNeighbors() {
        Chunk[] chunks = new Chunk[4];
        chunks[0] = _parent.getChunk((int) _position.x + 1, (int) _position.y, (int) _position.z);
        chunks[1] = _parent.getChunk((int) _position.x - 1, (int) _position.y, (int) _position.z);
        chunks[2] = _parent.getChunk((int) _position.x, (int) _position.y, (int) _position.z + 1);
        chunks[3] = _parent.getChunk((int) _position.x, (int) _position.y, (int) _position.z - 1);
        return chunks;
    }

    /**
     * 
     */
    public void markNeighborsDirty() {
        Chunk[] neighbors = getNeighbors();

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
        Chunk[] neighbors = getNeighbors();

        if (x == 0 && neighbors[1] != null) {
            neighbors[1]._dirty = true;
            neighbors[1]._lightDirty = true;
        }

        if (x == Configuration.CHUNK_DIMENSIONS.x - 1 && neighbors[0] != null) {
            neighbors[0]._dirty = true;
            neighbors[0]._lightDirty = true;
        }

        if (z == 0 && neighbors[3] != null) {
            neighbors[3]._dirty = true;
            neighbors[3]._lightDirty = true;
        }

        if (z == Configuration.CHUNK_DIMENSIONS.z - 1 && neighbors[2] != null) {
            neighbors[2]._dirty = true;
            neighbors[2]._lightDirty = true;
        }
    }

    /**
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
     * Chunks can be compared by the relative distance to the player.
     *
     * @param o The chunk to compare to
     * @return
     */
    @Override
    public int compareTo(Chunk o) {
        return new Double(calcDistanceToPlayer()).compareTo(o.calcDistanceToPlayer());
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
     * Saves this chunk to the disk.
     */
    public boolean writeChunkToDisk() {
        // Don't save fresh chunks
        if (_fresh) {
            return false;
        }

        ByteBuffer output = BufferUtils.createByteBuffer((int) Configuration.CHUNK_DIMENSIONS.x * (int) Configuration.CHUNK_DIMENSIONS.y * (int) Configuration.CHUNK_DIMENSIONS.z * 3);

        File dir = new File(String.format("%s", _parent.getTitle()));

        // Create directory (if it does not exist)
        if (!dir.exists()) {
            dir.mkdir();
        }

        File f = new File(String.format("%s/%d.bc", _parent.getTitle(), Helper.getInstance().cantorize((int) _position.x, (int) _position.z)));

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
     */
    public boolean loadChunkFromFile() {
        ByteBuffer input = BufferUtils.createByteBuffer((int) Configuration.CHUNK_DIMENSIONS.x * (int) Configuration.CHUNK_DIMENSIONS.y * (int) Configuration.CHUNK_DIMENSIONS.z * 3);
        File f = new File(String.format("%s/%d.bc", _parent.getTitle(), Helper.getInstance().cantorize((int) _position.x, (int) _position.z)));

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
                    _light[x][y][z] = input.get();
                }
            }
        }

        return true;
    }
}
