// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.utilities.subscribables.AbstractSubscribable;
import org.terasology.context.annotation.API;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@API
public class RenderingDebugConfig extends AbstractSubscribable implements PropertyChangeListener {
    public static final String WIREFRAME = "wireframe";
    public static final String ENABLED = "enabled";
    public static final String STAGE = "stage";
    public static final String FIRST_PERSON_ELEMENTS_HIDDEN = "FirstPersonElementsHidden";
    public static final String HUD_HIDDEN = "hudHidden";
    public static final String RENDER_CHUNK_BOUNDING_BOXES = "renderChunkBoundingBoxes";
    public static final String RENDER_SKELETONS = "renderSkeletons";
    public static final String RENDER_ENTITY_COLLIDERS = "renderEntityColliders";
    public static final String RENDER_ENTITY_BOUNDING_BOXES = "renderEntityBoundingBoxes";
  
    private static final Logger logger = LoggerFactory.getLogger(RenderingDebugConfig.class);

    private boolean enabled;
    private boolean firstPersonElementsHidden;
    private boolean hudHidden;
    private boolean wireframe;
    private boolean renderChunkBoundingBoxes;
    private boolean renderSkeletons;
    private boolean renderEntityColliders;

    public RenderingDebugConfig() {
        subscribe(this);
    }

    public boolean isRenderEntityBoundingBoxes() {
        return renderEntityColliders;
    }

    public void setRenderEntityBoundingBoxes(boolean colliders) {
        boolean oldValue = this.renderEntityColliders;
        this.renderEntityColliders = colliders;
        propertyChangeSupport.firePropertyChange(RENDER_ENTITY_BOUNDING_BOXES, oldValue, this.renderEntityColliders);
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
        logger.atDebug().log("Set {} property to {}.", evt.getPropertyName().toUpperCase(), evt.getNewValue());
    }
}
