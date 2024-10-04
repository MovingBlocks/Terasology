// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import org.slf4j.MDC;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.version.TerasologyVersion;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Configures the underlying logback logging framework.
 */
@SuppressWarnings("PMD.SystemPrintln")
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

    /**
     * The format of the log folder timestamps
     */
    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private static Path loggingPath = Paths.get(".");

    private LoggingContext() {
        // no instances
    }

    public static void initialize(Path logFileFolder) {
        TerasologyVersion terasologyVersion = TerasologyVersion.getInstance();
        String logFileDir = TIMESTAMP_FORMAT.format(new Date());
        if (!terasologyVersion.getengineVersion().equals("")) {
            logFileDir += "_" + terasologyVersion.getengineVersion();
        }
        if (!terasologyVersion.getBuildNumber().equals("")) {
            logFileDir += "_" + terasologyVersion.getBuildNumber();
        }
        loggingPath = logFileFolder.resolve(logFileDir).normalize();
        String pathString = loggingPath.toString();
        System.setProperty(LOG_FILE_FOLDER, pathString);

        try {
            deleteLogFiles(logFileFolder, Duration.ofDays(5).getSeconds());
        } catch (IOException e) {
            e.printStackTrace(); //NOPMD
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

    public static Path getLoggingPath() {
        return loggingPath;
    }

    private static void deleteLogFiles(final Path rootPath, final long maxAgeInSecs) throws IOException {
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) {
                if (path.equals(rootPath)) {
                    return FileVisitResult.CONTINUE;
                }

                // compare only the first subfolder
                String relPath = rootPath.relativize(path).getName(0).toString();

                try {
                    Date folderDate = TIMESTAMP_FORMAT.parse(relPath);
                    long ageInSecs = folderDate.toInstant().until(Instant.now(), ChronoUnit.SECONDS);

                    return ageInSecs > maxAgeInSecs ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
                } catch (ParseException e) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                if (file.toString().endsWith(".log")) {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        // we explicitly catch the exception so that other files
                        // will be removed even if this one fails
                        System.err.println("Could not delete log file: " + file);
                    }
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException exc) {

                if (path.toFile().list().length == 0) {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // we explicitly catch the exception so that other folders
                        // will be removed even if this one fails
                        System.err.println("Could not delete empty folder: " + path);
                    }
                }

                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void setGameState(GameState state) {
        String phase = state.getLoggingPhase();
        phase = phase.replaceAll("\\s", "_");
        MDC.put(PHASE_KEY, phase);
    }
}
