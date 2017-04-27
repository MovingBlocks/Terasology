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
package org.terasology.testUtil;

import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Some parameters for API test.
 * It includes the method signature for common classes.
 */
public class ApiTestsParams {

    /**
     * All public methods in Object class.
     */
    public static final Set<MethodSignature> ObjectClassMethods = new ImmutableSet.Builder<MethodSignature>()
            .add(new MethodSignature("equals", new Class[] {Object.class},Modifier.PUBLIC,boolean.class,null))
            .add(new MethodSignature("toString",null,Modifier.PUBLIC,String.class,null))
            .add(new MethodSignature("hashCode",null,Modifier.PUBLIC,int.class,null))
            .add(new MethodSignature("wait",new Class[]{long.class,int.class},Modifier.PUBLIC|Modifier.FINAL,void.class,new Class[]{InterruptedException.class}))
            .add(new MethodSignature("wait", new Class[]{long.class},Modifier.PUBLIC|Modifier.FINAL|Modifier.NATIVE,void.class,new Class[]{InterruptedException.class}))
            .add(new MethodSignature("wait", null,Modifier.PUBLIC|Modifier.FINAL, void.class, new Class[]{InterruptedException.class}))
            .add(new MethodSignature("getClass",null,Modifier.PUBLIC|Modifier.FINAL|Modifier.NATIVE, Class.class,null))
            .add(new MethodSignature("notify",null,Modifier.PUBLIC|Modifier.FINAL|Modifier.NATIVE,void.class,null))
            .add(new MethodSignature("notifyAll",null,Modifier.PUBLIC|Modifier.FINAL|Modifier.NATIVE,void.class,null))
            .build();
}
