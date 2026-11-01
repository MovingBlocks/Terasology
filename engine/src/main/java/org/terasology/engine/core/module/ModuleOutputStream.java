// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.module;

import org.terasology.context.annotation.API;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is a wrapper for {@link OutputStream} which should be used by modules.
 * <p>
 * By using this class, the caller is not allowed to close the stream at all.
 * For security reasons, only {@link SandboxFileManager} have the permissions to close it,
 * since the caller may completely forget to do so.
 */
@API
public class ModuleOutputStream extends OutputStream {

    private OutputStream outputStream;

    public ModuleOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        throw new IOException("You must not close the stream. " +
                "The SandboxFileManager will do it automatically.");
    }
}
