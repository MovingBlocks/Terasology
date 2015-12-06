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

/**
 */
public class RenderingDebugConfig {

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

        private DebugRenderingStage(int index, String defineName) {
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

    public void setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void cycleStage() {
        this.stage = DebugRenderingStage.values()[(stage.ordinal() + 1) % DebugRenderingStage.values().length];
    }

    public DebugRenderingStage getStage() {
        return stage;
    }

    public void setStage(DebugRenderingStage stage) {
        this.stage = stage;
    }

    public boolean isFirstPersonElementsHidden() {
        return firstPersonElementsHidden;
    }

    public void setFirstPersonElementsHidden(boolean firstPersonElementsHidden) {
        this.firstPersonElementsHidden = firstPersonElementsHidden;
    }

    public boolean isHudHidden() {
        return hudHidden;
    }

    public void setHudHidden(boolean hudHidden) {
        this.hudHidden = hudHidden;
    }

    public boolean isRenderChunkBoundingBoxes() {
        return renderChunkBoundingBoxes;
    }

    public void setRenderChunkBoundingBoxes(boolean renderChunkBoundingBoxes) {
        this.renderChunkBoundingBoxes = renderChunkBoundingBoxes;
    }

    public boolean isRenderSkeletons() {
        return renderSkeletons;
    }

    public void setRenderSkeletons(boolean renderSkeletons) {
        this.renderSkeletons = renderSkeletons;
    }
}
