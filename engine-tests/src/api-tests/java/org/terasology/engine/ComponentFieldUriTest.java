/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.engine;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import org.terasology.naming.Name;
import org.terasology.testUtil.ApiTestsParams;
import org.terasology.testUtil.ApiTestsUtils;
import org.terasology.testUtil.MethodSignature;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ComponentFieldUriTest {

    private static Class<ComponentFieldUri> cl;

    private static Set<MethodSignature> registeredMethods;

    private static Set<MethodSignature> registeredConstructors;

    @BeforeClass
    public static void setupClass() {
        cl = ComponentFieldUri.class;
        registeredMethods = ApiTestsUtils.getModifiableSet(ApiTestsParams.ObjectClassMethods);
        registeredMethods.add(new MethodSignature("getObjectName", null,Modifier.PUBLIC,Name.class,null));
        registeredMethods.add(new MethodSignature("getModuleName",null,Modifier.PUBLIC,Name.class,null));
        registeredMethods.add(new MethodSignature("getComponentUri",null,Modifier.PUBLIC,SimpleUri.class,null));
        registeredMethods.add(new MethodSignature("getFieldName", null,Modifier.PUBLIC,String.class,null));
        registeredMethods.add(new MethodSignature("isValid",null,Modifier.PUBLIC,boolean.class,null));

        registeredConstructors = new HashSet<>();
        registeredConstructors.add(new MethodSignature("ComponentFieldUri",new Class[]{SimpleUri.class,String.class},Modifier.PUBLIC,null));
        registeredConstructors.add(new MethodSignature("ComponentFieldUri",new Class[]{String.class},Modifier.PUBLIC,null));
    }

    @Test
    public void majorIncreaseTest() {
        ApiTestsUtils.compareConstructors(cl,registeredConstructors);
        ApiTestsUtils.compareMethods(cl,registeredMethods);
    }

    @Test
    public void minorIncreaseTest() {
        ApiTestsUtils.compareConstructorsNum(cl,registeredConstructors);
        ApiTestsUtils.compareMethodsNum(cl,registeredMethods);

    }

    @Test(timeout = 4000)
    public void constructorTest1()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = null;
        try {
            componentFieldUri0 = new ComponentFieldUri((String) null);
            fail("Expecting exception: NullPointerException");

        } catch(NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("org.terasology.engine.ComponentFieldUri", e);
        }
    }

    @Test(timeout = 4000)
    public void objectNameTest1()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri("");
        Name name0 = componentFieldUri0.getObjectName();
        assertEquals("", name0.toString());
    }

    @Test(timeout = 4000)
    public void objectNameTest2()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri((SimpleUri) null, "f`");
        // Undeclared exception!
        try {
            componentFieldUri0.getObjectName();
            fail("Expecting exception: NullPointerException");

        } catch(NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("org.terasology.engine.ComponentFieldUri", e);
        }
    }

    @Test(timeout = 4000)
    public void isValidTest1()  throws Throwable  {
        SimpleUri simpleUri0 = new SimpleUri("29N.~L;", "29N.~L;");
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri(simpleUri0, (String) null);
        componentFieldUri0.getObjectName();
        assertFalse(componentFieldUri0.isValid());
    }

    @Test(timeout = 4000)
    public void isValidTest2()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri((SimpleUri) null, "p");
        // Undeclared exception!
        try {
            componentFieldUri0.isValid();
            fail("Expecting exception: NullPointerException");

        } catch(NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("org.terasology.engine.ComponentFieldUri", e);
        }
    }

    @Test(timeout = 4000)
    public void isValidTest3()  throws Throwable  {
        SimpleUri simpleUri0 = new SimpleUri("29N.~L;", "29N.~L;");
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri(simpleUri0, (String) null);
        boolean boolean0 = componentFieldUri0.isValid();
        assertFalse(boolean0);
    }

    @Test(timeout = 4000)
    public void moduleNameTest1()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri("7bgvD4");
        Name name0 = componentFieldUri0.getModuleName();
        assertEquals("", name0.toString());
    }

    @Test(timeout = 4000)
    public void moduleNameTest2()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri((SimpleUri) null, "H0D)v=YM~g]");
        // Undeclared exception!
        try {
            componentFieldUri0.getModuleName();
            fail("Expecting exception: NullPointerException");

        } catch(NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("org.terasology.engine.ComponentFieldUri", e);
        }
    }

    @Test(timeout = 4000)
    public void moduleNameTest3()  throws Throwable  {
        SimpleUri simpleUri0 = new SimpleUri("29N.~L;", "29N.~L;");
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri(simpleUri0, (String) null);
        componentFieldUri0.getModuleName();
        assertFalse(componentFieldUri0.isValid());
    }

    @Test(timeout = 4000)
    public void fieldNameTest1()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri((SimpleUri) null, "p");
        String string0 = componentFieldUri0.getFieldName();
        assertEquals("p", string0);
    }

    @Test(timeout = 4000)
    public void fieldNameTest2()  throws Throwable  {
        SimpleUri simpleUri0 = new SimpleUri("", "");
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri(simpleUri0, "");
        String string0 = componentFieldUri0.getFieldName();
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void fieldNameTest3()  throws Throwable  {
        SimpleUri simpleUri0 = new SimpleUri("org.terasology.naming.Name", "org.terasology.naming.Name");
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri(simpleUri0, "org.terasology.naming.Name");
        componentFieldUri0.hashCode();
        assertEquals("org.terasology.naming.Name", componentFieldUri0.getFieldName());
    }

    @Test(timeout = 4000)
    public void fieldNameTest4()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri((SimpleUri) null, "$,,8|` ZR");
        componentFieldUri0.getComponentUri();
        assertEquals("$,,8|` ZR", componentFieldUri0.getFieldName());
    }

    @Test(timeout = 4000)
    public void componentUriTest1()  throws Throwable  {
        SimpleUri simpleUri0 = new SimpleUri("org.terasology.naming.Name", "org.terasology.naming.Name");
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri(simpleUri0, "org.terasology.naming.Name");
        componentFieldUri0.getComponentUri();
        assertEquals("org.terasology.naming.Name", componentFieldUri0.getFieldName());
    }

    @Test(timeout = 4000)
    public void componentUriTest2()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri("");
        SimpleUri simpleUri0 = componentFieldUri0.getComponentUri();
        assertFalse(simpleUri0.isValid());
    }

    @Test(timeout = 4000)
    public void toStringTest1()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri((SimpleUri) null, "N.sd6<;I6rqW%;<@%0");
        // Undeclared exception!
        try {
            componentFieldUri0.toString();
            fail("Expecting exception: NullPointerException");
        } catch(NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("org.terasology.engine.ComponentFieldUri", e);
        }
    }

    @Test(timeout = 4000)
    public void toStringTest2()  throws Throwable  {
        SimpleUri simpleUri0 = new SimpleUri("cX>?els)NUnN", "cX>?els)NUnN");
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri(simpleUri0, "cX>?els)NUnN");
        String string0 = componentFieldUri0.toString();
        assertEquals("cX>?els)NUnN:cX>?els)NUnN.cX>?els)NUnN", string0);
    }

    @Test(timeout = 4000)
    public void toStringTest3()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri("");
        String string0 = componentFieldUri0.toString();
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void hashCodeTest1()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri((SimpleUri) null, "");
        // Undeclared exception!
        try {
            componentFieldUri0.hashCode();
            fail("Expecting exception: NullPointerException");

        } catch(NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("org.terasology.engine.ComponentFieldUri", e);
        }
    }

    @Test(timeout = 4000)
    public void hashCodeTest2()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri("7bgvD4");
        componentFieldUri0.hashCode();
    }

    @Test(timeout = 4000)
    public void isValidFieldNameTest1()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri("po]&Kw14VaG.a#JgS");
        boolean boolean0 = componentFieldUri0.isValid();
        assertEquals("a#JgS", componentFieldUri0.getFieldName());
        assertFalse(boolean0);
    }

    @Test(timeout = 4000)
    public void isValidFieldNameTest2()  throws Throwable {
        SimpleUri simpleUri0 = new SimpleUri("org.terasology.naming.Name", "org.terasology.naming.Name");
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri(simpleUri0, "org.terasology.naming.Name");
        boolean boolean0 = componentFieldUri0.isValid();
        assertEquals("org.terasology.naming.Name", componentFieldUri0.getFieldName());
        assertTrue(boolean0);
    }

    @Test(timeout = 4000)
    public void isValidFieldNameTest3()  throws Throwable {
        SimpleUri simpleUri0 = new SimpleUri("29N.~L;", "29N.~L;");
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri(simpleUri0, (String) null);
        String string0 = componentFieldUri0.getFieldName();
        assertNull(string0);
        assertFalse(componentFieldUri0.isValid());
    }

    @Test(timeout = 4000)
    public void equalsTest1()  throws Throwable  {
        SimpleUri simpleUri0 = new SimpleUri("cX>?els)NUnN", "cX>?els)NUnN");
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri(simpleUri0, "cX>?els)NUnN");
        ComponentFieldUri componentFieldUri1 = new ComponentFieldUri("cX>?els)NUnN");
        boolean boolean0 = componentFieldUri1.equals(componentFieldUri0);
        assertEquals("cX>?els)NUnN", componentFieldUri0.getFieldName());
        assertFalse(boolean0);
    }

    @Test(timeout = 4000)
    public void equalsTest2()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri("(:HV;FAyy>9# F");
        ComponentFieldUri componentFieldUri1 = new ComponentFieldUri(".");
        boolean boolean0 = componentFieldUri0.equals(componentFieldUri1);
        assertTrue(boolean0);
    }

    @Test(timeout = 4000)
    public void equalsTest3()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri("7bgvD4");
        boolean boolean0 = componentFieldUri0.equals("7bgvD4");
        assertFalse(boolean0);
    }

    @Test(timeout = 4000)
    public void equalsTest4()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri((SimpleUri) null, (String) null);
        ComponentFieldUri componentFieldUri1 = new ComponentFieldUri(".");
        // Undeclared exception!
        try {
            componentFieldUri0.equals(componentFieldUri1);
            fail("Expecting exception: NullPointerException");

        } catch(NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("org.terasology.engine.ComponentFieldUri", e);
        }
    }

    @Test(timeout = 4000)
    public void equalsTest5()  throws Throwable  {
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri((SimpleUri) null, (String) null);
        boolean boolean0 = componentFieldUri0.equals(componentFieldUri0);
        assertTrue(boolean0);
    }

    @Test(timeout = 4000)
    public void equalsTest6()  throws Throwable  {
        SimpleUri simpleUri0 = new SimpleUri("29N.~L;", "29N.~L;");
        ComponentFieldUri componentFieldUri0 = new ComponentFieldUri(simpleUri0, (String) null);
        boolean boolean0 = componentFieldUri0.equals((Object) null);
        assertFalse(boolean0);
        assertFalse(componentFieldUri0.isValid());
    }
}
