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
package blockmania;

import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import java.nio.IntBuffer;
import org.lwjgl.util.vector.Vector3f;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.opengl.Texture;
import java.io.FileInputStream;
import org.newdawn.slick.opengl.TextureLoader;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Chunk extends RenderObject {

    static IntBuffer renderBuffer = BufferUtils.createIntBuffer(49152);
    //
    Random rand = new Random();
    //
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    // The actual block ids for the chunk
    int[][][] blocks;
    // Chunk
    int chunkID = -1;
    // Create an unique id for each chunk
    int displayListOpaqueFront = -1;
    int displayListOpaqueBack = -1;
    int displayListOpaqueLeft = -1;
    int displayListOpaqueRight = -1;
    int displayListOpaqueBottom = -1;
    int displayListOpaqueTop = -1;
    static Set<Chunk> chunks = new HashSet<Chunk>();
    // Texture map
    static Texture textureMap;
    // Size of one single chunk
    public static final Vector3f chunkDimensions = new Vector3f(16, 128, 16);
    // The parent world
    World parent = null;

    enum SIDE {

        LEFT, RIGHT, TOP, BOTTOM, FRONT, BACK;
    };

    public Chunk(World parent, Vector3f position) {
        this.position = position;
        this.parent = parent;

        blocks = new int[(int) chunkDimensions.x][(int) chunkDimensions.y][(int) chunkDimensions.z];
    }

    @Override
    public String toString() {
        int counter = 0;

        for (int x = 0; x < chunkDimensions.x; x++) {
            for (int y = 0; y < chunkDimensions.y; y++) {
                for (int z = 0; z < chunkDimensions.z; z++) {
                    if (blocks[x][y][z] > 0) {
                        counter++;
                    }
                }
            }
        }

        return counter + " Blocks in this chunk.";
    }

    public boolean updateDisplayList() {

        long timeStart = System.currentTimeMillis();

        if (chunkID == -1) {
            chunkID = rand.nextInt();

            displayListOpaqueFront = glGenLists(1);
            displayListOpaqueBack = glGenLists(1);
            displayListOpaqueLeft = glGenLists(1);
            displayListOpaqueRight = glGenLists(1);
            displayListOpaqueBottom = glGenLists(1);
            displayListOpaqueTop = glGenLists(1);

            chunks.add(this);
        }

        glNewList(displayListOpaqueFront, GL_COMPILE);
        glBegin(GL_QUADS);

        generateDisplayList(SIDE.FRONT);

        glEnd();
        glEndList();

        glNewList(displayListOpaqueBack, GL_COMPILE);
        glBegin(GL_QUADS);

        generateDisplayList(SIDE.BACK);

        glEnd();
        glEndList();

        glNewList(displayListOpaqueTop, GL_COMPILE);
        glBegin(GL_QUADS);

        generateDisplayList(SIDE.TOP);

        glEnd();
        glEndList();

        glNewList(displayListOpaqueBottom, GL_COMPILE);
        glBegin(GL_QUADS);

        generateDisplayList(SIDE.BOTTOM);

        glEnd();
        glEndList();

        glNewList(displayListOpaqueLeft, GL_COMPILE);
        glBegin(GL_QUADS);

        generateDisplayList(SIDE.LEFT);

        glEnd();
        glEndList();

        glNewList(displayListOpaqueRight, GL_COMPILE);
        glBegin(GL_QUADS);

        generateDisplayList(SIDE.RIGHT);

        glEnd();
        glEndList();

        //LOGGER.log(Level.INFO, "Updated chunk in {0} ms", System.currentTimeMillis() - timeStart);

        return true;
    }

    public static void renderAllChunks() {

        long timeStart = System.currentTimeMillis();


        for (Chunk c : chunks) {
            Vector3f pPos = c.parent.getPlayer().getPosition();
            Vector3f offset = new Vector3f(c.position.x * chunkDimensions.x, c.position.y * chunkDimensions.y, c.position.z * chunkDimensions.z);
            Vector3f distance = new Vector3f(offset.x - pPos.y, offset.y - pPos.x, offset.z - pPos.z);

            if (distance.x + distance.y + distance.z < 128) {
                renderBuffer.put(c.displayListOpaqueTop);
                renderBuffer.put(c.displayListOpaqueFront);
                renderBuffer.put(c.displayListOpaqueBack);
                renderBuffer.put(c.displayListOpaqueLeft);
                renderBuffer.put(c.displayListOpaqueRight);
                renderBuffer.put(c.displayListOpaqueBottom);
            }
            
        }

        renderBuffer.rewind();

        // And then the translucent elements
        //glEnable(GL_BLEND);

        glCallLists(renderBuffer);
        //glDisable(GL_BLEND);

        //LOGGER.log(Level.INFO, "Rendered chunks in {0} ms", System.currentTimeMillis() - timeStart);

    }

    public static void init() {
        try {
            textureMap = TextureLoader.getTexture("PNG", new FileInputStream(Chunk.class.getResource("Terrain.png").getPath()), GL_NEAREST);
            textureMap.bind();

        } catch (IOException ex) {
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    public int getBlock(int x, int y, int z) {
        return blocks[x][y][z];
    }

    public void setBlock(Vector3f pos, int type) {
        blocks[(int) pos.x][(int) pos.y][(int) pos.z] = type;
    }

    public Vector3f getBlockWorldPos(Vector3f pos) {
        Vector3f v = new Vector3f(pos.x + position.x * chunkDimensions.x, pos.y + position.y * chunkDimensions.y, pos.z + position.z * chunkDimensions.z);
        return v;
    }

    private float castRay(int x, int y, int z, SIDE side) {

        float result = 1.0f;

        if (side == SIDE.TOP) {
        } else if (side == SIDE.LEFT) {
            x -= 1;
        } else if (side == SIDE.RIGHT) {
            x += 1;
        } else if (side == SIDE.BOTTOM) {
            return 0.25f;
        } else if (side == SIDE.FRONT) {
            z -= 1;
        } else if (side == SIDE.BACK) {
            z += 1;
        }

        for (int i = y + 1; i < chunkDimensions.y; i++) {
            try {
                if (parent.getBlock(getBlockWorldPos(new Vector3f(x, i, z))) > 0) {
                    result = 0.25f;
                    break;
                }
            } catch (Exception e) {
            }
        }

        try {
            if (parent.getBlock(getBlockWorldPos(new Vector3f(x + 1, y + 1, z))) > 0) {
                result -= 0.33f;
            }
        } catch (Exception e) {
        }

        try {
            if (parent.getBlock(getBlockWorldPos(new Vector3f(x - 1, y + 1, z))) > 0) {
                result -= 0.33f;
            }
        } catch (Exception e) {
        }

        try {
            if (parent.getBlock(getBlockWorldPos(new Vector3f(x, y + 1, z + 1))) > 0) {
                result -= 0.33f;
            }
        } catch (Exception e) {
        }

        try {
            if (parent.getBlock(getBlockWorldPos(new Vector3f(x, y + 1, z - 1))) > 0) {
                result -= 0.33f;
            }
        } catch (Exception e) {
        }

        return result;
    }

    private boolean checkBlockTypeToDraw(int blockToCheck, int currentBlock) {
        return (blockToCheck == 0 || (BlockHelper.isBlockTypeTranslucent(blockToCheck) && !BlockHelper.isBlockTypeTranslucent(currentBlock)));
    }

    private void generateDisplayList(SIDE side) {

        Vector3f offset = new Vector3f(position.x * chunkDimensions.x, position.y * chunkDimensions.y, position.z * chunkDimensions.z);

        for (int x = 0; x < chunkDimensions.x; x++) {
            for (int y = 0; y < chunkDimensions.y; y++) {
                for (int z = 0; z < chunkDimensions.z; z++) {

                    int block = blocks[x][y][z];

                    if (block > 0) {

                        boolean drawFront, drawBack, drawLeft, drawRight, drawTop, drawBottom;
                        int blockToCheck = 0;

                        if (side == SIDE.TOP) {

                            blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y + 1, z))));
                            drawTop = checkBlockTypeToDraw(blockToCheck, block);

                            if (drawTop) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.TOP);
                                float shadowIntens = castRay(x, y, z, SIDE.TOP);
                                glColor3f(colorOffset.x * shadowIntens * parent.getDaylight(), colorOffset.y * shadowIntens * parent.getDaylight(), colorOffset.z * shadowIntens * parent.getDaylight());

                                float texOffsetX = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.TOP).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.TOP).y;
                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(-0.5f + x + offset.x, 0.5f + y + offset.y, 0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(0.5f + x + offset.x, 0.5f + y + offset.y, 0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x + offset.x, 0.5f + y + offset.y, -0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x + offset.x, 0.5f + y + offset.y, -0.5f + z + offset.z);
                            }

                        }

                        if (side == SIDE.FRONT) {

                            blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y, z - 1))));
                            drawFront = checkBlockTypeToDraw(blockToCheck, block);

                            if (drawFront) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.FRONT);
                                float shadowIntens = castRay(x, y, z, SIDE.FRONT);
                                glColor3f(colorOffset.x * shadowIntens * parent.getDaylight(), colorOffset.y * shadowIntens * parent.getDaylight(), colorOffset.z * shadowIntens * parent.getDaylight());

                                float texOffsetX = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.FRONT).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.FRONT).y;

                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(-0.5f + x + offset.x, 0.5f + y + offset.y, -0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(0.5f + x + offset.x, 0.5f + y + offset.y, -0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x + offset.x, -0.5f + y + offset.y, -0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x + offset.x, -0.5f + y + offset.y, -0.5f + z + offset.z);

                            }

                        }

                        if (side == SIDE.BACK) {

                            blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y, z + 1))));
                            drawBack = checkBlockTypeToDraw(blockToCheck, block);

                            if (drawBack) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.BACK);
                                float shadowIntens = castRay(x, y, z, SIDE.BACK);
                                glColor3f(colorOffset.x * shadowIntens * parent.getDaylight(), colorOffset.y * shadowIntens * parent.getDaylight(), colorOffset.z * shadowIntens * parent.getDaylight());
                                float texOffsetX = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.BACK).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.BACK).y;

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x + offset.x, -0.5f + y + offset.y, 0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x + offset.x, -0.5f + y + offset.y, 0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(0.5f + x + offset.x, 0.5f + y + offset.y, 0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(-0.5f + x + offset.x, 0.5f + y + offset.y, 0.5f + z + offset.z);
                            }

                        }

                        if (side == SIDE.LEFT) {

                            blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x - 1, y, z))));
                            drawLeft = checkBlockTypeToDraw(blockToCheck, block);

                            if (drawLeft) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.LEFT);
                                float shadowIntens = castRay(x, y, z, SIDE.LEFT);
                                glColor3f(colorOffset.x * shadowIntens * parent.getDaylight(), colorOffset.y * shadowIntens * parent.getDaylight(), colorOffset.z * shadowIntens * parent.getDaylight());
                                float texOffsetX = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.LEFT).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.LEFT).y;

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x + offset.x, -0.5f + y + offset.y, -0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x + offset.x, -0.5f + y + offset.y, 0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(-0.5f + x + offset.x, 0.5f + y + offset.y, 0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(-0.5f + x + offset.x, 0.5f + y + offset.y, -0.5f + z + offset.z);
                            }
                        }

                        if (side == SIDE.RIGHT) {

                            blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x + 1, y, z))));
                            drawRight = checkBlockTypeToDraw(blockToCheck, block);

                            if (drawRight) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.RIGHT);
                                float shadowIntens = castRay(x, y, z, SIDE.RIGHT);
                                glColor3f(colorOffset.x * shadowIntens * parent.getDaylight(), colorOffset.y * shadowIntens * parent.getDaylight(), colorOffset.z * shadowIntens * parent.getDaylight());
                                float texOffsetX = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.RIGHT).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.RIGHT).y;

                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(0.5f + x + offset.x, 0.5f + y + offset.y, -0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(0.5f + x + offset.x, 0.5f + y + offset.y, 0.5f + z + offset.z);


                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x + offset.x, -0.5f + y + offset.y, 0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x + offset.x, -0.5f + y + offset.y, -0.5f + z + offset.z);
                            }

                        }

                        if (side == SIDE.BOTTOM) {

                            blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y - 1, z))));
                            drawBottom = checkBlockTypeToDraw(blockToCheck, block);

                            if (drawBottom) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.BOTTOM);
                                float shadowIntens = castRay(x, y, z, SIDE.BOTTOM);
                                glColor3f(colorOffset.x * shadowIntens * parent.getDaylight(), colorOffset.y * shadowIntens * parent.getDaylight(), colorOffset.z * shadowIntens * parent.getDaylight());
                                float texOffsetX = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.BOTTOM).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(block, BlockHelper.SIDE.BOTTOM).y;

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x + offset.x, -0.5f + y + offset.y, -0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x + offset.x, -0.5f + y + offset.y, -0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(0.5f + x + offset.x, -0.5f + y + offset.y, 0.5f + z + offset.z);

                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(-0.5f + x + offset.x, -0.5f + y + offset.y, 0.5f + z + offset.z);
                            }

                        }

                    }

                }
            }
        }
    }
}
