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

import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.testUtil.TeraAssert;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class PlayerEntityStoreTest extends TerasologyTestingEnvironment {

    @Test
    public void storeAndRestore() throws IOException {
        EngineEntityManager entityManager = getEntityManager();
        EntityRef character = entityManager.create("engine:player");
        LocationComponent loc = character.getComponent(LocationComponent.class);
        loc.setWorldPosition(new Vector3f(1,2,3));
        character.saveComponent(loc);

        PlayerEntityStore entityStore = new PlayerEntityStore("testPlayerStore", getEntityManager());
        entityStore.beginStore();
        entityStore.setRelevanceLocation(new Vector3f(413, 5, 42));
        entityStore.setHasCharacter(true);
        entityStore.store(character, "character");
        entityStore.endStore();

        entityManager.clear();

        entityStore = new PlayerEntityStore("testPlayerStore", getEntityManager());
        entityStore.beginRestore();
        TeraAssert.assertEquals(new Vector3f(413, 5, 42), entityStore.getRelevanceLocation(), 0.001f);
        assertTrue(entityStore.hasCharacter());
        Map<String, EntityRef> loadedEntities = entityStore.restoreAll();
        assertTrue(loadedEntities.containsKey("character"));
        EntityRef characterEntity = loadedEntities.get("character");
        assertNotNull(characterEntity);
        assertTrue(characterEntity.exists());
        assertNotNull(characterEntity.getComponent(LocationComponent.class));
        TeraAssert.assertEquals(new Vector3f(1,2,3), characterEntity.getComponent(LocationComponent.class).getWorldPosition(), 0.001f);
    }

}
