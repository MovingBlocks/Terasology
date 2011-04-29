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

import java.nio.FloatBuffer;
import java.util.List;
import java.util.ArrayList;
import org.lwjgl.util.vector.Vector3f;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.opengl.Texture;
import java.io.FileInputStream;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.TextureLoader;
import static org.lwjgl.opengl.GL11.*;

/**
 * Chunks are the basic components of the world. Each chunk contains a fixed amount of blocks
 * determined by the dimensions. Chunks a used to manage the world efficiently and to
 * reduce the batch count.
 *
 * The default size of chunk is 16x128x16 (32768).
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Chunk extends RenderableObject implements Comparable<Chunk> {

    public boolean _dirty = true;
    private boolean _fresh = true;


    public static enum UPDATE_TYPE {

        LIGHT, BLOCK
    }
    /*
     * Global parameters for the chunks.
     */
    public static final Vector3f CHUNK_DIMENSIONS = new Vector3f(16, 128, 16);

    /*
     * Used to generate the ID of the chunks.
     */
    public static int maxChunkID = 0;

    /*
     * The texture atlas.
     */
    private static Texture _textureMap;

    /*
     * The ID of this chunk.
     */
    int _chunkID = -1;

    /*
     * Vertex, texture and color arrays used to generate the display list.
     */
    private final List<Float> _quads = new ArrayList<Float>();
    private final List<Float> _tex = new ArrayList<Float>();
    private final List<Float> _color = new ArrayList<Float>();

    /*
     * The parent world.
     */
    private World _parent = null;

    /*
     * The actual block-, sunlight- and light-values.
     */
    private int[][][] _blocks;
    private float[][][] _light;
    /*
     * The display list used for displaying the chunk.
     */
    private int _displayList = -1;

    enum SIDE {

        LEFT, RIGHT, TOP, BOTTOM, FRONT, BACK;
    };

    /**
     * Init. the textures used within chunks.
     */
    public static void init() {
        try {
            _textureMap = TextureLoader.getTexture("PNG", new FileInputStream(Chunk.class.getResource("/com/github/begla/blockmania/images/Terrain.png").getPath()), GL_NEAREST);
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
        _blocks = new int[(int) CHUNK_DIMENSIONS.x][(int) CHUNK_DIMENSIONS.y][(int) CHUNK_DIMENSIONS.z];
        _light = new float[(int) CHUNK_DIMENSIONS.x][(int) CHUNK_DIMENSIONS.y][(int) CHUNK_DIMENSIONS.z];

        _chunkID = maxChunkID + 1;
        maxChunkID++;
    }

    /**
     * Draws the chunk.
     */
    @Override
    public void render() {

        /*
         * Draws the outline of each chunk.
         */
        if (Configuration.SHOW_CHUNK_OUTLINES) {
            glLineWidth(2.0f);
            glColor3f(255.0f, 255.0f, 255.0f);

            glPushMatrix();
            glTranslatef(_position.x * (int) CHUNK_DIMENSIONS.x, _position.y * (int) CHUNK_DIMENSIONS.y, _position.z * (int) CHUNK_DIMENSIONS.z);

            glBegin(GL_LINE_LOOP);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(CHUNK_DIMENSIONS.x, 0.0f, 0.0f);
            glVertex3f(CHUNK_DIMENSIONS.x, CHUNK_DIMENSIONS.y, 0.0f);
            glVertex3f(0.0f, CHUNK_DIMENSIONS.y, 0.0f);
            glEnd();

            glBegin(GL_LINE_LOOP);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glVertex3f(0.0f, 0.0f, CHUNK_DIMENSIONS.z);
            glVertex3f(0.0f, CHUNK_DIMENSIONS.y, CHUNK_DIMENSIONS.z);
            glVertex3f(0.0f, CHUNK_DIMENSIONS.y, 0.0f);
            glVertex3f(0.0f, 0.0f, 0.0f);
            glEnd();

            glBegin(GL_LINE_LOOP);
            glVertex3f(0.0f, 0.0f, CHUNK_DIMENSIONS.z);
            glVertex3f(CHUNK_DIMENSIONS.x, 0.0f, CHUNK_DIMENSIONS.z);
            glVertex3f(CHUNK_DIMENSIONS.x, CHUNK_DIMENSIONS.y, CHUNK_DIMENSIONS.z);
            glVertex3f(0.0f, CHUNK_DIMENSIONS.y, CHUNK_DIMENSIONS.z);
            glVertex3f(0.0f, 0.0f, CHUNK_DIMENSIONS.z);
            glEnd();

            glBegin(GL_LINE_LOOP);
            glVertex3f(CHUNK_DIMENSIONS.x, 0.0f, 0.0f);
            glVertex3f(CHUNK_DIMENSIONS.x, 0.0f, CHUNK_DIMENSIONS.z);
            glVertex3f(CHUNK_DIMENSIONS.x, CHUNK_DIMENSIONS.y, CHUNK_DIMENSIONS.z);
            glVertex3f(CHUNK_DIMENSIONS.x, CHUNK_DIMENSIONS.y, 0.0f);
            glVertex3f(CHUNK_DIMENSIONS.x, 0.0f, 0.0f);
            glEnd();
            glPopMatrix();
        }

        _textureMap.bind();
        glEnable(GL_TEXTURE_2D);
        glCallList(_displayList);
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
        int xOffset = (int) _position.x * (int) CHUNK_DIMENSIONS.x;
        int yOffset = (int) _position.y * (int) CHUNK_DIMENSIONS.y;
        int zOffset = (int) _position.z * (int) CHUNK_DIMENSIONS.z;

        for (int x = 0; x < Chunk.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Chunk.CHUNK_DIMENSIONS.z; z++) {
                int height = (int) (calcTerrainElevation(x + xOffset, z + zOffset) + (calcTerrainRoughness(x + xOffset, z + zOffset) * calcTerrainDetail(x + xOffset, z + zOffset)) * 64);

                for (int i = (int) CHUNK_DIMENSIONS.y; i > 0; i--) {
                    if (calcCaveDensityAt(x + xOffset, i, z + zOffset) < 0.5 && calcCanyonDensity(x + xOffset, i + yOffset, z + zOffset) < 0.5f) {
                        if (i == height) {
                            /*
                             * Grass covers the terrain.
                             */
                            if (i != 32) {
                                setBlock(x, i, z, 0x1);
                            } else {
                                setBlock(x, i, z, 0x7);
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
                                setBlock(x, i, z, 0x2);
                            }

                            if (i <= 34 && i >= 32) {
                                /**
                                 * Generate "beach".
                                 */
                                setBlock(x, i, z, 0x7);
                            }
                        }
                    }

                    if (i <= 32) {
                        if (getBlock(x, i, z) == 0) {
                            setBlock(x, i, z, 0x4);
                        }
                    }
                }
            }
        }
    }

    /**
     * Populates the chunk (e.g. placement of trees etc.).
     *
     * TODO: Much to simple and boring
     */
    public void populate() {
        for (int y = 0; y < Chunk.CHUNK_DIMENSIONS.y; y++) {
            for (int x = 0; x < Chunk.CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < Chunk.CHUNK_DIMENSIONS.z; z++) {
                    float dens = calcForestDensity(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z));
                    if (dens > 0.55 && dens < 0.7f && getBlock(x, y, z) == 0x1 && y > 32) {
                        _parent.generateTree(getBlockWorldPosX(x), getBlockWorldPosY((int) y) + 1, getBlockWorldPosZ(z), false);
                        return;
                    } else if (dens >= 0.7f && getBlock(x, y, z) == 0x1 && y > 32) {
                        _parent.generatePineTree(getBlockWorldPosX(x), getBlockWorldPosY((int) y) + 1, getBlockWorldPosZ(z), false);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Generates the vertex-, texture- and color-arrays.
     */
    public synchronized void generateVertexArray() {
        _color.clear();
        _quads.clear();
        _tex.clear();

        Vector3f offset = new Vector3f(_position.x * CHUNK_DIMENSIONS.x, _position.y * CHUNK_DIMENSIONS.y, _position.z * CHUNK_DIMENSIONS.z);

        for (int x = 0; x < CHUNK_DIMENSIONS.x; x++) {
            for (int y = 0; y < CHUNK_DIMENSIONS.y; y++) {
                for (int z = 0; z < CHUNK_DIMENSIONS.z; z++) {

                    int block = _blocks[x][y][z];

                    if (block > 0) {

                        boolean drawFront, drawBack, drawLeft, drawRight, drawTop, drawBottom;
                        int blockToCheck = 0;

                        blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y + 1), getBlockWorldPosZ(z));
                        drawTop = checkBlockTypeToDraw(blockToCheck, block);

                        if (drawTop) {
                            Vector3f colorOffset = Helper.getInstance().getColorOffsetFor(block, Helper.SIDE.TOP);
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y + 1), getBlockWorldPosZ(z)) - (calcSimpleOcclusionAmount(x, y + 1, z) * Configuration.DIMMING_INTENS), Configuration.MIN_LIGHT);

                            float texOffsetX = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.TOP).x;
                            float texOffsetY = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.TOP).y;

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY);
                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY);
                            _quads.add(0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);
                        }

                        blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1));
                        drawFront = checkBlockTypeToDraw(blockToCheck, block);

                        if (drawFront) {
                            Vector3f colorOffset = Helper.getInstance().getColorOffsetFor(block, Helper.SIDE.FRONT);
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1)) - Configuration.BLOCK_SIDE_DIMMING - (calcSimpleOcclusionAmount(x, y, z - 1) * Configuration.DIMMING_INTENS), Configuration.MIN_LIGHT);

                            float texOffsetX = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.FRONT).x;
                            float texOffsetY = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.FRONT).y;

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY);
                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY);
                            _quads.add(0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);
                        }

                        blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1));
                        drawBack = checkBlockTypeToDraw(blockToCheck, block);

                        if (drawBack) {
                            Vector3f colorOffset = Helper.getInstance().getColorOffsetFor(block, Helper.SIDE.BACK);
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1)) - Configuration.BLOCK_SIDE_DIMMING - (calcSimpleOcclusionAmount(x, y, z + 1) * Configuration.DIMMING_INTENS), Configuration.MIN_LIGHT);


                            float texOffsetX = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.BACK).x;
                            float texOffsetY = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.BACK).y;

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY);

                            _quads.add(0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY);

                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);
                        }

                        blockToCheck = _parent.getBlock(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z));
                        drawLeft = checkBlockTypeToDraw(blockToCheck, block);

                        if (drawLeft) {
                            Vector3f colorOffset = Helper.getInstance().getColorOffsetFor(block, Helper.SIDE.LEFT);
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) - Configuration.BLOCK_SIDE_DIMMING - (calcSimpleOcclusionAmount(x - 1, y, z) * Configuration.DIMMING_INTENS), Configuration.MIN_LIGHT);

                            float texOffsetX = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.LEFT).x;
                            float texOffsetY = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.LEFT).y;

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY);

                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY);

                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);
                        }

                        blockToCheck = _parent.getBlock(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z));
                        drawRight = checkBlockTypeToDraw(blockToCheck, block);

                        if (drawRight) {
                            Vector3f colorOffset = Helper.getInstance().getColorOffsetFor(block, Helper.SIDE.RIGHT);
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) - Configuration.BLOCK_SIDE_DIMMING - (calcSimpleOcclusionAmount(x + 1, y, z) * Configuration.DIMMING_INTENS), Configuration.MIN_LIGHT);

                            float texOffsetX = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.RIGHT).x;
                            float texOffsetY = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.RIGHT).y;

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY);
                            _quads.add(0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY);
                            _quads.add(0.5f + x + offset.x);
                            _quads.add(0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);
                        }

                        blockToCheck = _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y - 1), getBlockWorldPosZ(z));
                        drawBottom = checkBlockTypeToDraw(blockToCheck, block);

                        if (drawBottom) {
                            Vector3f colorOffset = Helper.getInstance().getColorOffsetFor(block, Helper.SIDE.BOTTOM);
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y - 1), getBlockWorldPosZ(z)) - Configuration.BLOCK_SIDE_DIMMING - (calcSimpleOcclusionAmount(x, y - 1, z) * Configuration.DIMMING_INTENS), Configuration.MIN_LIGHT);

                            float texOffsetX = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.BOTTOM).x;
                            float texOffsetY = Helper.getInstance().getTextureOffsetFor(block, Helper.SIDE.BOTTOM).y;

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY);
                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY);
                            _quads.add(0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(-0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX + 0.0625f);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);

                            _color.add(colorOffset.x * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.y * shadowIntens * _parent.getDaylight());
                            _color.add(colorOffset.z * shadowIntens * _parent.getDaylight());
                            _tex.add(texOffsetX);
                            _tex.add(texOffsetY + 0.0625f);
                            _quads.add(-0.5f + x + offset.x);
                            _quads.add(-0.5f + y + offset.y);
                            _quads.add(0.5f + z + offset.z);
                        }
                    }

                }
            }
        }
    }

    /**
     * Generates the display list from the precalculated arrays.
     */
    public synchronized void generateDisplayList() {
        if (_color.isEmpty() && _tex.isEmpty() && _quads.isEmpty()) {
            return;
        }

        if (glIsList(_displayList)) {
            glDeleteLists(_displayList, 1);
        }

        _displayList = glGenLists(1);

        FloatBuffer cb = null;
        FloatBuffer tb = null;
        FloatBuffer vb = null;

        vb = BufferUtils.createFloatBuffer(_quads.size());

        for (Float f : _quads) {
            vb.put(f);
        }

        tb = BufferUtils.createFloatBuffer(_tex.size());

        for (Float f : _tex) {
            tb.put(f);
        }

        cb = BufferUtils.createFloatBuffer(_color.size());

        for (Float f : _color) {
            cb.put(f);
        }
        vb.flip();
        tb.flip();
        cb.flip();

        glNewList(_displayList, GL_COMPILE);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glTexCoordPointer(2, 0, tb);
        glColorPointer(3, 0, cb);
        glVertexPointer(3, 0, vb);
        glDrawArrays(GL_QUADS, 0, _quads.size() / 3);
        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
        glEndList();

        _quads.clear(); _tex.clear(); _color.clear();
    }

    /**
     * Returns false if the block type is "solid".
     */
    private boolean checkBlockTypeToDraw(int blockToCheck, int currentBlock) {
        return (blockToCheck == 0 || (Helper.getInstance().isBlockTypeTranslucent(blockToCheck) && !Helper.getInstance().isBlockTypeTranslucent(currentBlock)));
    }

    private void floodLight(int x, int y, int z) {

        x = getBlockWorldPosX(x);
        y = getBlockWorldPosY(y);
        z = getBlockWorldPosZ(z);

        float val_n1 = _parent.getBlock(x + 1, y, z) == 0 ? _parent.getLight(x + 1, y, z) : -1f;
        float val_n2 = _parent.getBlock(x - 1, y, z) == 0 ? _parent.getLight(x - 1, y, z) : -1f;
        float val_n3 = _parent.getBlock(x, y, z + 1) == 0 ? _parent.getLight(x, y, z + 1) : -1f;
        float val_n4 = _parent.getBlock(x, y, z - 1) == 0 ? _parent.getLight(x, y, z - 1) : -1f;
        float val_n5 = _parent.getBlock(x, y + 1, z) == 0 ? _parent.getLight(x, y + 1, z) : -1f;
        float val_n6 = _parent.getBlock(x, y - 1, z) == 0 ? _parent.getLight(x, y - 1, z) : -1f;

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
        result += _parent.getpGen1().noise(0.003f * x, 0.003f, 0.003f * z) * 170f;
        return Math.abs(result);
    }

    /**
     * Returns the roughness for the base terrain.
     */
    private float calcTerrainRoughness(float x, float z) {
        float result = 0.0f;
        result += _parent.getpGen1().noise(0.06f * x, 0.06f, 0.06f * z);
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
        return result;
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
        result += _parent.getpGen1().noise(0.6f * x, 0.6f * y, 0.6f * z);
        return result;
    }

    /**
     * Returns the position of the chunk within the world.
     */
    private int getChunkWorldPosX() {
        return (int) _position.x * (int) CHUNK_DIMENSIONS.x;
    }

    /**
     * Returns the position of the chunk within the world.
     */
    private int getChunkWorldPosY() {
        return (int) _position.y * (int) CHUNK_DIMENSIONS.y;
    }

    /**
     * Returns the position of the chunk within the world.
     */
    private int getChunkWorldPosZ() {
        return (int) _position.z * (int) CHUNK_DIMENSIONS.z;
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
        for (int x = 0; x < (int) CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < (int) CHUNK_DIMENSIONS.z; z++) {
                for (int y = (int) CHUNK_DIMENSIONS.y - 1; y > 0; y--) {
                    if (_blocks[x][y][z] == 0) {
                        _light[x][y][z] = Configuration.MAX_LIGHT;
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public void calcLight() {
        for (int ite = 0; ite < 16; ite++) {
            for (int x = 0; x < (int) CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < (int) CHUNK_DIMENSIONS.z; z++) {
                    for (int y = (int) CHUNK_DIMENSIONS.y - 1; y > 0; y--) {
                        if (getLight(x, y, z) == Configuration.MAX_LIGHT - ite * 0.0625f && getBlock(x, y, z) == 0) {
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

        for (int x = 0; x < (int) CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < (int) CHUNK_DIMENSIONS.z; z++) {
                for (int y = 0; y < (int) CHUNK_DIMENSIONS.y; y++) {
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

            // Make the neighbors as dirty
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
        float intens = 0;
        if (_parent.getBlock(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) > 0) {
            intens++;
        }
        if (_parent.getBlock(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) > 0) {
            intens++;
        }
        if (_parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1)) > 0) {
            intens++;
        }
        if (_parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1)) > 0) {
            intens++;
        }
        return intens;
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

        if (x == CHUNK_DIMENSIONS.x - 1) {
            neighbors[0]._dirty = true;
        }

        if (z == 0) {
            neighbors[3]._dirty = true;
        }

        if (z == CHUNK_DIMENSIONS.z - 1) {
            neighbors[2]._dirty = true;
        }
    }
}
