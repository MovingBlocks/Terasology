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
package org.terasology.engine.subsystem.lwjgl;

import org.lwjgl.LWJGLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.utilities.LWJGLHelper;

import java.io.PrintStream;

/**
 * @author Immortius
 */
public abstract class BaseLwjglSubsystem implements EngineSubsystem {

    private static boolean initialised;

    @Override
    public synchronized void preInitialise() {
        if (!initialised) {
            initLogger();
            LWJGLHelper.initNativeLibs();
            initialised = true;
        }
    }

    private void initLogger() {
        if (LWJGLUtil.DEBUG) {
            // Pipes System.out and err to log, because that's where lwjgl writes it to.
            System.setOut(new PrintStream(System.out) {
                private Logger logger = LoggerFactory.getLogger("org.lwjgl");

                @Override
                public void print(final String message) {
                    logger.info(message);
                }
            });
            System.setErr(new PrintStream(System.err) {
                private Logger logger = LoggerFactory.getLogger("org.lwjgl");

                @Override
                public void print(final String message) {
                    logger.error(message);
                }
            });
        }
    }
}
