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
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class MultiFileDownloader implements Callable<List<Path>> {
    private Map<URL, Path> urlToTargetMap;
    private MultiFileTransferProgressListener progressListener;

    public MultiFileDownloader(Map<URL, Path> urlToTargetMap, MultiFileTransferProgressListener progressListener) {
        this.urlToTargetMap = urlToTargetMap;
        this.progressListener = progressListener;
    }

    @Override
    public List<Path> call() throws IOException {

        // Obtain the size of each file
        final int numFiles = urlToTargetMap.size();
        if (numFiles < 1) { // avoid reporting progress like "Retrieving file size 1 of 0"
            return Collections.emptyList();
        }
        final Map<URL, Long> fileSizes = getSizes();
        final long totalBytes = sum(fileSizes.values());

        List<Path> downloadedFiles = new ArrayList<>();
        int index = 0;
        long completedFilesBytes = 0;
        for (Map.Entry<URL, Path> entry : urlToTargetMap.entrySet()) {
            final int downloadedFileCount = index + 1;
            final long previousFilesBytes = completedFilesBytes;
            SingleFileTransferProgressListener singleDownloadListener = (fileDownloadedBytes, fileTotalBytes) ->
                progressListener.onDownloadProgress(previousFilesBytes + fileDownloadedBytes, totalBytes, downloadedFileCount, numFiles);
            SingleFileDownloader fileDownloader = new SingleFileDownloader(entry.getKey(), entry.getValue(),
                    singleDownloadListener);
            downloadedFiles.add(fileDownloader.call());
            completedFilesBytes += fileSizes.get(entry.getKey());
            index++;
        }
        return downloadedFiles;
    }

    private Map<URL, Long> getSizes() throws IOException {
        Map<URL, Long> result = new HashMap<>();
        int current = 1;
        int total = urlToTargetMap.size();
        for (URL url: urlToTargetMap.keySet()) {
            progressListener.onSizeMetadataProgress(current, total);
            URLConnection conn = url.openConnection();
            result.put(url, conn.getContentLengthLong());
            current++;
        }
        return result;
    }

    private long sum(Iterable<Long> values) {
        long result = 0;
        for (Long value: values) {
            result += value;
        }
        return result;
    }
}
