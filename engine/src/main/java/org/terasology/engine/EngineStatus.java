/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.engine;

/**
 * Engine Status provides the current status of the engine - what it is doing, at a higher granularity than just running. This can be used by external and internal observers
 * to report on the state of the engine, such as splash screens/loading screens.
 */
public interface EngineStatus {

    /**
     * @return The description of the status
     */
    String getDescription();

    /**
     * @return Whether this is a status that "progresses" such as loading, with a known completion point
     */
    default boolean isProgressing() {
        return false;
    }

    /**
     * @return The progress of this status, between 0 and 1 inclusive where 1 is complete and 0 is just started.
     */
    default float getProgress() {
        return 0;
    }
}
