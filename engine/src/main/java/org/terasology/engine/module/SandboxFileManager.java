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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.paths.PathManager;
import org.terasology.module.sandbox.API;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.stream.Stream;

/**
 * This class wrap common file operations so they're only allowed to happen
 * within Terasology/sandbox directory.
 *
 * It gives modules the ability to read and write to the file system in a safe way.
 */
@API
public class SandboxFileManager {

    private static final Logger logger = LoggerFactory.getLogger(SandboxFileManager.class);

    private PathManager pathManager;

    public SandboxFileManager() {
        this.pathManager = PathManager.getInstance();
    }

    /**
     * Read the file that matches the passed filename.
     *
     * @param filename
     * @return String stream.
     */
    public Stream<String> readFile(String filename) {
        Path sandboxPath = pathManager.getSandboxPath(filename);

        return AccessController.doPrivileged((PrivilegedAction<Stream<String>>) () -> {
            try {
                return Files.lines(sandboxPath);
            } catch (IOException e) {
                logger.error("Could not read the file: " + filename, e);
            }

            return null;
        });
    }

    /**
     * Write a new file using the filename and data passed as parameter.
     *
     * @param filename
     * @param data The file's content.
     */
    public void writeFile(String filename, byte[] data) {
        Path sandboxPath = pathManager.getSandboxPath(filename);
        
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {
                Files.write(sandboxPath, data);
            } catch (IOException e) {
                logger.error("Could not write the file: " + filename, e);
            }

            return null;
        });
    }

    /**
     * Delete the file that matches the passed filename.
     *
     * @param filename
     */
    public void deleteFile(String filename) {
        Path sandboxPath = pathManager.getSandboxPath(filename);

        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {
                Files.delete(sandboxPath);
            } catch (IOException e) {
                logger.error("Could not delete the file: " + filename, e);
            }

            return null;
        });
    }
}
