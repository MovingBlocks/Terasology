// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.renderer;

import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.cameras.SubmersibleCamera;
import org.terasology.engine.rendering.dag.RenderGraph;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.RenderableChunk;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HeadlessWorldRenderer implements WorldRenderer {

    private static final int MAX_CHUNKS = ViewDistance.MEGA.getChunkDistance().x()
            * ViewDistance.MEGA.getChunkDistance().y()
            * ViewDistance.MEGA.getChunkDistance().z();

    private WorldProvider worldProvider;
    private ChunkProvider chunkProvider;

    private Camera noCamera = new NullCamera(null, null);

    /* CHUNKS */
    private boolean pendingChunks;
    private final List<Chunk> chunksInProximity = Lists.newArrayListWithCapacity(MAX_CHUNKS);
    private Vector3i chunkPos = new Vector3i();

    private Config config;

    public HeadlessWorldRenderer(Context context) {
        this.worldProvider = context.get(WorldProvider.class);
        this.chunkProvider = context.get(ChunkProvider.class);
        config = context.get(Config.class);
    }

    @Override
    public float getSecondsSinceLastFrame() {
        return 0;
    }

    @Override
    public Material getMaterial(String assetId) {
        return null;
    }

    @Override
    public boolean isFirstRenderingStageForCurrentFrame() {
        return false;
    }

    @Override
    public void onChunkLoaded(Vector3ic pos) {

    }

    @Override
    public void onChunkUnloaded(Vector3ic pos) {

    }

    @Override
    public SubmersibleCamera getActiveCamera() {
        return (SubmersibleCamera) noCamera;
    }

    @Override
    public void update(float delta) {

        worldProvider.processPropagation();

        // Free unused space
        PerformanceMonitor.startActivity("Update Chunk Cache");
        chunkProvider.update();
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Update Close Chunks");
        updateChunksInProximity(false);
        PerformanceMonitor.endActivity();
    }

    @Override
    public void increaseTrianglesCount(int increase) {
        // we are not going to count triangles in headless
    }

    @Override
    public void increaseNotReadyChunkCount(int increase) {
        // we are not going to count not ready chunks in headless
    }

    @Override
    public void render(RenderingStage mono) {
        // TODO Auto-generated method stub
    }

    @Override
    public void requestTaskListRefresh() {

    }

    @Override
    public void dispose() {
        worldProvider.dispose();

    }

    @Override
    public boolean pregenerateChunks() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setViewDistance(ViewDistance viewDistance, int chunkLods) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getRenderingLightIntensityAt(Vector3f vector3f) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getMainLightIntensityAt(Vector3f worldPos) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getBlockLightIntensityAt(Vector3f worldPos) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getTimeSmoothedMainLightIntensity() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getMillisecondsSinceRenderingStart() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public RenderingStage getCurrentRenderStage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMetrics() {
        return "";
    }

    @Override
    public RenderGraph getRenderGraph() {
        return null;
    }

    /**
     * Updates the list of chunks around the player.
     *
     * @param force Forces the update
     * @return True if the list was changed
     */
    public boolean updateChunksInProximity(boolean force) {
        Vector3i newChunkPos = calcCamChunkOffset();

        // TODO: This should actually be done based on events from the ChunkProvider on new chunk availability/old chunk removal
        boolean chunksCurrentlyPending = false;
        if (!newChunkPos.equals(chunkPos) || force || pendingChunks) {
            Vector3ic viewingDistance = config.getRendering().getViewDistance().getChunkDistance();
            BlockRegion viewRegion = new BlockRegion(newChunkPos)
                    .expand(new Vector3i(viewingDistance.x() / 2, viewingDistance.y() / 2, viewingDistance.z() / 2));
            if (chunksInProximity.size() == 0 || force || pendingChunks) {
                // just add all visible chunks
                chunksInProximity.clear();
                for (Vector3ic chunkPosition : viewRegion) {
                    Chunk c = chunkProvider.getChunk(chunkPosition);
                    if (c != null && worldProvider.getLocalView(c.getPosition(new Vector3i())) != null) {
                        chunksInProximity.add(c);
                    } else {
                        chunksCurrentlyPending = true;
                    }
                }
            } else {
                BlockRegion oldRegion = new BlockRegion(chunkPos)
                        .expand(new Vector3i(viewingDistance.x() / 2, viewingDistance.y() / 2, viewingDistance.z() / 2));

                // remove
                for (Vector3ic candidateForRemove : viewRegion) {
                    if (!oldRegion.contains(candidateForRemove)) {
                        Chunk c = chunkProvider.getChunk(candidateForRemove);
                        if (c != null) {
                            chunksInProximity.remove(c);
                            c.disposeMesh();
                        }
                    }
                }

                // add
                for (Vector3ic chunkPosition : viewRegion) {
                    Chunk c = chunkProvider.getChunk(chunkPosition);
                    if (c != null && worldProvider.getLocalView(c.getPosition(new Vector3i())) != null) {
                        chunksInProximity.add(c);
                    } else {
                        chunksCurrentlyPending = true;
                    }
                }
            }

            chunkPos.set(newChunkPos);
            pendingChunks = chunksCurrentlyPending;

            Collections.sort(chunksInProximity, new ChunkFrontToBackComparator());

            return true;
        }

        return false;
    }

    /**
     * Chunk position of the player.
     *
     * @return The player offset on the x-axis
     */
    private Vector3i calcCamChunkOffset() {
        return new Vector3i((int) (getActiveCamera().getPosition().x / Chunks.SIZE_X),
                (int) (getActiveCamera().getPosition().y / Chunks.SIZE_Y),
                (int) (getActiveCamera().getPosition().z / Chunks.SIZE_Z));
    }

    private float distanceToCamera(RenderableChunk chunk) {
        Vector3f result = chunk.getRenderPosition();
        result.add(Chunks.SIZE_X / 2f, Chunks.SIZE_Y / 2f, Chunks.SIZE_Z / 2f);
        result.sub(getActiveCamera().getPosition());

        return result.length();
    }

    private class ChunkFrontToBackComparator implements Comparator<RenderableChunk> {

        @Override
        public int compare(RenderableChunk o1, RenderableChunk o2) {
            double distance = distanceToCamera(o1);
            double distance2 = distanceToCamera(o2);

            if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }

            if (distance == distance2) {
                return 0;
            }

            return distance2 > distance ? -1 : 1;
        }
    }
}
