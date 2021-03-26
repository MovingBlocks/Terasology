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
package org.terasology.engine.entitySystem.systems;

/**
 * Instances implementing this interface see their methods called at specific stages during the rendering process,
 * with an OpenGL state compatible with what needs to be rendered - i.e. opaque objects
 * (a fairly standard opengl state) vs semi-transparent objects (opengl has blending enabled).
 * This allows a RenderSystem to update its own entities or inject its own objects for rendering.
 */
public interface RenderSystem extends ComponentSystem {

    /**
     * Called with an OpenGL state useful to the rendering of opaque objects. See OpaqueObjectsNode for more information.
     */
    void renderOpaque();

    /**
     * Called with an OpenGL state useful to the rendering of alpha blended objects. See SimpleBlendMaterialsNode for more information.
     */
    void renderAlphaBlend();

    /**
     * Called with an OpenGL state useful to the rendering of overlays. See OverlaysNode for more information.
     */
    void renderOverlay();

    /**
     * Currently not used.
     */
    @Deprecated
    void renderShadows();
}
