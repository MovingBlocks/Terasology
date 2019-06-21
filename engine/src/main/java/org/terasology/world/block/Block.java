/*
 * Copyright 2018 MovingBlocks
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

import org.terasology.math.Transform;
import org.terasology.math.geom.Quat4f;
import org.terasology.physics.shapes.CollisionShape;
import com.google.common.collect.Maps;

import org.terasology.math.Rotation;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.AABB;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector4f;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.primitives.BlockMeshGenerator;
import org.terasology.rendering.primitives.BlockMeshGeneratorSingleShape;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.utilities.collection.EnumBooleanMap;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.shapes.BlockMeshPart;
import org.terasology.world.block.sounds.BlockSounds;
import org.terasology.world.chunks.ChunkConstants;

import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

/**
 * Stores all information for a specific block type.
 */
public final class Block {

    // TODO: Use directional light(s) when rendering instead of this
    private static final Map<BlockPart, Float> DIRECTION_LIT_LEVEL = Maps.newEnumMap(BlockPart.class);


     // Initialize the LUTs
    static {
        DIRECTION_LIT_LEVEL.put(BlockPart.TOP, 0.9f);
        DIRECTION_LIT_LEVEL.put(BlockPart.BOTTOM, 0.9f);
        DIRECTION_LIT_LEVEL.put(BlockPart.FRONT, 1.0f);
        DIRECTION_LIT_LEVEL.put(BlockPart.BACK, 1.0f);
        DIRECTION_LIT_LEVEL.put(BlockPart.LEFT, 0.75f);
        DIRECTION_LIT_LEVEL.put(BlockPart.RIGHT, 0.75f);
        DIRECTION_LIT_LEVEL.put(BlockPart.CENTER, 0.8f);
    }

    private short id;
    private BlockUri uri;
    private String displayName = "Untitled block";
    private BlockFamily family;
    private Rotation rotation = Rotation.none();

    /* PROPERTIES */

    // Overall behavioural
    private boolean liquid;
    private boolean attachmentAllowed = true;
    private boolean replacementAllowed;
    private int hardness = 3;
    private boolean supportRequired;
    private EnumBooleanMap<Side> fullSide = new EnumBooleanMap<>(Side.class);
    private BlockSounds sounds;

    // Special rendering flags (TODO: clean this up)
    private boolean water;
    private boolean grass;
    private boolean ice;

    // Rendering related
    private BlockMeshGenerator meshGenerator = new BlockMeshGeneratorSingleShape(this);
    private boolean translucent;
    private boolean doubleSided;
    private boolean shadowCasting = true;
    private boolean waving;
    private byte luminance;
    private Vector3f tint = new Vector3f(0, 0, 0);
    private Map<BlockPart, BlockColorSource> colorSource = Maps.newEnumMap(BlockPart.class);
    private Map<BlockPart, Vector4f> colorOffsets = Maps.newEnumMap(BlockPart.class);

    // Collision related
    private boolean penetrable;
    private boolean targetable = true;
    private boolean climbable;

    // Physics
    private float mass = 10;
    private boolean debrisOnDestroy = true;
    private float friction = 0.5f;
    private float restitution = 0.0f;

    // Entity integration
    private Prefab prefab;
    private boolean keepActive;
    private EntityRef entity = EntityRef.NULL;
    private boolean lifecycleEventsRequired;

    // Inventory settings
    private boolean directPickup;
    private boolean stackable = true;

    private BlockAppearance primaryAppearance = new BlockAppearance();
    private Map<Side, BlockMeshPart> lowLiquidMesh = Maps.newEnumMap(Side.class);
    private Map<Side, BlockMeshPart> topLiquidMesh = Maps.newEnumMap(Side.class);

    /* Collision */
    private CollisionShape collisionShape;
    private Vector3f collisionOffset;
    private AABB bounds = AABB.createEmpty();

