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
package org.terasology.rendering.opengl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.common.ThreadManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.utilities.Assets;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The term "Post Processing" is in analogy to what occurs in the world of Photography:
 * first a number of shots are taken on location or in a studio. The resulting images
 * are then post-processed to achieve specific aesthetic or technical goals.
 *
 * In the context of Terasology, the WorldRenderer takes care of "taking the shots
 * on location" (a world of 3D data such as vertices, uv coordinates and so on)
 * while the PostProcessor starts from the resulting 2D buffers (which in most
 * cases can be thought as images) and through a series of steps it generates the
 * image seen on screen or saved in a screenshot.
 *
 * Most PostProcessor methods represent a single step of the process. I.e the
 * method generateLightShafts() only adds light shafts to the image.
 *
 * Also, most methods follow a typical pattern of operations:
 * 1) enable a material (a shader bundled with its control parameters)
 * 2) bind zero or more 2D buffers to sample from
 * 3) bind and configure an FBO to output to
 * 4) render a quad, usually covering the whole buffer area, but sometimes less
 *
 * Finally, post-processing can be thought as a part of the rendering pipeline,
 * the set of conceptually atomic steps (sometimes called "passes") that eventually
 * leads to the final image. In practice however these steps are not really arranged
 * in a "line" where a given step takes the output of the previous step and provides
 * an input to the next one. Some steps have no dependencies on previous steps and
 * only provide the basis for further processing. Other steps have instead one or
 * more inputs and in turn become the input to one or more further passes.
 *
 * The rendering pipeline the methods of the PostProcessor contribute to is therefore
 * better thought as a Directed Acyclic Graph (DAG) in which each step is a node
 * that might or might not have previous nodes in input but will always have at least
 * an output, with the final writing to the default framebuffer (the display) or
 * to a screenshot file.
 */
