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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.game.GameManifest;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.EntityData;
import org.terasology.utilities.concurrency.AbstractTask;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Task that writes a previously created memory snapshot of the game to the disk.
 *
 * The result of this task can be obtained via {@link #getResult()}.
 *
 * @author Florian <florian@fkoeberle.de>
 */
public class SaveTransaction extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(SaveTransaction.class);

    private static final ImmutableMap<String, String> CREATE_ZIP_OPTIONS = ImmutableMap.of("create", "true", "encoding", "UTF-8");
    private final GameManifest gameManifest;
    private final Lock worldDirectoryWriteLock;
    private volatile SaveTransactionResult result;

    // Unprocessed data to save:
    private final Map<String, EntityData.PlayerStore> playerStores;
    private final Map<Vector3i, CompressedChunkBuilder> compressedChunkBuilders;
    private final EntityData.GlobalStore globalStore;

    // Save parameters:
    private final boolean storeChunksInZips;

    // utility classes for saving:
    private final StoragePathProvider storagePathProvider;
    private final SaveTransactionHelper saveTransactionHelper;


    public SaveTransaction(Map<String, EntityData.PlayerStore> playerStores, EntityData.GlobalStore globalStore,
                           Map<Vector3i, CompressedChunkBuilder> compressedChunkBuilder, GameManifest gameManifest,
                           boolean storeChunksInZips, StoragePathProvider storagePathProvider,
                           Lock worldDirectoryWriteLock) {
        this.playerStores = playerStores;
        this.compressedChunkBuilders = compressedChunkBuilder;
        this.globalStore = globalStore;
        this.gameManifest = gameManifest;
        this.storeChunksInZips = storeChunksInZips;
        this.storagePathProvider = storagePathProvider;
        this.saveTransactionHelper = new SaveTransactionHelper(storagePathProvider);
        this.worldDirectoryWriteLock = worldDirectoryWriteLock;
    }


    @Override
    public String getName() {
        return "Saving";
    }

    public void run() {
        try {
            if (Files.exists(storagePathProvider.getUnmergedChangesPath())) {
                // should not happen, as initialization should clean it up
                throw new IOException("Save rand while there were unmerged changes");
            }
            saveTransactionHelper.cleanupSaveTransactionDirectory();
            createSaveTransactionDirectory();
            writePlayerStores();
            writeGlobalStore();
            writeChunkStores();
            saveGameManifest();
            perpareChangesForMerge();
            mergeChanges();
            result = SaveTransactionResult.createSuccessResult();
            logger.info("Save game finished");
        } catch (Throwable t) {
            logger.error("Save game creation failed", t);
            result = SaveTransactionResult.createFailureResult(t);
        }
    }



    private void createSaveTransactionDirectory() throws IOException {
        Path directory = storagePathProvider.getUnfinishedSaveTransactionPath();
        Files.createDirectories(directory);
    }

    private void perpareChangesForMerge() throws IOException {
        Path directoryForUnfinishedFiles = storagePathProvider.getUnfinishedSaveTransactionPath();
        Path directoryForFinishedFiles = storagePathProvider.getUnmergedChangesPath();

        try {
            Files.move(directoryForUnfinishedFiles, directoryForFinishedFiles, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            logger.warn("Atomic rename of merge folder was not possible, doing it non atomically...");
            Files.move(directoryForUnfinishedFiles, directoryForFinishedFiles);
        }
    }


    private void writePlayerStores() throws IOException {
        Files.createDirectories(storagePathProvider.getPlayersTempPath());
        for (Map.Entry<String, EntityData.PlayerStore> playerStoreEntry : playerStores.entrySet()) {
            Path playerFile = storagePathProvider.getPlayerFileTempPath(playerStoreEntry.getKey());
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(playerFile))) {
                playerStoreEntry.getValue().writeTo(out);
            }
        }
    }

    private void writeGlobalStore() throws IOException {
        Path path = storagePathProvider.getGlobalEntityStoreTempPath();
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
            globalStore.writeTo(out);
        }
    }

    private void writeChunkStores() throws IOException {
        FileSystemProvider zipProvider = getZipFileSystemProvider();

        Path chunksPath =  storagePathProvider.getWorldTempPath();
        Files.createDirectories(chunksPath);
        if (storeChunksInZips) {
            Map<Vector3i, FileSystem> newChunkZips = Maps.newHashMap();
            for (Map.Entry<Vector3i, CompressedChunkBuilder> entry : compressedChunkBuilders.entrySet()) {
                Vector3i chunkPos = entry.getKey();
                Vector3i chunkZipPos = storagePathProvider.getChunkZipPosition(chunkPos);
                FileSystem zip = newChunkZips.get(chunkZipPos);
                if (zip == null) {
                    Path targetPath = storagePathProvider.getChunkZipTempPath(chunkZipPos);
                    Files.deleteIfExists(targetPath);
                    zip = zipProvider.newFileSystem(targetPath, CREATE_ZIP_OPTIONS);
                    newChunkZips.put(chunkZipPos, zip);
                }
                Path chunkPath = zip.getPath(storagePathProvider.getChunkFilename(chunkPos));
                CompressedChunkBuilder compressedChunkBuilder = entry.getValue();
                byte[] compressedChunk = compressedChunkBuilder.buildEncodedChunk();
                try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(chunkPath))) {
                    bos.write(compressedChunk);
                }
            }
            // Copy existing, unmodified content into the zips and close them
            for (Map.Entry<Vector3i, FileSystem> chunkZipEntry : newChunkZips.entrySet()) {
                Vector3i chunkZipPos = chunkZipEntry.getKey();
                Path oldChunkZipPath = storagePathProvider.getChunkZipPath(chunkZipPos);
                final FileSystem zip = chunkZipEntry.getValue();
                if (Files.isRegularFile(oldChunkZipPath)) {
                    try (FileSystem oldZip = FileSystems.newFileSystem(oldChunkZipPath, null)) {
                        for (Path root : oldZip.getRootDirectories()) {
                            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                        throws IOException {
                                    if (!Files.isRegularFile(zip.getPath(file.toString()))) {
                                        Files.copy(file, zip.getPath(file.toString()));
                                    }
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                        }
                    }
                }
                zip.close();
            }
        } else {
            for (Map.Entry<Vector3i, CompressedChunkBuilder> entry : compressedChunkBuilders.entrySet()) {
                Vector3i chunkPos = entry.getKey();
                CompressedChunkBuilder compressedChunkBuilder = entry.getValue();
                byte[] compressedChunk = compressedChunkBuilder.buildEncodedChunk();
                Path chunkPath = storagePathProvider.getChunkTempPath(chunkPos);
                try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(chunkPath))) {
                    out.write(compressedChunk);
                }
            }
        }
    }

    private FileSystemProvider getZipFileSystemProvider() throws IOException {
        // This is a little bit of a hack to get around a JAVA 7 bug (hopefully fixed in JAVA 8
        FileSystemProvider zipProvider = null;
        for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
            if ("jar".equalsIgnoreCase(provider.getScheme())) {
                zipProvider = provider;
            }
        }

        if (zipProvider == null) {
            throw new IOException("Zip archive support missing! Unable to save chunks.");
        }
        return zipProvider;
    }


    /**
     *
     * @return the result if there is one yet or null. This method returns the value of a volatile variable and
     * can thus be used even from another thread.
     */
    public SaveTransactionResult getResult() {
        return result;
    }

    private void saveGameManifest() {
        try {
            Path path = storagePathProvider.getGameManifestTempPath();
            GameManifest.save(path, gameManifest);
        } catch (IOException e) {
            logger.error("Failed to save world manifest", e);
        }
    }

    private void mergeChanges() throws IOException {
        worldDirectoryWriteLock.lock();
        try {
            saveTransactionHelper.mergeChanges();
        } finally {
            worldDirectoryWriteLock.unlock();
        }
    }

}
