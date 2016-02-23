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
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.stubs.OwnerComponent;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import static org.junit.Assert.assertTrue;

/**
 */
public class FieldMetadataTest {

    private ReflectFactory factory = new ReflectionReflectFactory();
    private CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(factory);

    @Test
    public void testOwnsAnnotationProcessed() throws NoSuchMethodException {
        ComponentMetadata<OwnerComponent> classMetadata = new ComponentMetadata<>(new SimpleUri("unittest:owner"), OwnerComponent.class, factory, copyStrategyLibrary);
        ComponentFieldMetadata metadata = classMetadata.getField("child");
        assertTrue(metadata.isOwnedReference());
    }

    @Test
    public void testOwnsAnnotationCollectionProcessed() throws NoSuchMethodException {
        ComponentMetadata<OwnedCollectionComponent> classMetadata =
                new ComponentMetadata<>(new SimpleUri("unittest:OwnedCollectionComponent"), OwnedCollectionComponent.class, factory, copyStrategyLibrary);
        ComponentFieldMetadata metadata = classMetadata.getField("items");
        assertTrue(metadata.isOwnedReference());
    }

}
