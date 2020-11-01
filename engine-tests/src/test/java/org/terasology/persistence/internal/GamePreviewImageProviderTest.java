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
package org.terasology.persistence.internal;

import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GamePreviewImageProviderTest {

    private static final String PREVIEWS = "previews";
    private static final String DEFAULT_IMAGE_NAME = "1.jpg";
    private static final Path TMP_FOLDER = Paths.get("out", "test", "engine-tests", "tmp", PREVIEWS).toAbsolutePath();
    private static final Path TMP_PREVIEWS_FOLDER = TMP_FOLDER.resolve(PREVIEWS);

    @BeforeEach
    public void setUp() throws IOException {
        FileUtils.forceDelete(new File(TMP_FOLDER.toUri()));
        Files.createDirectories(TMP_FOLDER);
    }

    @AfterAll
    public static void clean() throws IOException {
        FileUtils.forceDelete(new File(Paths.get("out", "test", "engine-tests", "tmp").toUri()));
    }

    @Test
    public void getAllPreviewImagesEmptyTest() {
        final List<BufferedImage> result = GamePreviewImageProvider.getAllPreviewImages(TMP_FOLDER);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getAllPreviewImagesNotEmptyFolderButEmptyFileTest() throws IOException {
        Files.createDirectories(TMP_PREVIEWS_FOLDER);
        Files.createFile(TMP_PREVIEWS_FOLDER.resolve(DEFAULT_IMAGE_NAME));

        final List<BufferedImage> result = GamePreviewImageProvider.getAllPreviewImages(TMP_FOLDER);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getNextGamePreviewImagePathEmptyFolderTest() {
        final Path imagePath = GamePreviewImageProvider.getNextGamePreviewImagePath(TMP_FOLDER);

        assertNotNull(imagePath);
        assertEquals(TMP_PREVIEWS_FOLDER.resolve(DEFAULT_IMAGE_NAME), imagePath);
    }

    @Test
    public void getNextGamePreviewImagePathNotEmptyFolderTest() throws IOException {
        Files.createDirectories(TMP_PREVIEWS_FOLDER);
        Files.createFile(TMP_PREVIEWS_FOLDER.resolve(DEFAULT_IMAGE_NAME));

        final Path imagePath = GamePreviewImageProvider.getNextGamePreviewImagePath(TMP_FOLDER);

        assertNotNull(imagePath);
        assertEquals(TMP_PREVIEWS_FOLDER.resolve("2.jpg"), imagePath);
    }

    @Test
    public void getNextGamePreviewImagePathOldestFileTest() throws IOException, InterruptedException {
        Files.createDirectories(TMP_PREVIEWS_FOLDER);
        Files.createFile(TMP_PREVIEWS_FOLDER.resolve("1.jpg"));
        Files.createFile(TMP_PREVIEWS_FOLDER.resolve("2.jpg"));
        Files.createFile(TMP_PREVIEWS_FOLDER.resolve("3.jpg"));
        Files.createFile(TMP_PREVIEWS_FOLDER.resolve("4.jpg"));
        Files.createFile(TMP_PREVIEWS_FOLDER.resolve("5.jpg"));

        final Path expectedOldestFile = TMP_PREVIEWS_FOLDER.resolve("3.jpg");

        Files.setLastModifiedTime(expectedOldestFile, FileTime.fromMillis(0));

        final Path imagePath = GamePreviewImageProvider.getNextGamePreviewImagePath(TMP_FOLDER);

        assertNotNull(imagePath);
        assertEquals(expectedOldestFile, imagePath);
    }
}
