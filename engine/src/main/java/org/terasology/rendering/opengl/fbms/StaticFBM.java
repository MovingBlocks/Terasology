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
package org.terasology.rendering.opengl.fbms;

import org.terasology.rendering.opengl.AbstractFBM;
import org.terasology.rendering.opengl.FBOConfig;

/**
 * TODO: Add javadocs
 * TODO: Better naming
 */
public class StaticFBM extends AbstractFBM {

    @Override
    public void initialise() {
        super.initialise();
    }

    @Override
    public void allocateFBO(FBOConfig fboConfig) {
        if (fboUsageCountMap.containsKey(fboConfig.getResourceUrn())) {
            throw new IllegalArgumentException("There is already an FBO inside StaticFBMK, named as: " + fboConfig.getResourceUrn());
        }

        retain(fboConfig.getResourceUrn());
        generate(fboConfig, fboConfig.getDimensions());
    }
}
