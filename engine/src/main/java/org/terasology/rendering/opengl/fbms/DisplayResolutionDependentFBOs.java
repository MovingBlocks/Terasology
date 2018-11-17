/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.opengl.fbms;

import org.lwjgl.opengl.Display;
import org.terasology.config.RenderingConfig;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.ScreenshotSize;
import org.terasology.rendering.opengl.AbstractFBOsManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.SwappableFBO;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.config.RenderingConfig.FBO_SCALE;
import static org.terasology.engine.subsystem.lwjgl.LwjglDisplayDevice.DISPLAY_RESOLUTION_CHANGE;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

/**
 * An instance of this class manages FBOs that need to be regenerated on resolution changes.
 *
 * The FBOs are regenerated when the display resolution changes or when a screenshot is triggered
 * and the screenshot resolution differs from the display resolution.
 *
 * Before and after regeneration an event is fired to any subscribers of the instance.
 * See method propertyChange(PropertyChangeEvent) for details.
 *
 * An instance of this class also generates a number of default FBOs: see the constructor for details.
 */
public class DisplayResolutionDependentFBOs extends AbstractFBOsManager implements PropertyChangeListener {
    public static final SimpleUri FINAL_BUFFER = new SimpleUri("engine:fbo.finalBuffer");
    public static final String PRE_FBO_REGENERATION = "preFboRegeneration";
    public static final String POST_FBO_REGENERATION = "postFboRegeneration";

    private SwappableFBO gBufferPair;

    private FBO.Dimensions fullScale = new FBO.Dimensions();
    private RenderingConfig renderingConfig;
    private ScreenGrabber screenGrabber;

    private boolean wasTakingScreenshotLastFrame;

    /**
     * The constructor: returns an instance of this class, subscribes it and generates the default FBOs.
     *
     * The returned instance is subscribed to the RenderingConfig.FBO_SCALE and DisplayDevice.DISPLAY_RESOLUTION_CHANGE
     * settings, so that changes to either properties trigger the regeneration of the FBOs handled by this manager,
     * if necessary.
     *
     * This constructor also initializes the SwappableFBOs of the GBuffer and the buffer identified by the
     * SimpleUri stored in DisplayResolutionDependentFBOs.FINAL_BUFFER.
     *
     * @param renderingConfig the RenderingConfig instance.
     * @param screenGrabber the ScreenGrabber instance.
     * @param displayDevice the DisplayDevice instance
     */
    public DisplayResolutionDependentFBOs(RenderingConfig renderingConfig, ScreenGrabber screenGrabber, DisplayDevice displayDevice) {
        this.renderingConfig = renderingConfig;
        this.screenGrabber = screenGrabber;

        renderingConfig.subscribe(FBO_SCALE, this);

        displayDevice.subscribe(DISPLAY_RESOLUTION_CHANGE, this);

        updateFullScale();
        generateDefaultFBOs();
    }

    private void generateDefaultFBOs() {
        FBO gBuffer1 = generateWithDimensions(new FBOConfig(new SimpleUri("engine:fbo.gBuffer1"), FULL_SCALE, FBO.Type.HDR)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), fullScale);
        FBO gBuffer2 = generateWithDimensions(new FBOConfig(new SimpleUri("engine:fbo.gBuffer2"), FULL_SCALE, FBO.Type.HDR)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), fullScale);
        generateWithDimensions(new FBOConfig(FINAL_BUFFER, FULL_SCALE, FBO.Type.DEFAULT), fullScale);

        gBufferPair = new SwappableFBO(gBuffer1, gBuffer2);
    }

    @Override
    public FBO request(FBOConfig fboConfig) {
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
        fullScale.setDimensions(Display.getWidth(), Display.getHeight());
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
            // TODO: Remove dependency on Display
            fullScale.setDimensions(screenshotSize.getWidth(Display.getWidth()),
                    screenshotSize.getHeight(Display.getHeight()));
            regenerateFbos();

            wasTakingScreenshotLastFrame = true;
        }
    }

    private void regenerateFbos() {
        propertyChangeSupport.firePropertyChange(PRE_FBO_REGENERATION, 0, 1);

        for (SimpleUri urn : fboConfigs.keySet()) {
            FBOConfig fboConfig = getFboConfig(urn);
            fboConfig.setDimensions(fullScale.multiplyBy(fboConfig.getScale()));
            FBO.recreate(get(urn), getFboConfig(urn));
        }

        propertyChangeSupport.firePropertyChange(POST_FBO_REGENERATION, 0, 1);

        // Note that the "old" and "new" values (0 and 1) in the above calls aren't actually
        // used: they are only necessary to ensure that the event is fired up correctly.
   }

    /**
     * Returns the GBuffer FBOs as a SwappableFBO instance.
     *
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
     *
     * The regeneration takes place only if the PropertyChangeEvent passed to the method has
     * a property name equal to LwjglDisplayDevice.DISPLAY_RESOLUTION_CHANGE or RenderingConfig.FBO_SCALE.
     *
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
