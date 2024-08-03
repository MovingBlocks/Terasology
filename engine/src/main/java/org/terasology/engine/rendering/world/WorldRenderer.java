// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world;

import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.rendering.dag.RenderGraph;
import org.terasology.context.annotation.API;

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
@API
public interface WorldRenderer {
    float BLOCK_LIGHT_POW = 0.96f;
    float BLOCK_LIGHT_SUN_POW = 0.96f;
    float BLOCK_INTENSITY_FACTOR = 0.7f;

    /**
     * @return the number of seconds since last frame, as a float number.
     */
    float getSecondsSinceLastFrame();

    /**
     * Returns a Material instance corresponding to the asset identified by the input parameter.
     *
     * @param assetId a String uniquely identifying a Material.
     * @return a Material instance.
     */
    Material getMaterial(String assetId);

    /**
     * Informs the caller whether the renderer is in the first or second rendering stage of the current frame.
     *
     * In Stereo mode the first rendering stage corresponds to the rendering of the scene for the LEFT eye.
     * In Mono mode this method always returns True as there is only one rendering stage.
     *
     * @return a boolean: True if it is the first rendering stage of the current frame, False otherwise.
     */
    boolean isFirstRenderingStageForCurrentFrame();

    /**
     * This method is triggered when a chunk has been loaded.
     *
     * @param chunkPos a Vector3i providing the coordinates of the chunk that has just been loaded.
     */
    void onChunkLoaded(Vector3ic chunkPos);

    /**
     * This method is triggered when a chunk has been unloaded.
     *
     * @param chunkPos a Vector3i providing the coordinates of the chunk that has just been unloaded.
     */
    void onChunkUnloaded(Vector3ic chunkPos);

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
     * @return a SubmersibleCamera object
     */
    Camera getActiveCamera();

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
    //       systems dealing with beyond-the-horizon simulations. The latter usually only concerns the renderer.
    boolean pregenerateChunks();

    /**
     * Sets how far from the camera chunks are kept in memory and displayed.
     *
     * @param viewDistance a viewDistance value.
     * @param chunkLods the number of LOD levels to display beyond the loaded chunks.
     */
    void setViewDistance(ViewDistance viewDistance, int chunkLods);

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

    /***
     * Returns the RenderGraph.
     *
     * This object is used by Engine and Modules to add/remove Nodes to/from the rendering process.
     *
     * Nodes encapsulate the rendering functionality of the renderer. A node might provide a basic rendering
     * of the landscape, another might add deferred lighting to it while another might add tone mapping
     * to the resulting 2d image. Arbitrary features and effects can be added or removed by adding or removing
     * nodes to the graph and connecting them appropriately with other nodes.
     *
     * @return the RenderGraph containing the nodes used by the rendering process.
     */
    RenderGraph getRenderGraph();
}
