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
import org.jdom.Element;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.util.ResourceLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Block implements RenderableObject {

    protected byte _id = 0x0;
    protected String _title = "Untitled block";
    protected boolean _translucent, _invisible, _penetrable, _castsShadows, _noTesselation, _renderBoundingBox, _allowBlockAttachment, _bypassSelectionRay, _liquid;
    protected BLOCK_FORM _blockForm;
    protected COLOR_SOURCE _colorSource;
    protected byte _luminance, _hardness;
    protected Vector4f[] _colorOffset = new Vector4f[6];
    protected Vector2f[] _textureAtlasPos = new Vector2f[6];

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

    private int _displayList = -1;
    private static BufferedImage colorLut, foliageLut;

    static {
        try {
            colorLut = ImageIO.read(ResourceLoader.getResource("com/github/begla/blockmania/data/textures/grasscolor.png").openStream());
            foliageLut = ImageIO.read(ResourceLoader.getResource("com/github/begla/blockmania/data/textures/foliagecolor.png").openStream());
        } catch (IOException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    public Block() {
        _title = "Untitled block";

        for (int i = 0; i < 6; i++) {
            _colorOffset[i] = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
            _textureAtlasPos[i] = new Vector2f(0.0f, 0.0f);
        }

        _penetrable = false;
        _allowBlockAttachment = true;
        _bypassSelectionRay = false;
        _blockForm = BLOCK_FORM.DEFAULT;
        _colorSource = COLOR_SOURCE.DEFAULT;
        _castsShadows = true;
        _translucent = false;
        _invisible = false;
        _renderBoundingBox = true;
        _hardness = 15;
        _luminance = 0;
        _noTesselation = false;
        _liquid = false;
    }

    public Block(Element block) {
        this();

        try {
            _title = block.getAttribute("title").getValue();
            _id = Byte.parseByte(block.getAttribute("id").getValue());

            List<Element> blockChildren = block.getChildren();

            for (Element e : blockChildren) {
                if (e.getName().equals("property")) {
                    if (e.getAttributeValue("name").equals("colorOffset")) {
                        for (int i = 0; i < 6; i++)
                            _colorOffset[i] = new Vector4f(e.getAttribute("r").getFloatValue(), e.getAttribute("g").getFloatValue(), e.getAttribute("b").getFloatValue(), e.getAttribute("a").getFloatValue());
                    } else if (e.getAttributeValue("name").equals("colorOffsetTop")) {
                        _colorOffset[SIDE.TOP.ordinal()] = new Vector4f(e.getAttribute("r").getFloatValue(), e.getAttribute("g").getFloatValue(), e.getAttribute("b").getFloatValue(), e.getAttribute("a").getFloatValue());
                    } else if (e.getAttributeValue("name").equals("colorOffsetBottom")) {
                        _colorOffset[SIDE.BOTTOM.ordinal()] = new Vector4f(e.getAttribute("r").getFloatValue(), e.getAttribute("g").getFloatValue(), e.getAttribute("b").getFloatValue(), e.getAttribute("a").getFloatValue());
                    } else if (e.getAttributeValue("name").equals("colorOffsetRight")) {
                        _colorOffset[SIDE.RIGHT.ordinal()] = new Vector4f(e.getAttribute("r").getFloatValue(), e.getAttribute("g").getFloatValue(), e.getAttribute("b").getFloatValue(), e.getAttribute("a").getFloatValue());
                    } else if (e.getAttributeValue("name").equals("colorOffsetLeft")) {
                        _colorOffset[SIDE.LEFT.ordinal()] = new Vector4f(e.getAttribute("r").getFloatValue(), e.getAttribute("g").getFloatValue(), e.getAttribute("b").getFloatValue(), e.getAttribute("a").getFloatValue());
                    } else if (e.getAttributeValue("name").equals("colorOffsetFront")) {
                        _colorOffset[SIDE.FRONT.ordinal()] = new Vector4f(e.getAttribute("r").getFloatValue(), e.getAttribute("g").getFloatValue(), e.getAttribute("b").getFloatValue(), e.getAttribute("a").getFloatValue());
                    } else if (e.getAttributeValue("name").equals("colorOffsetBack")) {
                        _colorOffset[SIDE.BACK.ordinal()] = new Vector4f(e.getAttribute("r").getFloatValue(), e.getAttribute("g").getFloatValue(), e.getAttribute("b").getFloatValue(), e.getAttribute("a").getFloatValue());
                    } else if (e.getAttributeValue("name").equals("textureOffset")) {
                        for (int i = 0; i < 6; i++)
                            _textureAtlasPos[i] = new Vector2f(e.getAttribute("x").getFloatValue(), e.getAttribute("y").getFloatValue());
                    } else if (e.getAttributeValue("name").equals("textureOffsetTop")) {
                        _textureAtlasPos[SIDE.TOP.ordinal()] = new Vector2f(e.getAttribute("x").getIntValue(), e.getAttribute("y").getIntValue());
                    } else if (e.getAttributeValue("name").equals("textureOffsetBottom")) {
                        _textureAtlasPos[SIDE.BOTTOM.ordinal()] = new Vector2f(e.getAttribute("x").getIntValue(), e.getAttribute("y").getIntValue());
                    } else if (e.getAttributeValue("name").equals("textureOffsetRight")) {
                        _textureAtlasPos[SIDE.RIGHT.ordinal()] = new Vector2f(e.getAttribute("x").getIntValue(), e.getAttribute("y").getIntValue());
                    } else if (e.getAttributeValue("name").equals("textureOffsetLeft")) {
                        _textureAtlasPos[SIDE.LEFT.ordinal()] = new Vector2f(e.getAttribute("x").getIntValue(), e.getAttribute("y").getIntValue());
                    } else if (e.getAttributeValue("name").equals("textureOffsetFront")) {
                        _textureAtlasPos[SIDE.FRONT.ordinal()] = new Vector2f(e.getAttribute("x").getIntValue(), e.getAttribute("y").getIntValue());
                    } else if (e.getAttributeValue("name").equals("textureOffsetBack")) {
                        _textureAtlasPos[SIDE.BACK.ordinal()] = new Vector2f(e.getAttribute("x").getIntValue(), e.getAttribute("y").getIntValue());
                    } else if (e.getAttributeValue("name").equals("colorSource")) {
                        _colorSource = COLOR_SOURCE.values()[Integer.parseInt(e.getValue())];
                    } else if (e.getAttributeValue("name").equals("blockForm")) {
                        _blockForm = BLOCK_FORM.values()[Integer.parseInt(e.getValue())];
                    } else if (e.getAttributeValue("name").equals("hardness")) {
                        _hardness = Byte.parseByte(e.getValue());
                    } else if (e.getAttributeValue("name").equals("penetrable")) {
                        _penetrable = Boolean.parseBoolean(e.getValue());
                    } else if (e.getAttributeValue("name").equals("translucent")) {
                        _translucent = Boolean.parseBoolean(e.getValue());
                    } else if (e.getAttributeValue("name").equals("invisible")) {
                        _invisible = Boolean.parseBoolean(e.getValue());
                    } else if (e.getAttributeValue("name").equals("luminance")) {
                        _luminance = Byte.parseByte(e.getValue());
                    } else if (e.getAttributeValue("name").equals("noTessellation")) {
                        _noTesselation = Boolean.parseBoolean(e.getValue());
                    } else if (e.getAttributeValue("name").equals("castsShadows")) {
                        _castsShadows = Boolean.parseBoolean(e.getValue());
                    } else if (e.getAttributeValue("name").equals("bypassSelectionRay")) {
                        _bypassSelectionRay = Boolean.parseBoolean(e.getValue());
                    } else if (e.getAttributeValue("name").equals("renderBoundingBox")) {
                        _renderBoundingBox = Boolean.parseBoolean(e.getValue());
                    } else if (e.getAttributeValue("name").equals("allowBlockAttachment")) {
                        _allowBlockAttachment = Boolean.parseBoolean(e.getValue());
                    } else if (e.getAttributeValue("name").equals("liquid")) {
                        _liquid = Boolean.parseBoolean(e.getValue());
                    }
                }
            }
        } catch (Exception ex) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
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
        int rgbValue = colorLut.getRGB((int) ((1.0 - temp) * 255.0), (int) ((1.0 - hum) * 255.0));

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
        int rgbValue = foliageLut.getRGB((int) ((1.0 - temp) * 255.0), (int) ((1.0 - hum) * 255.0));

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

        if (_colorSource == COLOR_SOURCE.COLOR_LUT)
            color.set(colorForTemperatureAndHumidity(temperature, humidity));
        else if (_colorSource == COLOR_SOURCE.FOLIAGE_LUT) {
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
        return Helper.calcOffsetForTextureAt((int) _textureAtlasPos[side.ordinal()].x, (int) _textureAtlasPos[side.ordinal()].y);
    }

    /**
     * Returns true, if the block is invisible.
     *
     * @return True if invisible
     */
    public boolean isBlockInvisible() {
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
    public boolean isCastingShadows() {
        return _castsShadows;
    }

    /**
     * Returns true if this block should be ignored in the tessellation process.
     *
     * @return True if ignored while tessellating
     */
    public boolean doNotTessellate() {
        return _noTesselation;
    }

    /**
     * Returns true if this block should render a bounding box when selected by the player.
     *
     * @return True if a bounding box should be rendered
     */
    public boolean shouldRenderBoundingBox() {
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
        return _hardness >= 0;
    }

    /**
     * True if the player is allowed to attach blocks to this block.
     *
     * @return True if the player can attach blocks
     */
    public boolean allowsBlockAttachment() {
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
    public boolean letsSelectionRayThrough() {
        return _bypassSelectionRay;
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
        if (isBlockInvisible())
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
}
