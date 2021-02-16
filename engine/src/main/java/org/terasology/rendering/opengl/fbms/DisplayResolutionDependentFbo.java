// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.opengl.fbms;

import org.terasology.config.RenderingConfig;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.ScreenshotSize;
import org.terasology.rendering.opengl.AbstractFboManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FboConfig;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.SwappableFBO;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.config.RenderingConfig.FBO_SCALE;
import static org.terasology.engine.subsystem.lwjgl.LwjglDisplayDevice.DISPLAY_RESOLUTION_CHANGE;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

/**
 * An instance of this class manages FBOs that need to be regenerated on resolution changes.
 * <p>
 * The FBOs are regenerated when the display resolution changes or when a screenshot is triggered
 * and the screenshot resolution differs from the display resolution.
 * <p>
 * Before and after regeneration an event is fired to any subscribers of the instance.
 * See method propertyChange(PropertyChangeEvent) for details.
 * <p>
 * An instance of this class also generates a number of default FBOs: see the constructor for details.
 */
public class DisplayResolutionDependentFbo extends AbstractFboManager implements PropertyChangeListener {
    public static final SimpleUri FINAL_BUFFER = new SimpleUri("engine:fbo.finalBuffer");
    public static final String PRE_FBO_REGENERATION = "preFboRegeneration";
    public static final String POST_FBO_REGENERATION = "postFboRegeneration";

    private SwappableFBO gBufferPair;

    private FBO.Dimensions fullScale = new FBO.Dimensions();
    private RenderingConfig renderingConfig;
    private DisplayDevice displayDevice;
    private ScreenGrabber screenGrabber;

    private boolean wasTakingScreenshotLastFrame;

    /**
     * The constructor: returns an instance of this class, subscribes it and generates the default FBOs.
     * <p>
     * The returned instance is subscribed to the RenderingConfig.FBO_SCALE and DisplayDevice.DISPLAY_RESOLUTION_CHANGE
     * settings, so that changes to either properties trigger the regeneration of the FBOs handled by this manager,
     * if necessary.
     * <p>
     * This constructor also initializes the SwappableFBOs of the GBuffer and the buffer identified by the
     * SimpleUri stored in DisplayResolutionDependentFbo.FINAL_BUFFER.
     *
     * @param renderingConfig the RenderingConfig instance.
     * @param screenGrabber   the ScreenGrabber instance.
     * @param displayDevice   the DisplayDevice instance
     */
    public DisplayResolutionDependentFbo(RenderingConfig renderingConfig, ScreenGrabber screenGrabber, DisplayDevice displayDevice) {
        this.renderingConfig = renderingConfig;
        this.screenGrabber = screenGrabber;

        renderingConfig.subscribe(FBO_SCALE, this);
        this.displayDevice = displayDevice;
        displayDevice.subscribe(DISPLAY_RESOLUTION_CHANGE, this);

        updateFullScale();
        generateDefaultFBOs();
    }

    private void generateDefaultFBOs() {
        FBO gBuffer1 = generateWithDimensions(new FboConfig(new SimpleUri("engine:fbo.gBuffer1"), FULL_SCALE, FBO.Type.HDR)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), fullScale);
        FBO gBuffer2 = generateWithDimensions(new FboConfig(new SimpleUri("engine:fbo.gBuffer2"), FULL_SCALE, FBO.Type.HDR)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), fullScale);
        generateWithDimensions(new FboConfig(FINAL_BUFFER, FULL_SCALE, FBO.Type.DEFAULT), fullScale);

        gBufferPair = new SwappableFBO(gBuffer1, gBuffer2);
    }

    @Override
    public FBO request(FboConfig fboConfig) {
        FBO fbo;
        SimpleUri fboName = fboConfig.getName();
        if (fboConfigs.containsKey(fboName)) {
            if (!fboConfig.equals(fboConfigs.get(fboName))) {
                throw new IllegalArgumentException("Requested FBO is already available with different configuration");
            }
            fbo = fboLookup.get(fboConfig.getName());
        } else {
            fbo = generateWithDimensions(fboConfig, fullScale.multiplyBy(fboConfig.getScale()));
        }
        retain(fboName);
        return fbo;
    }

    private void updateFullScale() {
        fullScale.setDimensions(displayDevice.getWidth(), displayDevice.getHeight());
        fullScale.multiplySelfBy(renderingConfig.getFboScale() / 100f);
    }

    /**
     * Invoked before real-rendering starts
     */
    public void update() {
        if (!screenGrabber.isTakingScreenshot()) {
            if (wasTakingScreenshotLastFrame) {
                updateFullScale();
                regenerateFbos();

                wasTakingScreenshotLastFrame = false;
            }
        } else {
            ScreenshotSize screenshotSize = renderingConfig.getScreenshotSize();

            fullScale.setDimensions(screenshotSize.getWidth(displayDevice.getWidth()),
                    screenshotSize.getHeight(displayDevice.getHeight()));
            regenerateFbos();

            wasTakingScreenshotLastFrame = true;
        }
    }

    private void regenerateFbos() {
        propertyChangeSupport.firePropertyChange(PRE_FBO_REGENERATION, 0, 1);

        for (SimpleUri urn : fboConfigs.keySet()) {
            FboConfig fboConfig = getFboConfig(urn);
            fboConfig.setDimensions(fullScale.multiplyBy(fboConfig.getScale()));
            FBO.recreate(get(urn), getFboConfig(urn));
        }

        propertyChangeSupport.firePropertyChange(POST_FBO_REGENERATION, 0, 1);

        // Note that the "old" and "new" values (0 and 1) in the above calls aren't actually
        // used: they are only necessary to ensure that the event is fired up correctly.
    }

    private void disposeAllFbos() {
        // TODO: This should be public, and should be called while disposing an object of this class, to prevent leaks.
        for (SimpleUri urn : fboConfigs.keySet()) {
            fboLookup.get(urn).dispose();
        }
        fboLookup.clear();
    }

    /**
     * Returns the GBuffer FBOs as a SwappableFBO instance.
     * <p>
     * The GBuffer is constituted by a special pair of FBOs working in tandem: rendering nodes can use one of the
     * FBOs to read from while writing to the other FBO in the pair.
     *
     * @return a SwappableFBO object containing the two GBuffer FBOs.
     */
    public SwappableFBO getGBufferPair() {
        return gBufferPair;
    }

    /**
     * This method triggers the regeneration of the managed FBOs.
     * <p>
     * The regeneration takes place only if the PropertyChangeEvent passed to the method has
     * a property name equal to LwjglDisplayDevice.DISPLAY_RESOLUTION_CHANGE or RenderingConfig.FBO_SCALE.
     * <p>
     * Before and after the FBO regeneration event are fired to all subscribers of the manager, with
     * property names PRE_FBO_REGENERATION and POST_FBO_REGENERATION respectively.
     *
     * @param propertyChangeEvent a PropertyChangeEvent instance with name LwjglDisplayDevice.DISPLAY_RESOLUTION_CHANGE
     *                            or RenderingConfig.FBO_SCALE.
     */
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getPropertyName().equals(DISPLAY_RESOLUTION_CHANGE) || propertyChangeEvent.getPropertyName().equals(FBO_SCALE)) {
            updateFullScale();
            regenerateFbos();
        }
    }
}
