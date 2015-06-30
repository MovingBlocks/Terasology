/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.engine.splash;

/**
 * The basic interface for all splash screen implementations.
 * @author Martin Steiger
 */
public interface SplashScreenCore {

    /**
     * Adds a message to the message queue.
     * @param message the message to post
     */
    void post(String message);

    /**
     * Closes the splash screen through code. If not called it
     * will await for an AWT/Swing window to open.
     * Closing a closed splash screen does not have an effect.
     */
    void close();
}
