/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.logic.world.generator.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.terasology.logic.world.generator.core.ChunkGeneratorManager;

/**
 * Load and save the ChunkGeneratorManager.
 * 
 * @author Mathias Kalb
 */
public class ChunkGeneratorPersister {

    private final Logger logger = Logger.getLogger(getClass().getName());

    public void save(final File file, final ChunkGeneratorManager chunkGeneratorManager) throws IOException {
        final File parentFile = file.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs(); // TODO Error handling
        }
        final FileOutputStream out = new FileOutputStream(file);

        try {
            final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out));
            ChunkGeneratorJSONFormat.write(chunkGeneratorManager, bufferedWriter);
            bufferedWriter.flush();
        } finally {
            // JAVA7 : Replace with improved resource handling
            try {
                out.close();
            } catch (final IOException e) {
                logger.log(Level.SEVERE, "Failed to close file", e);
            }
        }
    }

    public ChunkGeneratorManager load(final File file) throws IOException {
        ChunkGeneratorManager chunkGeneratorManager = null;
        if (file.exists()) {
            final FileInputStream in = new FileInputStream(file);
            try {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                chunkGeneratorManager = ChunkGeneratorJSONFormat.read(bufferedReader);
            } finally {
                // JAVA7: Replace with improved resource handling
                try {
                    in.close();
                } catch (final IOException e) {
                    logger.log(Level.SEVERE, "Failed to close file", e);
                }
            }
        }
        return chunkGeneratorManager;
    }

}