    /**
     * Init. a new block with default properties in place.
     */
    public Block() {
        for (BlockPart part : BlockPart.values()) {
            colorSource.put(part, DefaultColorSource.DEFAULT);
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

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public Side getDirection() {
        return rotation.rotate(Side.FRONT);
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
     * @return The sound set used by this block. Never null.
     */
    public BlockSounds getSounds() {
        return sounds;
    }

    public void setSounds(BlockSounds sounds) {
        this.sounds = sounds;
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
     * @return The BlockMeshGenerator that is used in rendering, null if invisible.
     */
    public BlockMeshGenerator getMeshGenerator() {
        return meshGenerator;
    }

    /**
     * @param meshGenerator The new BlockMeshGenerator to use in rendering this block.
     *                      If meshGenerator is null then this block is invisible.
     */
    public void setMeshGenerator(BlockMeshGenerator meshGenerator) {
        this.meshGenerator = meshGenerator;
    }

    /**
     * @return Whether this block needs to be rendered at all
     * @deprecated Use getMeshGenerator()==null instead.
     */
    @Deprecated
    public boolean isInvisible() {
        return meshGenerator == null;
    }

    /**
     * @param invisible Set if invisible
     * @deprecated Use setMeshGenerator() instead.
     */
    @Deprecated
    public void setInvisible(boolean invisible) {
        if (invisible) {
            this.meshGenerator = null;
        }
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

    /**
     * @param targetable True if this block can be targetted for interactions
     */
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

    /**
     * @param waving True to waves in the wind
     */
    public void setWaving(boolean waving) {
        this.waving = waving;
    }

    /**
     * @return Whether this block can be replaced freely by other blocks
     */
    public boolean isReplacementAllowed() {
        return replacementAllowed;
    }

    /**
     * @param replacementAllowed True to allow replace freely by other blocks
     */
    public void setReplacementAllowed(boolean replacementAllowed) {
        this.replacementAllowed = replacementAllowed;
    }

    /**
     * @return Whether blocks can be attached to this block
     */
    public boolean isAttachmentAllowed() {
        return attachmentAllowed;
    }

    /**
     * @param attachmentAllowed True to allow attach another block on this block
     */
    public void setAttachmentAllowed(boolean attachmentAllowed) {
        this.attachmentAllowed = attachmentAllowed;
    }

    /**
     * Check can a block attach in the side of this block
     *
     * @param side The side of attaching
     * @return False if this block is not allowed attachment or the side of this block is not full side
     */
    public boolean canAttachTo(Side side) {
        return attachmentAllowed && fullSide.get(side);
    }

    /**
     * @return Whether this block should be destroyed when no longer attached
     */
    public boolean isSupportRequired() {
        return supportRequired;
    }

    /**
     * @param supportRequired True to set the block should destroyed when no longer attached
     */
    public void setSupportRequired(boolean supportRequired) {
        this.supportRequired = supportRequired;
    }

    /**
     * @return The entity prefab for this block
     */
    public Optional<Prefab> getPrefab() {
        return Optional.ofNullable(prefab);
    }

    public void setPrefab(Prefab value) {
        this.prefab = value;
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
    public int getHardness() {
        return hardness;
    }

    /**
     * Indestructible if hardness is 0
     *
     * @param hardness how much damage it takes to destroy the block, indestructible if hardness is 0
     */
    public void setHardness(int hardness) {
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

    /**
     * @param luminance the light level produced by this block
     */
    public void setLuminance(byte luminance) {
        this.luminance = (byte) TeraMath.clamp(luminance, 0, ChunkConstants.MAX_LIGHT);
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

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public float getRestitution() {
        return restitution;
    }

    public void setRestitution(float restitution) {
        this.restitution = restitution;
    }

    public BlockColorSource getColorSource(BlockPart part) {
        return colorSource.get(part);
    }

    public void setColorSource(BlockColorSource colorSource) {
        for (BlockPart part : BlockPart.values()) {
            this.colorSource.put(part, colorSource);
        }
    }

    public void setColorSource(BlockPart part, BlockColorSource value) {
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


    /**
     * @return Standalone mesh
     * @deprecated Use getMeshGenerator() instead.
     */
    @Deprecated
    public Mesh getMesh() {
        if (meshGenerator != null) {
            return meshGenerator.getStandaloneMesh();
        }
        return new Tessellator().generateMesh(new ResourceUrn("engine", "blockmesh", uri.toString()));
    }

    public BlockMeshPart getLowLiquidMesh(Side side) {
        return lowLiquidMesh.get(side);
    }

    public void setLowLiquidMesh(Side side, BlockMeshPart meshPart) {
        lowLiquidMesh.put(side, meshPart);
    }

    public BlockMeshPart getTopLiquidMesh(Side side) {
        return topLiquidMesh.get(side);
    }

    public void setTopLiquidMesh(Side side, BlockMeshPart meshPart) {
        topLiquidMesh.put(side, meshPart);
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
     * @param part  The block side
     * @param biome The block's biome
     * @return The color offset
     */
    public Vector4f calcColorOffsetFor(BlockPart part, Biome biome) {
        BlockColorSource source = getColorSource(part);
        Vector4f color = source.calcColor(biome);

        Vector4f colorOffset = colorOffsets.get(part);
        color.x *= colorOffset.x;
        color.y *= colorOffset.y;
        color.z *= colorOffset.z;
        color.w *= colorOffset.w;

        return color;
    }

    /**
     * Set the collision box for the block
     *
     * @param offset The offset to the block's center
     * @param shape  The shape of collision box
     */
    public void setCollision(Vector3f offset, CollisionShape shape) {
        collisionShape = shape;
        collisionOffset = offset;
        bounds = shape.getAABB(new Transform(offset, new Quat4f(0, 0, 0, 1), 1.0f));
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
        return getBounds(new Vector3i(floatPos, RoundingMode.HALF_UP));
    }

    public void renderWithLightValue(float sunlight, float blockLight) {
        if (meshGenerator == null) {
            return;
        }

        Material mat = Assets.getMaterial("engine:prog.block").orElseThrow(() -> new RuntimeException("Missing engine material"));
        mat.activateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);

        mat.enable();
        mat.setFloat("sunlight", sunlight);
        mat.setFloat("blockLight", blockLight);


        Mesh mesh = meshGenerator.getStandaloneMesh();
        if (mesh != null) {
            mesh.render();
        }

        mat.deactivateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);
    }

    @Override
    public String toString() {
        return uri.toString();
    }

}
