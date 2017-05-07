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
package org.terasology.registry;

import org.junit.Before;
import org.junit.Test;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class InjectionHelperTest {

    private ServiceA serviceA;
    private ServiceB serviceB;

    @Before
    public void setUp() {
        serviceA = new ServiceAImpl();
        serviceB = new ServiceBImpl();
        //injection helper uses the core registry, 
        //make sure the shared classes are not used over multiple tests
        CoreRegistry.setContext(new ContextImpl());
    }

    @Test
    public void test() {
        org.junit.Assert.assertThat(42, org.hamcrest.CoreMatchers.is(43));
    }

    @Test
    public void testSharePopulatesCoreRegistry() {
        assertThat(CoreRegistry.get(ServiceA.class), is(nullValue()));

        InjectionHelper.share(serviceA);

        assertThat(CoreRegistry.get(ServiceA.class), is(serviceA));
    }

    @Test
    public void testShareRequiresShareAnnotation() {
        InjectionHelper.share(new ServiceAImplNoAnnotation());

        assertThat(CoreRegistry.get(ServiceA.class), is(nullValue()));
    }

    @Test
    public void testDefaultFieldInjection() {
        InjectionHelper.share(serviceA);
        InjectionHelper.share(serviceB);

        FieldInjectionAB fieldInjectionAB = new FieldInjectionAB();
        InjectionHelper.inject(fieldInjectionAB);

        assertThat(fieldInjectionAB.getServiceA(), is(serviceA));
        assertThat(fieldInjectionAB.getServiceB(), is(serviceB));
    }

    @Test
    public void testInjectUnavailableObject() {
        InjectionHelper.share(serviceA);
        //  InjectionHelper.share(serviceB);

        FieldInjectionAB fieldInjectionAB = new FieldInjectionAB();
        InjectionHelper.inject(fieldInjectionAB);

        assertThat(fieldInjectionAB.getServiceA(), is(serviceA));
        assertThat(fieldInjectionAB.getServiceB(), is(nullValue()));
    }

    @Test
    public void testDefaultConstructorInjection() {
        Context context = new ContextImpl();
        context.put(ServiceA.class, serviceA);
        context.put(ServiceB.class, serviceB);

        ConstructorAB constructorAB = InjectionHelper.createWithConstructorInjection(ConstructorAB.class, context);

        //the two-arg constructor should be used as it has the most parameters and all can be populated
        assertThat(constructorAB.getServiceA(), is(serviceA));
        assertThat(constructorAB.getServiceB(), is(serviceB));
    }

    @Test
    public void testConstructorInjectionNotAllParametersPopulated() {
        Context context = new ContextImpl();
        context.put(ServiceA.class, serviceA);
        //context.put(ServiceB.class, serviceB);

        ConstructorAB constructorAB = InjectionHelper.createWithConstructorInjection(ConstructorAB.class, context);

        //the one-arg constructor should be used as it has the second most parameters and can be populated
        assertThat(constructorAB.getServiceA(), is(serviceA));
        assertThat(constructorAB.getServiceB(), is(serviceB));
    }

    //helper classes and interfaces

    private static interface ServiceA {

    }

    private static interface ServiceB {

    }

    @Share(ServiceA.class)
    private static class ServiceAImpl implements ServiceA {

    }

    private static class ServiceAImplNoAnnotation implements ServiceA {

    }

    @Share(ServiceB.class)
    private static class ServiceBImpl implements ServiceB {

    }

    private static class FieldInjectionAB {
        @In
        private ServiceA serviceA;

        @In
        private ServiceB serviceB;

        public ServiceA getServiceA() {
            return serviceA;
        }

        public ServiceB getServiceB() {
            return serviceB;
        }

    }

    public static class ConstructorAB {
        private ServiceA serviceA;

        private ServiceB serviceB;

        public ConstructorAB() {

        }

        public ConstructorAB(ServiceA serviceA) {
            this.serviceA = serviceA;
        }

        public ConstructorAB(ServiceA serviceA, ServiceB serviceB) {
            this.serviceA = serviceA;
            this.serviceB = serviceB;
        }

        public ServiceA getServiceA() {
            return serviceA;
        }

        public ServiceB getServiceB() {
            return serviceB;
        }
    }

}
