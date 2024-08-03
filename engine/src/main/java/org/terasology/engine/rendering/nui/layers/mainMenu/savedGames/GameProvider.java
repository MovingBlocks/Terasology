// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.savedGames;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.game.GameManifest;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GameProvider {

    private static final Logger logger = LoggerFactory.getLogger(GameProvider.class);
    private static final String DEFAULT_GAME_NAME_PREFIX = "Game ";

    private GameProvider() {
    }

    public static List<GameInfo> getSavedRecordings() {
        Path recordingPath = PathManager.getInstance().getRecordingsPath();
        return getSavedGameOrRecording(recordingPath);
    }

    public static List<GameInfo> getSavedGames() {
        Path savePath = PathManager.getInstance().getSavesPath();
        return getSavedGameOrRecording(savePath);
    }

    /**
     * Checks if saved games are present.
     */
    public static boolean isSavesFolderEmpty() {
        Path savePath = PathManager.getInstance().getSavesPath();
        if (savePath != null) {

            // Set the stream path in a try with resources construct first in order to close the stream.
            try (Stream<Path> stream = Files.list(savePath)
                    .filter(savedGameFolderPath -> Files.isDirectory(savedGameFolderPath)
                            && Files.isRegularFile(savedGameFolderPath.resolve(GameManifest.DEFAULT_FILE_NAME)))) {
                return stream.collect(Collectors.toList()).isEmpty();
            } catch (IOException e) {
                logger.warn("Can't read saves path {}", savePath, e);
            }
        }
        return true;
    }

    private static List<GameInfo> getSavedGameOrRecording(Path saveOrRecordingPath) {
        SortedMap<FileTime, Path> savedGamePaths = Maps.newTreeMap(Collections.reverseOrder());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(saveOrRecordingPath)) {
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
                        result.add(new GameInfo(info, date, world.getValue()));
                    }
                } catch (NullPointerException npe) {
                    logger.error("The save file was corrupted for: {}. The manifest can be found and restored at: {}",
                            world, gameManifest, npe);
                }
            } catch (IOException e) {
                logger.error("Failed reading world data object.", e);
            }
        }
        return result;
    }

    /**
     * Generates the game name based on the game number of the last saved game Uses {@link
     * GameProvider#DEFAULT_GAME_NAME_PREFIX} for resolve.
     */
    public static String getNextGameName() {
        return getNextGameName(DEFAULT_GAME_NAME_PREFIX);
    }

    /**
     * Retrieve the next game name based on the game number of the last saved game.
     * <p>
     * This will append a game number or increment a given name number if the name prefix is already present as saved
     * game.
     *
     * <pre>
     *     1. "Game"    → "Game"
     *     2. "Game"    → "Game 1"
     *     3. "Game 1"  → "Game 2"
     * </pre>
     * <p>
     * When incrementing the number the currently highest number is incremented by one (i.e., "gaps" are not filled).
     *
     * <pre>
     *     1. "Gooey 3"     → "Gooey 3"
     *     2. "Gooey"       → "Gooey 4"
     * </pre>
     *
     * @param gameName will to use as game prefix, if saves contains this game name
     * @return next game name with number, or the given name if unique
     */
    public static String getNextGameName(String gameName) {
        final NumberedGameName requestedName = NumberedGameName.fromString(gameName);

        final Map<String, List<NumberedGameName>> savedGames = GameProvider.getSavedGames().stream()
                .map(savedGame -> savedGame.getManifest().getTitle())
                .map(NumberedGameName::fromString)
                .collect(Collectors.groupingBy(numberedGameName -> numberedGameName.namePrefix));

        if (savedGames.containsKey(requestedName.namePrefix)) {
            final int nextNumber = highestGameNumber(savedGames.get(requestedName.namePrefix)) + 1;
            return new NumberedGameName(requestedName.namePrefix, Optional.of(nextNumber)).toString();
        } else {
            return requestedName.toString();
        }
    }

    /**
     * Find the highest game number in the list of numbered game names.
     *
     * @param names a list of numbered game names
     * @return the highest number associated with the a game name, or 0 if none was found
     */
    private static int highestGameNumber(final List<NumberedGameName> names) {
        return names.stream()
                .map(n -> n.number)
                .filter(Optional::isPresent)
                .mapToInt(Optional::get)
                .max()
                .orElse(0);
    }

}
