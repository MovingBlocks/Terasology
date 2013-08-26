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

import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.metadata.copying.CopyStrategyLibrary;
import org.terasology.entitySystem.metadata.internal.ClassMetadataImpl;
import org.terasology.entitySystem.metadata.internal.FieldMetadataImpl;
import org.terasology.entitySystem.metadata.reflect.ReflectFactory;
import org.terasology.entitySystem.metadata.reflect.ReflectionReflectFactory;
import org.terasology.persistence.typeSerialization.TypeSerializationLibrary;
import org.terasology.persistence.typeSerialization.typeHandlers.core.ListTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.extension.EntityRefTypeHandler;
import org.terasology.entitySystem.stubs.OwnerComponent;
import org.terasology.logic.inventory.InventoryComponent;

import static org.junit.Assert.assertTrue;

/**
 * @author Immortius
 */
public class FieldMetadataTest {

    private ReflectFactory factory = new ReflectionReflectFactory();
    private CopyStrategyLibrary copyStrategyLibrary = CopyStrategyLibrary.create(factory);

    @Test
    public void testOwnsAnnotationProcessed() throws NoSuchMethodException {
        ClassMetadata<OwnerComponent> classMetadata = new ClassMetadataImpl<>(OwnerComponent.class, copyStrategyLibrary, factory, "");
        FieldMetadata metadata = classMetadata.getField("child");
        assertTrue(metadata.isOwnedReference());
    }

    @Test
    public void testOwnsAnnotationCollectionProcessed() throws NoSuchMethodException {
        ClassMetadata<InventoryComponent> classMetadata = new ClassMetadataImpl<>(InventoryComponent.class, copyStrategyLibrary, factory, "");
        FieldMetadata metadata = classMetadata.getField("itemSlots");
        assertTrue(metadata.isOwnedReference());
    }

}
