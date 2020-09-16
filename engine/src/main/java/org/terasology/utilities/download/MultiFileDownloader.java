/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.utilities.download;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Downloads multiple files.
 */

public class MultiFileDownloader implements Callable<List<Path>> {
    private Map<URI, Path> uriToTargetMap;
    private MultiFileTransferProgressListener progressListener;

    /**
     * @param uriToTargetMap
     * @param progressListener a progress listener. Will be called with 0 repeatedly if size cannot be determined.
     */
    public MultiFileDownloader(Map<URI, Path> uriToTargetMap, MultiFileTransferProgressListener progressListener) {
        this.uriToTargetMap = uriToTargetMap;
        this.progressListener = progressListener;
    }

    @Override
    public List<Path> call() throws IOException {

        // Obtain the size of each file
        final int numFiles = uriToTargetMap.size();
        if (numFiles < 1) { // avoid reporting progress like "Retrieving file size 1 of 0"
            return Collections.emptyList();
        }
        final Map<URI, Long> fileSizes = getSizes();
        final long totalBytes = sum(fileSizes.values());

        List<Path> downloadedFiles = new ArrayList<>();
        int index = 0;
        long completedFilesBytes = 0;
        for (Map.Entry<URI, Path> entry : uriToTargetMap.entrySet()) {
            final int downloadedFileCount = index + 1;
            final long previousFilesBytes = completedFilesBytes;
            SingleFileTransferProgressListener singleDownloadListener = (fileDownloadedBytes, fileTotalBytes) ->
                    progressListener.onDownloadProgress(previousFilesBytes + fileDownloadedBytes, totalBytes, downloadedFileCount, numFiles);

            // Converting URI to URL in order to pass it SingleFileDownloader's constructor
            URL url = entry.getKey().toURL();
            SingleFileDownloader fileDownloader = new SingleFileDownloader(url, entry.getValue(),
                    singleDownloadListener);
            downloadedFiles.add(fileDownloader.call());
            completedFilesBytes += fileSizes.get(entry.getKey());
            index++;
        }
        return downloadedFiles;
    }
    /**
     * This method returns a Hash Map with the the uri as a kay and the size as a value.
     * @return HashMap<>.
     */
    private Map<URI, Long> getSizes() throws IOException {
        Map<URI, Long> result = new HashMap<>();
        int current = 1;
        int total = uriToTargetMap.size();
        for (URI uri: uriToTargetMap.keySet()) {
            progressListener.onSizeMetadataProgress(current, total);
            URLConnection connection = uri.toURL().openConnection();
            result.put(uri, connection.getContentLengthLong());
            current++;
        }
        return result;
    }

    /**
     * @param values
     * This method iterates through the Iterable<Long> parameter and sums the values of it.
     * @return long.
     */
    private long sum(Iterable<Long> values) {
        long result = 0;
        for (Long value: values) {
            result += value;
        }
        return result;
    }
}
