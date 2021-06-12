// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.savedGames;

import com.google.common.base.Charsets;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.game.GameManifest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class GameProviderTest {
    private static final int TIMESTAMP_DELAY = 1000;
    private static final String DEFAULT_GAME_NAME = "Game";
    private static final Path TMP_SAVES_FOLDER_PATH =
            Paths.get("out", "test", "engine-tests", "tmp", "saves").toAbsolutePath();
    private static final Path TMP_RECORDS_FOLDER_PATH =
            Paths.get("out", "test", "engine-tests", "tmp", "records").toAbsolutePath();
    private static final Path TMP_SAVE_GAME_PATH = TMP_SAVES_FOLDER_PATH.resolve(DEFAULT_GAME_NAME);
    private static final Path TMP_RECORD_GAME_PATH = TMP_RECORDS_FOLDER_PATH.resolve(DEFAULT_GAME_NAME);
    private static final String GAME_MANIFEST_JSON = "gameManifest.json";
    private static String MANIFEST_EXAMPLE;

    private static final Logger logger = LoggerFactory.getLogger(GameProviderTest.class);

    @BeforeAll
    public static void init()
            throws NoSuchFieldException, IllegalAccessException, IOException {
        PathManager pathManager = PathManager.getInstance();

        Field savesPathField = pathManager.getClass().getDeclaredField("savesPath");
        savesPathField.setAccessible(true);
        savesPathField.set(pathManager, TMP_SAVES_FOLDER_PATH);

        Field recordsPathField = pathManager.getClass().getDeclaredField("recordingsPath");
        recordsPathField.setAccessible(true);
        recordsPathField.set(pathManager, TMP_RECORDS_FOLDER_PATH);

        Files.createDirectories(TMP_SAVES_FOLDER_PATH);
        Files.createDirectories(TMP_RECORDS_FOLDER_PATH);

        final File file = new File(GameProviderTest.class.getClassLoader().getResource(GAME_MANIFEST_JSON).getFile());
        try {
            MANIFEST_EXAMPLE = com.google.common.io.Files.asCharSource(file, Charsets.UTF_8).read();
        } catch (IOException e) {
            fail("Could not load input file");
        }
    }

    private static Stream<Arguments> nextGameNamesProvider() {
        return Stream.of(
                Arguments.arguments("Custom", "Custom 1"),
                Arguments.arguments("Custom 1", "Custom 2"),
                Arguments.arguments("Custom 2", "Custom 3"),
                Arguments.arguments("Custom 9", "Custom 10"),
                Arguments.arguments("Custom 19", "Custom 20")
        );
    }

    @AfterEach
    public void cleanUp()
            throws IOException {
        FileUtils.cleanDirectory(new File(TMP_SAVES_FOLDER_PATH.toUri()));
        FileUtils.cleanDirectory(new File(TMP_RECORDS_FOLDER_PATH.toUri()));
    }

    @Test
    public void emptySavedGameManifestTest()
            throws IOException {
        Files.createDirectory(TMP_SAVE_GAME_PATH);
        Files.createFile(TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME));

        final List<GameInfo> result = GameProvider.getSavedGames();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void noSavedGames() {
        final List<GameInfo> result = GameProvider.getSavedGames();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void noSavedRecordings() {
        final List<GameInfo> result = GameProvider.getSavedRecordings();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void emptyRecordingGameManifestTest()
            throws IOException {
        Files.createDirectory(TMP_RECORD_GAME_PATH);
        Files.createFile(TMP_RECORD_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME));

        final List<GameInfo> result = GameProvider.getSavedRecordings();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void successTest()
            throws IOException {
        Files.createDirectories(TMP_SAVE_GAME_PATH);
        Path manifestFilePath = TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME);

        writeToFile(manifestFilePath, MANIFEST_EXAMPLE);

        final List<GameInfo> result = GameProvider.getSavedGames();
        assertNotNull(result);
        assertEquals(1, result.size());
        final GameInfo gameInfo = result.get(0);
        assertNotNull(gameInfo);
        assertNotNull(gameInfo.getManifest());
        assertNotNull(gameInfo.getTimestamp());
        assertNotNull(gameInfo.getSavePath());
        assertEquals(DEFAULT_GAME_NAME, gameInfo.getManifest().getTitle());
        assertEquals(TMP_SAVE_GAME_PATH, gameInfo.getSavePath());
    }

    @Test
    public void emptySavesGameFolderTest() {
        final boolean res = GameProvider.isSavesFolderEmpty();
        assertTrue(res);
    }

    @Test
    public void notEmptySavesGameFolderTest() throws IOException {
        Files.createDirectories(TMP_SAVE_GAME_PATH);
        Files.createFile(TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME));
        final boolean res = GameProvider.isSavesFolderEmpty();

        assertFalse(res);
    }

    @Test
    public void getNextGameNameDefaultNoSavesTest() {
        final String name = GameProvider.getNextGameName();

        assertNotNull(name);
        assertEquals(DEFAULT_GAME_NAME, name);
    }

    @Test
    public void getNextGameNameCustomNoSavesTest() {
        String gameName = "Custom";
        final String name = GameProvider.getNextGameName(gameName);

        assertNotNull(name);
        assertEquals(gameName, name);
    }

    @Test
    public void getNextGameNameNumberTest() throws IOException {
        Files.createDirectories(TMP_SAVE_GAME_PATH);
        Path manifestFilePath = TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME);
        writeToFile(manifestFilePath, MANIFEST_EXAMPLE);

        final String name = GameProvider.getNextGameName();

        assertNotNull(name);
        assertEquals("Game 1", name);
    }

    @ParameterizedTest(name = "getNextGameName(\"{0}\") -> \"{1}\"")
    @MethodSource("nextGameNamesProvider")
    public void getNextGameNameNumberCustomNameTest(String gameName, String nextGameName) throws IOException {
        mimicGameName(gameName);
        final String name = GameProvider.getNextGameName(gameName);

        assertNotNull(name);
        assertEquals(nextGameName, name);
    }

    @Test
    public void getNextGameNameDefaultExceptionTest() throws IOException {
        Files.createDirectories(TMP_SAVE_GAME_PATH);
        Path manifestFilePath = TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME);
        writeToFile(manifestFilePath, MANIFEST_EXAMPLE.replace(DEFAULT_GAME_NAME, "bad"));

        final String name = GameProvider.getNextGameName();

        assertNotNull(name);
        assertEquals(DEFAULT_GAME_NAME, name);
    }

    @Test
    public void getNextGameNameWithNumber() throws IOException, InterruptedException {
        mimicGameName("Custom");
        // wait to make sure save games don't clash due to equal timestamp
        Thread.sleep(TIMESTAMP_DELAY);
        mimicGameName("Custom 1");
        Thread.sleep(TIMESTAMP_DELAY);
        mimicGameName("Custom 2");
        Thread.sleep(TIMESTAMP_DELAY);
        final String name = GameProvider.getNextGameName("Custom 1");

        assertNotNull(name);
        assertEquals("Custom 3", name);
    }

    private void writeToFile(Path manifestFilePath, final String content)
            throws IOException {
        File manifest = new File(manifestFilePath.toUri());
        FileWriter fw = new FileWriter(manifest);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();
    }

    /**
     * Creates an empty folder with given name {@code gameName} to mimic a save game.
     */
    private void mimicGameName(String gameName) throws IOException {
        Path customSaveFolder = TMP_SAVES_FOLDER_PATH.resolve(gameName);
        Files.createDirectories(customSaveFolder);
        Path manifestFilePath = customSaveFolder.resolve(GameManifest.DEFAULT_FILE_NAME);
        writeToFile(manifestFilePath, MANIFEST_EXAMPLE.replace(DEFAULT_GAME_NAME, gameName));
    }

}
