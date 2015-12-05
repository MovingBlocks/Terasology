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
package org.terasology.reflection.reflect;

import org.junit.Test;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.logic.characters.events.AttackRequest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 */
public class ByteCodeReflectFactoryTest {

    @Test
    public void createConstructorObjectWithPublicConstructor() throws NoSuchMethodException {
        ReflectFactory reflectFactory = new ByteCodeReflectFactory();
        ObjectConstructor<LocationComponent> constructor = reflectFactory.createConstructor(LocationComponent.class);
        LocationComponent locationComponent = constructor.construct();
        assertNotNull(locationComponent);
    }

    @Test
    public void createConstructorObjectWithProtectedConstructor() throws Exception {
        ReflectFactory reflectFactory = new ByteCodeReflectFactory();
        ObjectConstructor<AttackRequest> constructor = reflectFactory.createConstructor(AttackRequest.class);
        AttackRequest result = constructor.construct();
        assertNotNull(result);
    }

    @Test
    public void createFieldAccessorWithGetterSetter() throws Exception {
        ReflectFactory reflectFactory = new ByteCodeReflectFactory();
        FieldAccessor<GetterSetterComponent, Vector3f> fieldAccessor
                = reflectFactory.createFieldAccessor(GetterSetterComponent.class, GetterSetterComponent.class.getDeclaredField("value"), Vector3f.class);
        GetterSetterComponent comp = new GetterSetterComponent();
        Vector3f newVal = new Vector3f(1, 2, 3);
        fieldAccessor.setValue(comp, newVal);
        assertTrue(comp.setterUsed);

        assertEquals(newVal, fieldAccessor.getValue(comp));
        assertTrue(comp.getterUsed);
    }

    @Test
    public void createFieldAccessorDirectToField() throws Exception {
        ReflectFactory reflectFactory = new ByteCodeReflectFactory();
        FieldAccessor<StringComponent, String> fieldAccessor
                = reflectFactory.createFieldAccessor(StringComponent.class, StringComponent.class.getDeclaredField("value"), String.class);
        StringComponent comp = new StringComponent();
        fieldAccessor.setValue(comp, "String");
        assertEquals("String", fieldAccessor.getValue(comp));
    }

    @Test
    public void accessIntegerField() throws Exception {
        ReflectFactory reflectFactory = new ByteCodeReflectFactory();
        FieldAccessor fieldAccessor
                = reflectFactory.createFieldAccessor(IntegerComponent.class, IntegerComponent.class.getDeclaredField("value"));
        IntegerComponent comp = new IntegerComponent();
        fieldAccessor.setValue(comp, 1);
        assertEquals(1, fieldAccessor.getValue(comp));
    }

}
