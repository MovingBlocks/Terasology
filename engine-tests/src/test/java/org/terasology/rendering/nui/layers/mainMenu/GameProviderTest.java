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
package org.terasology.rendering.nui.layers.mainMenu;

import com.google.common.base.Charsets;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.fail;

public class GameProviderTest {
    private static final String GAME_1 = "Game 1";
    private static final Path TMP_SAVES_FOLDER_PATH = Paths.get("out", "test", "engine-tests", "tmp", "saves").toAbsolutePath();
    private static final Path TMP_SAVE_GAME_PATH = TMP_SAVES_FOLDER_PATH.resolve(GAME_1);
    private static final Path TMP_MANIFEST_PATH = TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME);
    private static String MANIFEST_EXAMPLE;

    @BeforeClass
    public static void init()
            throws NoSuchFieldException, IllegalAccessException, IOException {
        PathManager pathManager = PathManager.getInstance();
        Field savesPathField = pathManager.getClass().getDeclaredField("savesPath");
        savesPathField.setAccessible(true);
        savesPathField.set(pathManager, TMP_SAVES_FOLDER_PATH);

        Files.createDirectories(TMP_SAVE_GAME_PATH);

        File file = new File(GameProviderTest.class.getClassLoader().getResource("gameManifest.json").getFile());
        try {
            MANIFEST_EXAMPLE = com.google.common.io.Files.toString(file, Charsets.UTF_8);
        } catch (IOException e) {
            Assert.fail("Could not load input file");
        }
    }

    @After
    public void cleanUp()
            throws IOException {
        Files.deleteIfExists(TMP_MANIFEST_PATH);
    }

    @Test
    public void emptyManifestTest()
            throws IOException {
        Files.createFile(TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME));

        List<GameInfo> result = GameProvider.getSavedGames();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void noSavedGames() {
        List<GameInfo> result = GameProvider.getSavedGames();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void successTest()
            throws IOException {
        Path manifestFilePath = TMP_SAVE_GAME_PATH.resolve(GameManifest.DEFAULT_FILE_NAME);

        writeToFile(manifestFilePath, MANIFEST_EXAMPLE);

        final List<GameInfo> result = GameProvider.getSavedGames();
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        final GameInfo gameInfo = result.get(0);
        Assert.assertNotNull(gameInfo);
        Assert.assertNotNull(gameInfo.getManifest());
        Assert.assertNotNull(gameInfo.getTimestamp());
        Assert.assertNull(gameInfo.getPreviewImage());
        Assert.assertEquals(gameInfo.getManifest().getTitle(), GAME_1);
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
