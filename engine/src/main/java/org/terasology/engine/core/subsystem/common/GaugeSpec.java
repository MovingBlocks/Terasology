// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.subsystem.common;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.function.ToDoubleFunction;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The information that defines a Gauge.
 * <p>
 * The Micrometer API doesn't let you define a Gauge without connecting it to
 * some MeterRegistry. This class provides an immutable record of all<sup>*</sup>
 * the properties of a Gauge, facilitating a more data-driven approach.
 * <p>
 * * <i>All the ones we use so far, anyway.</i>
 *
 * @param <T> the type this gauge reads from
 */
public class GaugeSpec<T> {
    public final String name;
    public final String description;
    public final ToDoubleFunction<T> valueFunction;
    public final String baseUnit;

    protected final Class<T> subjectType;

    public GaugeSpec(String name, String description, ToDoubleFunction<T> valueFunction) {
        this(name, description, valueFunction, null);
    }

    /** @see Gauge.Builder */
    public GaugeSpec(String name, String description, ToDoubleFunction<T> valueFunction, String baseUnit) {
        this.name = name;
        this.description = description;
        this.valueFunction = valueFunction;
        this.baseUnit = baseUnit;
        this.subjectType = getSubjectClass();
    }

    public Gauge register(MeterRegistry registry, T subject) {
        return Gauge.builder(name, subject, valueFunction)
                .description(description)
                .baseUnit(baseUnit)
                .register(registry);
    }

    /**
     * Creates a MeterBinder for this gauge.
     * <p>
     * This allows us to make things with the same interface as the meters
     * provided by {@link io.micrometer.core.instrument.binder}.
     *
     * @param subject passed to this gauge's {@link #valueFunction}
     * @return call to bind this gauge to a MeterRegistry
     */
    public MeterBinder binder(T subject) {
        return registry -> register(registry, subject);
    }

    public <U> MeterBinder binderAfterCasting(U subject) {
        checkArgument(isInstanceOfType(subject));
        T safeSubject = subjectType.cast(subject);
        return binder(safeSubject);
    }

    public boolean isInstanceOfType(Object object) {
        return subjectType.isInstance(object);
    }

    @SafeVarargs
    private Class<T> getSubjectClass(T...t) {
        // Thank you https://stackoverflow.com/a/40917725 for this amazing kludge
        //noinspection unchecked
        return (Class<T>) t.getClass().getComponentType();
    }
}
