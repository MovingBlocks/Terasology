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
package org.terasology.model.blocks;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.util.ResourceLoader;
import org.terasology.collection.EnumBooleanMap;
import org.terasology.game.Terasology;
import org.terasology.math.Side;
import org.terasology.model.shapes.BlockMeshPart;
import org.terasology.model.structures.AABB;
import org.terasology.rendering.interfaces.IGameObject;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;

import javax.imageio.ImageIO;
import javax.vecmath.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

/**
 * Stores all information for a specific block type.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class Block implements IGameObject, Cloneable {

    private static final Logger logger = Logger.getLogger(Block.class.getName());
    public static final int ATLAS_SIZE_IN_PX = 256;
    public static final int TEXTURE_SIZE_IN_PX = 16;
    public static final int ATLAS_ELEMENTS_PER_ROW_AND_COLUMN = ATLAS_SIZE_IN_PX / TEXTURE_SIZE_IN_PX;
    public static final float TEXTURE_OFFSET = 0.0625f;
    public static final float TEXTURE_OFFSET_WIDTH = 0.0624f;

    private static final EnumMap<Side, Float> DIRECTION_LIT_LEVEL = new EnumMap<Side, Float>(Side.class);

    /* LUTs */
    protected static BufferedImage _colorLut;
    protected static BufferedImage _foliageLut;

    /**
     * Possible forms of blocks.
     */
    public static enum BLOCK_FORM {
        DEFAULT, LOWERED_BLOCK, BILLBOARD
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
        DIRECTION_LIT_LEVEL.put(Side.TOP, 0.9f);
        DIRECTION_LIT_LEVEL.put(Side.BOTTOM, 0.9f);
        DIRECTION_LIT_LEVEL.put(Side.FRONT, 1.0f);
        DIRECTION_LIT_LEVEL.put(Side.BACK, 1.0f);
        DIRECTION_LIT_LEVEL.put(Side.LEFT, 0.75f);
        DIRECTION_LIT_LEVEL.put(Side.RIGHT, 0.75f);
        try {
            _colorLut = ImageIO.read(ResourceLoader.getResource("org/terasology/data/textures/grasscolor.png").openStream());
            _foliageLut = ImageIO.read(ResourceLoader.getResource("org/terasology/data/textures/foliagecolor.png").openStream());
        } catch (IOException e) {
            Terasology.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    /* PROPERTIES */
    private byte _id = 0x0;
    private String _title = "Untitled block";
    private BlockGroup _group = null;

    private static class SharedBlockInternals implements Cloneable {
        private boolean translucent;
        private boolean invisible;
        private boolean penetrable;
        private boolean castsShadows;
        private boolean renderBoundingBox;
        private boolean allowBlockAttachment;
        private boolean bypassSelectionRay;
        private boolean liquid;
        private boolean waving;

        private BLOCK_FORM blockForm;
        private COLOR_SOURCE colorSource;

        private byte luminance;
        private byte hardness;
    }

    private SharedBlockInternals _internals;

    /* RENDERING */
    private Mesh _mesh;
    private BlockMeshPart _centerMesh;
    private EnumMap<Side, BlockMeshPart> _sideMesh = new EnumMap<Side, BlockMeshPart>(Side.class);
    private EnumBooleanMap<Side> _fullSide = new EnumBooleanMap<Side>(Side.class);

    // For liquid handling
    private EnumMap<Side, BlockMeshPart> _loweredSideMesh = new EnumMap<Side, BlockMeshPart>(Side.class);

    private EnumMap<Side, Vector4f> _colorOffset = new EnumMap<Side, Vector4f>(Side.class);
    private EnumMap<Side, Vector2f> _textureAtlasPos = new EnumMap<Side, Vector2f>(Side.class);

    /* COLLISION */

    private List<AABB> _colliders = new ArrayList<AABB>();

    /**
     * Init. a new block with default properties in place.
     */
    public Block() {
        withTitle("Untitled block");
        _internals = new SharedBlockInternals();

        for (Side side : Side.values()) {
            withTextureAtlasPos(side, new Vector2f(0.0f, 0.0f));
            withColorOffset(side, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
            withFullSide(side, false);
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
        withHardness((byte) 3);
        withLuminance((byte) 0);
        withLiquid(false);
    }

    public Block clone() {
        Block clone = null;
        try {
            clone = (Block) super.clone();

            clone._group = null;
            clone._sideMesh = new EnumMap<Side, BlockMeshPart>(Side.class);
            clone._sideMesh.putAll(_sideMesh);
            clone._fullSide = new EnumBooleanMap<Side>(Side.class);
            clone._fullSide.putAll(_fullSide);
            clone._loweredSideMesh = new EnumMap<Side, BlockMeshPart>(Side.class);
            clone._loweredSideMesh.putAll(_loweredSideMesh);
            clone._colorOffset = new EnumMap<Side, Vector4f>(Side.class);
            clone._colorOffset.putAll(_colorOffset);
            clone._textureAtlasPos = new EnumMap<Side, Vector2f>(Side.class);
            clone._textureAtlasPos.putAll(_textureAtlasPos);
        } catch (CloneNotSupportedException e) {
            logger.log(Level.SEVERE, "Failed to clone block");
        }
        return clone;
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
    public Vector4f calcColorOffsetFor(Side side, double temperature, double humidity) {
        Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

        if (getColorSource() == COLOR_SOURCE.COLOR_LUT)
            color.set(calcColorForTemperatureAndHumidity(temperature, humidity));
        else if (getColorSource() == COLOR_SOURCE.FOLIAGE_LUT) {
            color.set(calcFoliageColorForTemperatureAndHumidity(temperature, humidity));
        }

        Vector4f colorOffset = _colorOffset.get(side);
        color.x *= colorOffset.x;
        color.y *= colorOffset.y;
        color.z *= colorOffset.z;
        color.w *= colorOffset.w;

        return color;
    }

    /**
     * Calculates the texture atlas offset for a given block type and a specific
     * side.
     *
     * @param side The side of the block
     * @return The texture offset
     */
    public Vector2f calcTextureOffsetFor(Side side) {
        Vector2f pos = getTextureAtlasPos(side);
        return new Vector2f((int) pos.x * TEXTURE_OFFSET, (int) pos.y * TEXTURE_OFFSET);
    }

    public void render() {
        if (isInvisible())
            return;

        if (_mesh == null) {
            Tessellator tessellator = new Tessellator();
            tessellator.setColor(new Vector4f(1, 1, 1, 1));
            if (_centerMesh != null) {
                tessellator.addMeshPart(_centerMesh);
            }
            for (Side dir : Side.values()) {
                BlockMeshPart part = _sideMesh.get(dir);
                if (part != null) {
                    float lightLevel = DIRECTION_LIT_LEVEL.get(dir);
                    tessellator.setColor(new Vector4f(lightLevel, lightLevel, lightLevel, lightLevel));
                    tessellator.addMeshPart(part);
                }
            }
            _mesh = tessellator.generateMesh();
        }

        if (getBlockForm() != BLOCK_FORM.BILLBOARD) {
            _mesh.render();
        } else {
            glDisable(GL11.GL_CULL_FACE);
            _mesh.render();
            glEnable(GL11.GL_CULL_FACE);
        }
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

    Block withBlockGroup(BlockGroup group) {
        _group = group;
        return this;
    }

    public Block withTranslucent(boolean translucent) {
        _internals.translucent = translucent;
        return this;
    }

    public Block withWaving(boolean waving) {
        _internals.waving = waving;
        return this;
    }

    public Block withInvisible(boolean invisible) {
        _internals.invisible = invisible;
        return this;
    }

    public Block withPenetrable(boolean penetrable) {
        _internals.penetrable = penetrable;
        return this;
    }

    public Block withCastsShadows(boolean castsShadows) {
        _internals.castsShadows = castsShadows;
        return this;
    }

    public Block withRenderBoundingBox(boolean renderBoundingBox) {
        _internals.renderBoundingBox = renderBoundingBox;
        return this;
    }

    public Block withAllowBlockAttachment(boolean allowBlockAttachment) {
        _internals.allowBlockAttachment = allowBlockAttachment;
        return this;
    }

    public Block withBypassSelectionRay(boolean bypassSelectionRay) {
        _internals.bypassSelectionRay = bypassSelectionRay;
        return this;
    }

    public Block withLiquid(boolean liquid) {
        _internals.liquid = liquid;
        return this;
    }

    public Block withBlockForm(BLOCK_FORM blockForm) {
        _internals.blockForm = blockForm;
        return this;
    }

    public Block withColorSource(COLOR_SOURCE colorSource) {
        _internals.colorSource = colorSource;
        return this;
    }

    public Block withLuminance(byte luminance) {
        _internals.luminance = luminance;
        return this;
    }

    public Block withHardness(byte hardness) {
        _internals.hardness = hardness;
        return this;
    }

    public Block withColorOffset(Side side, Vector4f colorOffset) {
        _colorOffset.put(side, colorOffset);
        return this;
    }

    public Block withTextureAtlasPos(Side side, Vector2f atlasPos) {
        _textureAtlasPos.put(side, atlasPos);
        return this;
    }

    public Block withTextureAtlasPosTopBottom(Vector2f atlasPos) {
        _textureAtlasPos.put(Side.TOP, atlasPos);
        _textureAtlasPos.put(Side.BOTTOM, atlasPos);
        return this;
    }

    public Block withTextureAtlasPos(Vector2f atlasPos) {
        for (Side side : Side.values()) {
            withTextureAtlasPos(side, atlasPos);
        }
        return this;
    }

    public Block withTextureAtlasPosMantle(Vector2f atlasPos) {
        for (Side side : Side.horizontalSides()) {
            _textureAtlasPos.put(side, atlasPos);
        }
        return this;
    }

    public Block withColorOffset(Vector4f colorOffset) {
        for (Side side : Side.values()) {
            withColorOffset(side, colorOffset);
        }
        return this;
    }

    public Block withCenterMesh(BlockMeshPart meshPart) {
        _centerMesh = meshPart;
        return this;
    }

    public Block withSideMesh(Side side, BlockMeshPart meshPart) {
        _sideMesh.put(side, meshPart);
        return this;
    }

    public Block withLoweredSideMesh(Side side, BlockMeshPart meshPart) {
        _loweredSideMesh.put(side, meshPart);
        return this;
    }

    public Block withFullSide(Side side, boolean full) {
        _fullSide.put(side, full);
        return this;
    }
    
    public void setColliders(List<AABB> colliders) {
        _colliders.clear();
        _colliders.addAll(colliders);
    }

    public COLOR_SOURCE getColorSource() {
        return _internals.colorSource;
    }

    public byte getHardness() {
        return _internals.hardness;
    }

    public Vector4f getColorOffset(Side side) {
        return _colorOffset.get(side);
    }

    public Vector2f getTextureAtlasPos(Side side) {
        return _textureAtlasPos.get(side);
    }

    public BLOCK_FORM getBlockForm() {
        return _internals.blockForm;
    }

    public String getTitle() {
        return _title;
    }

    public byte getId() {
        return _id;
    }

    public BlockGroup getBlockGroup() {
        return _group;
    }

    public boolean isBlockingSide(Side side) {
        return _fullSide.get(side);
    }

    public BlockMeshPart getSideMesh(Side side) {
        return _sideMesh.get(side);
    }

    public BlockMeshPart getCenterMesh() {
        return _centerMesh;
    }

    public BlockMeshPart getLoweredSideMesh(Side side) {
        return _loweredSideMesh.get(side);
    }

    public boolean isInvisible() {
        return _internals.invisible;
    }

    public boolean isPenetrable() {
        return _internals.penetrable;
    }

    public boolean isComplexCollider() {
        return !_colliders.isEmpty();
    }

    public boolean isCastsShadows() {
        return _internals.castsShadows;
    }

    public boolean isRenderBoundingBox() {
        return _internals.renderBoundingBox;
    }

    public byte getLuminance() {
        return _internals.luminance;
    }

    public boolean isDestructible() {
        return getHardness() > 0;
    }

    public boolean isAllowBlockAttachment() {
        return _internals.allowBlockAttachment;
    }

    public boolean isLiquid() {
        return _internals.liquid;
    }

    public boolean isWaving() {
        return _internals.waving;
    }

    public boolean isSelectionRayThrough() {
        return _internals.bypassSelectionRay;
    }

    public boolean isTranslucent() {
        return _internals.translucent;
    }

    /**
     * @param x x offset
     * @param y y offset
     * @param z z offset
     * @return An iterator over the colliders for this block, offset by the given values
     */
    public Iterable<AABB> getColliders(int x, int y, int z) {
        return new OffsetAABBIterator(_colliders, x, y, z);
    }

    private class OffsetAABBIterator implements Iterable<AABB>, Iterator<AABB> {
        Iterator<AABB> _source;
        int _x;
        int _y;
        int _z;

        public OffsetAABBIterator(Iterable<AABB> source, int x, int y, int z) {
            _source = source.iterator();
            _x = x;
            _y = y;
            _z = z;
        }

        public Iterator<AABB> iterator() {
            return this;
        }

        public boolean hasNext() {
            return _source.hasNext();
        }

        public AABB next() {
            AABB base = _source.next();
            return new AABB(new Vector3d(base.getPosition().x + _x, base.getPosition().y + _y, base.getPosition().z + _z),
                    base.getDimensions());
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns the AABB for a block at the given position.
     *
     * @param pos The position
     * @return The AABB
     */
    public static AABB AABBForBlockAt(Vector3d pos) {
        return new AABB(pos, new Vector3d(0.5f, 0.5f, 0.5f));
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
        return new AABB(new Vector3d(x, y, z), new Vector3d(0.5f, 0.5f, 0.5f));
    }

    public String toString() {
        return this.getClass().getSimpleName() + ":" + _title + ";id:" + _id;
    }

    public Block rotateClockwise(int steps) {
        Block block = clone();

        Quat4f rotation = new Quat4f();
        rotation.set(new AxisAngle4f(new Vector3f(0, -1, 0), (float) (0.5f * Math.PI * steps)));
        if (_centerMesh != null) {
            block._centerMesh = _centerMesh.rotate(rotation);
        }
        for (Side side : Side.values()) {
            Side rotatedSide = side.rotateClockwise(steps);
            block._fullSide.put(rotatedSide, _fullSide.get(side));
            block._colorOffset.put(rotatedSide, _colorOffset.get(side));
            block._textureAtlasPos.put(rotatedSide, _textureAtlasPos.get(side));

            BlockMeshPart sideMesh = _sideMesh.get(side);
            if (sideMesh != null) {
                block._sideMesh.put(rotatedSide, sideMesh.rotate(rotation));
            } else {
                block._sideMesh.put(rotatedSide, null);
            }
            BlockMeshPart loweredSideMesh = _loweredSideMesh.get(side);
            if (loweredSideMesh != null) {
                block._loweredSideMesh.put(rotatedSide, loweredSideMesh.rotate(rotation));
            } else {
                block._loweredSideMesh.put(rotatedSide, null);
            }
        }
        List<AABB> newAABBs = new ArrayList<AABB>(_colliders.size());
        for (AABB collider : _colliders) {
            newAABBs.add(rotateClockwiseAABB(collider, steps));
        }
        block._colliders = newAABBs;
        return block;
    }

    private AABB rotateClockwiseAABB(AABB collider, int steps) {
        if (steps < 0) {
            steps = -steps + 2;
        }
        steps = steps % 4;
        switch (steps) {
            case 1:
                return new AABB(new Vector3d(-collider.getPosition().z,collider.getPosition().y,collider.getPosition().x), new Vector3d(collider.getDimensions().z,collider.getDimensions().y, collider.getDimensions().x));
            case 2:
                return new AABB(new Vector3d(-collider.getPosition().x, collider.getPosition().y, -collider.getPosition().z), collider.getDimensions());
            case 3:
                return new AABB(new Vector3d(collider.getPosition().z,collider.getPosition().y,-collider.getPosition().x), new Vector3d(collider.getDimensions().z,collider.getDimensions().y, collider.getDimensions().x));
            default:
                return collider;
        }
    }

}
