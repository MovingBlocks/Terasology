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
import org.terasology.engine.TerasologyConstants;
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
 * Used to create a dump of the current state of the world (specifically, entities)
 * @author Immortius <immortius@gmail.com>
 */
public class WorldDumper {

    private WorldSerializer persisterHelper;

    public WorldDumper(EngineEntityManager entityManager) {
        this.persisterHelper = new WorldSerializerImpl(entityManager);
    }

    public void save(Path file) throws IOException {
        final EntityData.GlobalStore world = persisterHelper.serializeWorld(true);

        Path parentFile = file.getParent();
        if (!Files.isDirectory(parentFile)) {
            Files.createDirectories(parentFile);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file, TerasologyConstants.CHARSET)) {
            EntityDataJSONFormat.write(world, writer);
        }
    }
}
