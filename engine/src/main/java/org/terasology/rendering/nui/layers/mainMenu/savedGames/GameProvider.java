/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.savedGames;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 */
public final class GameProvider {

    private static final Logger logger = LoggerFactory.getLogger(GameProvider.class);
    private static final String DEFAULT_GAME_NAME_PREFIX = "Game ";

    private GameProvider() {
    }

    public static List<GameInfo> getSavedGames() {
        Path savedGames = PathManager.getInstance().getSavesPath();
        SortedMap<FileTime, Path> savedGamePaths = Maps.newTreeMap(Collections.reverseOrder());
        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(savedGames)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry.resolve(GameManifest.DEFAULT_FILE_NAME))) {
                    savedGamePaths.put(Files.getLastModifiedTime(entry.resolve(GameManifest.DEFAULT_FILE_NAME)), entry);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read saved games path", e);
        }

        List<GameInfo> result = Lists.newArrayListWithCapacity(savedGamePaths.size());

        for (Map.Entry<FileTime, Path> world : savedGamePaths.entrySet()) {
            Path gameManifest = world.getValue().resolve(GameManifest.DEFAULT_FILE_NAME);

            if (!Files.isRegularFile(gameManifest)) {
                continue;
            }
            try {
                GameManifest info = GameManifest.load(gameManifest);
                try {
                    if (!info.getTitle().isEmpty()) {
                        Date date = new Date(world.getKey().toMillis());
                        BufferedImage image = getSavedGamePreviewImage(world.getValue());
                        result.add(new GameInfo(info, date, image));
                    }
                } catch (NullPointerException npe) {
                    logger.error("The save file was corrupted for: " + world.toString() + ". The manifest can be found and restored at: " + gameManifest.toString(), npe);
                }
            } catch (IOException e) {
                logger.error("Failed reading world data object.", e);
            }
        }
        return result;
    }

    private static BufferedImage getSavedGamePreviewImage(Path path) {
        Path previewImagePath = path.resolve(TerasologyConstants.DEFAULT_GAME_PREVIEW_IMAGE_NAME);
        if (!previewImagePath.toFile().exists()) {
            return null;
        }
        try (InputStream in = new BufferedInputStream(Files.newInputStream(previewImagePath))) {
            return ImageIO.read(in);
        } catch (IOException ex) {
            logger.warn("Can't load an image", ex);
        }
        return null;
    }

    /**
     * Generates the game name based on the game number of the last saved game
     */
    public static String getNextGameName() {
        int gameNumber = 1;
        for (GameInfo info : GameProvider.getSavedGames()) {
            if (info.getManifest().getTitle().startsWith(DEFAULT_GAME_NAME_PREFIX)) {
                String remainder = info.getManifest().getTitle().substring(DEFAULT_GAME_NAME_PREFIX.length());
                try {
                    gameNumber = Math.max(gameNumber, Integer.parseInt(remainder) + 1);
                } catch (NumberFormatException e) {
                    logger.trace("Could not parse {} as integer (not an error)", remainder, e);
                }
            }
        }
        return DEFAULT_GAME_NAME_PREFIX + gameNumber;
    }

}
