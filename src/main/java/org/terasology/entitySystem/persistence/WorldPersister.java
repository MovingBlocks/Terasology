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
package org.terasology.entitySystem.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.protobuf.EntityData;

import com.google.protobuf.TextFormat;

/**
 * @author Immortius <immortius@gmail.com>
 */
// TODO: More Javadoc
public class WorldPersister {

    public enum SaveFormat {
        Binary {
            @Override
            void save(OutputStream out, EntityData.World world) throws IOException {
                world.writeTo(out);
                out.flush();
            }

            @Override
            EntityData.World load(InputStream in) throws IOException {
                return EntityData.World.parseFrom(in);
            }
        },
        Text {
            @Override
            void save(OutputStream out, EntityData.World world) throws IOException {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out));
                TextFormat.print(world, bufferedWriter);
                bufferedWriter.flush();
            }

            @Override
            EntityData.World load(InputStream in) throws IOException {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                EntityData.World.Builder builder = EntityData.World.newBuilder();
                TextFormat.merge(bufferedReader, builder);
                return builder.build();
            }
        },
        JSON {
            @Override
            void save(OutputStream out, EntityData.World world) throws IOException {
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out));
                EntityDataJSONFormat.write(world, bufferedWriter);
                bufferedWriter.flush();
            }

            @Override
            EntityData.World load(InputStream in) throws IOException {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                return EntityDataJSONFormat.readWorld(bufferedReader);
            }
        };

        abstract void save(OutputStream out, EntityData.World world) throws IOException;

        abstract EntityData.World load(InputStream in) throws IOException;
    }

    private static final Logger logger = LoggerFactory.getLogger(WorldPersister.class);
    private EntityManager entityManager;
    private WorldSerializer persisterHelper;

    public WorldPersister(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.persisterHelper = new WorldSerializerImpl((PersistableEntityManager) entityManager);
    }

    public void save(File file, SaveFormat format) throws IOException {
        final EntityData.World world = persisterHelper.serializeWorld();

        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                logger.error("Failed to create world save directory {}", parentFile);
            }
        }
        FileOutputStream out = new FileOutputStream(file);

        try {
            format.save(out, world);
        } finally {
            // JAVA7 : Replace with improved resource handling
            try {
                out.close();
            } catch (IOException e) {
                logger.error("Failed to close file", e);
            }
        }
    }

    public void load(File file, SaveFormat format) throws IOException {
        entityManager.clear();

        FileInputStream in = new FileInputStream(file);
        EntityData.World world = null;
        try {
            world = format.load(in);
        } finally {
            // JAVA7: Replace with improved resource handling
            try {
                in.close();
            } catch (IOException e) {
                logger.error("Failed to close file", e);
            }
        }

        if (world != null) {
            persisterHelper.deserializeWorld(world);
        }
    }
}
