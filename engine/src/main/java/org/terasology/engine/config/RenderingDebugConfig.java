// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.engine.rendering.world.WorldRendererImpl;
import org.terasology.engine.utilities.subscribables.AbstractSubscribable;

@API
public class RenderingDebugConfig extends AbstractSubscribable implements PropertyChangeListener {
    public static final String WIREFRAME = "wireframe";
    public static final String ENABLED = "enabled";
    public static final String STAGE = "stage";
    public static final String FIRST_PERSON_ELEMENTS_HIDDEN = "FirstPersonElementsHidden";
    public static final String HUD_HIDDEN = "hudHidden";
    public static final String RENDER_CHUNK_BOUNDING_BOXES = "renderChunkBoundingBoxes";
    public static final String RENDER_SKELETONS = "renderSkeletons";
    private static final Logger logger = LoggerFactory.getLogger(WorldRendererImpl.class);

    private boolean enabled;
    private boolean firstPersonElementsHidden;
    private boolean hudHidden;
    private boolean wireframe;
    private boolean renderChunkBoundingBoxes;
    private boolean renderSkeletons;

    public RenderingDebugConfig() {
        subscribe(this);
    }

    public boolean isWireframe() {
        return wireframe;
    }

    public void setWireframe(boolean wireframe) {
        boolean oldValue = this.wireframe;
        this.wireframe = wireframe;
        propertyChangeSupport.firePropertyChange(WIREFRAME, oldValue, this.wireframe);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        boolean oldValue = this.enabled;
        this.enabled = enabled;
        propertyChangeSupport.firePropertyChange(ENABLED, oldValue, this.enabled);
    }

    public boolean isFirstPersonElementsHidden() {
        return firstPersonElementsHidden;
    }

    public void setFirstPersonElementsHidden(boolean firstPersonElementsHidden) {
        boolean oldValue = this.firstPersonElementsHidden;
        this.firstPersonElementsHidden = firstPersonElementsHidden;
        propertyChangeSupport.firePropertyChange(FIRST_PERSON_ELEMENTS_HIDDEN, oldValue, this.firstPersonElementsHidden);
    }

    public boolean isHudHidden() {
        return hudHidden;
    }

    public void setHudHidden(boolean hudHidden) {
        boolean oldValue = this.hudHidden;
        this.hudHidden = hudHidden;
        propertyChangeSupport.firePropertyChange(HUD_HIDDEN, oldValue, this.hudHidden);
    }

    public boolean isRenderChunkBoundingBoxes() {
        return renderChunkBoundingBoxes;
    }

    public void setRenderChunkBoundingBoxes(boolean renderChunkBoundingBoxes) {
        boolean oldValue = this.renderChunkBoundingBoxes;
        this.renderChunkBoundingBoxes = renderChunkBoundingBoxes;
        propertyChangeSupport.firePropertyChange(RENDER_CHUNK_BOUNDING_BOXES, oldValue, this.renderChunkBoundingBoxes);
    }

    public boolean isRenderSkeletons() {
        return renderSkeletons;
    }

    public void setRenderSkeletons(boolean renderSkeletons) {
        boolean oldValue = this.renderSkeletons;
        this.renderSkeletons = renderSkeletons;
        propertyChangeSupport.firePropertyChange(RENDER_SKELETONS, oldValue, this.renderSkeletons);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.info("Set {} property to {}. ", evt.getPropertyName().toUpperCase(), evt.getNewValue()); // for debugging purposes
    }
}
