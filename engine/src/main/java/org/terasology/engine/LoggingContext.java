/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.engine;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.MDC;
import org.terasology.engine.modes.GameState;

/**
 * Configures the underlying logback logging framework.
 * @author Martin Steiger
 */
public final class LoggingContext {

    /**
     * The identifier for the initialization phase
     */
    public static final String INIT_PHASE = "init";

    /**
     * The identifier for the menu phase
     */
    public static final String MENU = "menu";

    /**
     * The variable name for the discriminator in the sifting appender
     */
    private static final String PHASE_KEY = "phase";

    /**
     * The variable name for the log file root folder as defined in logback.xml
     */
    private static final String LOG_FILE_FOLDER = "logFileFolder";

    private LoggingContext() {
        // no instances
    }

    public static void initialize(Path logFileFolder) {
        String pathString = logFileFolder.normalize().toString();
        System.setProperty(LOG_FILE_FOLDER, pathString);

        try {
            deleteLogFiles(logFileFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Unfortunately, setting context-based variables works only after initialization
        // has completed. Manual initialization will work but is overriden by the first
        // (static) access to slf4j's StaticLoggerBinder.
        // This default initialization will attempt to create a folder "logFileFolder_IS_UNDEFINED" though.
        // TODO: file a report at logback/slf4j

//        LoggerContext context = new LoggerContext();
//        context.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
//        context.putProperty("targetFolder", pathString);
//        JoranConfigurator configurator = new JoranConfigurator();
//        configurator.setContext(context);
//
//        try {
//            ContextInitializer ci = new ContextInitializer(context);
//            ci.autoConfig();
//        } catch (JoranException e) {
//            e.printStackTrace();
//        }
    }

    private static void deleteLogFiles(Path rootPath) throws IOException {
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                if (file.toString().endsWith(".log")) {
                    tryDelete(rootPath, file);
                }

                return FileVisitResult.CONTINUE;
            }

            private void tryDelete(Path path, Path file) {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    System.err.println(String.format("Could not delete log file \"%s\" - %s",
                            path.relativize(file), e.getMessage()));
                }
            }
        });
    }

    public static void setGameState(GameState state) {
        String phase = state.getLoggingPhase();
        phase = phase.replaceAll("\\s", "_");
        MDC.put(PHASE_KEY, phase);
    }
}
