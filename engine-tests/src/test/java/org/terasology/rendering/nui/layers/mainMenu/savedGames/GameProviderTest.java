// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.layers.mainMenu.savedGames;

import com.google.common.base.Charsets;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.engine.MockedPathManager;
import org.terasology.engine.core.paths.PathManager;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.engine.rendering.nui.layers.mainMenu.savedGames.GameProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class GameProviderTest implements MockedPathManager {
    private static final int TIMESTAMP_DELAY = 1000;
    private static final String DEFAULT_GAME_NAME = "Game";
    private static final String GAME_MANIFEST_JSON = "gameManifest.json";
    private static String manifestExample;

    private Path tmpSavesFolderPath;
    private Path tmpRecordsFolderPath;
    private Path tmpSaveGamePath;
    private Path tmpRecordGamePath;

    private static Stream<Arguments> nextGameNamesProvider() {
        return Stream.of(
                Arguments.arguments("Custom", "Custom 1"),
                Arguments.arguments("Custom 1", "Custom 2"),
                Arguments.arguments("Custom 2", "Custom 3"),
                Arguments.arguments("Custom 9", "Custom 10"),
                Arguments.arguments("Custom 19", "Custom 20")
        );
    }

    @BeforeEach
    public void init()
            throws NoSuchFieldException, IllegalAccessException, IOException {
        String gamefolder = UUID.randomUUID().toString();
        tmpSavesFolderPath = Paths.get("out", "test", "engine-tests", gamefolder, "saves").toAbsolutePath();
        tmpRecordsFolderPath = Paths.get("out", "test", "engine-tests", gamefolder, "records").toAbsolutePath();
        tmpSaveGamePath = tmpSavesFolderPath.resolve(DEFAULT_GAME_NAME);
        tmpRecordGamePath = tmpRecordsFolderPath.resolve(DEFAULT_GAME_NAME);
        PathManager pathManager = PathManager.getInstance();

        Field savesPathField = pathManager.getClass().getDeclaredField("savesPath");
        savesPathField.setAccessible(true);
        savesPathField.set(pathManager, tmpSavesFolderPath);

        Field recordsPathField = pathManager.getClass().getDeclaredField("recordingsPath");
        recordsPathField.setAccessible(true);
        recordsPathField.set(pathManager, tmpRecordsFolderPath);

        Files.createDirectories(tmpSavesFolderPath);
        Files.createDirectories(tmpRecordsFolderPath);

        final File file = new File(GameProviderTest.class.getClassLoader().getResource(GAME_MANIFEST_JSON).getFile());
        try {
            manifestExample = com.google.common.io.Files.asCharSource(file, Charsets.UTF_8).read();
        } catch (IOException e) {
            fail("Could not load input file");
        }
    }

    @AfterEach
    public void cleanUp()
            throws IOException {
        FileUtils.cleanDirectory(new File(tmpSavesFolderPath.toUri()));
        FileUtils.cleanDirectory(new File(tmpRecordsFolderPath.toUri()));
    }

    @Test
    public void emptySavedGameManifestTest()
            throws IOException {
        Files.createDirectory(tmpSaveGamePath);
        Files.createFile(tmpSaveGamePath.resolve(GameManifest.DEFAULT_FILE_NAME));

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
        Files.createDirectory(tmpRecordGamePath);
        Files.createFile(tmpRecordGamePath.resolve(GameManifest.DEFAULT_FILE_NAME));

        final List<GameInfo> result = GameProvider.getSavedRecordings();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void successTest()
            throws IOException {
        Files.createDirectories(tmpSaveGamePath);
        Path manifestFilePath = tmpSaveGamePath.resolve(GameManifest.DEFAULT_FILE_NAME);

        writeToFile(manifestFilePath, manifestExample);

        final List<GameInfo> result = GameProvider.getSavedGames();
        assertNotNull(result);
        assertEquals(1, result.size());
        final GameInfo gameInfo = result.get(0);
        assertNotNull(gameInfo);
        assertNotNull(gameInfo.getManifest());
        assertNotNull(gameInfo.getTimestamp());
        assertNotNull(gameInfo.getSavePath());
        assertEquals(DEFAULT_GAME_NAME, gameInfo.getManifest().getTitle());
        assertEquals(tmpSaveGamePath, gameInfo.getSavePath());
    }

    @Test
    public void emptySavesGameFolderTest() {
        final boolean res = GameProvider.isSavesFolderEmpty();
        assertTrue(res);
    }

    @Test
    public void notEmptySavesGameFolderTest() throws IOException {
        Files.createDirectories(tmpSaveGamePath);
        Files.createFile(tmpSaveGamePath.resolve(GameManifest.DEFAULT_FILE_NAME));
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
        Files.createDirectories(tmpSaveGamePath);
        Path manifestFilePath = tmpSaveGamePath.resolve(GameManifest.DEFAULT_FILE_NAME);
        writeToFile(manifestFilePath, manifestExample);

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
        Files.createDirectories(tmpSaveGamePath);
        Path manifestFilePath = tmpSaveGamePath.resolve(GameManifest.DEFAULT_FILE_NAME);
        writeToFile(manifestFilePath, manifestExample.replace(DEFAULT_GAME_NAME, "bad"));

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
        Path customSaveFolder = tmpSavesFolderPath.resolve(gameName);
        Files.createDirectories(customSaveFolder);
        Path manifestFilePath = customSaveFolder.resolve(GameManifest.DEFAULT_FILE_NAME);
        writeToFile(manifestFilePath, manifestExample.replace(DEFAULT_GAME_NAME, gameName));
    }

}
