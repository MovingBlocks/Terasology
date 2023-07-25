// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.world;

import com.google.common.base.MoreObjects;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.rust.resource.ChunkGeometry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkBlockIterator;
import org.terasology.joml.geom.AABBfc;
import org.terasology.protobuf.EntityData;

import java.text.NumberFormat;

public class DummyChunk implements Chunk {
    private final Vector3ic chunkPos;
    private boolean dirty;
    private ChunkGeometry mesh;
    private boolean ready;

    public DummyChunk(Vector3ic position) {
        this.chunkPos = position;
    }

    @Override
    public Vector3ic getPosition() {
        return chunkPos;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return null;
    }

    @Override
    public Block setBlock(int x, int y, int z, Block block) {
        return null;
    }

    @Override
    public void setExtraData(int index, int x, int y, int z, int value) {

    }

    @Override
    public int getExtraData(int index, int x, int y, int z) {
        return 0;
    }

    @Override
    public Vector3i getChunkWorldOffset(Vector3i pos) {
        return null;
    }

    @Override
    public int getChunkWorldOffsetX() {
        return chunkPos.x() * getChunkSizeX();
    }

    @Override
    public int getChunkWorldOffsetY() {
        return chunkPos.y() * getChunkSizeY();
    }

    @Override
    public int getChunkWorldOffsetZ() {
        return chunkPos.z() * getChunkSizeZ();
    }

    @Override
    public Vector3i chunkToWorldPosition(int x, int y, int z, Vector3i dest) {
        return null;
    }

    @Override
    public int chunkToWorldPositionX(int x) {
        return 0;
    }

    @Override
    public int chunkToWorldPositionY(int y) {
        return 0;
    }

    @Override
    public int chunkToWorldPositionZ(int z) {
        return 0;
    }

    @Override
    public BlockRegionc getRegion() {
        return null;
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        return 0;
    }

    @Override
    public boolean setSunlight(int x, int y, int z, byte amount) {
        return false;
    }

    @Override
    public byte getSunlightRegen(int x, int y, int z) {
        return 0;
    }

    @Override
    public boolean setSunlightRegen(int x, int y, int z, byte amount) {
        return false;
    }

    @Override
    public byte getLight(int x, int y, int z) {
        return 0;
    }

    @Override
    public boolean setLight(int x, int y, int z, byte amount) {
        return false;
    }

    @Override
    public int getEstimatedMemoryConsumptionInBytes() {
        return 0;
    }

    @Override
    public ChunkBlockIterator getBlockIterator() {
        return null;
    }

    @Override
    public void markReady() {
        ready = true;
    }

    @Override
    public AABBfc getAABB() {
        return null;
    }

    @Override
    public void setMesh(ChunkGeometry newMesh) {
        this.mesh = newMesh;
    }

    @Override
    public void setAnimated(boolean animated) {

    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public boolean hasMesh() {
        return mesh != null;
    }

    @Override
    public ChunkGeometry getMesh() {
        return mesh;
    }

    @Override
    public void disposeMesh() {
        mesh = null;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public void deflate() {

    }

    @Override
    public void deflateSunlight() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void prepareForReactivation() {

    }

    @Override
    public EntityData.ChunkStore.Builder encode() {
        return null;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public String toString() {
        // I think using scientific notation for small integers adds a lot of noise.
        // Should we set `joml.format` to false?
        String pos = ((Vector3i) chunkPos).toString(NumberFormat.getIntegerInstance());
        return MoreObjects.toStringHelper(this)
                .addValue(pos)
                .add("dirty", dirty)
                .add("ready", ready)
                .add("mesh", mesh)
                .toString();
    }
}
