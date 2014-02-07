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

import javax.vecmath.Vector3f;

import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.math.AABB;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.DefaultRenderingProcess.StereoRenderState;
import org.terasology.rendering.world.Skysphere;
import org.terasology.rendering.world.ViewDistance;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkProvider;

public class HeadlessWorldRenderer implements WorldRenderer {

    private WorldProvider worldProvider;
    private ChunkProvider chunkProvider;

    private Camera noCamera = new NullCamera();

    /* PHYSICS */
    // TODO: Remove physics handling from world renderer
    private final BulletPhysics bulletPhysics;

    public HeadlessWorldRenderer(WorldProvider worldProvider, ChunkProvider chunkProvider, LocalPlayerSystem localPlayerSystem) {
        this.worldProvider = worldProvider;
        this.chunkProvider = chunkProvider;
        bulletPhysics = new BulletPhysics(worldProvider);
        localPlayerSystem.setPlayerCamera(noCamera);
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
    public PhysicsEngine getBulletRenderer() {
        return bulletPhysics;
    }

    @Override
    public void setPlayer(LocalPlayer localPlayer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void update(float delta) {
        // TODO Auto-generated method stub

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

}