// TODO: Future work should not only "think" in terms of a DAG-like rendering pipeline
// TODO: but actually implement one, see https://github.com/MovingBlocks/Terasology/issues/1741
public class PostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PostProcessor.class);

    private FrameBuffersManager buffersManager;
    private Materials materials = new Materials();
    private Buffers buffers = new Buffers();

    private boolean isTakingScreenshot;

    private RenderingConfig renderingConfig = CoreRegistry.get(Config.class).getRendering();

    private ThreadManager threadManager = CoreRegistry.get(ThreadManager.class);
    private float currentExposure;

    /**
     * Returns a PostProcessor instance. On instantiation the returned instance is not
     * yet usable. It lacks references to Material assets and Frame Buffer Objects (FBOs)
     * it needs to function.
     *
     * Method initializeMaterials() must be called to initialize the Materials references.
     * Method obtainStaticFBOs() must be called to initialize unchanging FBOs references.
     * Method refreshDynamicFBOs() must be called at least once to initialize all other FBOs references.
     *
     * @param buffersManager An FrameBuffersManager instance, required to obtain FBO references.
     */
    public PostProcessor(FrameBuffersManager buffersManager) {
        this.buffersManager = buffersManager;
    }

    /**
     * Initializes the internal references to Materials assets.
     *
     * Must be called at least once before the PostProcessor instance is in use. Failure to do so will result
     * in NullPointerExceptions. Calling it additional times shouldn't hurt but shouldn't be necessary either:
     * the asset system refreshes the assets behind the scenes if necessary.
     */
    public void initializeMaterials() {

        // final post-processing
        materials.ocDistortion = getMaterial("engine:prog.ocDistortion");
        materials.finalPost = getMaterial("engine:prog.post");           // TODO: rename shader to finalPost
        materials.debug = getMaterial("engine:prog.debug");
    }

    private Material getMaterial(String assetId) {
        return Assets.getMaterial(assetId).orElseThrow(() ->
                new RuntimeException("Failed to resolve required asset: '" + assetId + "'"));
    }

    /**
     * Fetches a number of FBOs from the FrameBuffersManager instance and initializes or refreshes
     * a number of internal references with them. These FBOs may become obsolete over the lifetime
     * of a PostProcessor instance and refreshing the internal references might be needed.
     * These FBOs are therefore referred to as "dynamic" FBOs.
     *
     * This method must be called at least once for the PostProcessor instance to function.
     * Failure to do so will result in NullPointerExceptions. It will then need to be called
     * every time the dynamic FBOs become obsolete and the internal references need to be
     * refreshed with new FBOs.
     */
    public void refreshDynamicFBOs() {
        // initial renderings
        buffers.sceneOpaque         = buffersManager.getFBO("sceneOpaque");
        buffers.sceneOpaquePingPong = buffersManager.getFBO("sceneOpaquePingPong");



        // final post-processing
        buffers.ocUndistorted   = buffersManager.getFBO("ocUndistorted");
        buffers.sceneFinal      = buffersManager.getFBO("sceneFinal");

    }

    /**
     * In a number of occasions the rendering loop swaps two important FBOs.
     * This method is used to trigger the PostProcessor instance into
     * refreshing the internal references to these FBOs.
     */
    public void refreshSceneOpaqueFBOs() {
        buffers.sceneOpaque         = buffersManager.getFBO("sceneOpaque");
        buffers.sceneOpaquePingPong = buffersManager.getFBO("sceneOpaquePingPong");
    }

    /**
     * Disposes of the PostProcessor instance.
     */
    // Not strictly necessary given the simplicity of the objects being nulled,
    // it is probably a good habit to have a dispose() method. It both properly
    // dispose support objects and it clearly marks the end of a PostProcessor
    // instance's lifecycle.
    public void dispose() {
        buffersManager = null;
    }

    /**
     * Returns the current exposure value (set in downsampleSceneAndUpdateExposure()).
     *
     * @return a float representing the current exposure value.
     */
    public float getExposure() {
        return currentExposure;
    }

    // TODO: Remove this method, temporarly here for DownSampleSceneAndUpdateExposure
    public void setExposure(float exposure) {
        this.currentExposure = exposure;
    }

    /**
     * Triggers a screenshot.
     *
     * Notice that this method just starts the process: screenshot data is captured and written to file
     * as soon as possible but not necessarily immediately after the trigger.
     */
    public void takeScreenshot() {
        isTakingScreenshot = true;
    }

    /**
     * Schedules the saving of screenshot data to file.
     *
     * Screenshot data from the GPU is obtained as soon as this method executes. However, the data is only scheduled
     * to be written to file, by submitting a task to the ThreadManager. The task is then executed as soon as possible
     * but not necessarily immediately.
     *
     * The file is then saved in the designated screenshot folder with a filename in the form:
     *
     *     Terasology-[yyMMddHHmmss]-[width]x[height].[format]
     *
     * If no screenshot data is available an error is logged and the method returns doing nothing.
     */
    public void saveScreenshot() {
        final ByteBuffer buffer = buffersManager.getSceneFinalRawData();
        if (buffer == null) {
            logger.error("No screenshot data available. No screenshot will be saved.");
            return;
        }

        int width = buffers.sceneFinal.width();
        int height = buffers.sceneFinal.height();

        Runnable task = () -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");

            final String format = renderingConfig.getScreenshotFormat();
            final String fileName = "Terasology-" + sdf.format(new Date()) + "-" + width + "x" + height + "." + format;
            Path path = PathManager.getInstance().getScreenshotPath().resolve(fileName);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int i = (x + width * y) * 4;
                    int r = buffer.get(i) & 0xFF;
                    int g = buffer.get(i + 1) & 0xFF;
                    int b = buffer.get(i + 2) & 0xFF;
                    image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                }
            }

            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
                ImageIO.write(image, format, out);
                logger.info("Screenshot '" + fileName + "' saved! ");
            } catch (IOException e) {
                logger.warn("Failed to save screenshot!", e);
            }
        };

        threadManager.submitTask("Write screenshot", task);
        isTakingScreenshot = false;
    }

    /**
     * Returns true if the rendering engine is not in the process of taking a screenshot.
     * Returns false if a screenshot is being taken.
     *
     * @return true if no screenshot is being taken, false otherwise
     */
    // for code readability it make sense to have this method rather than its opposite.
    public boolean isNotTakingScreenshot() {
        return !isTakingScreenshot;
    }


    private class Materials {
        // final post-processing
        public Material ocDistortion;
        public Material finalPost;
        public Material debug;
    }

    private class Buffers {
        // initial renderings
        public FBO sceneOpaque;
        public FBO sceneOpaquePingPong;



        // final post-processing
        public FBO ocUndistorted;
        public FBO sceneFinal;
    }
}
