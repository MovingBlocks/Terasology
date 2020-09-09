// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import com.google.common.base.Charsets;
import org.lwjgl.LWJGLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.utilities.LWJGLHelper;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 *
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
                    private final Logger lwjglLogger = LoggerFactory.getLogger("org.lwjgl");

                    @Override
                    public void print(final String message) {
                        lwjglLogger.info(message);
                    }
                });
                System.setErr(new PrintStream(System.err, false, Charsets.UTF_8.name()) {
                    private final Logger lwjglLogger = LoggerFactory.getLogger("org.lwjgl");

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
