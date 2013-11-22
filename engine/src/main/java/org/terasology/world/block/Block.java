/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.block;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.Maps;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.util.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.AABB;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.utilities.collection.EnumBooleanMap;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.shapes.BlockMeshPart;
import org.terasology.world.chunks.Chunk;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glIsEnabled;

/**
 * Stores all information for a specific block type.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
// TODO: Make this immutable, add a block builder class?
public final class Block {
    private static final Logger logger = LoggerFactory.getLogger(Block.class);

    // TODO: Use directional light(s) when rendering instead of this
    private static final Map<BlockPart, Float> DIRECTION_LIT_LEVEL = Maps.newEnumMap(BlockPart.class);

    /**
     * Different color sources for blocks.
     */
    public static enum ColorSource {
        DEFAULT {
            @Override
            public Vector4f calcColor(float temperature, float humidity) {
                return new Vector4f(1, 1, 1, 1);
            }
        },
        COLOR_LUT {
            @Override
            public Vector4f calcColor(float temperature, float humidity) {
                float prod = temperature * humidity;
                int rgbValue = colorLut.getRGB((int) ((1.0 - temperature) * 255.0), (int) ((1.0 - prod) * 255.0));

                Color c = new Color(rgbValue);
                return new Vector4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1.0f);
            }
        },
        FOLIAGE_LUT {
            @Override
            public Vector4f calcColor(float temperature, float humidity) {
                float prod = humidity * temperature;
                int rgbValue = foliageLut.getRGB((int) ((1.0 - temperature) * 255.0), (int) ((1.0 - prod) * 255.0));

                Color c = new Color(rgbValue);
                return new Vector4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1.0f);
            }
        };

        public abstract Vector4f calcColor(float temperature, float humidity);
    }

    /* LUTs */
    private static BufferedImage colorLut;
    private static BufferedImage foliageLut;

    /**
     * Init. the LUTs.
     */
    static {
        DIRECTION_LIT_LEVEL.put(BlockPart.TOP, 0.9f);
        DIRECTION_LIT_LEVEL.put(BlockPart.BOTTOM, 0.9f);
        DIRECTION_LIT_LEVEL.put(BlockPart.FRONT, 1.0f);
        DIRECTION_LIT_LEVEL.put(BlockPart.BACK, 1.0f);
        DIRECTION_LIT_LEVEL.put(BlockPart.LEFT, 0.75f);
        DIRECTION_LIT_LEVEL.put(BlockPart.RIGHT, 0.75f);
        DIRECTION_LIT_LEVEL.put(BlockPart.CENTER, 0.8f);
        try {
            // TODO: Read these from asset manager
            colorLut = ImageIO.read(ResourceLoader.getResource("assets/textures/grasscolor.png").openStream());
            foliageLut = ImageIO.read(ResourceLoader.getResource("assets/textures/foliagecolor.png").openStream());
        } catch (IOException e) {
            logger.error("Failed to load LUTs", e);
        }
    }

    private short id;
    private String displayName = "Untitled block";
    private BlockUri uri;
    private BlockFamily family;
    private Side direction = Side.FRONT;

    /* PROPERTIES */

    // Overall behavioural
    private boolean liquid;
    private boolean attachmentAllowed = true;
    private boolean replacementAllowed;
    private byte hardness = 0x3;
    private boolean supportRequired;
    private EnumBooleanMap<Side> fullSide = new EnumBooleanMap<>(Side.class);

    // Special rendering flags (TODO: clean this up)
    private boolean water;
    private boolean lava;
    private boolean grass;
    private boolean ice;

    // Rendering related
    private boolean invisible;
    private boolean translucent;
    private boolean doubleSided;
    private boolean shadowCasting = true;
    private boolean waving;
    private byte luminance;
    private Vector3f tint = new Vector3f(0, 0, 0);
    private Map<BlockPart, ColorSource> colorSource = Maps.newEnumMap(BlockPart.class);
    private Map<BlockPart, Vector4f> colorOffsets = Maps.newEnumMap(BlockPart.class);

    // Collision related
    private boolean penetrable;
    private boolean targetable = true;
    private boolean climbable;

    // Physics
    private float mass = 10;
    private boolean debrisOnDestroy = true;

    // Entity integration
    private String prefab = "";
    private boolean keepActive;
    private EntityRef entity = EntityRef.NULL;
    private boolean lifecycleEventsRequired;

    // Inventory settings
    private boolean directPickup;
    private boolean stackable = true;

    /* Mesh */
    private Mesh mesh;
    private BlockAppearance primaryAppearance = new BlockAppearance();
    // TODO: Remove once liquids have nicer generation
    private Map<Side, BlockMeshPart> loweredLiquidMesh = Maps.newEnumMap(Side.class);

    /* Collision */
    private CollisionShape collisionShape;
    private Vector3f collisionOffset;
    private AABB bounds = AABB.createEmpty();

    /**
     * Init. a new block with default properties in place.
     */
    public Block() {
        for (BlockPart part : BlockPart.values()) {
            colorSource.put(part, ColorSource.DEFAULT);
            colorOffsets.put(part, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
        }
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public BlockUri getURI() {
        return uri;
    }

    public void setUri(BlockUri uri) {
        this.uri = uri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BlockFamily getBlockFamily() {
        return family;
    }

    public void setBlockFamily(BlockFamily value) {
        this.family = value;
    }

    public void setDirection(Side direction) {
        this.direction = direction;
    }

    public Side getDirection() {
        return direction;
    }

    /**
     * @return Whether this block should be rendered Double Sided
     */
    public boolean isDoubleSided() {
        return doubleSided;
    }

    public void setDoubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
    }

    /**
     * A liquid has some special handling around shape
     *
     * @return Whether this block is a liquid
     */
    public boolean isLiquid() {
        return liquid;
    }

    public void setLiquid(boolean liquid) {
        this.liquid = liquid;
    }

    public boolean isWater() {
        return water;
    }

    public void setWater(boolean water) {
        this.water = water;
    }

    public boolean isLava() {
        return lava;
    }

    public void setLava(boolean lava) {
        this.lava = lava;
    }

    public boolean isGrass() {
        return grass;
    }

    public void setGrass(boolean grass) {
        this.grass = grass;
    }

    public boolean isIce() {
        return ice;
    }

    public void setIce(boolean ice) {
        this.ice = ice;
    }


    /**
     * @return Whether this block is translucent/alpha masked
     */
    public boolean isTranslucent() {
        return translucent;
    }

    public void setTranslucent(boolean translucent) {
        this.translucent = translucent;
    }

    /**
     * @return Whether this block needs to be rendered at all
     */
    public boolean isInvisible() {
        return invisible;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    /**
     * A block is penetrable if it does not block solid objects.
     *
     * @return Whether this block allows solid objects to pass through it.
     */
    public boolean isPenetrable() {
        return penetrable;
    }

    public void setPenetrable(boolean penetrable) {
        this.penetrable = penetrable;
    }

    /**
     * @return Does this block create a slight shadow around it
     */
    // TODO: Remove this once SSAO is implemented?
    public boolean isShadowCasting() {
        return shadowCasting && luminance == 0;
    }

    public void setShadowCasting(boolean shadowCasting) {
        this.shadowCasting = shadowCasting;
    }

    /**
     * @return Can this block be targetted for interactions
     */
    public boolean isTargetable() {
        return targetable;
    }

    public void setTargetable(boolean targetable) {
        this.targetable = targetable;
    }

    public boolean isClimbable() {
        return climbable;
    }

    public void setClimbable(boolean value) {
        this.climbable = value;
    }

    /**
     * @return Whether this block waves in the wind
     */
    public boolean isWaving() {
        return waving;
    }

    public void setWaving(boolean waving) {
        this.waving = waving;
    }

    /**
     * @return Whether this block can be replaced freely by other blocks
     */
    public boolean isReplacementAllowed() {
        return replacementAllowed;
    }

    public void setReplacementAllowed(boolean replacementAllowed) {
        this.replacementAllowed = replacementAllowed;
    }

    /**
     * @return Whether blocks can be attached to this block
     */
    public boolean isAttachmentAllowed() {
        return attachmentAllowed;
    }

    public void setAttachmentAllowed(boolean attachmentAllowed) {
        this.attachmentAllowed = attachmentAllowed;
    }

    public boolean canAttachTo(Side side) {
        return attachmentAllowed && fullSide.get(side);
    }

    /**
     * @return Whether this block should be destroyed when no longer attached
     */
    public boolean isSupportRequired() {
        return supportRequired;
    }

    public void setSupportRequired(boolean supportRequired) {
        this.supportRequired = supportRequired;
    }

    /**
     * @return The entity prefab for this block
     */
    public String getPrefab() {
        return prefab;
    }

    public void setPrefab(String value) {
        prefab = (value == null) ? "" : value;
    }

    public boolean isKeepActive() {
        return keepActive;
    }

    public void setKeepActive(boolean keepActive) {
        this.keepActive = keepActive;
    }

    public EntityRef getEntity() {
        return entity;
    }

    public void setEntity(EntityRef entity) {
        this.entity = entity;
    }

    public void setLifecycleEventsRequired(boolean lifecycleEventsRequired) {
        this.lifecycleEventsRequired = lifecycleEventsRequired;
    }

    public boolean isLifecycleEventsRequired() {
        return lifecycleEventsRequired;
    }

    /**
     * @return Whether this block should go directly into a character's inventory when harvested
     */
    public boolean isDirectPickup() {
        return directPickup;
    }

    public void setDirectPickup(boolean directPickup) {
        this.directPickup = directPickup;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    /**
     * @return How much damage it takes to destroy the block
     */
    public byte getHardness() {
        return hardness;
    }

    public void setHardness(byte hardness) {
        this.hardness = hardness;
    }

    public boolean isDestructible() {
        return getHardness() > 0;
    }

    /**
     * @return The light level produced by this block
     */
    public byte getLuminance() {
        return luminance;
    }

    public void setLuminance(byte luminance) {
        this.luminance = (byte) TeraMath.clamp(luminance, 0, Chunk.MAX_LIGHT);
    }

    public Vector3f getTint() {
        return tint;
    }

    public void setTint(Vector3f tint) {
        this.tint.set(tint);
    }

    /**
     * @return Whether physics debris of the block is created when the block is destroyed
     */
    public boolean isDebrisOnDestroy() {
        return debrisOnDestroy;
    }

    public void setDebrisOnDestroy(boolean debrisOnDestroy) {
        this.debrisOnDestroy = debrisOnDestroy;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public ColorSource getColorSource(BlockPart part) {
        return colorSource.get(part);
    }

    public void setColorSource(ColorSource colorSource) {
        for (BlockPart part : BlockPart.values()) {
            this.colorSource.put(part, colorSource);
        }
    }

    public void setColorSource(BlockPart part, ColorSource value) {
        this.colorSource.put(part, value);
    }

    public Vector4f getColorOffset(BlockPart part) {
        return colorOffsets.get(part);
    }

    public void setColorOffset(BlockPart part, Vector4f color) {
        colorOffsets.put(part, color);
    }

    public void setColorOffsets(Vector4f color) {
        for (BlockPart part : BlockPart.values()) {
            colorOffsets.put(part, color);
        }
    }

    public BlockAppearance getPrimaryAppearance() {
        return primaryAppearance;
    }

    public BlockAppearance getAppearance(Map<Side, Block> adjacentBlocks) {
        return primaryAppearance;
    }

    public void setPrimaryAppearance(BlockAppearance appearence) {
        this.primaryAppearance = appearence;
    }

    public Mesh getMesh() {
        if (mesh == null) {
            generateMesh();
        }
        return mesh;
    }

    public BlockMeshPart getLoweredLiquidMesh(Side side) {
        return loweredLiquidMesh.get(side);
    }

    public void setLoweredLiquidMesh(Side side, BlockMeshPart meshPart) {
        loweredLiquidMesh.put(side, meshPart);
    }

    /**
     * @param side
     * @return Is the given side of the block "full" (a full square filling the side)
     */
    public boolean isFullSide(Side side) {
        return fullSide.get(side);
    }

    public void setFullSide(Side side, boolean full) {
        fullSide.put(side, full);
    }

    /**
     * Calculates the color offset for a given block type and a specific
     * side of the block.
     *
     * @param part        The block side
     * @param temperature The temperature
     * @param humidity    The humidity
     * @return The color offset
     */
    public Vector4f calcColorOffsetFor(BlockPart part, float temperature, float humidity) {
        ColorSource source = getColorSource(part);
        Vector4f color = source.calcColor(temperature, humidity);

        Vector4f colorOffset = colorOffsets.get(part);
        color.x *= colorOffset.x;
        color.y *= colorOffset.y;
        color.z *= colorOffset.z;
        color.w *= colorOffset.w;

        return color;
    }

    public void setCollision(Vector3f offset, CollisionShape shape) {
        collisionShape = shape;
        collisionOffset = offset;
        Transform t = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), offset, 1.0f));
        Vector3f min = new Vector3f();
        Vector3f max = new Vector3f();
        shape.getAabb(t, min, max);

        bounds = AABB.createMinMax(min, max);
    }

    public CollisionShape getCollisionShape() {
        return collisionShape;
    }

    public Vector3f getCollisionOffset() {
        return collisionOffset;
    }

    public AABB getBounds(Vector3i pos) {
        return bounds.move(pos.toVector3f());
    }

    public AABB getBounds(Vector3f floatPos) {
        return getBounds(new Vector3i(floatPos, 0.5f));
    }

    public void renderWithLightValue(float sunlight, float blockLight) {
        if (isInvisible()) {
            return;
        }

        Material mat = Assets.getMaterial("engine:block");
        mat.activateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);

        mat.enable();
        mat.setFloat("sunlight", sunlight);
        mat.setFloat("blockLight", blockLight);

        if (mesh == null) {
            generateMesh();
        } else if (mesh.isDisposed()) {
            logger.error("Cannot render disposed mesh");
            return;
        }

        if (!isDoubleSided() || !glIsEnabled(GL11.GL_CULL_FACE)) {
            mesh.render();
        } else {
            glDisable(GL11.GL_CULL_FACE);
            mesh.render();
            glEnable(GL11.GL_CULL_FACE);
        }

        mat.deactivateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);
    }

    private void generateMesh() {
        Tessellator tessellator = new Tessellator();
        for (BlockPart dir : BlockPart.values()) {
            BlockMeshPart part = primaryAppearance.getPart(dir);
            if (part != null) {
                float lightLevel = DIRECTION_LIT_LEVEL.get(dir);
                tessellator.setColor(new Vector4f(lightLevel, lightLevel, lightLevel, lightLevel));
                tessellator.addMeshPart(part);
            }
        }
        mesh = tessellator.generateMesh(new AssetUri(AssetType.MESH, uri.toString()));
    }

    @Override
    public String toString() {
        return uri.toString();
    }

}
