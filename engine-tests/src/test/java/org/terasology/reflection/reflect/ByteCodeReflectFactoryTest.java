// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.reflection.reflect;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.terasology.engine.logic.characters.events.AttackRequest;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.reflection.reflect.ByteCodeReflectFactory;
import org.terasology.unittest.stubs.GetterSetterComponent;
import org.terasology.unittest.stubs.IntegerComponent;
import org.terasology.unittest.stubs.StringComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ByteCodeReflectFactoryTest {

    @Test
    public void testCreateConstructorObjectWithPublicConstructor() throws NoSuchMethodException {
        ReflectFactory reflectFactory = new ByteCodeReflectFactory();
        ObjectConstructor<LocationComponent> constructor = reflectFactory.createConstructor(LocationComponent.class);
        LocationComponent locationComponent = constructor.construct();
        assertNotNull(locationComponent);
    }

    @Test
    public void testCreateConstructorObjectWithProtectedConstructor() throws Exception {
        ReflectFactory reflectFactory = new ByteCodeReflectFactory();
        ObjectConstructor<AttackRequest> constructor = reflectFactory.createConstructor(AttackRequest.class);
        AttackRequest result = constructor.construct();
        assertNotNull(result);
    }

    @Test
    public void testCreateFieldAccessorWithGetterSetter() throws Exception {
        ReflectFactory reflectFactory = new ByteCodeReflectFactory();
        FieldAccessor<GetterSetterComponent, Vector3f> fieldAccessor = reflectFactory.createFieldAccessor(GetterSetterComponent.class,
                GetterSetterComponent.class.getDeclaredField("value"), Vector3f.class);
        GetterSetterComponent comp = new GetterSetterComponent();
        Vector3f newVal = new Vector3f(1, 2, 3);
        fieldAccessor.setValue(comp, newVal);
        assertTrue(comp.setterUsed);

        assertEquals(newVal, fieldAccessor.getValue(comp));
        assertTrue(comp.getterUsed);
    }

    @Test
    public void testCreateFieldAccessorDirectToField() throws Exception {
        ReflectFactory reflectFactory = new ByteCodeReflectFactory();
        FieldAccessor<StringComponent, String> fieldAccessor
                = reflectFactory.createFieldAccessor(StringComponent.class, StringComponent.class.getDeclaredField("value"), String.class);
        StringComponent comp = new StringComponent();
        fieldAccessor.setValue(comp, "String");
        assertEquals("String", fieldAccessor.getValue(comp));
    }

    @Test
    public void testAccessIntegerField() throws Exception {
        ReflectFactory reflectFactory = new ByteCodeReflectFactory();
        FieldAccessor fieldAccessor
                = reflectFactory.createFieldAccessor(IntegerComponent.class, IntegerComponent.class.getDeclaredField("value"));
        IntegerComponent comp = new IntegerComponent();
        fieldAccessor.setValue(comp, 1);
        assertEquals(1, fieldAccessor.getValue(comp));
    }

}
