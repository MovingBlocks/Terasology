// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.entitySystem.stubs.OwnerComponent;
import org.terasology.engine.entitySystem.stubs.StringComponent;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 */
public class ComponentMetadataTest {

    private Context context;
    private ReflectFactory reflectFactory = new ReflectionReflectFactory();
    private CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);

    @BeforeEach
    public void prepare() {
        context = new ContextImpl();
        context.put(ReflectFactory.class, reflectFactory);
        context.put(CopyStrategyLibrary.class, copyStrategies);
    }

    @Test
    public void testStaticFieldsIgnored() {
        Reflections reflections = new Reflections(getClass().getClassLoader());
        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(context, new TypeHandlerLibraryImpl(reflections));
        ComponentLibrary lib = entitySystemLibrary.getComponentLibrary();
        lib.register(new ResourceUrn("unittest:string"), StringComponent.class);
        ComponentMetadata<StringComponent> metadata = lib.getMetadata(StringComponent.class);
        assertNull(metadata.getField("STATIC_VALUE"));
    }

    @Test
    public void testOwnsReferencesPopulated() {
        Reflections reflections = new Reflections(getClass().getClassLoader());
        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(context, new TypeHandlerLibraryImpl(reflections));
        ComponentLibrary lib = entitySystemLibrary.getComponentLibrary();
        lib.register(new ResourceUrn("unittest:owner"), OwnerComponent.class);
        ComponentMetadata<OwnerComponent> metadata = lib.getMetadata(OwnerComponent.class);
        assertTrue(metadata.isReferenceOwner());
    }

}
