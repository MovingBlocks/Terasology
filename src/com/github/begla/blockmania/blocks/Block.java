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
import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.rendering.RenderableObject;
import com.github.begla.blockmania.rendering.TextureManager;
import com.github.begla.blockmania.utilities.Helper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.util.ResourceLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Block implements RenderableObject {

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
        NORMAL, CACTUS, LOWERED_BOCK, BILLBOARD
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
    private static final Vector4f _colorOffset = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

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
    public Vector4f getColorOffsetFor(SIDE side, double temp, double hum) {
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

    public Vector2f getTerrainTextureOffsetFor(SIDE side) {
        return getTextureOffsetFor(side);
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

    public boolean playerCanAttachBlocks() {
        return (getBlockForm() == BLOCK_FORM.NORMAL);
    }

    public static AABB AABBForBlockAt(Vector3f pos) {
        return new AABB(pos, new Vector3f(0.5f, 0.5f, 0.5f));
    }

    public static AABB AABBForBlockAt(int x, int y, int z) {
        return new AABB(new Vector3f(x, y, z), new Vector3f(0.5f, 0.5f, 0.5f));
    }

    public BLOCK_FORM getBlockForm() {
        return BLOCK_FORM.NORMAL;
    }

    public boolean letSelectionRayThrough() {
        return false;
    }

    public void render() {
        if (isBlockInvisible())
            return;

        glEnable(GL_TEXTURE_2D);
        TextureManager.getInstance().bindTexture("terrain");

        glBegin(GL_QUADS);
        GL11.glColor3f(1.0f, 1.0f, 1.0f);

        // TOP
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.TOP).x, getTerrainTextureOffsetFor(SIDE.TOP).y);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.TOP).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.TOP).y);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.TOP).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.TOP).y + 0.0624f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.TOP).x, getTerrainTextureOffsetFor(SIDE.TOP).y + 0.0624f);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);

        // LEFT
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.LEFT).x, getTerrainTextureOffsetFor(SIDE.LEFT).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.LEFT).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.LEFT).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.LEFT).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.LEFT).y);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.LEFT).x, getTerrainTextureOffsetFor(SIDE.LEFT).y);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);


        // BACK
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.BACK).x, getTerrainTextureOffsetFor(SIDE.BACK).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.BACK).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.BACK).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.BACK).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.BACK).y);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.BACK).x, getTerrainTextureOffsetFor(SIDE.BACK).y);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);

        // RIGHT
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.RIGHT).x, getTerrainTextureOffsetFor(SIDE.RIGHT).y);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.RIGHT).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.RIGHT).y);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.RIGHT).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.RIGHT).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.RIGHT).x, getTerrainTextureOffsetFor(SIDE.RIGHT).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);

        GL11.glColor3f(0.5f, 0.5f, 0.5f);

        // FRONT
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.FRONT).x, getTerrainTextureOffsetFor(SIDE.FRONT).y);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.FRONT).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.FRONT).y);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.FRONT).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.FRONT).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.FRONT).x, getTerrainTextureOffsetFor(SIDE.FRONT).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        // BOTTOM
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.BOTTOM).x, getTerrainTextureOffsetFor(SIDE.BOTTOM).y);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.BOTTOM).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.BOTTOM).y);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.BOTTOM).x + 0.0624f, getTerrainTextureOffsetFor(SIDE.BOTTOM).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(getTerrainTextureOffsetFor(SIDE.BOTTOM).x, getTerrainTextureOffsetFor(SIDE.BOTTOM).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);

        GL11.glEnd();

        glDisable(GL11.GL_TEXTURE_2D);
    }

    public void update() {
        // Do nothing
    }

    BufferedImage colorLut, foliageLut;

    public Block() {
        try {
            colorLut = ImageIO.read(ResourceLoader.getResource("com/github/begla/blockmania/data/textures/grassColor.png").openStream());
            foliageLut = ImageIO.read(ResourceLoader.getResource("com/github/begla/blockmania/data/textures/foliagecolor.png").openStream());
        } catch (IOException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    public Vector4f colorForTemperatureAndHumidity(double temp, double hum) {
        hum *= temp;
        int rgbValue = colorLut.getRGB((int) ((1.0 - temp) * 255.0), (int) ((1.0 - hum) * 255.0));

        Color c = new Color(rgbValue);
        return new Vector4f((float) c.getRed() / 255f, (float) c.getGreen() / 255f, (float) c.getBlue() / 255f, 1.0f);
    }

    public Vector4f foliageColorForTemperatureAndHumidity(double temp, double hum) {
        hum *= temp;
        int rgbValue = colorLut.getRGB((int) ((1.0 - temp) * 255.0), (int) ((1.0 - hum) * 255.0));

        Color c = new Color(rgbValue);
        return new Vector4f((float) c.getRed() / 255f, (float) c.getGreen() / 255f, (float) c.getBlue() / 255f, 1.0f);
    }
}
