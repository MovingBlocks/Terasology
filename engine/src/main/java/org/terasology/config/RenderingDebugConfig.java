/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.utilities.subscribables.AbstractSubscribable;

/**
 */
public class RenderingDebugConfig extends AbstractSubscribable {

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

    private boolean enabled;
    private DebugRenderingStage stage;
    private boolean firstPersonElementsHidden;
    private boolean hudHidden;
    private boolean wireframe;
    private boolean renderChunkBoundingBoxes;
    private boolean renderSkeletons;


    public boolean isWireframe() {
        return wireframe;
    }

    public static final String WIREFRAME = "wireframe";
    public void setWireframe(boolean Wireframe) {
        this.wireframe = Wireframe;
        propertyChangeSupport.firePropertyChange(WIREFRAME, !this.wireframe, this.wireframe);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static final String ENABLED = "enabled";
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        propertyChangeSupport.firePropertyChange(ENABLED, !this.enabled, this.enabled);
    }

    public void cycleStage() {
        this.stage = DebugRenderingStage.values()[(stage.ordinal() + 1) % DebugRenderingStage.values().length];
    }

    public DebugRenderingStage getStage() {
        return stage;
    }

    public static final String STAGE = "stage";
    public void setStage(DebugRenderingStage stage) {
        DebugRenderingStage oldStage = this.stage;
        this.stage = stage;
        propertyChangeSupport.firePropertyChange(STAGE, oldStage, this.stage);
    }

    public boolean isFirstPersonElementsHidden() {
        return firstPersonElementsHidden;
    }

    public static final String FIRST_PERSON_ELEMENTS_HIDDEN = "FirstPersonElementsHidden";
    public void setFirstPersonElementsHidden(boolean FirstPersonElementsHidden) {
        this.firstPersonElementsHidden = FirstPersonElementsHidden;
        propertyChangeSupport.firePropertyChange(FIRST_PERSON_ELEMENTS_HIDDEN, !this.firstPersonElementsHidden, this.firstPersonElementsHidden);
    }

    public boolean isHudHidden() {
        return hudHidden;
    }

    public static final String HUD_HIDDEN = "hudHidden";
    public void setHudHidden(boolean hudHidden) {
        this.hudHidden = hudHidden;
        propertyChangeSupport.firePropertyChange(HUD_HIDDEN, !this.hudHidden, this.hudHidden);
    }

    public boolean isRenderChunkBoundingBoxes() {
        return renderChunkBoundingBoxes;
    }

    public static final String RENDER_CHUNK_BOUNDING_BOXES = "renderChunkBoundingBoxes";
    public void setRenderChunkBoundingBoxes(boolean renderChunkBoundingBoxes) {
        this.renderChunkBoundingBoxes = renderChunkBoundingBoxes;
        propertyChangeSupport.firePropertyChange(RENDER_CHUNK_BOUNDING_BOXES, !this.renderChunkBoundingBoxes, this.renderChunkBoundingBoxes);
    }

    public boolean isRenderSkeletons() {
        return renderSkeletons;
    }

    public static final String RENDER_SKELETONS = "renderSkeletons";
    public void setRenderSkeletons(boolean renderSkeletons) {
        this.renderSkeletons = renderSkeletons;
        propertyChangeSupport.firePropertyChange(RENDER_SKELETONS, !this.renderSkeletons, this.renderSkeletons);
    }

}
