/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.game.modes;

/**
 * @author Immortius
 */
public interface LoadProcess {

    public static final int UNKNOWN_STEPS = -1;

    /**
     *
     * @return A message describing the state of the step
     */
    String getMessage();

    /**
     * Runs a single step.
     * @return Whether the overall step is finished
     */
    boolean step();

    /**
     * Begins the loading
     * @return The total number of expected steps for this LoadProcess, 0 if nothing to do
     */
    int begin();

}
