// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.game.GameManifest;

import java.nio.file.Path;

public class StoragePathProvider {
    private static final String PLAYERS_PATH = "players";
    private static final String WORLDS_PATH = "worlds";
    private static final String PREVIEWS_PATH = "previews";
    private static final String PLAYER_STORE_EXTENSION = ".player";
    private static final String GLOBAL_ENTITY_STORE = "global.dat";
    private static final String UNFINISHED_SAVE_TRANSACTION = "unfinished-save-transaction";
    private static final String UNMERGED_CHANGED = "unmerged-changes";
    private static final int CHUNK_ZIP_DIM = 32;

    private final Path storagePathDirectory;
    private final Path playersPath;
    private final Path worldsPath;
    private final Path previewsPath;
    private Path worldPath;
    private Path globalEntityStorePath;
    private Path unfinishedSaveTransactionPath;
    private Path unmergedChangesPath;


    public StoragePathProvider(Path storagePathDirectory) {
        this.storagePathDirectory = storagePathDirectory;
        this.playersPath = storagePathDirectory.resolve(PLAYERS_PATH);
        this.worldsPath = storagePathDirectory.resolve(WORLDS_PATH);
        this.previewsPath = storagePathDirectory.resolve(PREVIEWS_PATH);
        this.worldPath = worldsPath.resolve(TerasologyConstants.MAIN_WORLD);
        this.globalEntityStorePath = storagePathDirectory.resolve(GLOBAL_ENTITY_STORE);
        this.unfinishedSaveTransactionPath = storagePathDirectory.resolve(UNFINISHED_SAVE_TRANSACTION);
        this.unmergedChangesPath = storagePathDirectory.resolve(UNMERGED_CHANGED);
    }


    public Path getPlayersPath() {
        return playersPath;
    }


    public Path getPlayersTempPath() {
        return unfinishedSaveTransactionPath.resolve(PLAYERS_PATH);
    }

    public Path getWorldsPath() {
        return worldsPath;
    }

    public Path getPreviewsPath() {
        return previewsPath;
    }

    public Path getPlayerFilePath(String playerId) {
        return playersPath.resolve(playerId + PLAYER_STORE_EXTENSION);
    }

    public Path getPlayerFileTempPath(String playerId) {
        return getPlayersTempPath().resolve(playerId + PLAYER_STORE_EXTENSION);
    }

    public Path getWorldPath() {
        return worldPath;
    }

    public Path getWorldTempPath() {
        return unfinishedSaveTransactionPath.resolve(WORLDS_PATH).resolve(TerasologyConstants.MAIN_WORLD);
    }

    public Path getChunkZipPath(Vector3i chunkZipPos) {
        return worldPath.resolve(getChunkZipFilename(chunkZipPos));
    }


    public Path getChunkZipTempPath(Vector3i chunkZipPos) {
        return getWorldTempPath().resolve(getChunkZipFilename(chunkZipPos));
    }

    public Path getGlobalEntityStorePath() {
        return globalEntityStorePath;
    }

    public Path getGlobalEntityStoreTempPath() {
        return unfinishedSaveTransactionPath.resolve(GLOBAL_ENTITY_STORE);
    }

    public String getChunkFilename(Vector3ic pos) {
        return String.format("%d.%d.%d.chunk", pos.x(), pos.y(), pos.z());
    }


    private String getChunkZipFilename(Vector3ic pos) {
        return String.format("%d.%d.%d.chunks.zip", pos.x(), pos.y(), pos.z());
    }

    public Vector3i getChunkZipPosition(Vector3ic chunkPos) {
        Vector3i result = new Vector3i(chunkPos);
        result.div(CHUNK_ZIP_DIM);
        if (chunkPos.x() < 0) {
            result.x -= 1;
        }
        if (chunkPos.y() < 0) {
            result.y -= 1;
        }
        if (chunkPos.z() < 0) {
            result.z -= 1;
        }
        return result;
    }

    public Path getChunkPath(Vector3ic chunkPos) {
        return worldPath.resolve(getChunkFilename(chunkPos));
    }

    public Path getChunkTempPath(Vector3ic chunkPos) {
        return getWorldTempPath().resolve(getChunkFilename(chunkPos));
    }


    public Path getGameManifestTempPath() {
        return unfinishedSaveTransactionPath.resolve(GameManifest.DEFAULT_FILE_NAME);
    }

    public Path getUnfinishedSaveTransactionPath() {
        return unfinishedSaveTransactionPath;
    }

    public Path getUnmergedChangesPath() {
        return unmergedChangesPath;
    }

    public Path getStoragePathDirectory() {
        return storagePathDirectory;
    }
}
