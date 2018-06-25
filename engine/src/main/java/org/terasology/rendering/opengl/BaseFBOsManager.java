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
package org.terasology.rendering.opengl;

import org.terasology.engine.SimpleUri;
import org.terasology.utilities.subscribables.Subscribable;

/**
 * Frame Buffer managers are responsible for the instantiation, storage, provision and eventually disposal
 * of Frame Buffer Objects - the buffers used by the renderer for reading and writing rendering data.
 *
 * In most instances Frame Buffers can be thought of as 2D arrays of pixels in GPU memory: shaders write to them or
 * read from them. Some buffers are static and never change for the lifetime of the manager. Some buffers are dynamic:
 * they get disposed and regenerated. Some buffers hold intermediate steps of the rendering process and the content
 * of the final buffer is eventually sent to the display.
 *
 * Different types of buffers are handled by different managers: static buffers are handled by one manager
 * while dynamic buffers are handled by multiple managers, each regenerating their FBOs whenever some set
 * conditions are in place, i.e. a change in display resolution.
 *
 * This interface defines the minimum set of methods each Frame Buffer Manager must implement to provide its services.
 */
public interface BaseFBOsManager extends Subscribable {

    /**
     * Used to release an FBO when it is no longer needed.
     *
     * When each request() has been matched by a release() call, the FBO identified
     * by the SimpleUri provided in input is disposed.
     *
     * @param fboName a SimpleUri detailing which fbo should be released.
     */
    void release(SimpleUri fboName);

    /**
     * Returns an FBO with the characteristics defined by the FBOConfig provided in input.
     *
     * Normally this method always returns an FBO with the requested characteristics,
     * either because one exists already or because the manager creates one on the first request for it.
     *
     * This method can however throw an IllegalArgumentException if an FBO with the name
     * in the FBOConfig already exists within the manager but has different characteristics.
     * However, separate managers may use the same fboName for FBOs having different characteristics.
     *
     * Callers of this method must eventually call the method release() to ensure
     * that unused FBOs are eventually disposed.
     *
     * @param fboConfig an FBOConfig object providing the characteristics of the requested FBO.
     * @return an FBO object with the requested characteristics.
     */
    FBO request(FBOConfig fboConfig);

    /**
     * Returns an FBO given a SimpleUri uniquely identifying it within the context of a single manager.
     *
     * Notice that the exact same fboName can be used by multiple managers. The fboName uniquely identifies
     * an FBO only within the context of a given manager.
     *
     * @param fboName a SimpleUri uniquely identifying a managed FBO.
     * @return the FBO matching the given fboName
     */
    FBO get(SimpleUri fboName);

    /**
     * Obtains the FBOConfig of the FBO uniquely associated with the given fboName.
     *
     * @param fboName a SimpleUri uniquely identifying an FBO stored by the manager.
     * @return the FBOConfig associated with the given fboName
     */
    FBOConfig getFboConfig(SimpleUri fboName);
}
