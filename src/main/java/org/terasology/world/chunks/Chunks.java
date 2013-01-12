package org.terasology.world.chunks;

import java.util.Map;

import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.world.chunks.blockdata.TeraArrays;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public final class Chunks {

    private static final Config config;
    
    private static final TeraArrays.Entry blockDataEntry;
    private static final TeraArrays.Entry sunlightDataEntry;
    private static final TeraArrays.Entry lightDataEntry;
    private static final TeraArrays.Entry extraDataEntry;
    
    private static final Map<String, TeraArrays.Entry> modDataEntries;
    
    private static final Chunk.ProtobufHandler handler;
    
    static {
        config = CoreRegistry.get(Config.class);
        
        blockDataEntry = TeraArrays.getEntry(config.getAdvancedConfig().getBlocksFactoryName());
        sunlightDataEntry = TeraArrays.getEntry(config.getAdvancedConfig().getSunlightFactoryName());
        lightDataEntry = TeraArrays.getEntry(config.getAdvancedConfig().getLightFactoryName());
        extraDataEntry = TeraArrays.getEntry(config.getAdvancedConfig().getExtraFactoryName());
        
        modDataEntries = Maps.newHashMap();
        
        handler = new Chunk.ProtobufHandler();
    }
    
    private Chunks() {}
    
    public static final TeraArrays.Entry getBlockDataEntry() {
        return blockDataEntry;
    }

    public static final TeraArrays.Entry getSunlightDataEntry() {
        return sunlightDataEntry;
    }

    public static final TeraArrays.Entry getLightDataEntry() {
        return lightDataEntry;
    }

    public static final TeraArrays.Entry getExtraDataEntry() {
        return extraDataEntry;
    }
    
    public static final void registerModData(String id, TeraArrays.Entry entry) {
        Preconditions.checkNotNull(id, "The parameter 'id' must not be null");
        Preconditions.checkArgument(!id.trim().isEmpty(), "The parameter 'id' must not be empty");
        Preconditions.checkNotNull(entry, "The parameter 'entry' must not be null");
        Preconditions.checkState(!modDataEntries.containsKey(id), "The mod data id '" + id + "' is already in use");
        modDataEntries.put(id, entry);
    }
    
    public static final ChunksProtobuf.Chunk encode(Chunk chunk) {
        return handler.encode(chunk);
    }
    
    public static final Chunk decode(ChunksProtobuf.Chunk message) {
        return handler.decode(message);
    }
}
