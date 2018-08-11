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
package org.terasology.rendering.nui.layers.mainMenu.savedGames;

import com.google.common.base.Charsets;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class GameProviderTest {
    private static final String GAME_1 = "Game 1";
    private static final Path TMP_SAVES_FOLDER_PATH = Paths.get("out", "test", "engine-tests", "tmp", "saves").toAbsolutePath();
    private static final Path TMP_RECORDS_FOLDER_PATH = Paths.get("out", "test", "engine-tests", "tmp", "records").toAbsolutePath();
    private static final Path TMP_SAVE_GAME_PATH = TMP_SAVES_FOLDER_PATH.resolve(GAME_1);
    private static final Path TMP_RECORD_GAME_PATH = TMP_RECORDS_FOLDER_PATH.resolve(GAME_1);
    private static final String GAME_MANIFEST_JSON = "gameManifest.json";
    private static String MANIFEST_EXAMPLE;

    @BeforeClass
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
            MANIFEST_EXAMPLE = com.google.common.io.Files.toString(file, Charsets.UTF_8);
        } catch (IOException e) {
            Assert.fail("Could not load input file");
        }
    }

    @After
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

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void noSavedGames() {
        final List<GameInfo> result = GameProvider.getSavedGames();

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void noSavedRecordings() {
        final List<GameInfo> result = GameProvider.getSavedRecordings();

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void emptyRecordingGameManifestTest()
            throws IOException {
        Files.createDirectory(TMP_RECORD_GAME_PATH);
        Files.createFile(TMP_RECORD_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME));

        final List<GameInfo> result = GameProvider.getSavedRecordings();

        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void successTest()
            throws IOException {
        Files.createDirectories(TMP_SAVE_GAME_PATH);
        Path manifestFilePath = TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME);

        writeToFile(manifestFilePath, MANIFEST_EXAMPLE);

        final List<GameInfo> result = GameProvider.getSavedGames();
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        final GameInfo gameInfo = result.get(0);
        Assert.assertNotNull(gameInfo);
        Assert.assertNotNull(gameInfo.getManifest());
        Assert.assertNotNull(gameInfo.getTimestamp());
        Assert.assertNotNull(gameInfo.getSavePath());
        Assert.assertEquals(GAME_1, gameInfo.getManifest().getTitle());
        Assert.assertEquals(TMP_SAVE_GAME_PATH, gameInfo.getSavePath());
    }

    @Test
    public void emptySavesGameFolderTest() {
        final boolean res = GameProvider.isSavesFolderEmpty();
        Assert.assertTrue(res);
    }

    @Test
    public void notEmptySavesGameFolderTest() throws IOException {
        Files.createDirectories(TMP_SAVE_GAME_PATH);
        Files.createFile(TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME));
        final boolean res = GameProvider.isSavesFolderEmpty();
        Assert.assertFalse(res);
    }

    @Test
    public void getNextGameNameDefaultNoSavesTest() {
        final String name = GameProvider.getNextGameName();

        Assert.assertNotNull(name);
        Assert.assertEquals(GAME_1, name);
    }

    @Test
    public void getNextGameNameNumberTest() throws IOException {

        Files.createDirectories(TMP_SAVE_GAME_PATH);
        Path manifestFilePath = TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME);
        writeToFile(manifestFilePath, MANIFEST_EXAMPLE);

        final String name = GameProvider.getNextGameName();

        Assert.assertNotNull(name);
        Assert.assertEquals("Game 2", name);
    }

    @Test
    public void getNextGameNameDefaultExceptionTest() throws IOException {

        Files.createDirectories(TMP_SAVE_GAME_PATH);
        Path manifestFilePath = TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME);
        writeToFile(manifestFilePath, MANIFEST_EXAMPLE.replace(GAME_1, "bad"));

        final String name = GameProvider.getNextGameName();

        Assert.assertNotNull(name);
        Assert.assertEquals(GAME_1, name);
    }

    private void writeToFile(Path manifestFilePath, final String content)
            throws IOException {
        File manifest = new File(manifestFilePath.toUri());
        FileWriter fw = new FileWriter(manifest);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();
    }

}
