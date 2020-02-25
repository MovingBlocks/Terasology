/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.input.lwjgl;

import org.junit.jupiter.api.Test;
import org.terasology.config.ControllerConfig;
import org.terasology.utilities.LWJGLHelper;

import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class JInputControllerDeviceTest {

    /**
     * Tests that the constructor does not throw an error or exception
     * and that it interrupts the threads created by the controller
     * environment.
     */
    @Test
    void testConstructor() {
        LWJGLHelper.initNativeLibs();
        ControllerConfig controllerConfig = new ControllerConfig();
        JInputControllerDevice controllerDevice = new JInputControllerDevice(controllerConfig);
        assertTrue(controllerDevice.getControllers().size() >= 0);
        // assert thread created is interrupted by the previous controller environment
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread thread : threadSet) {
            if (thread.getClass().getName().toLowerCase().equals("net.java.games.input.rawinputeventqueue$queuethread".toLowerCase())) {
                assertTrue(thread.isInterrupted());
            }
        }
    }
}