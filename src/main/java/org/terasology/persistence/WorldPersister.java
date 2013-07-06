/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.persistence;

import com.google.protobuf.TextFormat;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.persistence.serializers.EntityDataJSONFormat;
import org.terasology.persistence.serializers.WorldSerializer;
import org.terasology.persistence.serializers.WorldSerializerImpl;
import org.terasology.protobuf.EntityData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Immortius <immortius@gmail.com>
 */
// TODO: More Javadoc
public class WorldPersister {

    public enum SaveFormat {
        Binary(false) {
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
        Text(true) {
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
        JSON(true) {
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

        private boolean verbose = false;

        private SaveFormat(boolean verbose) {
            this.verbose = verbose;
        }

        abstract void save(OutputStream out, EntityData.World world) throws IOException;

        abstract EntityData.World load(InputStream in) throws IOException;

        public boolean isVerbose() {
            return verbose;
        }
    }

    private EngineEntityManager entityManager;
    private WorldSerializer persisterHelper;

    public WorldPersister(EngineEntityManager entityManager) {
        this.entityManager = entityManager;
        this.persisterHelper = new WorldSerializerImpl(entityManager);
    }

    public void save(Path file, SaveFormat format) throws IOException {
        final EntityData.World world = persisterHelper.serializeWorld(format.isVerbose());

        Path parentFile = file.getParent();
        if (!Files.isDirectory(parentFile)) {
            Files.createDirectories(parentFile);
        }

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file))) {
            format.save(out, world);
        }
    }

    public void load(Path file, SaveFormat format) throws IOException {
        entityManager.clear();

        EntityData.World world;
        try (InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            world = format.load(in);
        }

        if (world != null) {
            persisterHelper.deserializeWorld(world);
        }
    }
}
