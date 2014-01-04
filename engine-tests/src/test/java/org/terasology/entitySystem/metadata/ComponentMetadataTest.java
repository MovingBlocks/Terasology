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

package org.terasology.entitySystem.metadata;

import org.junit.Test;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.classMetadata.reflect.ReflectionReflectFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.stubs.OwnerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.persistence.typeSerialization.TypeSerializationLibrary;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Immortius
 */
public class ComponentMetadataTest {

    private ReflectFactory reflectFactory = new ReflectionReflectFactory();
    private CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);

    @Test
    public void staticFieldsIgnored() {
        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(reflectFactory, copyStrategies, new TypeSerializationLibrary(reflectFactory, copyStrategies));
        ComponentLibrary lib = entitySystemLibrary.getComponentLibrary();
        lib.register(new SimpleUri("unittest:string"), StringComponent.class);
        ComponentMetadata<StringComponent> metadata = lib.getMetadata(StringComponent.class);
        assertNull(metadata.getField("STATIC_VALUE"));
    }

    @Test
    public void ownsReferencesPopulated() {
        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(reflectFactory, copyStrategies, new TypeSerializationLibrary(reflectFactory, copyStrategies));
        ComponentLibrary lib = entitySystemLibrary.getComponentLibrary();
        lib.register(new SimpleUri("unittest:owner"), OwnerComponent.class);
        ComponentMetadata<OwnerComponent> metadata = lib.getMetadata(OwnerComponent.class);
        assertTrue(metadata.isReferenceOwner());
    }

}
