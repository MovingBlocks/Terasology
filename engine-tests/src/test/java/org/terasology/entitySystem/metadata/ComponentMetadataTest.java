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
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.stubs.OwnerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 */
public class ComponentMetadataTest {

    private Context context;
    private ReflectFactory reflectFactory = new ReflectionReflectFactory();
    private CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);

    @Before
    public void prepare() {
        context = new ContextImpl();
        context.put(ReflectFactory.class, reflectFactory);
        context.put(CopyStrategyLibrary.class, copyStrategies);
    }

    @Test
    public void staticFieldsIgnored() {
        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(context, new TypeSerializationLibrary(reflectFactory, copyStrategies));
        ComponentLibrary lib = entitySystemLibrary.getComponentLibrary();
        lib.register(new SimpleUri("unittest:string"), StringComponent.class);
        ComponentMetadata<StringComponent> metadata = lib.getMetadata(StringComponent.class);
        assertNull(metadata.getField("STATIC_VALUE"));
    }

    @Test
    public void ownsReferencesPopulated() {
        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(context, new TypeSerializationLibrary(reflectFactory, copyStrategies));
        ComponentLibrary lib = entitySystemLibrary.getComponentLibrary();
        lib.register(new SimpleUri("unittest:owner"), OwnerComponent.class);
        ComponentMetadata<OwnerComponent> metadata = lib.getMetadata(OwnerComponent.class);
        assertTrue(metadata.isReferenceOwner());
    }

}
