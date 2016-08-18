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

import org.terasology.assets.ResourceUrn;

/**
 * TODO: Add javadocs
 */
public enum DefaultDynamicFBOs {
    ReadOnlyGBuffer(new FBOConfig(new ResourceUrn("engine:sceneOpaque"), 1.0f, FBO.Type.HDR).useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer()),
    WriteOnlyGBuffer(new FBOConfig(new ResourceUrn("engine:sceneOpaquePingPong"), 1.0f, FBO.Type.HDR).useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer()),
    Final(new FBOConfig(new ResourceUrn("engine:sceneFinal"), 1.0f, FBO.Type.DEFAULT));

    private final FBOConfig fboConfig;

    DefaultDynamicFBOs(FBOConfig fboConfig) {
        this.fboConfig = fboConfig;
    }

    public FBOConfig getFboConfig() {
        return fboConfig;
    }

    public ResourceUrn getResourceUrn() {
        return fboConfig.getName();
    }

    @Override
    public String toString() {
        return getResourceUrn().toString();
    }
}
