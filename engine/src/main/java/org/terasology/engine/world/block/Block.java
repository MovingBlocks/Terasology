// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.joml.Quaternionf;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Side;
import org.terasology.engine.physics.shapes.CollisionShape;
import org.terasology.engine.rendering.primitives.BlockMeshGenerator;
import org.terasology.engine.rendering.primitives.BlockMeshGeneratorSingleShape;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.shapes.BlockMeshPart;
import org.terasology.engine.world.block.sounds.BlockSounds;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.joml.geom.AABBf;
import org.terasology.math.TeraMath;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

import java.util.Map;
import java.util.Optional;

/**
 * Stores all information for a specific block type.
 */
public final class Block {

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
    private final boolean[] fullSide = new boolean[Side.values().length];
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
    private Map<BlockPart, Colorc> colorOffsets = Maps.newEnumMap(BlockPart.class);


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
    private final BlockMeshPart[] lowLiquidMesh = new BlockMeshPart[Side.values().length];
    private final BlockMeshPart[] topLiquidMesh = new BlockMeshPart[Side.values().length];

    /* Collision */
    private CollisionShape collisionShape;
    private Vector3f collisionOffset;
    private AABBf bounds = new AABBf();

    /**
     * Init. a new block with default properties in place.
     */
    public Block() {
        for (BlockPart part : BlockPart.values()) {
            colorSource.put(part, DefaultColorSource.DEFAULT);
            colorOffsets.put(part, Color.white);
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
     * MeshGenerator drives how the mesh is produced for this single block.
     * by default this uses {@link BlockMeshGeneratorSingleShape} and can't be set to null.
     *
     * @return The BlockMeshGenerator that is used in rendering.
     */
    public BlockMeshGenerator getMeshGenerator() {
        return meshGenerator;
    }

    /**
     * @param meshGenerator The new {@link BlockMeshGenerator} to use in rendering this block.
     */
    public void setMeshGenerator(BlockMeshGenerator meshGenerator) {
        Preconditions.checkNotNull(meshGenerator);
        this.meshGenerator = meshGenerator;
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
        return attachmentAllowed && fullSide[side.ordinal()];
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
        this.luminance = (byte) TeraMath.clamp(luminance, 0, Chunks.MAX_LIGHT);
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

    public BlockAppearance getPrimaryAppearance() {
        return primaryAppearance;
    }

    public BlockAppearance getAppearance(Map<Side, Block> adjacentBlocks) {
        return primaryAppearance;
    }

    public void setPrimaryAppearance(BlockAppearance appearence) {
        this.primaryAppearance = appearence;
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

    public Colorc getColorOffset(BlockPart part) {
        return colorOffsets.get(part);
    }

    public void setColorOffset(BlockPart part, Colorc color) {
        colorOffsets.put(part, color);
    }

    public void setColorOffsets(Colorc color) {
        for (BlockPart part : BlockPart.values()) {
            colorOffsets.put(part, color);
        }
    }

    public BlockMeshPart getLowLiquidMesh(Side side) {
        return lowLiquidMesh[side.ordinal()];
    }

    public void setLowLiquidMesh(Side side, BlockMeshPart meshPart) {
        lowLiquidMesh[side.ordinal()] = meshPart;
    }

    public BlockMeshPart getTopLiquidMesh(Side side) {
        return topLiquidMesh[side.ordinal()];
    }

    public void setTopLiquidMesh(Side side, BlockMeshPart meshPart) {
        topLiquidMesh[side.ordinal()] = meshPart;
    }

    /**
     * @param side
     * @return Is the given side of the block "full" (a full square filling the side)
     */
    public boolean isFullSide(Side side) {
        return fullSide[side.ordinal()];
    }

    public void setFullSide(Side side, boolean full) {
        fullSide[side.ordinal()] = full;
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
        bounds = shape.getAABB(offset, new Quaternionf(0, 0, 0, 1), 1.0f);
    }

    public CollisionShape getCollisionShape() {
        return collisionShape;
    }

    public Vector3f getCollisionOffset() {
        return collisionOffset;
    }

    public AABBf getBounds(Vector3ic pos) {
        return new AABBf(bounds).translate(pos.x(), pos.y(), pos.z());
    }

    public AABBf getBounds(Vector3f floatPos) {
        return getBounds(new Vector3i(floatPos, RoundingMode.HALF_UP));
    }


    @Override
    public String toString() {
        return uri.toString();
    }

}
