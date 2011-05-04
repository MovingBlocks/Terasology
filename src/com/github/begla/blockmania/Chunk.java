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

import com.github.begla.blockmania.blocks.Block;
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
 * determined by their dimensions. Chunks are used to manage the world efficiently and
 * reduce the batch count within the render loop.
 *
 * Chunks are tessellated on creation and saved to vertex arrays. From those a display list is generated
 * which is then used for the actual rendering process.
 *
 * The default size of chunk is 16x128x16 (32768) blocks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Chunk extends RenderableObject implements Comparable<Chunk> {

    public boolean _dirty = true;
    private boolean _fresh = true;
    /* ------ */
    private static int _maxChunkID = 0;
    private int _chunkID = -1;
    /* ------ */
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
    private int[][][] _blocks;
    private float[][][] _light;
    /* ------ */
    private int _displayListOpaque = -1;
    private int _displayListTranslucent = -1;
    private int _displayListBillboard = -1;

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
     * Init. the chunk.
     */
    public Chunk(World p, Vector3f position) {
        this._position = position;
        _parent = p;
        _blocks = new int[(int) Configuration.CHUNK_DIMENSIONS.x][(int) Configuration.CHUNK_DIMENSIONS.y][(int) Configuration.CHUNK_DIMENSIONS.z];
        _light = new float[(int) Configuration.CHUNK_DIMENSIONS.x][(int) Configuration.CHUNK_DIMENSIONS.y][(int) Configuration.CHUNK_DIMENSIONS.z];

        _chunkID = _maxChunkID + 1;
        _maxChunkID++;
    }

    /**
     * Draws the chunk.
     */
    public void render(boolean translucent) {

        /*
         * Draws the outline of each chunk.
         */
        if (Configuration.SHOW_CHUNK_OUTLINES) {
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

    /*
     * Chunks can be compared by the relative distance to the player.
     */
    @Override
    public int compareTo(Chunk o) {
        return new Double(calcDistanceToPlayer()).compareTo(o.calcDistanceToPlayer());
    }

    /*
     * Returns some information about a chunk as a string.
     */
    @Override
    public String toString() {
        return String.format("Chunk (%d) cotaining %d Blocks at %s.", _chunkID, blockCount(), _position);
    }

    public boolean generate() {
        if (_fresh) {
            generateTerrain();
            populate();
            calcSunlight();
            calcLight();
            _fresh = false;
            return true;
        }
        return false;
    }

    /**
     * Generates the terrain within this chunk.
     *
     * TODO: Much to simple and boring
     */
    public void generateTerrain() {
        int xOffset = (int) _position.x * (int) Configuration.CHUNK_DIMENSIONS.x;
        int yOffset = (int) _position.y * (int) Configuration.CHUNK_DIMENSIONS.y;
        int zOffset = (int) _position.z * (int) Configuration.CHUNK_DIMENSIONS.z;

        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                int height = (int) (calcTerrainElevation(x + xOffset, z + zOffset) + (calcTerrainRoughness(x + xOffset, z + zOffset) * calcTerrainDetail(x + xOffset, z + zOffset)) * 64);

                for (int i = (int) Configuration.CHUNK_DIMENSIONS.y; i >= 0; i--) {
                    if (calcCaveDensityAt(x + xOffset, i + yOffset, z + zOffset) < 0.5) {
                        if (calcCanyonDensity(x + xOffset, i + yOffset, z + zOffset) > 0.1f) {
                            if (i == height) {
                                /*
                                 * Grass covers the terrain.
                                 */
                                if (i > 32) {
                                    setBlock(x, i, z, 0x1);
                                } else if (i <= 34 && i >= 28) {
                                    setBlock(x, i, z, 0x7);
                                } else {
                                    setBlock(x, i, z, 0x2);
                                }
                            } else if (i < height) {
                                if (i < height * 0.75f) {

                                    /*
                                     * Generate stone within the terrain
                                     */
                                    setBlock(x, i, z, 0x3);
                                } else {
                                    /*
                                     * The upper layer is filled with dirt.
                                     */
                                    if (i <= 34 && i >= 28) {
                                        setBlock(x, i, z, 0x7);
                                    } else {
                                        setBlock(x, i, z, 0x2);
                                    }
                                }

                                if (i <= 34 && i >= 28) {
                                    /**
                                     * Generate beach.
                                     */
                                    setBlock(x, i, z, 0x7);
                                }
                            }
                        }
                    }

                    if (i <= 30 && i > 0) {
                        if (getBlock(x, i, z) == 0) {
                            setBlock(x, i, z, 0x4);
                        }
                    }

                    if (i == 0) {
                        setBlock(x, i, z, 0x8);
                    }
                }
            }
        }
    }

    /**
     * Populates the chunk (e.g. placement of trees etc.).
     *
     * TODO: Much to simple and boring :-(
     */
    public void populate() {
        for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
            for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {

                    float dens = calcForestDensity(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z));

                    // Generate grass
                    if (getBlock(x, y, z) == 0x1 && dens > 0.25) {
                        setBlock(x, y + 1, z, 0xB);
                    }

                    // Generate some flowers
                    if (getBlock(x, y, z) == 0x1 && dens > 0.5) {
                        if (_parent.getRand().randomDouble() > 0.5f) {
                            setBlock(x, y + 1, z, 0x9);
                        } else {
                            setBlock(x, y + 1, z, 0xA);
                        }
                    }

                    // Check the distance to the last placed trees
                    if (dens > 0.7 && getBlock(x, y, z) == 0x1 && y > 32) {
                        _parent.generatePineTree(getBlockWorldPosX(x), getBlockWorldPosY((int) y) + 1, getBlockWorldPosZ(z), false);
                    } else if (dens > 0.6f && getBlock(x, y, z) == 0x1 && y > 32) {
                        _parent.generateTree(getBlockWorldPosX(x), getBlockWorldPosY((int) y) + 1, getBlockWorldPosZ(z), false);
                    }
                }
            }
        }
    }

    /**
     * Generates the vertex-, texture- and color-arrays.
     */
    public synchronized void generateVertexArrays() {
        for (int x = 0; x < Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < Configuration.CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < Configuration.CHUNK_DIMENSIONS.z; z++) {
                    generateBlockVertices(x, y, z);
                    generateBillboardVertices(x, y, z);
                }
            }
        }
    }

    private void generateBillboardVertices(int x, int y, int z) {
        int block = _blocks[x][y][z];

        // Ignore invisible blocks like air and normal block!
        if (Block.getBlock(block).isBlockInvisible() || !Block.getBlock(block).isBlockBillboard()) {
            return;
        }

        Vector3f offset = new Vector3f(_position.x * Configuration.CHUNK_DIMENSIONS.x, _position.y * Configuration.CHUNK_DIMENSIONS.y, _position.z * Configuration.CHUNK_DIMENSIONS.z);

        List<Float> quads = new ArrayList<Float>();
        List<Float> tex = new ArrayList<Float>();
        List<Float> color = new ArrayList<Float>();

        /*
         * First side of the billboard
         */
        Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.FRONT);
        float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.FRONT).x;
        float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.FRONT).y;

        color.add(colorOffset.x * _parent.getDaylight());
        color.add(colorOffset.y * _parent.getDaylight());
        color.add(colorOffset.z * _parent.getDaylight());
        color.add(colorOffset.w);

        tex.add(texOffsetX);
        tex.add(texOffsetY + 0.0624f);
        quads.add(-0.5f + x + offset.x);
        quads.add(-0.5f + y + offset.y);
        quads.add(z + offset.z);

        color.add(colorOffset.x * _parent.getDaylight());
        color.add(colorOffset.y * _parent.getDaylight());
        color.add(colorOffset.z * _parent.getDaylight());
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY + 0.0624f);
        quads.add(0.5f + x + offset.x);
        quads.add(-0.5f + y + offset.y);
        quads.add(z + offset.z);

        color.add(colorOffset.x * _parent.getDaylight());
        color.add(colorOffset.y * _parent.getDaylight());
        color.add(colorOffset.z * _parent.getDaylight());
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY);

        quads.add(0.5f + x + offset.x);
        quads.add(0.5f + y + offset.y);
        quads.add(z + offset.z);

        color.add(colorOffset.x * _parent.getDaylight());
        color.add(colorOffset.y * _parent.getDaylight());
        color.add(colorOffset.z * _parent.getDaylight());
        color.add(colorOffset.w);

        tex.add(texOffsetX);
        tex.add(texOffsetY);

        quads.add(-0.5f + x + offset.x);
        quads.add(0.5f + y + offset.y);
        quads.add(z + offset.z);

        /*
         * Second side of the billboard
         */
        colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.BACK);
        texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BACK).x;
        texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BACK).y;

        color.add(colorOffset.x * _parent.getDaylight());
        color.add(colorOffset.y * _parent.getDaylight());
        color.add(colorOffset.z * _parent.getDaylight());
        color.add(colorOffset.w);

        tex.add(texOffsetX);
        tex.add(texOffsetY + 0.0624f);
        quads.add(x + offset.x);
        quads.add(-0.5f + y + offset.y);
        quads.add(-0.5f + z + offset.z);

        color.add(colorOffset.x * _parent.getDaylight());
        color.add(colorOffset.y * _parent.getDaylight());
        color.add(colorOffset.z * _parent.getDaylight());
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY + 0.0624f);
        quads.add(x + offset.x);
        quads.add(-0.5f + y + offset.y);
        quads.add(0.5f + z + offset.z);

        color.add(colorOffset.x * _parent.getDaylight());
        color.add(colorOffset.y * _parent.getDaylight());
        color.add(colorOffset.z * _parent.getDaylight());
        color.add(colorOffset.w);

        tex.add(texOffsetX + 0.0624f);
        tex.add(texOffsetY);

        quads.add(x + offset.x);
        quads.add(0.5f + y + offset.y);
        quads.add(0.5f + z + offset.z);

        color.add(colorOffset.x * _parent.getDaylight());
        color.add(colorOffset.y * _parent.getDaylight());
        color.add(colorOffset.z * _parent.getDaylight());
        color.add(colorOffset.w);

        tex.add(texOffsetX);
        tex.add(texOffsetY);

        quads.add(x + offset.x);
        quads.add(0.5f + y + offset.y);
        quads.add(-0.5f + z + offset.z);

        // All billboards are translucent
        _quadsBillboard.addAll(quads);
        _texBillboard.addAll(tex);
        _colorBillboard.addAll(color);
    }

    private void generateBlockVertices(int x, int y, int z) {
        int block = _blocks[x][y][z];

        // Ignore invisible blocks like air and billboards like flowers!
        if (Block.getBlock(block).isBlockInvisible() || Block.getBlock(block).isBlockBillboard()) {
            return;
        }

        Vector3f offset = new Vector3f(_position.x * Configuration.CHUNK_DIMENSIONS.x, _position.y * Configuration.CHUNK_DIMENSIONS.y, _position.z * Configuration.CHUNK_DIMENSIONS.z);
        boolean drawFront, drawBack, drawLeft, drawRight, drawTop, drawBottom;
        int blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y + 1), getBlockWorldPosZ(z));

        drawTop = isSideVisibleForBlockTypes(blockToCheck, block);

        List<Float> quads = new ArrayList<Float>();
        List<Float> tex = new ArrayList<Float>();
        List<Float> color = new ArrayList<Float>();

        if (drawTop) {
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.TOP);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y + 1), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x, y + 1, z)), Configuration.MIN_LIGHT);

            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.TOP).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.TOP).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(-0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);


        }

        blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1));
        drawFront = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawFront) {
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.FRONT);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1)) * (calcSimpleOcclusionAmount(x, y, z - 1) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT);

            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.FRONT).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.FRONT).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(-0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);


        }

        blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1));
        drawBack = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawBack) {
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.BACK);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1)) * (calcSimpleOcclusionAmount(x, y, z + 1) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT);


            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BACK).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BACK).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);

            quads.add(0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);

            quads.add(-0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);


        }

        blockToCheck = _parent.getBlock(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z));
        drawLeft = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawLeft) {
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.LEFT);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x - 1, y, z) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT);

            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.LEFT).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.LEFT).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);

            quads.add(-0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);

            quads.add(-0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);

        }

        blockToCheck = _parent.getBlock(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z));
        drawRight = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawRight) {
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.RIGHT);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x + 1, y, z) - Configuration.BLOCK_SIDE_DIMMING), Configuration.MIN_LIGHT);

            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.RIGHT).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.RIGHT).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offset.x);
            quads.add(0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);


        }

        blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y - 1), getBlockWorldPosZ(z));
        drawBottom = isSideVisibleForBlockTypes(blockToCheck, block);

        if (drawBottom) {
            Vector4f colorOffset = Block.getBlock(block).getColorOffsetFor(Block.SIDE.BOTTOM);
            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y - 1), getBlockWorldPosZ(z)) * (calcSimpleOcclusionAmount(x, y - 1, z)), Configuration.MIN_LIGHT);

            float texOffsetX = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BOTTOM).x;
            float texOffsetY = Block.getBlock(block).getTextureOffsetFor(Block.SIDE.BOTTOM).y;

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY);
            quads.add(-0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY);
            quads.add(0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(-0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX + 0.0624f);
            tex.add(texOffsetY + 0.0624f);
            quads.add(0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);

            color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
            color.add(colorOffset.w);

            tex.add(texOffsetX);
            tex.add(texOffsetY + 0.0624f);
            quads.add(-0.5f + x + offset.x);
            quads.add(-0.5f + y + offset.y);
            quads.add(0.5f + z + offset.z);


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
     * Generates the display list from the precalculated arrays.
     */
    public synchronized void generateDisplayList() {
        if (_colorOpaque.isEmpty() && _texOpaque.isEmpty() && _quadsOpaque.isEmpty() && _colorTranslucent.isEmpty() && _texTranslucent.isEmpty() && _quadsTranslucent.isEmpty()) {
            return;
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

    private void floodLight(int x, int y, int z) {
        x = getBlockWorldPosX(x);
        y = getBlockWorldPosY(y);
        z = getBlockWorldPosZ(z);

        float val_n1 = Block.getBlock(_parent.getBlock(x + 1, y, z)).isBlockTypeTranslucent() ? _parent.getLight(x + 1, y, z) : -1f;
        float val_n2 = Block.getBlock(_parent.getBlock(x - 1, y, z)).isBlockTypeTranslucent() ? _parent.getLight(x - 1, y, z) : -1f;
        float val_n3 = Block.getBlock(_parent.getBlock(x, y, z + 1)).isBlockTypeTranslucent() ? _parent.getLight(x, y, z + 1) : -1f;
        float val_n4 = Block.getBlock(_parent.getBlock(x, y, z - 1)).isBlockTypeTranslucent() ? _parent.getLight(x, y, z - 1) : -1f;
        float val_n5 = Block.getBlock(_parent.getBlock(x, y + 1, z)).isBlockTypeTranslucent() ? _parent.getLight(x, y + 1, z) : -1f;
        float val_n6 = Block.getBlock(_parent.getBlock(x, y - 1, z)).isBlockTypeTranslucent() ? _parent.getLight(x, y - 1, z) : -1f;

        float val_light = _parent.getLight(x, y, z);
        float val_light_next = Math.max(val_light - 0.0625f, 0f);

        if (val_n1 < val_light_next && val_n1 != -1) {
            _parent.setLight(x + 1, y, z, val_light_next);
        }

        if (val_n2 < val_light_next && val_n2 != -1) {
            _parent.setLight(x - 1, y, z, val_light_next);
        }

        if (val_n3 < val_light_next && val_n3 != -1) {
            _parent.setLight(x, y, z + 1, val_light_next);
        }

        if (val_n4 < val_light_next && val_n4 != -1) {
            _parent.setLight(x, y, z - 1, val_light_next);
        }

        if (val_n5 < val_light_next && val_n5 != -1) {
            _parent.setLight(x, y + 1, z, val_light_next);
        }

        if (val_n6 < val_light_next && val_n6 != -1) {
            _parent.setLight(x, y - 1, z, val_light_next);
        }
    }

    /**
     * Returns the base elevation for the terrain.
     */
    private float calcTerrainElevation(float x, float z) {
        float result = 0.0f;
        result += _parent.getpGen1().noise(0.003f * x, 0.003f, 0.003f * z) * 128f;
        return Math.abs(result);
    }

    /**
     * Returns the roughness for the base terrain.
     */
    private float calcTerrainRoughness(float x, float z) {
        float result = 0.0f;
        result += _parent.getpGen1().noise(0.04f * x, 0.04f, 0.04f * z);
        return result;
    }

    /**
     * Returns the detail level for the base terrain.
     */
    private float calcTerrainDetail(float x, float z) {
        float result = 0.0f;
        result += _parent.getpGen2().noise(0.02f * x, 0.02f, 0.02f * z);
        return result;
    }

    /**
     * Returns the canyon density for the base terrain.
     */
    private float calcCanyonDensity(float x, float y, float z) {
        float result = 0.0f;
        result += _parent.getpGen2().noise(0.01f * x, 0.01f * y, 0.01f * z);
        return (float) Math.abs(result);
    }

    /**
     * Returns the cave density for the base terrain.
     */
    private float calcCaveDensityAt(float x, float y, float z) {
        float result = 0.0f;
        result += _parent.getpGen3().noise(0.06f * x, 0.06f * y, 0.06f * z);
        return result;
    }

    /**
     * Returns the cave density for the base terrain.
     */
    private float calcForestDensity(float x, float y, float z) {
        float result = 0.0f;
        result += _parent.getpGen3().noise(0.8f * x, 0.8f * y, 0.8f * z);
        return result;
    }

    /**
     * Returns the position of the chunk within the world.
     */
    private int getChunkWorldPosX() {
        return (int) _position.x * (int) Configuration.CHUNK_DIMENSIONS.x;
    }

    /**
     * Returns the position of the chunk within the world.
     */
    private int getChunkWorldPosY() {
        return (int) _position.y * (int) Configuration.CHUNK_DIMENSIONS.y;
    }

    /**
     * Returns the position of the chunk within the world.
     */
    private int getChunkWorldPosZ() {
        return (int) _position.z * (int) Configuration.CHUNK_DIMENSIONS.z;
    }

    /**
     * Returns the position of block within the world.
     */
    private int getBlockWorldPosX(int x) {
        return x + getChunkWorldPosX();
    }

    /**
     * Returns the position of block within the world.
     */
    private int getBlockWorldPosY(int y) {
        return y + getChunkWorldPosY();
    }

    /**
     * Returns the position of block within the world.
     */
    private int getBlockWorldPosZ(int z) {
        return z + getChunkWorldPosZ();
    }

    /**
     * Calculates the sunlight.
     */
    public void calcSunlight() {
        for (int x = 0; x < (int) Configuration.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < (int) Configuration.CHUNK_DIMENSIONS.z; z++) {
                calcSunlightAtLocalPos(x, z);
            }
        }
    }

    public void calcSunlightAtLocalPos(int x, int z) {
        float light = Configuration.MAX_LIGHT;

        for (int y = (int) Configuration.CHUNK_DIMENSIONS.y - 1; y >= 0; y--) {
            if (Block.getBlock(_blocks[x][y][z]).isBlockTypeTranslucent()) {
                _light[x][y][z] = light;

                if (_blocks[x][y][z] != 0x0) {
                    light -= Configuration.LIGHT_ABSORPTION;
                    light = Math.max(Configuration.MIN_LIGHT, light);
                }
            } else {
                break;
            }
        }
    }

    public void calcLight() {
        for (int ite = 0; ite < 16; ite++) {
            for (int x = 0; x < (int) Configuration.CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < (int) Configuration.CHUNK_DIMENSIONS.z; z++) {
                    for (int y = (int) Configuration.CHUNK_DIMENSIONS.y - 1; y > 0; y--) {
                        if (getLight(x, y, z) == Configuration.MAX_LIGHT - ite * 0.0625f && Block.getBlock(getBlock(x, y, z)).isBlockTypeTranslucent()) {
                            floodLight(x, y, z);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the amount of blocks with a value greater than zero.
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
     */
    public double calcDistanceToPlayer() {
        double distance = Math.sqrt(Math.pow(_parent.getPlayer().getPosition().x - getChunkWorldPosX(), 2) + Math.pow(_parent.getPlayer().getPosition().z - getChunkWorldPosZ(), 2));
        return distance;
    }

    /*
     * Return the light value at the given position.
     */
    public float getLight(int x, int y, int z) {
        float result = 0f;

        try {
            result = _light[x][y][z];
        } catch (Exception e) {
        }

        return result;
    }

    /*
     * Sets the light value at the given position.
     */
    public void setLight(int x, int y, int z, float intens) {
        try {
            _light[x][y][z] = intens;
            _dirty = true;
            // Mark the neighbors as dirty
            markNeighborsDirty(x, z);
        } catch (Exception e) {
        }
    }

    /*
     * Returns the block value at the given position.
     */
    public int getBlock(int x, int y, int z) {
        try {
            return _blocks[x][y][z];
        } catch (Exception e) {
            return 0;
        }
    }

    /*
     * Sets the block value at the given position.
     */
    public void setBlock(int x, int y, int z, int type) {
        try {
            _blocks[x][y][z] = type;
            _dirty = true;
            // Make the neighbors as dirty
            markNeighborsDirty(x, z);
        } catch (Exception e) {
        }
    }

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

    public Chunk[] getNeighbors() {
        Chunk[] chunks = new Chunk[4];
        chunks[0] = _parent.getChunk((int) _position.x + 1, (int) _position.y, (int) _position.z);
        chunks[1] = _parent.getChunk((int) _position.x - 1, (int) _position.y, (int) _position.z);
        chunks[2] = _parent.getChunk((int) _position.x, (int) _position.y, (int) _position.z + 1);
        chunks[3] = _parent.getChunk((int) _position.x, (int) _position.y, (int) _position.z - 1);
        return chunks;
    }

    public void markNeighborsDirty(int x, int z) {
        Chunk[] neighbors = getNeighbors();

        if (x == 0) {
            neighbors[1]._dirty = true;
        }

        if (x == Configuration.CHUNK_DIMENSIONS.x - 1) {
            neighbors[0]._dirty = true;
        }

        if (z == 0) {
            neighbors[3]._dirty = true;
        }

        if (z == Configuration.CHUNK_DIMENSIONS.z - 1) {
            neighbors[2]._dirty = true;
        }
    }
}
