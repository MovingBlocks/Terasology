// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameScheduler;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.persistence.internal.GamePreviewImageProvider;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.opengl.fbms.DisplayResolutionDependentFbo;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.Date;

// TODO: Future work should not only "think" in terms of a DAG-like rendering pipeline
// TODO: but actually implement one, see https://github.com/MovingBlocks/Terasology/issues/1741
public class ScreenGrabber {
    private static final Logger logger = LoggerFactory.getLogger(ScreenGrabber.class);

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyMMddHHmmss");
    private static final String SCREENSHOT_FILENAME_PATTERN = "Terasology-%s-%dx%d.%s";

    private RenderingConfig renderingConfig;
    private SystemConfig systemConfig;
    private float currentExposure;
    private boolean isTakingScreenshot;
    private DisplayResolutionDependentFbo displayResolutionDependentFBOs;
    private Path savedGamePath;
    private boolean savingGamePreview;

    /**
     * @param context
     */
    public ScreenGrabber(Context context) {
        this(context.get(Config.class), context.get(SystemConfig.class));
    }

    @Inject
    public ScreenGrabber(Config config, SystemConfig systemConfig) {
        renderingConfig = config.getRendering();
        this.systemConfig = systemConfig;
    }

    /**
     * Returns the current exposure value (set in downsampleSceneAndUpdateExposure()).
     *
     * @return a float representing the current exposure value.
     */
    public float getExposure() {
        return currentExposure;
    }

    // TODO: Remove this method, temporarily here for DownSampleSceneAndUpdateExposure
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
     * The file is then saved in the designated screenshot folder, or if it's a game preview image, it'll be placed in the save game folder.
     *
     * If no screenshot data is available an error is logged and the method returns doing nothing.
     *
     * While debug mode is on, a non-preview screenshot is left pending. The scene buffer is not read here;
     * {@link #saveCompositedScreenshot(DisplayDevice)} reads the default framebuffer after the UI has been drawn.
     */
    public void saveScreenshot() {
        if (capturesCompositedFramebuffer()) {
            return;
        }

        // Since ScreenGrabber is initialized before DisplayResolutionDependentFbo (because the latter contains a reference to the former)
        // on first call on saveScreenshot() displayResolutionDependentFBOs will be null.
        if (displayResolutionDependentFBOs == null) {
            displayResolutionDependentFBOs = CoreRegistry.get(DisplayResolutionDependentFbo.class);
        }

        FBO sceneFinalFbo = displayResolutionDependentFBOs.get(DisplayResolutionDependentFbo.FINAL_BUFFER);

        final ByteBuffer buffer = sceneFinalFbo.getColorBufferRawData();
        if (buffer == null) {
            logger.error("No screenshot data available. No screenshot will be saved.");
            return;
        }

        int width = sceneFinalFbo.width();
        int height = sceneFinalFbo.height();

        Runnable task;
        if (savingGamePreview) {
            task = () -> saveGamePreviewTask(buffer, width, height);
            this.savingGamePreview = false;
        } else {
            task = () -> saveScreenshotTask(buffer, width, height);
        }

        GameScheduler.scheduleParallel("Write screenshot", task);
        isTakingScreenshot = false;
    }

    /**
     * Reads the default framebuffer (framebuffer 0) at the current display size and schedules the image to be written.
     *
     * <p>Call this after the user interface has been drawn, and only while {@link #capturesCompositedFramebuffer()}
     * is true. A headless display skips the read and clears the pending screenshot.</p>
     *
     * @param displayDevice the display that was just rendered
     */
    public void saveCompositedScreenshot(DisplayDevice displayDevice) {
        if (!capturesCompositedFramebuffer()) {
            return;
        }
        if (displayDevice.isHeadless()) {
            isTakingScreenshot = false;
            return;
        }

        int width = displayDevice.getWidth();
        int height = displayDevice.getHeight();
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glReadBuffer(GL11.GL_BACK);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        GameScheduler.scheduleParallel("Write screenshot", () -> saveScreenshotTask(buffer, width, height));
        isTakingScreenshot = false;
    }

    /**
     * Saves given screenshot data to file.
     *
     * The file is then saved in the designated screenshot folder with a filename in the form:
     *
     *     Terasology-[yyMMddHHmmss]-[width]x[height].[format]
     */
    private void saveScreenshotTask(ByteBuffer buffer, int width, int height) {
        final String format = renderingConfig.getScreenshotFormat();
        final Path screenshotPath = getScreenshotPath(width, height, format);
        final BufferedImage image = convertByteBufferToBufferedImage(buffer, width, height);
        writeImageToFile(image, screenshotPath, format);
    }

    /**
     *  Saves given image data to file in save game folder.
     */
    private void saveGamePreviewTask(ByteBuffer buffer, int width, int height) {
        BufferedImage image = convertByteBufferToBufferedImage(buffer, width, height);
        writeImageToFile(image, savedGamePath, "jpg");
    }

    private void writeImageToFile(BufferedImage image, Path path, String format) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
                ImageIO.write(image, format, out);
                logger.info("Screenshot saved to {}! ", path);
            } catch (IOException e) {
                logger.warn("Failed to save screenshot!", e);
            }
            return null;
        });

    }

    /**
     * Transforms ByteBuffer into BufferedImage with specified width and height.
     */
    private BufferedImage convertByteBufferToBufferedImage(ByteBuffer buffer, int width, int height) {
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
        return image;
    }

    private Path getScreenshotPath(final int width, final int height, final String format) {
        final String fileName = String.format(
          SCREENSHOT_FILENAME_PATTERN, SIMPLE_DATE_FORMAT.format(new Date()), width, height, format);
        return PathManager.getInstance().getScreenshotPath().resolve(fileName);
    }

    /**
     * Returns true if the rendering engine is in the process of taking a screenshot.
     * Returns false if a screenshot is not being taken.
     *
     * @return true if a screenshot is being taken, false otherwise
     */
    public boolean isTakingScreenshot() {
        return isTakingScreenshot;
    }

    /**
     * True when the in-progress screenshot should be read from the default framebuffer after the
     * user interface has been drawn, so the debug overlay and HUD are included.
     *
     * <p>This is the case only while a screenshot (not a save-game preview) is pending and debug mode is on.</p>
     *
     * @return true when the capture must wait for the composited framebuffer
     */
    public boolean capturesCompositedFramebuffer() {
        return isTakingScreenshot && systemConfig.debugEnabled.get() && !savingGamePreview;
    }

    /**
     * Schedules the saving of game preview screenshot.
     *
     * @param saveDirPath to save folder
     */
    public void takeGamePreview(final Path saveDirPath) {
        this.savingGamePreview = true;
        this.savedGamePath = GamePreviewImageProvider.getNextGamePreviewImagePath(saveDirPath);
        this.saveScreenshot();
    }
}
