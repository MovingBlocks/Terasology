// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.metadata;

import org.junit.jupiter.api.Test;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.unittest.stubs.OwnerComponent;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FieldMetadataTest {

    private ReflectFactory factory = new ReflectionReflectFactory();
    private CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(factory);

    @Test
    public void testOwnsAnnotationProcessed() throws NoSuchMethodException {
        ComponentMetadata<OwnerComponent> classMetadata = new ComponentMetadata<>(new ResourceUrn("unittest:owner"), OwnerComponent.class, factory, copyStrategyLibrary);
        ComponentFieldMetadata metadata = classMetadata.getField("child");
        assertTrue(metadata.isOwnedReference());
    }

    @Test
    public void testOwnsAnnotationCollectionProcessed() throws NoSuchMethodException {
        ComponentMetadata<org.terasology.engine.entitySystem.metadata.OwnedCollectionComponent> classMetadata =
                new ComponentMetadata<>(new ResourceUrn("unittest:OwnedCollectionComponent"),
                        org.terasology.engine.entitySystem.metadata.OwnedCollectionComponent.class, factory, copyStrategyLibrary);
        ComponentFieldMetadata metadata = classMetadata.getField("items");
        assertTrue(metadata.isOwnedReference());
    }

}
