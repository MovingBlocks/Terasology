// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.benchmark.reflectFactory;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.reflection.reflect.ByteCodeReflectFactory;
import org.terasology.reflection.reflect.FieldAccessor;
import org.terasology.reflection.reflect.ObjectConstructor;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class ReflectionFactoryBenchmark {

    @Benchmark
    public Object byteCodeConstructor(ByteCodeState state) {
        return state.constructor.construct();
    }

    @Benchmark
    public Object reflectionConstructor(ReflectionState state) {
        return state.constructor.construct();
    }

    @Benchmark
    public Object byteCodeFieldAccessGet(ByteCodeState state, FieldComponentState fieldComponentState) {
        return state.fieldAccessor.getValue(fieldComponentState.component);
    }

    @Benchmark
    public Object reflectionFieldAccessGet(ReflectionState state, FieldComponentState fieldComponentState) {
        return state.fieldAccessor.getValue(fieldComponentState.component);
    }

    @Benchmark
    public void byteCodeFieldAccessSet(ByteCodeState state, FieldComponentState fieldComponentState) {
        state.fieldAccessor.setValue(fieldComponentState.component, fieldComponentState.value);
    }

    @Benchmark
    public void reflectionFieldAccessSet(ReflectionState state, FieldComponentState fieldComponentState) {
        state.fieldAccessor.setValue(fieldComponentState.component, fieldComponentState.value);
    }

    @Benchmark
    public Object byteCodeGetterSetterAccessGet(ByteCodeState state,
                                                GetterSetterComponentState getterSetterComponentState) {
        return state.getterSetterAccessor.getValue(getterSetterComponentState.component);
    }

    @Benchmark
    public Object reflectionGetterSetterAccessGet(ReflectionState state,
                                                  GetterSetterComponentState getterSetterComponentState) {
        return state.getterSetterAccessor.getValue(getterSetterComponentState.component);
    }


    @Benchmark
    public void byteCodeGetterSetterAccessSet(ByteCodeState state,
                                              GetterSetterComponentState getterSetterComponentState) {
        state.getterSetterAccessor.setValue(getterSetterComponentState.component, getterSetterComponentState.value);
    }

    @Benchmark
    public void reflectionGetterSetterAccessSet(ReflectionState state,
                                                GetterSetterComponentState getterSetterComponentState) {
        state.getterSetterAccessor.setValue(getterSetterComponentState.component, getterSetterComponentState.value);
    }

    @State(Scope.Thread)
    public static class GetterSetterComponentState {
        private GetterSetterComponent component;
        private int value;

        @Setup
        public void setup() {
            component = new GetterSetterComponent();
            component.setValue(1);
            value = 90;
        }
    }

    @State(Scope.Thread)
    public static class FieldComponentState {
        private DisplayNameComponent component;
        private String value;

        @Setup
        public void setup() {
            component = new DisplayNameComponent();
            component.name = "dummy";
            value = "dummy1";
        }
    }


    @State(Scope.Thread)
    public static class ByteCodeState extends StateObject {

        @Override
        ReflectFactory getReflectFactory() {
            return new ByteCodeReflectFactory();
        }
    }

    @State(Scope.Thread)
    public static class ReflectionState extends StateObject {

        @Override
        ReflectFactory getReflectFactory() {
            return new ReflectionReflectFactory();
        }
    }

    public abstract static class StateObject {
        ObjectConstructor<LocationComponent> constructor;
        FieldAccessor fieldAccessor;
        FieldAccessor getterSetterAccessor;

        @Setup
        public void setup() throws Exception {
            ReflectFactory reflectFactory = getReflectFactory();
            constructor = reflectFactory.createConstructor(LocationComponent.class);
            fieldAccessor = reflectFactory.createFieldAccessor(DisplayNameComponent.class,
                    DisplayNameComponent.class.getField("description"));
            getterSetterAccessor = reflectFactory.createFieldAccessor(GetterSetterComponent.class,
                    GetterSetterComponent.class.getDeclaredField("value"));
        }

        abstract ReflectFactory getReflectFactory();
    }
}
