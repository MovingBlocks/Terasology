/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.engine.module;

import org.terasology.module.sandbox.API;

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
    public void close() throws IOException {
        throw new IOException("You must not close the stream. " +
                "The SandboxFileManager will do it automatically.");
    }
}
