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

import com.google.common.base.Charsets;
import org.lwjgl.LWJGLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.utilities.LWJGLHelper;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 */
public abstract class BaseLwjglSubsystem implements EngineSubsystem {

    private static final Logger logger = LoggerFactory.getLogger(BaseLwjglSubsystem.class);
    private static boolean initialised;

    @Override
    public void preInitialise(Context context) {
        if (!initialised) {
            initLogger();
            LWJGLHelper.initNativeLibs();
            initialised = true;
        }
    }

    private void initLogger() {
        if (LWJGLUtil.DEBUG) {
            try {
            // Pipes System.out and err to log, because that's where lwjgl writes it to.
            System.setOut(new PrintStream(System.out, false, Charsets.UTF_8.name()) {
                private Logger lwjglLogger = LoggerFactory.getLogger("org.lwjgl");

                @Override
                public void print(final String message) {
                    lwjglLogger.info(message);
                }
            });
            System.setErr(new PrintStream(System.err, false, Charsets.UTF_8.name()) {
                private Logger lwjglLogger = LoggerFactory.getLogger("org.lwjgl");

                @Override
                public void print(final String message) {
                    lwjglLogger.error(message);
                }
            });
            } catch (UnsupportedEncodingException e) {
                logger.error("Failed to map lwjgl logging", e);
            }
        }
    }
}
