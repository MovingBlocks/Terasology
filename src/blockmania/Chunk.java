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

import java.util.Vector;
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

    // Daylight
    float daylight = 0.75f;
    // The actual block ids for the chunk
    int[][][] blocks;
    // Create an unique id for each chunk
    int displayListOpaque = -1;
    int displayListTranslucent = -1;
    static List<Integer> displayListsOpaque = new ArrayList<Integer>();
    static List<Integer> displayListsTranslucent = new ArrayList<Integer>();
    // Display lists for rendering the chunks
    static IntBuffer bufferDisplayListsT = BufferUtils.createIntBuffer(8192);
    static IntBuffer bufferDisplayListsO = BufferUtils.createIntBuffer(8192);
    //int displayListDebugID = -1;
    boolean opqaueDirty = false;
    boolean translucentDirty = false;
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

    public boolean updateDisplayList(boolean translucent) {

        //glPushMatrix();
        //glTranslatef(, , );

        Vector3f offset = new Vector3f(position.x * chunkDimensions.x, position.y * chunkDimensions.y, position.z * chunkDimensions.z);

        // If the chunk changed, recreate the display list
        if ((translucentDirty && translucent) || (opqaueDirty && !translucent)) {
            if (displayListOpaque == -1 || displayListTranslucent == -1) {
                displayListOpaque = glGenLists(1);
                displayListTranslucent = glGenLists(1);
                displayListsOpaque.add(displayListOpaque);
                displayListsTranslucent.add(displayListTranslucent);
            }

            int activeDisplayList = -1;

            if (translucent) {
                activeDisplayList = displayListTranslucent;
            } else {
                activeDisplayList = displayListOpaque;
            }

            glNewList(activeDisplayList, GL_COMPILE);
            glBegin(GL_QUADS);

            for (int x = 0; x < chunkDimensions.x; x++) {
                for (int y = 0; y < chunkDimensions.y; y++) {
                    for (int z = 0; z < chunkDimensions.z; z++) {

                        int block = blocks[x][y][z];

                        if (block > 0) {

                            if ((translucent && BlockHelper.isBlockTypeTranslucent(block)) || (!translucent && !BlockHelper.isBlockTypeTranslucent(block))) {

                                boolean drawFront = true, drawBack = true, drawLeft = true, drawRight = true, drawTop = true, drawBottom = true;

                                int blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y, z - 1))));
                                drawFront = shouldSideBeDrawn(blockToCheck);

                                blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y, z + 1))));
                                drawBack = shouldSideBeDrawn(blockToCheck);

                                blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x - 1, y, z))));
                                drawLeft = shouldSideBeDrawn(blockToCheck);

                                blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x + 1, y, z))));
                                drawRight = shouldSideBeDrawn(blockToCheck);

                                blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y + 1, z))));
                                drawTop = shouldSideBeDrawn(blockToCheck);

                                blockToCheck = parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y - 1, z))));
                                drawBottom = shouldSideBeDrawn(blockToCheck);

                                if (drawTop) {
                                    Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.TOP);
                                    float shadowIntens = castRay(x, y, z, SIDE.TOP);
                                    glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);

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

                                if (drawFront) {
                                    Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.FRONT);
                                    float shadowIntens = castRay(x, y, z, SIDE.FRONT);
                                    glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);

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

                                if (drawBack) {
                                    Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.BACK);
                                    float shadowIntens = castRay(x, y, z, SIDE.BACK);
                                    glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);
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



                                if (drawLeft) {
                                    Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.LEFT);
                                    float shadowIntens = castRay(x, y, z, SIDE.LEFT);
                                    glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);
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

                                if (drawRight) {
                                    Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.RIGHT);
                                    float shadowIntens = castRay(x, y, z, SIDE.RIGHT);
                                    glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);
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

                                if (drawBottom) {
                                    Vector3f colorOffset = BlockHelper.getColorOffsetFor(block, BlockHelper.SIDE.BOTTOM);
                                    float shadowIntens = castRay(x, y, z, SIDE.BOTTOM);
                                    glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);
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

            glEnd();
            glEndList();

            if (translucent) {
                translucentDirty = false;
            } else {
                opqaueDirty = false;
            }
            return true;

        }
        return false;
    }

    public static void renderAllChunks() {

        if (bufferDisplayListsT.position() < displayListsTranslucent.size()) {
            for (int i=displayListsTranslucent.size()-1; i>=0; i--) {
                bufferDisplayListsT.put(displayListsTranslucent.get(i));
            }

            if (bufferDisplayListsO.position() < displayListsOpaque.size()) {
                for (Integer i : displayListsOpaque) {
                    bufferDisplayListsO.put(i);
                }
            }

            bufferDisplayListsO.rewind();
            bufferDisplayListsT.rewind();

            glEnable(GL_TEXTURE_2D);
            // Draw the opaque elements first
            glCallLists(bufferDisplayListsO);
            // And then the translucent elements
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glCallLists(bufferDisplayListsT);
            glDisable(GL_BLEND);
            glDisable(GL_TEXTURE_2D);
        }
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
        if (BlockHelper.isBlockTypeTranslucent(type)) {
            translucentDirty = true;
        } else {
            opqaueDirty = true;
        }
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
                    result = 0.5f;
                    break;
                }
            } catch (Exception e) {
            }
        }

        try {
            if (parent.getBlock(getBlockWorldPos(new Vector3f(x + 1, y + 1, z))) > 0) {
                result -= 0.15f;
            }
        } catch (Exception e) {
        }

        try {
            if (parent.getBlock(getBlockWorldPos(new Vector3f(x - 1, y + 1, z))) > 0) {
                result -= 0.15f;
            }
        } catch (Exception e) {
        }

        try {
            if (parent.getBlock(getBlockWorldPos(new Vector3f(x, y + 1, z + 1))) > 0) {
                result -= 0.15f;
            }
        } catch (Exception e) {
        }

        try {
            if (parent.getBlock(getBlockWorldPos(new Vector3f(x, y + 1, z - 1))) > 0) {
                result -= 0.15f;
            }
        } catch (Exception e) {
        }

        return result;
    }

    private boolean shouldSideBeDrawn(int blockToCheck) {
        return (blockToCheck == 0 || BlockHelper.isBlockTypeTranslucent(blockToCheck));
    }
}
