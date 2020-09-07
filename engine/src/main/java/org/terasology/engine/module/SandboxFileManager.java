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
import org.terasology.gestalt.module.sandbox.API;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.function.Consumer;

/**
 * This class wrap common file operations so they're only allowed to happen
 * within Terasology/sandbox directory.
 * <p>
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
     * Reads the file that matches the passed filename.
     * <p>
     * How to use:
     * <ul>
     * <li>First of all, we need to create a file read consumer, for example:
     * <pre>{@code
     * Consumer<InputStream> consumer = inputStream -> {
     *      try {
     *          int value = inputStream.read();
     *
     *          while (value != -1) {
     *              doSomething(value);
     *              value = inputStream.read();
     *          }
     *      } catch (IOException e) {
     *          logger.error("Cannot read file.");
     *      }
     * };
     * }</pre></li>
     * <li>Call {@code readFile} passing in the filename and the consumer as parameter.</li>
     * <li>When the execution is completed the {@code InputStream} is automatically closed.</li>
     * </ul>
     *
     * @param filename Filename.
     * @param consumer Consumer to read the file.
     */
    public void readFile(String filename, Consumer<InputStream> consumer) {
        String sandboxPath = pathManager.getSandboxPath(filename).toString();

        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try (InputStream inputStream = new FileInputStream(sandboxPath)) {
                ModuleInputStream moduleInputStream = new ModuleInputStream(inputStream);

                // consumer to read the file, if it exists
                consumer.accept(moduleInputStream);
            } catch (IOException e) {
                logger.error("Could not read the file: " + filename, e);
            }

            return null;
        });
    }

    /**
     * Write a new file.
     * <p>
     * How to use:
     * <ul>
     * <li>First of all, we need to create a file writer consumer, for example:
     * <pre>{@code
     * Consumer<OutputStream> consumer = outputStream -> {
     *      try {
     *          outputStream.write(someBytes);
     *      } catch (IOException e) {
     *          logger.error("error", e);
     *      }
     * };
     * }</pre></li>
     * <li>Call {@code writeFile} passing in the filename and the consumer as parameter.</li>
     * <li>When the execution is completed the {@code OutputStream} is automatically closed.</li>
     * </ul>
     *
     * @param filename Filename.
     * @param consumer Consumer to write the file.
     */
    public void writeFile(String filename, Consumer<OutputStream> consumer) {
        String sandboxPath = pathManager.getSandboxPath(filename).toString();

        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try (OutputStream outputStream = new FileOutputStream(sandboxPath)) {
                ModuleOutputStream moduleInputStream = new ModuleOutputStream(outputStream);

                // consumer to write the file
                consumer.accept(moduleInputStream);
            } catch (IOException e) {
                logger.error("Could not write the file: " + filename, e);
            }

            return null;
        });
    }
}
