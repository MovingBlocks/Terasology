// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads a file.
 */
public class SingleFileDownloader implements Callable<Path> {

    private static final Logger logger = LoggerFactory.getLogger(SingleFileDownloader.class);

    private final URL url;
    private final Path target;
    private final SingleFileTransferProgressListener listener;

    public SingleFileDownloader(URL url, Path target) {
        this(url, target, (transferred, total) -> { });
    }

    /**
     * @param url
     * @param listener a progress listener. Will be called with 0 repeatedly if size cannot be determined.
     */
    public SingleFileDownloader(URL url, Path target, SingleFileTransferProgressListener listener) {
        this.url = url;
        this.target = target;
        this.listener = listener;
    }

    @Override
    public Path call() throws IOException {
        Path folder = target.getParent();
        String prefix = target.getFileName().toString();
        Path tempModuleLocation = Files.createTempFile(folder, prefix + "_", ".tmp");

        tempModuleLocation.toFile().deleteOnExit();

        logger.debug("Downloading {} from {}", target, url);

        long length = -1;
        URLConnection conn = url.openConnection();
        if (conn instanceof HttpURLConnection) {
            length = ((HttpURLConnection) conn).getContentLengthLong();
        }

        try (InputStream is = url.openStream();
             OutputStream os = Files.newOutputStream(tempModuleLocation)) {
            copy(is, os, length, listener);
            Files.move(tempModuleLocation, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return target;
    }

    /**
     * Reads all bytes from an input stream and writes them to an output stream.
     * Copied and adapted from Files.copy().
     */
    private static long copy(InputStream source, OutputStream sink, long max, SingleFileTransferProgressListener listener) throws IOException {
        long nread = 0L;
        int bufferSize = 0x10000;  // 64 kB
        byte[] buf = new byte[bufferSize];
        int n;
        while ((n = source.read(buf)) > 0) {
            sink.write(buf, 0, n);
            nread += n;
            if (max <= 0) {
                listener.onProgress(0, max);
            } else {
                listener.onProgress(nread, max);
            }
        }
        return nread;
    }
}
