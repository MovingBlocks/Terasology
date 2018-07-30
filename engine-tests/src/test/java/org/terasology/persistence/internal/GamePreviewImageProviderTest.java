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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GamePreviewImageProviderTest {

    private static final String PREVIEWS = "previews";
    private static final String DEFAULT_IMAGE_NAME = "1.jpg";
    private static final Path TMP_FOLDER = Paths.get("out", "test", "engine-tests", "tmp", PREVIEWS).toAbsolutePath();

    @Before
    public void setUp() throws IOException {
        Files.createDirectories(TMP_FOLDER);
    }

    @AfterClass
    public static void clean() throws IOException {
        FileUtils.deleteDirectory(new File(TMP_FOLDER.toUri()));
    }

    @After
    public void cleanUp() throws IOException {
        FileUtils.cleanDirectory(new File(TMP_FOLDER.toUri()));
    }

    @Test
    public void getAllPreviewImagesEmptyTest() {
        final List<BufferedImage> result = GamePreviewImageProvider.getAllPreviewImages(TMP_FOLDER);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void getAllPreviewImagesNotEmptyFolderButEmptyFileTest() throws IOException {
        Files.createDirectories(TMP_FOLDER.resolve(PREVIEWS));
        Files.createFile(TMP_FOLDER.resolve(PREVIEWS).resolve(DEFAULT_IMAGE_NAME));

        final List<BufferedImage> result = GamePreviewImageProvider.getAllPreviewImages(TMP_FOLDER);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void getNextGamePreviewImagePathEmptyFolderTest() {
        final Path imagePath = GamePreviewImageProvider.getNextGamePreviewImagePath(TMP_FOLDER);

        Assert.assertNotNull(imagePath);
        Assert.assertEquals(TMP_FOLDER.resolve(PREVIEWS).resolve(DEFAULT_IMAGE_NAME), imagePath);
    }

    @Test
    public void getNextGamePreviewImagePathNotEmptyFolderTest() throws IOException {
        Files.createDirectories(TMP_FOLDER.resolve(PREVIEWS));
        Files.createFile(TMP_FOLDER.resolve(PREVIEWS).resolve(DEFAULT_IMAGE_NAME));

        final Path imagePath = GamePreviewImageProvider.getNextGamePreviewImagePath(TMP_FOLDER);

        Assert.assertNotNull(imagePath);
        Assert.assertEquals(TMP_FOLDER.resolve(PREVIEWS).resolve("2.jpg"), imagePath);
    }

    @Test
    public void getNextGamePreviewImagePathOldestFileTest() throws IOException {
        Files.createDirectories(TMP_FOLDER.resolve(PREVIEWS));
        Files.createFile(TMP_FOLDER.resolve(PREVIEWS).resolve("1.jpg"));
        Files.createFile(TMP_FOLDER.resolve(PREVIEWS).resolve("2.jpg"));
        Files.createFile(TMP_FOLDER.resolve(PREVIEWS).resolve("3.jpg"));
        Files.createFile(TMP_FOLDER.resolve(PREVIEWS).resolve("4.jpg"));
        Files.createFile(TMP_FOLDER.resolve(PREVIEWS).resolve("5.jpg"));

        final Path imagePath = GamePreviewImageProvider.getNextGamePreviewImagePath(TMP_FOLDER);

        Assert.assertNotNull(imagePath);
        Assert.assertEquals(TMP_FOLDER.resolve(PREVIEWS).resolve("1.jpg"), imagePath);
    }
}
