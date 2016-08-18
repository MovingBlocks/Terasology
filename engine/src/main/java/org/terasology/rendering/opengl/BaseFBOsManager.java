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
public interface BaseFBOsManager {

    boolean subscribe(FBOManagerSubscriber subscriber);

    boolean unsubscribe(FBOManagerSubscriber subscriber);

    void release(ResourceUrn fboName);

    FBO request(FBOConfig fboConfig);

    FBO get(ResourceUrn fboName);

    boolean bindFboColorTexture(ResourceUrn fboName);

    boolean bindFboDepthTexture(ResourceUrn fboName);

    boolean bindFboNormalsTexture(ResourceUrn fboName);

    boolean bindFboLightBufferTexture(ResourceUrn fboName);

    // TODO: move all these bindings in their own StateChange implementations.
    // TODO: i.e. BindFBOColorBuffer, BindFBODepthBuffer and so on.
}
