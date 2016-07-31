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
package org.terasology.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.world.WorldRendererImpl;
import org.terasology.utilities.subscribables.AbstractSubscribable;

/**
 */
public class RenderingDebugConfig extends AbstractSubscribable implements PropertyChangeListener {
    public enum DebugRenderingStage {
        OPAQUE_COLOR(0, "DEBUG_STAGE_OPAQUE_COLOR"),
        TRANSPARENT_COLOR(1, "DEBUG_STAGE_TRANSPARENT_COLOR"),
        OPAQUE_NORMALS(2, "DEBUG_STAGE_OPAQUE_NORMALS"),
        OPAQUE_DEPTH(3, "DEBUG_STAGE_OPAQUE_DEPTH"),
        OPAQUE_SUNLIGHT(4, "DEBUG_STAGE_OPAQUE_SUNLIGHT"),
        BAKED_OCCLUSION(5, "DEBUG_STAGE_BAKED_OCCLUSION"),
        SSAO(6, "DEBUG_STAGE_SSAO"),
        OPAQUE_LIGHT_BUFFER(7, "DEBUG_STAGE_OPAQUE_LIGHT_BUFFER"),
        SHADOW_MAP(8, "DEBUG_STAGE_SHADOW_MAP"),
        SOBEL(9, "DEBUG_STAGE_SOBEL"),
        HIGH_PASS(10, "DEBUG_STAGE_HIGH_PASS"),
        BLOOM(11, "DEBUG_STAGE_BLOOM"),
        SKY_BAND(12, "DEBUG_STAGE_SKY_BAND"),
        LIGHT_SHAFTS(13, "DEBUG_STAGE_LIGHT_SHAFTS"),
        RECONSTRUCTED_POSITION(14, "DEBUG_STAGE_RECONSTRUCTED_POSITION"),
        VOLUMETRIC_LIGHTING(15, "DEBUG_STAGE_VOLUMETRIC_LIGHTING");

        private int index;
        private String defineName;

        DebugRenderingStage(int index, String defineName) {
            this.index = index;
            this.defineName = defineName;
        }

        public int getIndex() {
            return index;
        }

        public String getDefineName() {
            return defineName;
        }
    }

    public static final String WIREFRAME = "wireframe";
    public static final String ENABLED = "enabled";
    public static final String STAGE = "stage";
    public static final String FIRST_PERSON_ELEMENTS_HIDDEN = "FirstPersonElementsHidden";
    public static final String HUD_HIDDEN = "hudHidden";
    public static final String RENDER_CHUNK_BOUNDING_BOXES = "renderChunkBoundingBoxes";
    public static final String RENDER_SKELETONS = "renderSkeletons";
    private static final Logger logger = LoggerFactory.getLogger(WorldRendererImpl.class);

    private boolean enabled;
    private DebugRenderingStage stage;
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

    public void cycleStage() {
        this.stage = DebugRenderingStage.values()[(stage.ordinal() + 1) % DebugRenderingStage.values().length];
    }

    public DebugRenderingStage getStage() {
        return stage;
    }

    public void setStage(DebugRenderingStage stage) {
        DebugRenderingStage oldStage = this.stage;
        this.stage = stage;
        propertyChangeSupport.firePropertyChange(STAGE, oldStage, this.stage);
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
