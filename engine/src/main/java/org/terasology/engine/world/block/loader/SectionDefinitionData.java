// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.loader;

import com.google.common.collect.Maps;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.terasology.engine.world.block.DefaultColorSource;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.engine.world.block.sounds.BlockSounds;
import org.terasology.context.annotation.API;
import org.terasology.engine.world.block.BlockPart;
import org.terasology.engine.world.block.tiles.BlockTile;

import java.util.EnumMap;

@API
public class SectionDefinitionData {
    private String displayName = "";
    private boolean liquid;
    private int hardness = 0x3;

    private boolean attachmentAllowed = true;
    private boolean replacementAllowed;
    private boolean supportRequired;

    private boolean penetrable;
    private boolean targetable = true;
    private boolean climbable;

    private boolean invisible;
    private boolean translucent;
    private boolean doubleSided;
    private boolean shadowCasting = true;
    private boolean waving;
    private BlockSounds sounds;

    private byte luminance;

    private Vector3f tint = new Vector3f();

    private EnumMap<BlockPart, BlockTile> blockTiles = Maps.newEnumMap(BlockPart.class);
    private EnumMap<BlockPart, DefaultColorSource> colorSources;
    // Vector4f is used because it's deserializable, it will be converted into a Color later
    private EnumMap<BlockPart, Vector4f> colorOffsets;

    private float mass = 10f;
    private boolean debrisOnDestroy = true;
    private float friction = 0.5f;
    private float restitution = 0.0f;

    private EntityData entity = new EntityData();
    private InventoryData inventory = new InventoryData();

    private BlockShape shape;
    private boolean water;
    private boolean grass;
    private boolean ice;

    public SectionDefinitionData() {
        colorSources = Maps.newEnumMap(BlockPart.class);
        colorOffsets = Maps.newEnumMap(BlockPart.class);
        for (BlockPart part : BlockPart.values()) {
            colorSources.put(part, DefaultColorSource.DEFAULT);
            colorOffsets.put(part, new Vector4f(1, 1, 1, 1));
        }
    }

    public SectionDefinitionData(SectionDefinitionData other) {
        this.displayName = other.displayName;
        this.liquid = other.liquid;
        this.hardness = other.hardness;

        this.attachmentAllowed = other.attachmentAllowed;
        this.replacementAllowed = other.replacementAllowed;
        this.supportRequired = other.supportRequired;

        this.penetrable = other.penetrable;
        this.targetable = other.targetable;
        this.climbable = other.climbable;

        this.invisible = other.invisible;
        this.translucent = other.translucent;
        this.doubleSided = other.doubleSided;
        this.shadowCasting = other.shadowCasting;
        this.waving = other.waving;
        this.sounds = other.sounds;

        this.luminance = other.luminance;
        this.tint = new Vector3f(other.tint);

        this.blockTiles = new EnumMap<>(other.blockTiles);
        this.colorSources = new EnumMap<>(other.colorSources);
        this.colorOffsets = new EnumMap<>(other.colorOffsets);

        this.mass = other.mass;
        this.debrisOnDestroy = other.debrisOnDestroy;
        this.friction = other.friction;
        this.restitution = other.restitution;

        this.entity = new EntityData(other.entity);
        this.inventory = new InventoryData(other.inventory);

        this.shape = other.shape;

        this.water = other.water;
        this.grass = other.grass;
        this.ice = other.ice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isLiquid() {
        return liquid;
    }

    public void setLiquid(boolean liquid) {
        this.liquid = liquid;
    }

    public int getHardness() {
        return hardness;
    }

    public void setHardness(int hardness) {
        this.hardness = hardness;
    }

    public boolean isAttachmentAllowed() {
        return attachmentAllowed;
    }

    public void setAttachmentAllowed(boolean attachmentAllowed) {
        this.attachmentAllowed = attachmentAllowed;
    }

    public boolean isReplacementAllowed() {
        return replacementAllowed;
    }

    public void setReplacementAllowed(boolean replacementAllowed) {
        this.replacementAllowed = replacementAllowed;
    }

    public boolean isSupportRequired() {
        return supportRequired;
    }

    public void setSupportRequired(boolean supportRequired) {
        this.supportRequired = supportRequired;
    }

    public boolean isPenetrable() {
        return penetrable;
    }

    public void setPenetrable(boolean penetrable) {
        this.penetrable = penetrable;
    }

    public boolean isTargetable() {
        return targetable;
    }

    public void setTargetable(boolean targetable) {
        this.targetable = targetable;
    }

    public boolean isClimbable() {
        return climbable;
    }

    public void setClimbable(boolean climbable) {
        this.climbable = climbable;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    public boolean isTranslucent() {
        return translucent;
    }

    public void setTranslucent(boolean translucent) {
        this.translucent = translucent;
    }

    public boolean isDoubleSided() {
        return doubleSided;
    }

    public void setDoubleSided(boolean doubleSided) {
        this.doubleSided = doubleSided;
    }

    public boolean isShadowCasting() {
        return shadowCasting;
    }

    public void setShadowCasting(boolean shadowCasting) {
        this.shadowCasting = shadowCasting;
    }

    public boolean isWaving() {
        return waving;
    }

    public void setWaving(boolean waving) {
        this.waving = waving;
    }

    public BlockSounds getSounds() {
        return sounds;
    }

    public void setSounds(BlockSounds sounds) {
        this.sounds = sounds;
    }

    public byte getLuminance() {
        return luminance;
    }

    public void setLuminance(byte luminance) {
        this.luminance = luminance;
    }

    public Vector3f getTint() {
        return tint;
    }

    public void setTint(Vector3f tint) {
        this.tint = tint;
    }

    public EnumMap<BlockPart, BlockTile> getBlockTiles() {
        return blockTiles;
    }

    public void setAllTiles(BlockTile tile) {
        for (BlockPart part : BlockPart.values()) {
            blockTiles.put(part, tile);
        }
    }

    public EnumMap<BlockPart, DefaultColorSource> getColorSources() {
        return colorSources;
    }

    public void setAllColorSources(DefaultColorSource source) {
        for (BlockPart part : BlockPart.values()) {
            colorSources.put(part, source);
        }
    }

    public EnumMap<BlockPart, Vector4f> getColorOffsets() {
        return colorOffsets;
    }

    public void setAllColorOffsets(Vector4f offset) {
        for (BlockPart part : BlockPart.values()) {
            colorOffsets.put(part, offset);
        }
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public boolean isDebrisOnDestroy() {
        return debrisOnDestroy;
    }

    public void setDebrisOnDestroy(boolean debrisOnDestroy) {
        this.debrisOnDestroy = debrisOnDestroy;
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

    public EntityData getEntity() {
        return entity;
    }

    public void setEntity(EntityData entity) {
        this.entity = entity;
    }

    public InventoryData getInventory() {
        return inventory;
    }

    public void setInventory(InventoryData inventory) {
        this.inventory = inventory;
    }

    public BlockShape getShape() {
        return shape;
    }

    public void setShape(BlockShape shape) {
        this.shape = shape;
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
}
