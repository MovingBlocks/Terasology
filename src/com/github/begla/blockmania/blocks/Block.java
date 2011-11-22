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
import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.rendering.interfaces.RenderableObject;
import com.github.begla.blockmania.rendering.manager.TextureManager;
import com.github.begla.blockmania.utilities.Helper;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.util.ResourceLoader;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;

/**
 * Stores all information for a specific block type.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Block implements RenderableObject {

    /* PROPERTIES */
    private byte _id = 0x0;
    private String _title = "Untitled block";

    private boolean _translucent;
    private boolean _invisible;
    private boolean _penetrable;
    private boolean _castsShadows;
    private boolean _disableTessellation;
    private boolean _renderBoundingBox;
    private boolean _allowBlockAttachment;
    private boolean _bypassSelectionRay;
    private boolean _liquid;

    private BLOCK_FORM _blockForm;
    private COLOR_SOURCE _colorSource;

    private byte _luminance;
    private byte _hardness;

    private Vector4f[] _colorOffset = new Vector4f[6];
    private Vector2f[] _textureAtlasPos = new Vector2f[6];

    /* RENDERING */
    private int _displayList = -1;

    /* LUTs */
    private static BufferedImage _colorLut;
    private static BufferedImage _foliageLut;

    /**
     * The six sides of a block.
     */
    public static enum SIDE {
        LEFT, RIGHT, TOP, BOTTOM, FRONT, BACK
    }

    /**
     * Possible forms of blocks.
     */
    public static enum BLOCK_FORM {
        DEFAULT, CACTUS, LOWERED_BLOCK, BILLBOARD
    }

    /**
     * Different color sources for blocks.
     */
    public static enum COLOR_SOURCE {
        DEFAULT, COLOR_LUT, FOLIAGE_LUT
    }

    /**
     * Init. the LUTs.
     */
    static {
        try {
            _colorLut = ImageIO.read(ResourceLoader.getResource("com/github/begla/blockmania/data/textures/grasscolor.png").openStream());
            _foliageLut = ImageIO.read(ResourceLoader.getResource("com/github/begla/blockmania/data/textures/foliagecolor.png").openStream());
        } catch (IOException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    /**
     * Init. a new block with default properties in place.
     */
    public Block() {
        withTitle("Untitled block");

        for (int i = 0; i < 6; i++) {
            withTextureAtlasPos(SIDE.values()[i], new Vector2f(0.0f, 0.0f));
            withColorOffset(SIDE.values()[i], new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
        }

        // Load the default settings
        withPenetrable(false);
        withAllowBlockAttachment(true);
        withBypassSelectionRay(false);
        withBlockForm(BLOCK_FORM.DEFAULT);
        withColorSource(COLOR_SOURCE.DEFAULT);
        withCastsShadows(true);
        withTranslucent(false);
        withInvisible(false);
        withRenderBoundingBox(true);
        withHardness((byte) 15);
        withLuminance((byte) 0);
        withDisableTessellation(false);
        withLiquid(false);
    }

    /**
     * Calculates the block color offset value for the given humidity and temperature.
     *
     * @param temp The temperature
     * @param hum  The humidity
     * @return The color value
     */
    public Vector4f calcColorForTemperatureAndHumidity(double temp, double hum) {
        hum *= temp;
        int rgbValue = _colorLut.getRGB((int) ((1.0 - temp) * 255.0), (int) ((1.0 - hum) * 255.0));

        Color c = new Color(rgbValue);
        return new Vector4f((float) c.getRed() / 255f, (float) c.getGreen() / 255f, (float) c.getBlue() / 255f, 1.0f);
    }

    /**
     * Calculates the foliage color for the given humidity and temperature.
     *
     * @param temp The temperature
     * @param hum  The humidity
     * @return The color value
     */
    public Vector4f calcFoliageColorForTemperatureAndHumidity(double temp, double hum) {
        hum *= temp;
        int rgbValue = _foliageLut.getRGB((int) ((1.0 - temp) * 255.0), (int) ((1.0 - hum) * 255.0));

        Color c = new Color(rgbValue);
        return new Vector4f((float) c.getRed() / 255f, (float) c.getGreen() / 255f, (float) c.getBlue() / 255f, 1.0f);
    }

    /**
     * Calculates the color offset for a given block type and a specific
     * side of the block.
     *
     * @param side        The block side
     * @param temperature The temperature
     * @param humidity    The humidity
     * @return The color offset
     */
    public Vector4f calcColorOffsetFor(SIDE side, double temperature, double humidity) {
        Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

        if (getColorSource() == COLOR_SOURCE.COLOR_LUT)
            color.set(calcColorForTemperatureAndHumidity(temperature, humidity));
        else if (getColorSource() == COLOR_SOURCE.FOLIAGE_LUT) {
            color.set(calcFoliageColorForTemperatureAndHumidity(temperature, humidity));
        }

        color.x *= _colorOffset[side.ordinal()].x;
        color.y *= _colorOffset[side.ordinal()].y;
        color.z *= _colorOffset[side.ordinal()].z;
        color.w *= _colorOffset[side.ordinal()].w;

        return color;
    }

    /**
     * Calculates the texture atlas offset for a given block type and a specific
     * side.
     *
     * @param side The side of the block
     * @return The texture offset
     */
    public Vector2f calcTextureOffsetFor(SIDE side) {
        return Helper.calcOffsetForTextureAt((int) getTextureAtlasPos()[side.ordinal()].x, (int) getTextureAtlasPos()[side.ordinal()].y);
    }

    public void render() {
        if (isInvisible())
            return;

        if (_displayList == -1)
            _displayList = generateDisplayList();

        glEnable(GL_TEXTURE_2D);
        TextureManager.getInstance().bindTexture("terrain");

        glCallList(_displayList);

        glDisable(GL11.GL_TEXTURE_2D);
    }

    public void update() {
        // Do nothing
    }

    public Block withId(byte id) {
        _id = id;
        return this;
    }

    public Block withTitle(String title) {
        _title = title;
        return this;
    }

    public Block withTranslucent(boolean translucent) {
        _translucent = translucent;
        return this;
    }

    public Block withInvisible(boolean invisible) {
        _invisible = invisible;
        return this;
    }

    public Block withPenetrable(boolean penetrable) {
        _penetrable = penetrable;
        return this;
    }

    public Block withCastsShadows(boolean castsShadows) {
        _castsShadows = castsShadows;
        return this;
    }

    public Block withDisableTessellation(boolean disableTessellation) {
        _disableTessellation = disableTessellation;
        return this;
    }

    public Block withRenderBoundingBox(boolean renderBoundingBox) {
        _renderBoundingBox = renderBoundingBox;
        return this;
    }

    public Block withAllowBlockAttachment(boolean allowBlockAttachment) {
        _allowBlockAttachment = allowBlockAttachment;
        return this;
    }

    public Block withBypassSelectionRay(boolean bypassSelectionRay) {
        _bypassSelectionRay = bypassSelectionRay;
        return this;
    }

    public Block withLiquid(boolean liquid) {
        _liquid = liquid;
        return this;
    }

    public Block withBlockForm(BLOCK_FORM blockForm) {
        _blockForm = blockForm;
        return this;
    }

    public Block withColorSource(COLOR_SOURCE colorSource) {
        _colorSource = colorSource;
        return this;
    }

    public Block withLuminance(byte luminance) {
        _luminance = luminance;
        return this;
    }

    public Block withHardness(byte hardness) {
        _hardness = hardness;
        return this;
    }

    public Block withColorOffset(SIDE side, Vector4f colorOffset) {
        _colorOffset[side.ordinal()] = colorOffset;
        return this;
    }

    public Block withTextureAtlasPos(SIDE side, Vector2f atlasPos) {
        _textureAtlasPos[side.ordinal()] = atlasPos;
        return this;
    }

    public Block withTextureAtlasPosTopBottom(Vector2f atlasPos) {
        _textureAtlasPos[SIDE.TOP.ordinal()] = atlasPos;
        _textureAtlasPos[SIDE.BOTTOM.ordinal()] = atlasPos;
        return this;
    }

    public Block withTextureAtlasPos(Vector2f atlasPos) {
        for (int i = 0; i < 6; i++) {
            withTextureAtlasPos(SIDE.values()[i], atlasPos);
        }
        return this;
    }

    public Block withTextureAtlasPosMantle(Vector2f atlasPos) {
        _textureAtlasPos[SIDE.LEFT.ordinal()] = atlasPos;
        _textureAtlasPos[SIDE.RIGHT.ordinal()] = atlasPos;
        _textureAtlasPos[SIDE.FRONT.ordinal()] = atlasPos;
        _textureAtlasPos[SIDE.BACK.ordinal()] = atlasPos;
        return this;
    }

    public Block withColorOffset(Vector4f colorOffset) {
        for (int i = 0; i < 6; i++) {
            withColorOffset(SIDE.values()[i], colorOffset);
        }
        return this;
    }

    public COLOR_SOURCE getColorSource() {
        return _colorSource;
    }

    public byte getHardness() {
        return _hardness;
    }

    public Vector4f[] getColorOffset() {
        return _colorOffset;
    }

    public Vector2f[] getTextureAtlasPos() {
        return _textureAtlasPos;
    }

    public BLOCK_FORM getBlockForm() {
        return _blockForm;
    }

    public String getTitle() {
        return _title;
    }

    public byte getId() {
        return _id;
    }

    public boolean isInvisible() {
        return _invisible;
    }

    public boolean isPenetrable() {
        return _penetrable;
    }

    public boolean isCastsShadows() {
        return _castsShadows;
    }

    public boolean isDisableTessellation() {
        return _disableTessellation;
    }

    public boolean isRenderBoundingBox() {
        return _renderBoundingBox;
    }

    public byte getLuminance() {
        return _luminance;
    }

    public boolean isDestructible() {
        return getHardness() >= 0;
    }

    public boolean isAllowBlockAttachment() {
        return _allowBlockAttachment;
    }

    public boolean isLiquid() {
        return _liquid;
    }

    public boolean isSelectionRayThrough() {
        return _bypassSelectionRay;
    }

    public boolean isTranslucent() {
        return _translucent;
    }

    /**
     * Generates the display list used for rendering the block.
     *
     * @return The id of the display list
     */
    private int generateDisplayList() {
        int id = glGenLists(1);

        glNewList(id, GL11.GL_COMPILE);
        glBegin(GL_QUADS);
        GL11.glColor3f(1.0f, 1.0f, 1.0f);

        // TOP
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.TOP).x, calcTextureOffsetFor(SIDE.TOP).y);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.TOP).x + 0.0624f, calcTextureOffsetFor(SIDE.TOP).y);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.TOP).x + 0.0624f, calcTextureOffsetFor(SIDE.TOP).y + 0.0624f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.TOP).x, calcTextureOffsetFor(SIDE.TOP).y + 0.0624f);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);

        // LEFT
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.LEFT).x, calcTextureOffsetFor(SIDE.LEFT).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.LEFT).x + 0.0624f, calcTextureOffsetFor(SIDE.LEFT).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.LEFT).x + 0.0624f, calcTextureOffsetFor(SIDE.LEFT).y);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.LEFT).x, calcTextureOffsetFor(SIDE.LEFT).y);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);


        // BACK
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.BACK).x, calcTextureOffsetFor(SIDE.BACK).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.BACK).x + 0.0624f, calcTextureOffsetFor(SIDE.BACK).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.BACK).x + 0.0624f, calcTextureOffsetFor(SIDE.BACK).y);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.BACK).x, calcTextureOffsetFor(SIDE.BACK).y);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);

        // RIGHT
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.RIGHT).x, calcTextureOffsetFor(SIDE.RIGHT).y);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.RIGHT).x + 0.0624f, calcTextureOffsetFor(SIDE.RIGHT).y);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.RIGHT).x + 0.0624f, calcTextureOffsetFor(SIDE.RIGHT).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.RIGHT).x, calcTextureOffsetFor(SIDE.RIGHT).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);

        GL11.glColor3f(0.5f, 0.5f, 0.5f);

        // FRONT
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.FRONT).x, calcTextureOffsetFor(SIDE.FRONT).y);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.FRONT).x + 0.0624f, calcTextureOffsetFor(SIDE.FRONT).y);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.FRONT).x + 0.0624f, calcTextureOffsetFor(SIDE.FRONT).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.FRONT).x, calcTextureOffsetFor(SIDE.FRONT).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        // BOTTOM
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.BOTTOM).x, calcTextureOffsetFor(SIDE.BOTTOM).y);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.BOTTOM).x + 0.0624f, calcTextureOffsetFor(SIDE.BOTTOM).y);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.BOTTOM).x + 0.0624f, calcTextureOffsetFor(SIDE.BOTTOM).y + 0.0624f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glTexCoord2f(calcTextureOffsetFor(SIDE.BOTTOM).x, calcTextureOffsetFor(SIDE.BOTTOM).y + 0.0624f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);

        GL11.glEnd();
        glEndList();

        return id;
    }

    /**
     * Returns the AABB for a block at the given position.
     *
     * @param pos The position
     * @return The AABB
     */
    public static AABB AABBForBlockAt(Vector3f pos) {
        return new AABB(pos, new Vector3f(0.5f, 0.5f, 0.5f));
    }

    /**
     * Returns the AABB for a block at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     * @return The AABB
     */
    public static AABB AABBForBlockAt(int x, int y, int z) {
        return new AABB(new Vector3f(x, y, z), new Vector3f(0.5f, 0.5f, 0.5f));
    }
}
