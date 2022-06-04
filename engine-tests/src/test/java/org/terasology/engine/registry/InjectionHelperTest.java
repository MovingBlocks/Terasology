// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.registry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InjectionHelperTest {

    private ServiceA serviceA;
    private ServiceB serviceB;

    @BeforeEach
    public void setUp() {
        serviceA = new ServiceAImpl();
        serviceB = new ServiceBImpl();
        //injection helper uses the core registry, 
        //make sure the shared classes are not used over multiple tests
        CoreRegistry.setContext(new ContextImpl());
    }

    @Test
    public void testSharePopulatesCoreRegistry() {
        assertNull(CoreRegistry.get(ServiceA.class));

        InjectionHelper.share(serviceA);

        assertEquals(CoreRegistry.get(ServiceA.class), serviceA);
    }

    @Test
    public void testShareRequiresShareAnnotation() {
        InjectionHelper.share(new ServiceAImplNoAnnotation());

        assertNull(CoreRegistry.get(ServiceA.class));
    }

    @Test
    @SuppressWarnings("removal")
    public void testDefaultFieldInjection() {
        InjectionHelper.share(serviceA);
        InjectionHelper.share(serviceB);

        //no default constructor required
        FieldInjectionAB fieldInjectionAB = new FieldInjectionAB();
        InjectionHelper.inject(fieldInjectionAB);

        assertEquals(fieldInjectionAB.getServiceA(), serviceA);
        assertEquals(fieldInjectionAB.getServiceB(), serviceB);
    }

    @Test
    @SuppressWarnings("removal")
    public void testInjectUnavailableObject() {
        InjectionHelper.share(serviceA);
        //  InjectionHelper.share(serviceB);

        FieldInjectionAB fieldInjectionAB = new FieldInjectionAB();
        InjectionHelper.inject(fieldInjectionAB);

        assertEquals(fieldInjectionAB.getServiceA(), serviceA);
        assertNull(fieldInjectionAB.getServiceB());
    }

    @Test
    public void testDefaultConstructorInjection() {
        Context context = new ContextImpl();
        context.put(ServiceA.class, serviceA);
        context.put(ServiceB.class, serviceB);

        ConstructorAB constructorAB = InjectionHelper.createWithConstructorInjection(ConstructorAB.class, context);

        //the two-arg constructor should be used as it has the most parameters and all can be populated
        assertEquals(constructorAB.getServiceA(), serviceA);
        assertEquals(constructorAB.getServiceB(), serviceB);
    }

    @Test
    public void testConstructorInjectionNotAllParametersPopulated() {
        Context context = new ContextImpl();
        context.put(ServiceA.class, serviceA);
        //context.put(ServiceB.class, serviceB);

        ConstructorAB constructorAB = InjectionHelper.createWithConstructorInjection(ConstructorAB.class, context);

        //the two-arg constructor can't be populated because serviceB is not available
        //there is no fallback for a constructor with only serviceA, so the default constructor is called
        assertNull(constructorAB.getServiceA());
        assertNull(constructorAB.getServiceB());
    }

    @SuppressWarnings("checkstyle:LocalVariableName")
    @Test
    public void testConstructorInjectionNotAllParametersPopulatedFallback() {
        Context context = new ContextImpl();
        context.put(ServiceA.class, serviceA);
        //context.put(ServiceB.class, serviceB);

        ConstructorA_AB constructorA_AB = InjectionHelper.createWithConstructorInjection(ConstructorA_AB.class, context);

        //the one-arg constructor is used as it can be populated  with serviceA which is available
        assertEquals(constructorA_AB.getServiceA(), serviceA);
        assertNull(constructorA_AB.getServiceB());
    }

    @Test
    public void testConstructorInjectionNoDefaultConstructorForFallback() {
        Context context = new ContextImpl();
        context.put(ServiceA.class, serviceA);
        //context.put(ServiceB.class, serviceB);

        //there is only one constructor for serviceB which is not present on the context.
        //a default constructor is not available, so the injection fails.
        Assertions.assertThrows(NoSuchElementException.class,
                () -> InjectionHelper.createWithConstructorInjection(ConstructorB.class, context));
    }

    //test classes and interfaces for injection

    private interface ServiceA {

    }

    private interface ServiceB {

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

    @SuppressWarnings("checkstyle:TypeName")
    public static class ConstructorA_AB {
        private ServiceA serviceA;

        private ServiceB serviceB;

        public ConstructorA_AB() {

        }

        public ConstructorA_AB(ServiceA serviceA) {
            this.serviceA = serviceA;
        }

        public ConstructorA_AB(ServiceA serviceA, ServiceB serviceB) {
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

    public static class ConstructorB {
        private ServiceB serviceB;

        public ConstructorB(ServiceB serviceB) {
            this.serviceB = serviceB;
        }

        public ServiceB getServiceB() {
            return serviceB;
        }
    }

}
