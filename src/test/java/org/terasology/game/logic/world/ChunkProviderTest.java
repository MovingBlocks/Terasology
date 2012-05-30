/*
 * Copyright 2012
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

package org.terasology.game.logic.world;

import org.junit.Before;
import org.junit.Test;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.common.NullIterator;
import org.terasology.logic.world.chunkCache.NullChunkStore;
import org.terasology.logic.world.LocalChunkProvider;
import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.NullChunkGeneratorManager;

import javax.vecmath.Vector3f;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Immortius
 */
public class ChunkProviderTest {

    LocalChunkProvider chunkProvider;

    @Before
    public void setup() {
        chunkProvider = new LocalChunkProvider(new NullChunkStore(), new NullChunkGeneratorManager());
    }

    @Test
    public void testCreateChunksInRegion() throws Exception{
        chunkProvider.addRegionEntity(new StubLocationEntity(new Vector3f(0,0,0)), 1);

        Chunk chunk = chunkProvider.getChunk(0, 0, 0);
        assertNotNull(chunk);

    }

    public class StubLocationEntity extends EntityRef
    {
        private LocationComponent loc = new LocationComponent();

        public StubLocationEntity(Vector3f position) {
            loc.setWorldPosition(position);
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public boolean hasComponent(Class<? extends Component> component) {
            return component == LocationComponent.class;
        }

        @Override
        public <T extends Component> T getComponent(Class<T> componentClass) {
            if (componentClass == LocationComponent.class) {
                return componentClass.cast(loc);
            }
            return null;
        }

        @Override
        public <T extends Component> T addComponent(T component) {
            return null;
        }

        @Override
        public void removeComponent(Class<? extends Component> componentClass) {
        }

        @Override
        public void saveComponent(Component component) {
        }

        @Override
        public Iterable<Component> iterateComponents() {
            return NullIterator.newInstance();
        }

        @Override
        public void destroy() {
        }

        @Override
        public void send(Event event) {
        }

        @Override
        public int getId() {
            return 1;
        }
    }
}
