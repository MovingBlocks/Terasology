/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.chunks.localChunkProvider;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.world.RelevanceRegionComponent;

/**
 */
public class RelevanceSystem extends BaseComponentSystem {

    private LocalChunkProvider chunkProvider;

    public RelevanceSystem(LocalChunkProvider chunkProvider) {
        this.chunkProvider = chunkProvider;
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
