// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.module;

import org.terasology.context.annotation.API;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is a wrapper for {@link InputStream} which should be used by modules.
 * <p>
 * By using this class, the caller is not allowed to close the stream at all.
 * For security reasons, only {@link SandboxFileManager} have the permissions to close it,
 * since the caller may completely forget to do so.
 */
@API
public class ModuleInputStream extends InputStream {

    private InputStream inputStream;

    public ModuleInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        throw new IOException("You must not close the stream. " +
                "The SandboxFileManager will do it automatically.");
    }

    @Override
    public synchronized void mark(int readLimit) {
        inputStream.mark(readLimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }
}
