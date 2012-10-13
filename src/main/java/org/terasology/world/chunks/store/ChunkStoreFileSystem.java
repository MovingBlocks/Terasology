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
package org.terasology.world.chunks.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkStore;

public class ChunkStoreFileSystem implements ChunkStore {

    private static final Logger logger = LoggerFactory.getLogger(ChunkStoreFileSystem.class);

    private File worldPath;

    public ChunkStoreFileSystem(File worldPath) {
        this.worldPath = worldPath;
    }

    private String getFileNameFor(Vector3i pos) {
        return pos.x + "." + pos.y + "." + pos.z + ".chunk";
    }

    public Chunk get(Vector3i id) {
        File f = new File(worldPath, getFileNameFor(id));

        if (!f.exists())
            return null;

        try {
            FileInputStream fileIn = new FileInputStream(f);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            Chunk result = (Chunk) in.readObject();

            in.close();
            fileIn.close();

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void put(Chunk c) {
        if (!worldPath.exists()) {
            if (!worldPath.mkdirs()) {
                logger.error("Failed to create save directory, aborting save.");
                return;
            }
        }

        File f = new File(worldPath, getFileNameFor(c.getPos()));

        try {
            FileOutputStream fileOut = new FileOutputStream(f);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(c);
            out.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean contains(Vector3i position) {
        File f = new File(worldPath, getFileNameFor(position));
        return f.exists();

    }

    @Override
    public float size() {
        return 0;
    }

    @Override
    public void dispose() {
    }
}
