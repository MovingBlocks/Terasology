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
    // Stores the light intensity for each block
    int[][][] light;
    // Create an unique id for each chunk
    static int maxChunkID = 0;
    int chunkID;
    // After init. display lists are used for rendering the chunks
    int displayListID = -1;
    //int displayListDebugID = -1;
    boolean dirty = false;
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

        // Generate and assign the chunk id
        chunkID = maxChunkID;
        maxChunkID++;

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

        // If the chunk changed, recreate the display list
        if (dirty) {
            if (displayListID == -1) {
                displayListID = glGenLists(1);
            }

            glNewList(displayListID, GL_COMPILE);
            glBegin(GL_QUADS);

            for (int x = 0; x < chunkDimensions.x; x++) {
                for (int y = 0; y < chunkDimensions.y; y++) {
                    for (int z = 0; z < chunkDimensions.z; z++) {

                        if (blocks[x][y][z] > 0) {

                            boolean drawFront = true, drawBack = true, drawLeft = true, drawRight = true, drawTop = true, drawBottom = true;

                            if (parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y, z - 1)))) != 0) {
                                drawFront = false;
                            }

                            if (parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y, z + 1)))) != 0) {
                                drawBack = false;
                            }

                            if (parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x - 1, y, z)))) != 0) {
                                drawLeft = false;
                            }

                            if (parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x + 1, y, z)))) != 0) {
                                drawRight = false;
                            }

                            if (parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y + 1, z)))) != 0) {
                                drawTop = false;
                            }

                            if (parent.getBlock(new Vector3f(getBlockWorldPos(new Vector3f(x, y - 1, z)))) != 0) {
                                drawBottom = false;
                            }

                            if (drawTop) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(blocks[x][y][z], BlockHelper.SIDE.TOP);
                                float shadowIntens = castRay(x, y, z, SIDE.TOP);
                                glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);

                                float texOffsetX = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.TOP).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.TOP).y;
                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(-0.5f + x, 0.5f + y, 0.5f + z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(0.5f + x, 0.5f + y, 0.5f + z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x, 0.5f + y, -0.5f + z);

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x, 0.5f + y, -0.5f + z);
                            }

                            if (drawFront) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(blocks[x][y][z], BlockHelper.SIDE.FRONT);
                                float shadowIntens = castRay(x, y, z, SIDE.FRONT);
                                glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);

                                float texOffsetX = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.FRONT).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.FRONT).y;

                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(-0.5f + x, 0.5f + y, -0.5f + z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(0.5f + x, 0.5f + y, -0.5f + z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x, -0.5f + y, -0.5f + z);

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x, -0.5f + y, -0.5f + z);

                            }

                            if (drawBack) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(blocks[x][y][z], BlockHelper.SIDE.BACK);
                                float shadowIntens = castRay(x, y, z, SIDE.BACK);
                                glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);
                                float texOffsetX = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.BACK).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.BACK).y;

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x, -0.5f + y, 0.5f + z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x, -0.5f + y, 0.5f + z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(0.5f + x, 0.5f + y, 0.5f + z);

                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(-0.5f + x, 0.5f + y, 0.5f + z);
                            }



                            if (drawLeft) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(blocks[x][y][z], BlockHelper.SIDE.LEFT);
                                float shadowIntens = castRay(x, y, z, SIDE.LEFT);
                                glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);
                                float texOffsetX = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.LEFT).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.LEFT).y;

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x, -0.5f + y, -0.5f + z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x, -0.5f + y, 0.5f + z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(-0.5f + x, 0.5f + y, 0.5f + z);

                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(-0.5f + x, 0.5f + y, -0.5f + z);
                            }

                            if (drawRight) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(blocks[x][y][z], BlockHelper.SIDE.RIGHT);
                                float shadowIntens = castRay(x, y, z, SIDE.RIGHT);
                                glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);
                                float texOffsetX = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.RIGHT).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.RIGHT).y;

                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(0.5f + x, 0.5f + y, -0.5f + z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(0.5f + x, 0.5f + y, 0.5f + z);


                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x, -0.5f + y, 0.5f + z);

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x, -0.5f + y, -0.5f + z);
                            }

                            if (drawBottom) {
                                Vector3f colorOffset = BlockHelper.getColorOffsetFor(blocks[x][y][z], BlockHelper.SIDE.BOTTOM);
                                float shadowIntens = castRay(x, y, z, SIDE.BOTTOM);
                                glColor3f(colorOffset.x * shadowIntens * daylight, colorOffset.y * shadowIntens * daylight, colorOffset.z * shadowIntens * daylight);
                                float texOffsetX = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.BOTTOM).x;
                                float texOffsetY = BlockHelper.getTextureOffsetFor(blocks[x][y][z], BlockHelper.SIDE.BOTTOM).y;

                                glTexCoord2f(texOffsetX, texOffsetY + 0.0625f);
                                glVertex3f(-0.5f + x, -0.5f + y, -0.5f + z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY + 0.0625f);
                                glVertex3f(0.5f + x, -0.5f + y, -0.5f + z);

                                glTexCoord2f(texOffsetX + 0.0625f, texOffsetY);
                                glVertex3f(0.5f + x, -0.5f + y, 0.5f + z);

                                glTexCoord2f(texOffsetX, texOffsetY);
                                glVertex3f(-0.5f + x, -0.5f + y, 0.5f + z);
                            }

                        }
                    }
                }
            }

            glEnd();
            glEndList();

            dirty = false;
            return true;

        }
        return false;
    }

    @Override
    public void render() {
        glEnable(GL_TEXTURE_2D);
        if (displayListID != -1) {
            glPushMatrix();
            glTranslatef(position.x * (int) chunkDimensions.x, position.y * (int) chunkDimensions.y, position.z * (int) chunkDimensions.z);
            glCallList(displayListID);
            glPopMatrix();
        }
        glDisable(GL_TEXTURE_2D);
    }

    public static void init() {
        try {
            textureMap = TextureLoader.getTexture("PNG", new FileInputStream(Chunk.class.getResource("Terrain.png").getPath()), GL_NEAREST);
            textureMap.bind();

        } catch (IOException ex) {
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    public int getLight(int x, int y, int z) {
        return light[x][y][z];
    }

    public int getBlock(int x, int y, int z) {
        return blocks[x][y][z];
    }

    public void setLight(Vector3f pos, int type) {
        light[(int) pos.x][(int) pos.y][(int) pos.z] = type;
        dirty = true;
    }

    public void setBlock(Vector3f pos, int type) {
        blocks[(int) pos.x][(int) pos.y][(int) pos.z] = type;
        dirty = true;
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
}
