/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.game;

/**
 * @author Immortius
 */
public interface Timer {

    void tick();

    float getDelta();

    long getDeltaInMS();

    double getFps();

    long getRawTimeInMs();

    /**
     * @return Game time in milliseconds. This is synched with the server.
     */
    long getTimeInMs();

    /**
     * Updates the server time. This is used to resynchronise with the server.
     * @param ms
     */
    void updateServerTime(long ms, boolean immediate);
}
