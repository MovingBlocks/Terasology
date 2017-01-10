/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.world;

import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.viewDistance.ViewDistance;

/**
 * Implementations of this class are responsible for rendering the whole 3D world,
 * inclusive of background.
 *
 * While 2D GUIs are dealt with elsewhere, it is in the remit of implementations of this class
 * to provide Augmented-Reality-Style user interfaces, i.e. boxes around selected objects
 * 3d-positioned labels above player's heads or held-in-hand tools.
 *
 * If this is the first time you look into this interface, you might want to start with
 * the update and render methods as they are central to a rendering implementation.
 */
public interface WorldRenderer {
    float BLOCK_LIGHT_POW = 0.96f;
    float BLOCK_LIGHT_SUN_POW = 0.96f;
    float BLOCK_INTENSITY_FACTOR = 0.7f;

    float getSecondsSinceLastFrame();

    Material getMaterial(String assetId);

    boolean isFirstRenderingStageForCurrentFrame();

    /**
     * This method is triggered when a chunk has been loaded.
     *
     * @param chunkPos a Vector3i providing the coordinates of the chunk that has just been loaded.
     */
    void onChunkLoaded(Vector3i chunkPos);

    /**
     * This method is triggered when a chunk has been unloaded.
     *
     * @param chunkPos a Vector3i providing the coordinates of the chunk that has just been unloaded.
     */
    void onChunkUnloaded(Vector3i chunkPos);

    /**
     * Lists the stages the rendering engine may go through on a given frame.
     *
     * At the time of the writing the rendering engine works in two conceptual modes: mono or stereo.
     *
     * In mono mode, used to render to a standard monitor, the rendering engine goes through
     * a single rendering stage every frame, called MONO.
     *
     * Stereo mode is for stereoscopic displays: every frame the rendering engine first goes through
     * the LEFT_EYE rendering stage and then the RIGHT_EYE rendering stage. Each stage produces
     * an image and the two images are then combined at the end of the RIGHT_EYE stage.
     *
     * Notice that the renderer has no explicit notion of mono/stereo. It only knows which stage is the current one.
     */
    enum RenderingStage {
        MONO,
        LEFT_EYE,
        RIGHT_EYE
    }

    /**
     * Retrieves the camera the world is being rendered from.
     *
     * @return a Camera object
     */
    Camera getActiveCamera();

    /**
     * Retrieves the camera positioned and oriented to look down on the world from the point of view
     * of the main light source. This camera is used to produce the shadow map.
     *
     * @return a Camera object
     */
    Camera getLightCamera();

    /**
     * Called potentially multiple times per frame, this method allows the renderer to trigger the update
     * of any data it requires with a higher frequency than just once per frame.
     *
     * This is usually not necessary. Normally a renderer is interested in data updated with the same frequency
     * as it renders to the display, once per frame. As such, implementations might want to leave this method
     * fairly lightweight, with most updates occurring somewhere at the beginning of the render() method execution
     * instead.
     *
     * However, there are effects for which it would be beneficial to have more frequent updates, for example
     * multi-segment motion blur.
     *
     * @param delta The elapsed time, in seconds, since the previous update.
     */
    void update(float delta);

    /**
     * Increase the triangles count, eventually retrieved through the getMetrics() method.
     *
     * @param increase An integer representing the triangle count increase.
     */
    void increaseTrianglesCount(int increase);

    /**
     * Increases the count of chunks that are not ready yet. The count is eventually retrieved through the getMetrics() method.
     *
     * @param increase An integer representing the not-ready chunk count increase.
     */
    void increaseNotReadyChunkCount(int increase);

    /**
     * This method triggers the execution of the rendering pipeline and, eventually, sends the output to the display
     * or to a file, when grabbing a screenshot.
     *
     * @param renderingStage "MONO" for standard rendering and "LEFT_EYE" or "RIGHT_EYE" for stereoscopic displays.
     */
    void render(RenderingStage renderingStage);

    /**
     * Request a refresh of the render task list. The refresh takes place before the next frame.
     */
    void requestTaskListRefresh();

    /**
     * Gives the chance to an implementation to deal with anything that might need a more careful disposal
     * than standard garbage collection would afford.
     */
    void dispose();

    /**
     * Used only during the early part of the WorldRenderer lifecycle, this method triggers the loading of
     * chunks around the camera and the generation of their meshes.
     *
     * @return Returns True if all necessary chunks have been loaded and their meshes have been generated, False otherwise.
     */
    // TODO: it might be desirable to separate the loading of chunks and mesh generation. The former may concern
    // TODO: systems dealing with beyond-the-horizon simulations. The latter usually only concerns the renderer.
    boolean pregenerateChunks();

    /**
     * Sets how far from the camera chunks are kept in memory and displayed.
     *
     * @param viewDistance a viewDistance value.
     */
    void setViewDistance(ViewDistance viewDistance);

    /**
     * Returns the intensity of the light at a given location due to the combination of main light (sun or moon)
     * and in-scene light sources, i.e. torches.
     *
     * @param coordinates a Vector3f providing the coordinates of the block to calculate the light intensity from.
     * @return a float value representing the intensity of the main light and in-scene lights combined
     */
    float getRenderingLightIntensityAt(Vector3f coordinates);

    /**
     * Returns the intensity of the main light (sun or moon) at the given location
     *
     * @param coordinates a vector3f providing the coordinates of the block to sample the intensity of the main light from
     * @return a float value representing the intensity of the main light at the given coordinates
     */
    float getMainLightIntensityAt(Vector3f coordinates);

    /**
     * Returns the intensity of the light at the given location due to in-scene sources,
     *
     * @param coordinates a vector3f providing the coordinates of the block to sample the intensity of in-scene sources from
     * @return a float value representing the total light intensity of the in-scene sources, at the given coordinates
     */
    float getBlockLightIntensityAt(Vector3f coordinates);

    /**
     * Returns the time-smoothed intensity of the main light (sun or moon) at the camera's location
     *
     * @return a float value representing the time-smoothed light intensity of the main light at the camera's coordinates
     */
    float getTimeSmoothedMainLightIntensity();

    /**
     * Returns True if the head of the player is underwater. False otherwise.
     *
     * Implementations must take in account waves if present.
     *
     * @return True if the head of the player is underwater. False otherwise.
     */
    // TODO: while useful to the renderer, this should probably be moved somewhere closer
    // TODO: to the camera-handling classes and perhaps renamed isCameraUnderWater()
    boolean isHeadUnderWater();

    /**
     * Returns the current tick, an always progressing time value animations are based on.
     *
     * @return the number of milliseconds since rendering started
     */
    float getMillisecondsSinceRenderingStart();

    /**
     * Returns the current rendering stage, i.e. mono, left eye or right eye.
     *
     * @return the current WorldRenderingStage
     */
    RenderingStage getCurrentRenderStage();

    /**
     * Returns a string containing a number of metrics in the format:
     *
     * metric label 1: value1\n
     * metric label 2: value2\n
     * .
     * .
     * metric label N: value3\n
     *
     * @return a string containing metrics labels and associated values, separated by new lines.
     */
    // TODO: transform this to return an object or a map. Consumers would then take care of formatting.
    String getMetrics();
}
