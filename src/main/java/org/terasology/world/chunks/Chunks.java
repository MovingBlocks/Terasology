package org.terasology.world.chunks;

import java.util.Map;

import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.world.chunks.blockdata.TeraArrays;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Chunks is the central registration point for chunk data. 
 * <p/>
 * Serialization and deserialization of Chunks into/from protobuf messages is supported through the methods
 * {@code Chunks.encode(Chunk)} and {@code Chunks.decode(ChunksProtobuf.Chunk)}.
 * <p/>
 * Mods can register chunk data extensions through the method {@code Chunks.register(String, TeraArrays.Entry)}.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 * @todo Finish support for chunk data extensions.
 *
 */
public final class Chunks {

    private static final Chunks instance = new Chunks();
    
    private final Config config;
    
    private final TeraArrays.Entry blockDataEntry;
    private final TeraArrays.Entry sunlightDataEntry;
    private final TeraArrays.Entry lightDataEntry;
    private final TeraArrays.Entry extraDataEntry;
    
    private final Map<String, TeraArrays.Entry> modDataEntries;
    
    private final Chunk.ProtobufHandler handler;
    
    private Chunks() {
        config = CoreRegistry.get(Config.class);
        
        final TeraArrays t = TeraArrays.getInstance();
        blockDataEntry = t.getEntry(config.getAdvanced().getBlocksFactoryName());
        sunlightDataEntry = t.getEntry(config.getAdvanced().getSunlightFactoryName());
        lightDataEntry = t.getEntry(config.getAdvanced().getLightFactoryName());
        extraDataEntry = t.getEntry(config.getAdvanced().getExtraFactoryName());
        
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
        Preconditions.checkState(!modDataEntries.containsKey(id), "The mod data id '" + id + "' is already in use");
        modDataEntries.put(id, entry);
    }
    
    public final ChunksProtobuf.Chunk encode(Chunk chunk) {
        return handler.encode(chunk);
    }
    
    public final Chunk decode(ChunksProtobuf.Chunk message) {
        return handler.decode(message);
    }
    
    public static final Chunks getInstance() {
        return instance;
    }
}
