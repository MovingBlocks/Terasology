// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GamePreviewImageProviderTest {

    private static final String PREVIEWS = "previews";
    private static final String DEFAULT_IMAGE_NAME = "1.jpg";

    private Path tmpFolder;
    private Path tmpPreviewsFolder;

    @BeforeEach
    public void setUp() throws IOException {
        tmpFolder = Paths.get("out", "test", "engine-tests", UUID.randomUUID().toString(), PREVIEWS).toAbsolutePath();
        tmpPreviewsFolder = tmpFolder.resolve(PREVIEWS);
        FileUtils.forceDelete(new File(tmpFolder.toUri()));
        Files.createDirectories(tmpFolder);
    }

    @AfterAll
    public static void clean() throws IOException {
        FileUtils.forceDelete(new File(Paths.get("out", "test", "engine-tests", "tmp").toUri()));
    }

    @Test
    public void getAllPreviewImagesEmptyTest() {
        final List<BufferedImage> result = GamePreviewImageProvider.getAllPreviewImages(tmpFolder);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getAllPreviewImagesNotEmptyFolderButEmptyFileTest() throws IOException {
        Files.createDirectories(tmpPreviewsFolder);
        Files.createFile(tmpPreviewsFolder.resolve(DEFAULT_IMAGE_NAME));

        final List<BufferedImage> result = GamePreviewImageProvider.getAllPreviewImages(tmpFolder);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getNextGamePreviewImagePathEmptyFolderTest() {
        final Path imagePath = GamePreviewImageProvider.getNextGamePreviewImagePath(tmpFolder);

        assertNotNull(imagePath);
        assertEquals(tmpPreviewsFolder.resolve(DEFAULT_IMAGE_NAME), imagePath);
    }

    @Test
    public void getNextGamePreviewImagePathNotEmptyFolderTest() throws IOException {
        Files.createDirectories(tmpPreviewsFolder);
        Files.createFile(tmpPreviewsFolder.resolve(DEFAULT_IMAGE_NAME));

        final Path imagePath = GamePreviewImageProvider.getNextGamePreviewImagePath(tmpFolder);

        assertNotNull(imagePath);
        assertEquals(tmpPreviewsFolder.resolve("2.jpg"), imagePath);
    }

    @Test
    public void getNextGamePreviewImagePathOldestFileTest() throws IOException, InterruptedException {
        Files.createDirectories(tmpPreviewsFolder);
        Files.createFile(tmpPreviewsFolder.resolve("1.jpg"));
        Files.createFile(tmpPreviewsFolder.resolve("2.jpg"));
        Files.createFile(tmpPreviewsFolder.resolve("3.jpg"));
        Files.createFile(tmpPreviewsFolder.resolve("4.jpg"));
        Files.createFile(tmpPreviewsFolder.resolve("5.jpg"));

        final Path expectedOldestFile = tmpPreviewsFolder.resolve("3.jpg");

        Files.setLastModifiedTime(expectedOldestFile, FileTime.fromMillis(0));

        final Path imagePath = GamePreviewImageProvider.getNextGamePreviewImagePath(tmpFolder);

        assertNotNull(imagePath);
        assertEquals(expectedOldestFile, imagePath);
    }
}
