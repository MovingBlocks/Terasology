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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.stubs.EntityRefComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.mod.ModManager;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.internal.StorageManagerInternal;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class StorageManagerTest {

    public static final String PLAYER_ID = "someId";
    private StorageManager esm;
    private EngineEntityManager entityManager;

    @Before
    public void setup() {
        ModManager modManager = new ModManager();
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        entityManager = new EntitySystemBuilder().build(modManager, networkSystem);
        esm = new StorageManagerInternal(entityManager);
    }

    @Test
    public void getUnstoredPlayerReturnsNothing() {
        assertNull(esm.loadStore(PLAYER_ID));
    }

    @Test
    public void storeAndRestorePlayerStore() {
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.save();
        PlayerStore restoredStore = esm.loadStore(PLAYER_ID);
        assertNotNull(restoredStore);
        assertFalse(restoredStore.hasCharacter());
        assertEquals(new Vector3f(), restoredStore.getRelevanceLocation());
    }

    @Test
    public void playerRelevanceLocationSurvivesStorage() {
        Vector3f loc = new Vector3f(1,2,3);
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.setRelevanceLocation(loc);
        assertEquals(loc, store.getRelevanceLocation());
        store.save();

        PlayerStore restored = esm.loadStore(PLAYER_ID);
        assertEquals(loc, restored.getRelevanceLocation());
    }

    @Test
    public void addCharacterSurvivesStorage() {
        EntityRef character = entityManager.create();
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        assertFalse(store.hasCharacter());
        store.setCharacter(character);
        assertTrue(store.hasCharacter());
        assertEquals(character, store.getCharacter());
        store.save();

        PlayerStore restored = esm.loadStore(PLAYER_ID);
        restored.restore();
        assertTrue(restored.hasCharacter());
        assertEquals(character, restored.getCharacter());
    }

    @Test
    public void relevanceLocationSetToCharacterLocation() {
        Vector3f loc = new Vector3f(1,2,3);
        EntityRef character = entityManager.create(new LocationComponent(loc));
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.setCharacter(character);
        assertEquals(loc, store.getRelevanceLocation());
    }

    @Test
    public void characterEntityDeactivatedWhileStored() {
        EntityRef character = entityManager.create();
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.setCharacter(character);
        store.save();
        assertFalse(character.isActive());
    }

    @Test
    public void referenceCorrectlyInvalidatedWhileStored() {
        EntityRef someEntity = entityManager.create();
        EntityRef character = entityManager.create(new EntityRefComponent(someEntity));
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.setCharacter(character);
        store.save();

        someEntity.destroy();
        entityManager.create(); // This causes the destroyed entity's id to be reused

        PlayerStore restored = esm.loadStore(PLAYER_ID);
        restored.restore();
        assertFalse(character.getComponent(EntityRefComponent.class).entityRef.exists());
    }

}
