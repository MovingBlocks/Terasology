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
import org.terasology.persistence.internal.StorageManagerInternal;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class StorageManagerTest {

    public static final String PLAYER_ID = "someId";
    private StorageManager esm;

    @Before
    public void setup() {
        esm = new StorageManagerInternal();
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
}
