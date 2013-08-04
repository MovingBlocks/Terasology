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

package org.terasology.world.chunks;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.EntityData;
import org.terasology.world.chunks.blockdata.TeraArrays;

import java.util.Map;

/**
 * Chunks is the central registration point for chunk data.
 * <p/>
 * Serialization and deserialization of Chunks into/from protobuf messages is supported through the methods
 * {@code Chunks.encode(Chunk)} and {@code Chunks.decode(ChunksProtobuf.Chunk)}.
 * <p/>
 * Mods can register chunk data extensions through the method {@code Chunks.register(String, TeraArrays.Entry)}.
 *
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 */
// TODO: Finish support for chunk data extensions.
public final class Chunks {

    private static final Chunks INSTANCE = new Chunks();

    private final TeraArrays.Entry blockDataEntry;
    private final TeraArrays.Entry sunlightDataEntry;
    private final TeraArrays.Entry lightDataEntry;
    private final TeraArrays.Entry extraDataEntry;

    private final Map<String, TeraArrays.Entry> modDataEntries;

    private final Chunk.ProtobufHandler handler;

    private Chunks() {
        final TeraArrays t = TeraArrays.getInstance();
        blockDataEntry = t.getEntry(ChunksProtobuf.Type.DenseArray16Bit);
        sunlightDataEntry = t.getEntry(ChunksProtobuf.Type.DenseArray8Bit);
        lightDataEntry = t.getEntry(ChunksProtobuf.Type.DenseArray8Bit);
        extraDataEntry = t.getEntry(ChunksProtobuf.Type.DenseArray8Bit);

        modDataEntries = Maps.newHashMap();

        handler = new Chunk.ProtobufHandler();
    }

    public final TeraArrays.Entry getBlockDataEntry() {
        return blockDataEntry;
    }

    public final TeraArrays.Entry getSunlightDataEntry() {
        return sunlightDataEntry;
    }

    public final TeraArrays.Entry getLightDataEntry() {
        return lightDataEntry;
    }

    public final TeraArrays.Entry getExtraDataEntry() {
        return extraDataEntry;
    }

    public final void registerModData(String id, TeraArrays.Entry entry) {
        Preconditions.checkNotNull(id, "The parameter 'id' must not be null");
        Preconditions.checkArgument(!id.trim().isEmpty(), "The parameter 'id' must not be empty");
        Preconditions.checkNotNull(entry, "The parameter 'entry' must not be null");
        Preconditions.checkState(!modDataEntries.containsKey(id), "The module data id '" + id + "' is already in use");
        modDataEntries.put(id, entry);
    }

    public final EntityData.ChunkStore encode(Chunk chunk, boolean coreOnly) {
        return handler.encode(chunk, coreOnly).build();
    }

    public final Chunk decode(EntityData.ChunkStore message) {
        return handler.decode(message);
    }

    public static Chunks getInstance() {
        return INSTANCE;
    }
}
