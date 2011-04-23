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
import java.util.Random;
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
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Chunk extends RenderObject implements Comparable<Chunk> {

    /*
     * Paramters for the world illumination.
     */
    private static final float MAX_LIGHT = 1.0f;
    private static final float MIN_LIGHT = 0.1f;
    private static final float DIMMING_INTENS = 0.075f;
    private static final float BLOCK_SIDE_DIMMING = 0.075f;

    /*
     * TODO
     */
    private boolean _dirty;

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
     * Random  number generator used for the terrain generation.
     */
    private static final Random _rand = new Random();

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
            _textureMap.bind();

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
        clear();
    }

    /**
     * Draws the chunk.
     */
    @Override
    public void render() {

        /*
         * Draws the outline of each chunk.
         */
        if (Configuration._showChunkOutlines) {
            glPushMatrix();
            glTranslatef(_position.x * (int) CHUNK_DIMENSIONS.x, _position.y * (int) CHUNK_DIMENSIONS.y, _position.z * (int) CHUNK_DIMENSIONS.z);
            glColor3f(255.0f, 0.0f, 0.0f);
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
        return String.format("Chunk (%d) cotaining %d Blocks.", _chunkID, blockCount());
    }

    /**
     * Generates the terrain within this chunk.
     */
    public void generate() {

        clear();

        int xOffset = (int) _position.x * (int) CHUNK_DIMENSIONS.x;
        int yOffset = (int) _position.y * (int) CHUNK_DIMENSIONS.y;
        int zOffset = (int) _position.z * (int) CHUNK_DIMENSIONS.z;

        for (int x = 0; x < Chunk.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Chunk.CHUNK_DIMENSIONS.z; z++) {
                int height = (int) (calcTerrainElevation(x + xOffset, z + zOffset) + (calcTerrainRoughness(x + xOffset, z + zOffset) * calcTerrainDetail(x + xOffset, z + zOffset)) * 64) + 64;

                for (int i = (int) CHUNK_DIMENSIONS.y; i >= 0; i--) {
                    if (calcCaveDensityAt(x + xOffset, i, z + zOffset) < 0.5 && calcCanyonDensity(x + xOffset, i + yOffset, z + zOffset) < 0.5) {
                        if (i == height) {
                            // Block has air "on top" => Dirt!
                            setBlock(x, i, z, 0x1);
                        } else if (i < height) {
                            setBlock(x, i, z, 0x2);

                            if (height - i < height * 0.75f) {
                                setBlock(x, i, z, 0x3);
                            }
                        }
                    }

                    if (i < 32) {
                        if (getBlock(x, i, z) == 0) {
                            setBlock(x, i, z, 0x4);
                        }

                    }
                }
            }
        }

        _dirty = true;
    }

    /**
     * Populates the chunk (e.g. placement of trees etc.).
     */
    public void populate() {
        for (int x = 0; x < Chunk.CHUNK_DIMENSIONS.x; x++) {
            for (int z = 0; z < Chunk.CHUNK_DIMENSIONS.z; z++) {
                for (int y = 32; y < Chunk.CHUNK_DIMENSIONS.y; y++) {
                    if (_parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z)) == 0x1 && _rand.nextFloat() < 0.009f) {
                        if (_rand.nextBoolean()) {
                            _parent.generateTree(getBlockWorldPosX(x), getBlockWorldPosY((int) y) + 1, getBlockWorldPosZ(z));
                        } else {
                            _parent.generatePineTree(getBlockWorldPosX(x), getBlockWorldPosY((int) y) + 1, getBlockWorldPosZ(z));
                        }
                        return;
                    }
                }
            }
        }

        _dirty = true;
    }

    /**
     * Generates the vertex-, texture- and color-arrays.
     */
    public void generateVertexArray() {
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
                            ;
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y + 1), getBlockWorldPosZ(z)) - (dimBlockAtLocalPos(x, y + 1, z) ? DIMMING_INTENS : 0.0f), MIN_LIGHT);

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
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1)) - BLOCK_SIDE_DIMMING - (dimBlockAtLocalPos(x, y, z - 1) ? DIMMING_INTENS : 0.0f), MIN_LIGHT);


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
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1)) - BLOCK_SIDE_DIMMING - (dimBlockAtLocalPos(x, y, z + 1) ? DIMMING_INTENS : 0.0f), MIN_LIGHT);


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
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) - BLOCK_SIDE_DIMMING - (dimBlockAtLocalPos(x - 1, y, z) ? DIMMING_INTENS : 0.0f), MIN_LIGHT);

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
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) - BLOCK_SIDE_DIMMING - (dimBlockAtLocalPos(x + 1, y, z) ? DIMMING_INTENS : 0.0f), MIN_LIGHT);

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
                            float shadowIntens = Math.max(_parent.getLight(getBlockWorldPosX(x), getBlockWorldPosY(y - 1), getBlockWorldPosZ(z)) - BLOCK_SIDE_DIMMING - (dimBlockAtLocalPos(x, y - 1, z) ? DIMMING_INTENS : 0.0f), MIN_LIGHT);

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
    public void generateDisplayList() {

        if (_chunkID == -1) {
            _chunkID = maxChunkID + 1;
            maxChunkID++;
            _displayList = glGenLists(1);
        }

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

        _tex.clear();

        cb = BufferUtils.createFloatBuffer(_color.size());

        for (Float f : _color) {
            cb.put(f);
        }

        _color.clear();

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

        _quads.clear();

        vb = null;
        tb = null;
        cb = null;

        _dirty = false;

    }

    /**
     * Clears all chunk values.
     */
    public void clear() {
        _blocks = new int[(int) CHUNK_DIMENSIONS.x][(int) CHUNK_DIMENSIONS.y][(int) CHUNK_DIMENSIONS.z];
        _light = new float[(int) CHUNK_DIMENSIONS.x][(int) CHUNK_DIMENSIONS.y][(int) CHUNK_DIMENSIONS.z];
    }

    /**
     * Returns false if the block type is "solid".
     */
    private boolean checkBlockTypeToDraw(int blockToCheck, int currentBlock) {
        return (blockToCheck == 0 || (Helper.getInstance().isBlockTypeTranslucent(blockToCheck) && !Helper.getInstance().isBlockTypeTranslucent(currentBlock)));
    }

    private void floodLightAtWorldPos(int x, int y, int z) {

        /*
         * Check which neighbors have a lower light value than the current light value minus one light step.
         */
        float val_n1 = _parent.getLight(x + 1, y, z);
        float val_n2 = _parent.getLight(x - 1, y, z);
        float val_n3 = _parent.getLight(x, y, z + 1);
        float val_n4 = _parent.getLight(x, y, z - 1);
        float val_n5 = _parent.getLight(x, y + 1, z);
        float val_n6 = _parent.getLight(x, y - 1, z);
        /*
         * Get light value for the current block.
         */
        float val_light = Math.max(0f, _parent.getLight(x, y, z));
        float val_light_next = Math.max(val_light - 0.0625f, 0f);
        /*
         * Check the neighbors and recursively flood those.
         */
        if (val_n1 < val_light_next && _parent.getBlock(x + 1, y, z) == 0) {
            _parent.setLight(x + 1, y, z, val_light_next);
        }


        if (val_n2 < val_light_next && _parent.getBlock(x - 1, y, z) == 0) {
            _parent.setLight(x - 1, y, z, val_light_next);
        }


        if (val_n3 < val_light_next && _parent.getBlock(x, y, z + 1) == 0) {
            _parent.setLight(x, y, z + 1, val_light_next);
        }

        if (val_n4 < val_light_next && _parent.getBlock(x, y, z - 1) == 0) {
            _parent.setLight(x, y, z - 1, val_light_next);
        }


        if (val_n5 < val_light_next && _parent.getBlock(x, y + 1, z) == 0) {
            _parent.setLight(x, y + 1, z, val_light_next);
        }

        if (val_n6 < val_light_next && _parent.getBlock(x, y - 1, z) == 0) {
            _parent.setLight(x, y - 1, z, val_light_next);
        }
    }

    /**
     * Returns the base elevation for the terrain.
     */
    private float calcTerrainElevation(float x, float z) {
        float result = 0.0f;
        result += _parent.getpGen1().noise(0.002f * x, 0.002f, 0.002f * z) * 80;
        return result;
    }

    /**
     * Returns the roughness for the base terrain.
     */
    private float calcTerrainRoughness(float x, float z) {
        float result = 0.0f;
        result += _parent.getpGen1().noise(0.01f * x, 0.01f, 0.01f * z);
        return Math.abs(result);
    }

    /**
     * Returns the detail level for the base terrain.
     */
    private float calcTerrainDetail(float x, float z) {
        float result = 0.0f;
        result += _parent.getpGen1().noise(0.08f * x, 0.08f, 0.08f * z);
        return Math.abs(result);
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
                calcSunlightAtLocalPos(x, z);
            }
        }
    }

    public void calcSunlightAtLocalPos(int x, int z) {
        boolean covered = false;
        for (int y = (int) CHUNK_DIMENSIONS.y - 1; y > 0; y--) {
            if (_blocks[x][y][z] == 0 && !covered) {
                _light[x][y][z] = MAX_LIGHT;
            } else if (_blocks[x][y][z] == 0 && covered) {
                _light[x][y][z] = 0.0f;
            } else {
                covered = true;
            }
        }
    }

    /**
     * Calculates the flooded lighting based on the precalculated sunlight.
     */
    public void calcLight() {
        for (int ite = 0; ite < 16; ite++) {
            for (int x = 0; x < (int) CHUNK_DIMENSIONS.x; x++) {
                for (int z = 0; z < (int) CHUNK_DIMENSIONS.z; z++) {
                    for (int y = (int) CHUNK_DIMENSIONS.y - 1; y > 0; y--) {
                        if (_blocks[x][y][z] == 0 && _light[x][y][z] == MAX_LIGHT - ite * 0.0625f) {
                            floodLightAtWorldPos(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z));
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the amount of blocks with a value greater as zero.
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
    private double calcDistanceToPlayer() {
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
        } catch (Exception e) {
        }

        _dirty = true;
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
        } catch (Exception e) {
        }
    }

    public boolean dimBlockAtLocalPos(int x, int y, int z) {
        if (_parent.getBlock(getBlockWorldPosX(x + 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) > 0 || _parent.getBlock(getBlockWorldPosX(x - 1), getBlockWorldPosY(y), getBlockWorldPosZ(z)) > 0 || _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z + 1)) > 0 || _parent.getBlock(getBlockWorldPosX(x), getBlockWorldPosY(y), getBlockWorldPosZ(z - 1)) > 0) {
            return true;
        }
        return false;
    }

    public void markDirty() {
        _dirty = true;
    }

    public boolean isDirty() {
        return _dirty;
    }
}
