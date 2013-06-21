package org.terasology.world.chunks.localChunkProvider;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.world.RelevanceRegionComponent;

/**
 * @author Immortius
 */
public class RelevanceSystem implements ComponentSystem {

    private LocalChunkProvider chunkProvider;

    public RelevanceSystem(LocalChunkProvider chunkProvider) {
        this.chunkProvider = chunkProvider;
    }

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {RelevanceRegionComponent.class, LocationComponent.class})
    public void onNewRelevanceRegion(OnActivatedComponent event, EntityRef entity) {
        chunkProvider.addRelevanceEntity(entity, entity.getComponent(RelevanceRegionComponent.class).distance);
    }

    @ReceiveEvent(components = {RelevanceRegionComponent.class})
    public void onRelevanceRegionChanged(OnChangedComponent event, EntityRef entity) {
        chunkProvider.updateRelevanceEntity(entity, entity.getComponent(RelevanceRegionComponent.class).distance);
    }

    @ReceiveEvent(components = {RelevanceRegionComponent.class, LocationComponent.class})
    public void onLostRelevanceRegion(BeforeDeactivateComponent event, EntityRef entity) {
        chunkProvider.removeRelevanceEntity(entity);
    }
}
