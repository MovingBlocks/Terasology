/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;
import org.terasology.math.Vector3i;

import java.nio.file.Path;

/**
 * @author Florian <florian@fkoeberle.de>
 */
public class StoragePathProvider {
    private static final String PLAYERS_PATH = "players";
    private static final String WORLDS_PATH = "worlds";
    private static final String PLAYER_STORE_EXTENSION = ".player";
    private static final String GLOBAL_ENTITY_STORE = "global.dat";
    private static final int CHUNK_ZIP_DIM = 32;

    private final Path storagePathDirectory;
    private final Path playersPath;
    private final Path worldsPath;
    private Path worldPath;
    private Path globalEntityStorePath;


    public StoragePathProvider(Path storagePathDirectory) {
        this.storagePathDirectory = storagePathDirectory;
        this.playersPath = storagePathDirectory.resolve(PLAYERS_PATH);
        this.worldsPath = storagePathDirectory.resolve(WORLDS_PATH);
        this.worldPath = worldsPath.resolve(TerasologyConstants.MAIN_WORLD);
        this.globalEntityStorePath = storagePathDirectory.resolve(GLOBAL_ENTITY_STORE);
    }


    public Path getPlayersPath() {
        return playersPath;
    }

    public Path getWorldsPath() {
        return worldsPath;
    }

    public Path getPlayerFilePath(String playerId) {
        return playersPath.resolve(playerId + PLAYER_STORE_EXTENSION);
    }

    public Path getWorldPath() {
        return worldPath;
    }

    public Path getChunkZipPath(Vector3i chunkZipPos) {
        return worldPath.resolve(getChunkZipFilename(chunkZipPos));
    }

    public Path getGlobalEntityStorePath() {
        return globalEntityStorePath;
    }

    public String getChunkFilename(Vector3i pos) {
        return String.format("%d.%d.%d.chunk", pos.x, pos.y, pos.z);
    }


    private String getChunkZipFilename(Vector3i pos) {
        return String.format("%d.%d.%d.chunks.zip", pos.x, pos.y, pos.z);
    }

    private String getChunkZipTempFilename(Vector3i pos) {
        return String.format("%d.%d.%d.chunks.tmp", pos.x, pos.y, pos.z);
    }

    public Vector3i getChunkZipPosition(Vector3i chunkPos) {
        Vector3i result = new Vector3i(chunkPos);
        result.divide(CHUNK_ZIP_DIM);
        if (chunkPos.x < 0) {
            result.x -= 1;
        }
        if (chunkPos.y < 0) {
            result.y -= 1;
        }
        if (chunkPos.z < 0) {
            result.z -= 1;
        }
        return result;
    }

    public Path getChunkPath(Vector3i chunkPos) {
        return worldsPath.resolve(getChunkFilename(chunkPos));
    }

    public Path getChunkZipTempFilePath(Vector3i chunkZipPos) {
        return worldsPath.resolve(getChunkZipTempFilename(chunkZipPos));
    }
}
