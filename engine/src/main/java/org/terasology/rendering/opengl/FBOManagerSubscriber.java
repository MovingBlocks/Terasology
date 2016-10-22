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
 * Interface to connect an object to an FBO manager so that the manager can inform it about changes.
 *
 * This is useful when the FBOs are not static and need to be regenerated at runtime.
 */
public interface FBOManagerSubscriber {

    /**
     * Triggered by an FBO manager, for example when the FBOs have been regenerated.
     *
     * This way the subscriber might react to the event obtaining the new FBOs,
     * for example when the FBO's dimensions have changed.
     */
    void update();

}
