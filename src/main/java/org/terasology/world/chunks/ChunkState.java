package org.terasology.world.chunks;

public enum ChunkState {
    ADJACENCY_GENERATION_PENDING(0),
    INTERNAL_LIGHT_GENERATION_PENDING(1),
    LIGHT_PROPAGATION_PENDING(2),
    FULL_LIGHT_CONNECTIVITY_PENDING(3),
    COMPLETE(4);
    
    private ChunkState(int id) {
        this.id = id;
    }
    
    public final int id;
    
    public static final ChunkState getStateById(int id) {
        switch(id) {
        case 0: return ADJACENCY_GENERATION_PENDING;
        case 1: return INTERNAL_LIGHT_GENERATION_PENDING;
        case 2: return LIGHT_PROPAGATION_PENDING;
        case 3: return FULL_LIGHT_CONNECTIVITY_PENDING;
        case 4: return COMPLETE;
        }
        return null;
    }
}