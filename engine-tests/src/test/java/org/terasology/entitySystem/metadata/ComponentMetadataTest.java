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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.stubs.OwnerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
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
        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(context, new TypeHandlerLibrary(reflections));
        ComponentLibrary lib = entitySystemLibrary.getComponentLibrary();
        lib.register(new ResourceUrn("unittest:string"), StringComponent.class);
        ComponentMetadata<StringComponent> metadata = lib.getMetadata(StringComponent.class);
        assertNull(metadata.getField("STATIC_VALUE"));
    }

    @Test
    public void testOwnsReferencesPopulated() {
        Reflections reflections = new Reflections(getClass().getClassLoader());
        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(context, new TypeHandlerLibrary(reflections));
        ComponentLibrary lib = entitySystemLibrary.getComponentLibrary();
        lib.register(new ResourceUrn("unittest:owner"), OwnerComponent.class);
        ComponentMetadata<OwnerComponent> metadata = lib.getMetadata(OwnerComponent.class);
        assertTrue(metadata.isReferenceOwner());
    }

}
