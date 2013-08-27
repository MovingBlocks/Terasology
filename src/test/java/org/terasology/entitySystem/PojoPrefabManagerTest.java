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
package org.terasology.entitySystem;

import org.junit.Before;
import org.junit.Test;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.internal.PojoPrefabManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.classMetadata.reflect.ReflectionReflectFactory;
import org.terasology.persistence.typeSerialization.TypeSerializationLibrary;
import org.terasology.persistence.typeSerialization.typeHandlers.extension.Quat4fTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.stubs.StringComponent;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Immortius <immortius@gmail.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class PojoPrefabManagerTest {

    public static final String PREFAB_NAME = "unittest:myprefab";
    private EntitySystemLibrary entitySystemLibrary;
    private ComponentLibrary componentLibrary;
    private PojoPrefabManager prefabManager;

    @Before
    public void setup() {
        ReflectFactory reflectFactory = new ReflectionReflectFactory();
        CopyStrategyLibrary copyStrategyLibrary = CopyStrategyLibrary.create(reflectFactory);
        TypeSerializationLibrary lib = new TypeSerializationLibrary(reflectFactory, copyStrategyLibrary);
        lib.add(Vector3f.class, new Vector3fTypeHandler());
        lib.add(Quat4f.class, new Quat4fTypeHandler());
        entitySystemLibrary = new EntitySystemLibrary(reflectFactory, copyStrategyLibrary, lib);
        componentLibrary = entitySystemLibrary.getComponentLibrary();
        prefabManager = new PojoPrefabManager();
    }

    @Test
    public void retrieveNonExistentPrefab() {
        assertNull(prefabManager.getPrefab(PREFAB_NAME));
    }

    @Test
    public void retrievePrefab() {
        PrefabData data = new PrefabData();
        data.addComponent(new StringComponent("Test"));
        Prefab prefab = Assets.generateAsset(new AssetUri(AssetType.PREFAB, PREFAB_NAME), data, Prefab.class);
        prefabManager.registerPrefab(prefab);
        Prefab ref = prefabManager.getPrefab(PREFAB_NAME);
        assertNotNull(ref);
        assertEquals(PREFAB_NAME, ref.getName());
    }


}
