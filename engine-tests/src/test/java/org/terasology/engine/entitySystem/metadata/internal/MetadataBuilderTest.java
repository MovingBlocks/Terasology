// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.metadata.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.DefaultClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetadataBuilderTest {

    private final ReflectFactory factory = new ReflectionReflectFactory();
    private final CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(factory);

    @BeforeEach
    public void setup() {
    }

    @Test
    public void testDetectsLackOfDefaultConstructor() throws Exception {
        DefaultClassMetadata<NoDefaultConstructor> metadata = new DefaultClassMetadata<>("engine:empty",
                NoDefaultConstructor.class, factory, copyStrategyLibrary);
        assertFalse(metadata.isConstructable());
    }

    @Test
    public void testTrivialMetadata() throws Exception {
        DefaultClassMetadata<Trivial> metadata = new DefaultClassMetadata<>("engine:empty",
                Trivial.class, factory, copyStrategyLibrary);
        assertNotNull(metadata);
        assertEquals(0, metadata.getFieldCount());
        assertTrue(metadata.isConstructable());
    }

    @Test
    public void testPrivateField() throws Exception {
        DefaultClassMetadata<PrivateField> metadata = new DefaultClassMetadata<>("engine:empty",
                PrivateField.class, factory, copyStrategyLibrary);
        assertNotNull(metadata);
        assertEquals(1, metadata.getFieldCount());
        FieldMetadata fieldMetadata = metadata.getField("name");
        assertNotNull(fieldMetadata);
        assertEquals(String.class, fieldMetadata.getType());
        assertEquals("name", fieldMetadata.getName());
        assertNotNull(metadata.newInstance());
    }

    @Test
    public void testInheritsFields() throws Exception {
        DefaultClassMetadata<Inheriting> metadata = new DefaultClassMetadata<>("engine:empty",
                Inheriting.class, factory, copyStrategyLibrary);
        assertNotNull(metadata);
        assertEquals(2, metadata.getFieldCount());
        assertNotNull(metadata.getField("name"));
        assertNotNull(metadata.getField("value"));
        assertNotNull(metadata.newInstance());
    }

    private static class NoDefaultConstructor {
        NoDefaultConstructor(String name) {

        }
    }

    public static class Trivial {
    }

    public static class PrivateField {
        private String name;

        private PrivateField() {
        }

        public PrivateField(String name) {
            this.name = name;
        }

        public String whatName() {
            return name;
        }
    }

    public static class Inheriting extends PrivateField {
        private String value;

        private Inheriting() {
        }

        public Inheriting(String name, String value) {
            super(name);
            this.value = value;
        }

        public String whatValue() {
            return value;
        }
    }
}
