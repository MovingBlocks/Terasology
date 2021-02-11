// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockRegion;

/**
 * A static, far away chunk that has only the data needed for rendering.
 */
public class LodChunk implements RenderableChunk {
    private static final String UNSUPPORTED_MESSAGE = "LOD chunks can only be used for certain rendering-related operations.";
    public final int scale;
    private Vector3ic position;
    private ChunkMesh mesh;

    public LodChunk(Vector3ic pos, ChunkMesh mesh, int scale) {
        position = pos;
        this.mesh = mesh;
        this.scale = scale;
    }

    @Override
    public Vector3ic getPosition() {
        return position;
    }

    @Override
    public Vector3i getPosition(Vector3i dest) {
        return dest.set(position);
    }


    @Override
    public Vector3i getChunkWorldOffset(Vector3i dest) {
        return position.mul(Chunks.CHUNK_SIZE, dest);
    }

    @Override
    public AABBfc getAABB() {
        Vector3f min = new Vector3f(getChunkWorldOffset(new Vector3i()));
        return new AABBf(min, new Vector3f(Chunks.CHUNK_SIZE).mul(1 << scale).add(min));
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public void setAnimated(boolean animated) {
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean hasMesh() {
        return true;
    }

    @Override
    public ChunkMesh getMesh() {
        return mesh;
    }

    @Override
    public void setMesh(ChunkMesh newMesh) {
        mesh = newMesh;
    }

    @Override
    public void disposeMesh() {
        if (mesh != null) {
            mesh.dispose();
            mesh = null;
        }
    }

    @Override
    public boolean isReady() {
        return mesh != null;
    }

    @Override
    public Block getBlock(Vector3ic pos) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public short getBlockId(Vector3ic pos) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public short getBlockId(int x, int y, int z) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public Block setBlock(int x, int y, int z, Block block) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public Block setBlock(Vector3ic pos, Block block) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public void setExtraData(int index, int x, int y, int z, int value) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public void setExtraData(int index, Vector3ic pos, int value) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int getExtraData(int index, int x, int y, int z) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int getExtraData(int index, Vector3ic pos) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int getChunkWorldOffsetX() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int getChunkWorldOffsetY() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int getChunkWorldOffsetZ() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public Vector3i chunkToWorldPosition(Vector3ic blockPos, Vector3i dest) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public Vector3i chunkToWorldPosition(int x, int y, int z, Vector3i dest) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int chunkToWorldPositionX(int x) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int chunkToWorldPositionY(int y) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int chunkToWorldPositionZ(int z) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int getChunkSizeX() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int getChunkSizeY() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int getChunkSizeZ() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public BlockRegion getRegion() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public int getEstimatedMemoryConsumptionInBytes() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public ChunkBlockIterator getBlockIterator() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public byte getSunlight(Vector3ic pos) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public boolean setSunlight(Vector3ic pos, byte amount) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public boolean setSunlight(int x, int y, int z, byte amount) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public byte getSunlightRegen(Vector3ic pos) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public byte getSunlightRegen(int x, int y, int z) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public boolean setSunlightRegen(Vector3ic pos, byte amount) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public boolean setSunlightRegen(int x, int y, int z, byte amount) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public byte getLight(Vector3ic pos) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public byte getLight(int x, int y, int z) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public boolean setLight(Vector3ic pos, byte amount) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public boolean setLight(int x, int y, int z, byte amount) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public void setDirty(boolean dirty) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public void setPendingMesh(ChunkMesh newPendingMesh) {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public boolean hasPendingMesh() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }

    @Override
    public ChunkMesh getPendingMesh() {
        throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
    }
}
