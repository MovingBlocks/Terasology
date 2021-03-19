/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.engine.persistence.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides methods for working with game preview images.
 */
public final class GamePreviewImageProvider {

    private static final Logger logger = LoggerFactory.getLogger(GamePreviewImageProvider.class);
    private static final int LIMIT = 5;
    private static final String JPG_FILE_TYPE = ".jpg";
    private static final String DEFAULT_PREVIEW_NAME = "1.jpg";

    private GamePreviewImageProvider() {
    }

    /**
     * Gets all images for saved game.
     *
     * @param savePath a path to saves directory
     * @return all preview images
     */
    public static List<BufferedImage> getAllPreviewImages(final Path savePath) {
        final List<BufferedImage> result = new ArrayList<>();
        final StoragePathProvider storagePathProvider = new StoragePathProvider(savePath);
        final Path previewsDirPath = storagePathProvider.getPreviewsPath();
        for (Path previewPath : getAllPathsToFilesInFolder(previewsDirPath)) {
            try (InputStream in = new BufferedInputStream(Files.newInputStream(previewPath))) {
                result.add(ImageIO.read(in));
            } catch (IOException ex) {
                logger.warn("Can't load an image", ex);
            }
        }
        result.removeIf(Objects::isNull);
        return result;
    }

    /**
     * Gets a path to next game preview image.
     *
     * @param savePath a path to saves directory
     * @return a path to preview image
     */
    public static Path getNextGamePreviewImagePath(final Path savePath) {
        final StoragePathProvider storagePathProvider = new StoragePathProvider(savePath);
        final Path previewsDirPath = storagePathProvider.getPreviewsPath();

        final List<Path> previewsPaths = getAllPathsToFilesInFolder(previewsDirPath);
        final String fileName = getNextPreviewImageName(previewsPaths);

        return previewsDirPath.resolve(fileName);
    }

    private static void createDirectory(Path dirPath) {
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            logger.warn("Can't create folder, {}", dirPath);
        }
    }

    private static List<Path> getAllPathsToFilesInFolder(final Path dirPath) {
        // create folder if not exists yet
        createDirectory(dirPath);

        try (Stream<Path> stream = Files.list(dirPath).filter(Files::isRegularFile)) {
            return stream.collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Can't correctly read files from {}", dirPath);
            return Collections.emptyList();
        }
    }

    /**
     * Gets next preview image name. Names are looped and starts from 1.jpg.
     * If there are less than limit files. It takes next number, else the oldest file.
     */
    private static String getNextPreviewImageName(final List<Path> paths) {
        if (paths.size() < LIMIT) {
            return getNextNumberFileName(paths);
        }
        return getOldestFileName(paths);
    }

    private static String getOldestFileName(final List<Path> paths) {
        FileTime oldestTime = null;
        Path oldestPath = null;
        for (Path path : paths) {
            try {
                final FileTime fileTime = Files.getLastModifiedTime(path);
                if (oldestTime == null || fileTime.compareTo(oldestTime) < 0) {
                    oldestTime = fileTime;
                    oldestPath = path;
                }
            } catch (IOException e) {
                logger.error("Can't read last modified time for path {}", path);
            }
        }

        if (oldestPath != null) {
            return oldestPath.getFileName().toString();
        }
        return DEFAULT_PREVIEW_NAME;
    }

    private static String getNextNumberFileName(final List<Path> paths) {
        int counter = 1;
        for (Path path : paths) {
            final String fileName = path.getFileName().toString().split("\\.")[0];
            try {
                counter = Math.max(counter, Integer.parseInt(fileName) + 1);
            } catch (NumberFormatException e) {
                logger.warn("Could not parse {} as integer (not an error)", fileName, e);
            }
        }
        return counter + JPG_FILE_TYPE;
    }
}
