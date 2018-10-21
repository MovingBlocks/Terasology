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

@API
public class SandboxManager {

    private static final Logger logger = LoggerFactory.getLogger(SandboxManager.class);

    private PathManager pathManager;

    public SandboxManager() {
        this.pathManager = PathManager.getInstance();
    }

    public byte[] readFile(String filename) {
        Path sandboxPath = pathManager.getSandboxPath(filename);
        try {
            return Files.readAllBytes(sandboxPath);
        } catch (IOException e) {
            logger.error("Could not read the file: " + filename, e);
        }

        return null;
    }

    public void writeFile(String filename, byte[] data) {
        Path sandboxPath = pathManager.getSandboxPath(filename);
        try {
            Files.write(sandboxPath, data);
        } catch (IOException e) {
            logger.error("Could not write the file: " + filename, e);
        }
    }

    public void deleteFile(String filename) {
        Path sandboxPath = pathManager.getSandboxPath(filename);
        try {
            Files.delete(sandboxPath);
        } catch (IOException e) {
            logger.error("Could not delete the file: " + filename, e);
        }
    }
}
