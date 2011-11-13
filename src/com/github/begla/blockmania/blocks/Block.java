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
public class Block implements RenderableObject {

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

    private int _displayList = -1;
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

    public static enum COLOR_SOURCE {
        DEFAULT, COLOR_LUT, FOLIAGE_LUT
    }

    static {
        try {
            _colorLut = ImageIO.read(ResourceLoader.getResource("com/github/begla/blockmania/data/textures/grasscolor.png").openStream());
            _foliageLut = ImageIO.read(ResourceLoader.getResource("com/github/begla/blockmania/data/textures/foliagecolor.png").openStream());
        } catch (IOException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    public Block() {
        withTitle("Untitled block");

        for (int i = 0; i < 6; i++) {
            withTextureAtlasPos(SIDE.values()[i], new Vector2f(0.0f, 0.0f));
            withColorOffset(SIDE.values()[i], new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
        }

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
        withDisableTesselation(false);
        withLiquid(false);
    }

    /**
     * Calculates the block color offset value for the given humidity and temperature.
     *
     * @param temp The temperature
     * @param hum  The humidity
     * @return The color value
     */
    public Vector4f colorForTemperatureAndHumidity(double temp, double hum) {
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
    public Vector4f foliageColorForTemperatureAndHumidity(double temp, double hum) {
        hum *= temp;
        int rgbValue = _foliageLut.getRGB((int) ((1.0 - temp) * 255.0), (int) ((1.0 - hum) * 255.0));

        Color c = new Color(rgbValue);
        return new Vector4f((float) c.getRed() / 255f, (float) c.getGreen() / 255f, (float) c.getBlue() / 255f, 1.0f);
    }

    /**
     * Returns true if a given block type is translucent.
     *
     * @return True if the block type is translucent
     */
    public boolean isTranslucent() {
        return _translucent;
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
    public Vector4f getColorOffsetFor(SIDE side, double temperature, double humidity) {
        Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

        if (getColorSource() == COLOR_SOURCE.COLOR_LUT)
            color.set(colorForTemperatureAndHumidity(temperature, humidity));
        else if (getColorSource() == COLOR_SOURCE.FOLIAGE_LUT) {
            color.set(foliageColorForTemperatureAndHumidity(temperature, humidity));
        }

        color.x *= _colorOffset[side.ordinal()].x;
        color.y *= _colorOffset[side.ordinal()].y;
        color.z *= _colorOffset[side.ordinal()].z;
        color.w *= _colorOffset[side.ordinal()].w;

        return color;
    }

    /**
     * Calculates the texture offset for a given block type and a specific
     * side of the block in the actual texture used for world rendering.
     *
     * @param side The side of the block
     * @return The texture offset
     */
    public Vector2f getTextureOffsetFor(SIDE side) {
        return Helper.calcOffsetForTextureAt((int) getTextureAtlasPos()[side.ordinal()].x, (int) getTextureAtlasPos()[side.ordinal()].y);
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

    private void generateDisplayList() {
        if (_displayList > 0)
            return;

        _displayList = glGenLists(1);

        glNewList(_displayList, GL11.GL_COMPILE);
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
        glEndList();
    }

    public void render() {
        if (isInvisible())
            return;

        if (_displayList == -1)
            generateDisplayList();

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

    public Block withDisableTesselation(boolean disableTesselation) {
        _disableTessellation = disableTesselation;
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

    /**
     * Returns the form of the block.
     *
     * @return The form of the block
     */
    public BLOCK_FORM getBlockForm() {
        return _blockForm;
    }

    /**
     * @return The title of the block
     */
    public String getTitle() {
        return _title;
    }

    public byte getId() {
        return _id;
    }

    /**
     * Returns true, if the block is invisible.
     *
     * @return True if invisible
     */
    public boolean isInvisible() {
        return _invisible;
    }

    /**
     * Returns true, if the block should be ignored
     * during collision checks.
     *
     * @return True if penetrable
     */
    public boolean isPenetrable() {
        return _penetrable;
    }

    /**
     * Returns true, if the block should be considered while calculating shadows.
     *
     * @return True if casting shadows
     */
    public boolean isCastsShadows() {
        return _castsShadows;
    }

    /**
     * Returns true if this block should be ignored in the tessellation process.
     *
     * @return True if ignored while tessellating
     */
    public boolean isDisableTesselation() {
        return _disableTessellation;
    }

    /**
     * Returns true if this block should render a bounding box when selected by the player.
     *
     * @return True if a bounding box should be rendered
     */
    public boolean isRenderBoundingBox() {
        return _renderBoundingBox;
    }

    /**
     * Returns the luminance of the block.
     *
     * @return The luminance
     */
    public byte getLuminance() {
        return _luminance;
    }

    /**
     * Returns true if the block can be destroyed by the player.
     *
     * @return True if removable
     */
    public boolean isDestructible() {
        return getHardness() >= 0;
    }

    /**
     * True if the player is allowed to attach blocks to this block.
     *
     * @return True if the player can attach blocks
     */
    public boolean isAllowBlockAttachment() {
        return _allowBlockAttachment;
    }

    /**
     * @return True if this block is a liquid
     */
    public boolean isLiquid() {
        return _liquid;
    }

    /**
     * True if the selection ray can pass through the block.
     *
     * @return True if selection ray can pass
     */
    public boolean isSelectionRayThrough() {
        return _bypassSelectionRay;
    }
}
