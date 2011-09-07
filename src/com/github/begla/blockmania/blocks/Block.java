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
package com.github.begla.blockmania.blocks;

import com.github.begla.blockmania.datastructures.AABB;
import com.github.begla.blockmania.rendering.TextureManager;
import com.github.begla.blockmania.rendering.VectorPool;
import com.github.begla.blockmania.utilities.Helper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Block {

    /**
     * The six sides of a block.
     */
    public static enum SIDE {

        /**
         * Left side.
         */
        LEFT,
        /**
         * Right side.
         */
        RIGHT,
        /**
         * Top side.
         */
        TOP,
        /**
         * Bottom side.
         */
        BOTTOM,
        /**
         * Front side.
         */
        FRONT,
        /**
         * Back side.
         */
        BACK
    }


    public static enum BLOCK_FORM {
        NORMAL, CACTUS, LOWERED_BOCK
    }

    private static final Block[] _blocks = {
            new BlockAir(), new BlockGrass(), new BlockDirt(), new BlockStone(), // 0-3
            new BlockWater(), new BlockTreeTrunk(), new BlockLeaf(), new BlockSand(), // 4-7
            new BlockHardStone(), new BlockRedFlower(), new BlockYellowFlower(), // 8-10
            new BlockHighGrass(), new BlockLargeHighGrass(), new BlockTorch(), new BlockLava(), // 11-14
            new BlockWood(), new BlockCobbleStone(), new BlockIce(), new BlockGlass(), new BlockBrick(), // 15-19
            new BlockCoal(), new BlockGold(), new BlockDarkLeaf(), new BlockSnow(), new BlockCactus(), // 20-24
            new BlockBookShelf(), new BlockColorBlack(), new BlockColorBlue(), new BlockColorBrown(), new BlockColorGreen(), // 25-29
            new BlockColorPurple(), new BlockColorRed(), new BlockColorWhite(), new BlockRedStone(), new BlockSilver(), new BlockDiamond() // 30-35
    };
    private static final BlockNil NIL_BLOCK = new BlockNil();
    private static Vector4f _colorOffset = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    /**
     * Returns the object for the given block type ID.
     *
     * @param type Block type ID
     * @return The object for the given ID
     */
    public static Block getBlockForType(byte type) {
        if (type < 0 || type >= _blocks.length) {
            return NIL_BLOCK;
        }

        return _blocks[type];
    }

    /**
     * Returns the amount of blocks available.
     *
     * @return Amount of blocks available
     */
    public static int getBlockCount() {
        return _blocks.length;
    }

    /**
     * Returns true if a given block type is translucent.
     *
     * @return True if the block type is translucent
     */
    public boolean isBlockTypeTranslucent() {
        return false;
    }

    /**
     * Calculates the color offset for a given block type and a specific
     * side of the block.
     *
     * @param side The block side
     * @return The color offset
     */
    public Vector4f getColorOffsetFor(SIDE side) {
        return _colorOffset;
    }

    /**
     * Calculates the texture offset for a given block type and a specific
     * side of the block.
     *
     * @param side The side of the block
     * @return The texture offset
     */
    public Vector2f getTextureOffsetFor(SIDE side) {
        return Helper.calcOffsetForTextureAt(2, 0);
    }

    /**
     * Returns true, if the current block is a billboard.
     *
     * @return True if billboard
     */
    public boolean isBlockBillboard() {
        return false;
    }

    /**
     * Returns true, if the block is invisible.
     *
     * @return True if invisible
     */
    public boolean isBlockInvisible() {
        return false;
    }

    /**
     * Returns true, if the block should be ignored
     * within the collision checks.
     *
     * @return True if penetrable
     */
    public boolean isPenetrable() {
        return false;
    }

    /**
     * Returns true, if the block should be considered
     * while calculating shadows.
     *
     * @return True if casting shadows
     */
    public boolean isCastingShadows() {
        return true;
    }

    public boolean doNotTessellate() {
        return false;
    }

    public boolean shouldRenderBoundingBox() {
        return true;
    }

    public byte getLuminance() {
        return 0;
    }

    public boolean isRemovable() {
        return true;
    }

    public static AABB AABBForBlockAt(int x, int y, int z) {
        return new AABB(VectorPool.getVector(x, y, z), VectorPool.getVector(0.5f, 0.5f, 0.5f));
    }

    public BLOCK_FORM getBlockForm() {
        return BLOCK_FORM.NORMAL;
    }

    public void renderBlock(boolean shaded) {
        if (isBlockInvisible())
            return;

        glEnable(GL11.GL_TEXTURE_2D);
        TextureManager.getInstance().bindTexture("terrain");

        glBegin(GL_QUADS);
        GL11.glColor3f(1.0f, 1.0f, 1.0f);

        // TOP
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.TOP).x, getTextureOffsetFor(SIDE.TOP).y);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.TOP).x + 0.0624f, getTextureOffsetFor(SIDE.TOP).y);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.TOP).x + 0.0624f, getTextureOffsetFor(SIDE.TOP).y + 0.0624f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.TOP).x, getTextureOffsetFor(SIDE.TOP).y + 0.0624f);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);

        // LEFT
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.LEFT).x, getTextureOffsetFor(SIDE.LEFT).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.LEFT).x + 0.0624f, getTextureOffsetFor(SIDE.LEFT).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.LEFT).x + 0.0624f, getTextureOffsetFor(SIDE.LEFT).y);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.LEFT).x, getTextureOffsetFor(SIDE.LEFT).y);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);


        // BACK
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.BACK).x, getTextureOffsetFor(SIDE.BACK).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.BACK).x + 0.0624f, getTextureOffsetFor(SIDE.BACK).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.BACK).x + 0.0624f, getTextureOffsetFor(SIDE.BACK).y);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.BACK).x, getTextureOffsetFor(SIDE.BACK).y);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);

        // RIGHT
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.RIGHT).x, getTextureOffsetFor(SIDE.RIGHT).y);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.RIGHT).x + 0.0624f, getTextureOffsetFor(SIDE.RIGHT).y);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.RIGHT).x + 0.0624f, getTextureOffsetFor(SIDE.RIGHT).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.RIGHT).x, getTextureOffsetFor(SIDE.RIGHT).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);

        if (shaded)
            GL11.glColor3f(0.5f, 0.5f, 0.5f);

        // FRONT
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.FRONT).x, getTextureOffsetFor(SIDE.FRONT).y);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.FRONT).x + 0.0624f, getTextureOffsetFor(SIDE.FRONT).y);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.FRONT).x + 0.0624f, getTextureOffsetFor(SIDE.FRONT).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.FRONT).x, getTextureOffsetFor(SIDE.FRONT).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        // BOTTOM
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.BOTTOM).x, getTextureOffsetFor(SIDE.BOTTOM).y);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.BOTTOM).x + 0.0624f, getTextureOffsetFor(SIDE.BOTTOM).y);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.BOTTOM).x + 0.0624f, getTextureOffsetFor(SIDE.BOTTOM).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(getTextureOffsetFor(SIDE.BOTTOM).x, getTextureOffsetFor(SIDE.BOTTOM).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);

        GL11.glEnd();

        glDisable(GL11.GL_TEXTURE_2D);
    }
}
