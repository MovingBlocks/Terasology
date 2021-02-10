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

/**
 * TODO: Add Javadocs
 */
public class SwappableFBO {
    private FBO lastUpdatedFbo;
    private FBO staleFbo;
    private boolean isSwapped;

    public SwappableFBO(FBO lastUpdatedFbo, FBO staleFbo) {
        this.lastUpdatedFbo = lastUpdatedFbo;
        this.staleFbo = staleFbo;
        isSwapped = false;
    }

    public FBO getStaleFbo() {
        return isSwapped ? lastUpdatedFbo : staleFbo;
    }

    public FBO getLastUpdatedFbo() {
        return isSwapped ? staleFbo : lastUpdatedFbo;
    }

    public void swap() {
        isSwapped = !isSwapped;
    }
}
