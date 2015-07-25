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
package org.terasology.rendering.world;

import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkProvider;

public interface WorldRenderer {
    float BLOCK_LIGHT_POW = 0.96f;
    float BLOCK_LIGHT_SUN_POW = 0.96f;
    float BLOCK_INTENSITY_FACTOR = 1.25f;

    void onChunkLoaded(Vector3i chunkPos);

    void onChunkUnloaded(Vector3i chunkPos);

    public enum WorldRenderingStage {
        MONO,
        LEFT_EYE,
        RIGHT_EYE
    }

    Camera getActiveCamera();

    Camera getLightCamera();

    ChunkProvider getChunkProvider();

    void setPlayer(LocalPlayer localPlayer);

    void update(float delta);

    void render(WorldRenderingStage renderingStage);

    void dispose();

    boolean pregenerateChunks();

    WorldProvider getWorldProvider();

    void changeViewDistance(ViewDistance viewDistance);

    float getSunlightValue();

    float getBlockLightValue();

    float getRenderingLightValueAt(Vector3f vector3f);

    float getSunlightValueAt(Vector3f worldPos);

    float getBlockLightValueAt(Vector3f worldPos);

    float getSmoothedPlayerSunlightValue();

    boolean isHeadUnderWater();

    Vector3f getTint();

    float getTick();

    WorldRenderingStage getCurrentRenderStage();

    String getMetrics();
}
