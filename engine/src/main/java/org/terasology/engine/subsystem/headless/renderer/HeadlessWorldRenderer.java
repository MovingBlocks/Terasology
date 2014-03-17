/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.headless.renderer;

import com.google.common.collect.Lists;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.math.AABB;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.DefaultRenderingProcess.StereoRenderState;
import org.terasology.rendering.world.Skysphere;
import org.terasology.rendering.world.ViewDistance;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldCommands;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.internal.ChunkImpl;

import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class HeadlessWorldRenderer implements WorldRenderer {

    private static final int MAX_CHUNKS = ViewDistance.MEGA.getChunkDistance().x * ViewDistance.MEGA.getChunkDistance().y * ViewDistance.MEGA.getChunkDistance().z;

    private WorldProvider worldProvider;
    private ChunkProvider chunkProvider;

    private Camera noCamera = new NullCamera();

    /* CHUNKS */
    private boolean pendingChunks;
    private final List<ChunkImpl> chunksInProximity = Lists.newArrayListWithCapacity(MAX_CHUNKS);
    private Vector3i chunkPos = new Vector3i();

    private Config config;

    public HeadlessWorldRenderer(WorldProvider worldProvider, ChunkProvider chunkProvider, LocalPlayerSystem localPlayerSystem) {
        this.worldProvider = worldProvider;
        this.chunkProvider = chunkProvider;

        localPlayerSystem.setPlayerCamera(noCamera);
        config = CoreRegistry.get(Config.class);
        CoreRegistry.get(ComponentSystemManager.class).register(new WorldCommands(chunkProvider));
    }

    @Override
    public void onChunkLoaded(Vector3i pos) {

    }

    @Override
    public void onChunkUnloaded(Vector3i pos) {

    }

    @Override
    public Camera getActiveCamera() {
        return noCamera;
    }

    @Override
    public Camera getLightCamera() {
        return noCamera;
    }

    @Override
    public ChunkProvider getChunkProvider() {
        return chunkProvider;
    }

    @Override
    public WorldProvider getWorldProvider() {
        return worldProvider;
    }

    @Override
    public void setPlayer(LocalPlayer localPlayer) {
        // TODO Auto-generated method stub

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
    public void render(StereoRenderState mono) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean pregenerateChunks() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void changeViewDistance(ViewDistance viewDistance) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getDaylight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getSunlightValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getBlockLightValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getRenderingLightValueAt(Vector3f vector3f) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getSunlightValueAt(Vector3f worldPos) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getBlockLightValueAt(Vector3f worldPos) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getSmoothedPlayerSunlightValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isHeadUnderWater() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Vector3f getTint() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float getTick() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Skysphere getSkysphere() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAABBVisible(AABB aabb) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public WorldRenderingStage getCurrentRenderStage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getMetrics() {
        return "";
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
            Vector3i viewingDistance = config.getRendering().getViewDistance().getChunkDistance();
            Region3i viewRegion = Region3i.createFromCenterExtents(newChunkPos, new Vector3i(viewingDistance.x / 2, viewingDistance.y / 2, viewingDistance.z / 2));
            if (chunksInProximity.size() == 0 || force || pendingChunks) {
                // just add all visible chunks
                chunksInProximity.clear();
                for (Vector3i chunkPosition : viewRegion) {
                    ChunkImpl c = chunkProvider.getChunk(chunkPosition);
                    if (c != null && c.isReady() && worldProvider.getLocalView(c.getPos()) != null) {
                        chunksInProximity.add(c);
                    } else {
                        chunksCurrentlyPending = true;
                    }
                }
            } else {
                Region3i oldRegion = Region3i.createFromCenterExtents(chunkPos, new Vector3i(viewingDistance.x / 2, viewingDistance.y / 2, viewingDistance.z / 2));

                Iterator<Vector3i> chunksForRemove = oldRegion.subtract(viewRegion);
                // remove
                while (chunksForRemove.hasNext()) {
                    Vector3i r = chunksForRemove.next();
                    ChunkImpl c = chunkProvider.getChunk(r);
                    if (c != null) {
                        chunksInProximity.remove(c);
                        c.disposeMesh();
                    }
                }

                // add
                for (Vector3i chunkPosition : viewRegion) {
                    ChunkImpl c = chunkProvider.getChunk(chunkPosition);
                    if (c != null && c.isReady() && worldProvider.getLocalView(c.getPos()) != null) {
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
        return new Vector3i((int) (getActiveCamera().getPosition().x / ChunkConstants.SIZE_X),
                (int) (getActiveCamera().getPosition().y / ChunkConstants.SIZE_Y),
                (int) (getActiveCamera().getPosition().z / ChunkConstants.SIZE_Z));
    }

    private static float distanceToCamera(ChunkImpl chunk) {
        Vector3f result = new Vector3f((chunk.getPos().x + 0.5f) * ChunkConstants.SIZE_X, 0, (chunk.getPos().z + 0.5f) * ChunkConstants.SIZE_Z);

        Vector3f cameraPos = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
        result.x -= cameraPos.x;
        result.z -= cameraPos.z;

        return result.length();
    }

    private static class ChunkFrontToBackComparator implements Comparator<ChunkImpl> {

        @Override
        public int compare(ChunkImpl o1, ChunkImpl o2) {
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
