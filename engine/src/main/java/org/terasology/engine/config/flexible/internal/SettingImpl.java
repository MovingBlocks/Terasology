// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.SettingChangeListener;
import org.terasology.engine.config.flexible.constraints.SettingConstraint;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * {@inheritDoc}
 *
 * @param <T> The type of the value this {@link SettingImpl} contains.
 */
class SettingImpl<T> implements Setting<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingImpl.class);

    protected T value;

    private final T defaultValue;
    private final TypeInfo<T> valueType;

    private final String humanReadableName;
    private final String description;

    private final Supplier<Optional<T>> override;

    private final SettingConstraint<T> constraint;
    private final Set<SettingChangeListener<T>> subscribers = Sets.newHashSet();

    /**
     * Creates a new {@link SettingImpl} with the given id, default value and constraint.
     *
     * @param valueType The {@link TypeInfo} describing the type of values t
     * @param defaultValue The default value of the setting.
     * @param constraint The constraint that the setting values must satisfy.
     * @param humanReadableName The human readable name of the setting.
     * @param description A description of the setting.
     * @param override A override provider of the setting.
     */
    SettingImpl(TypeInfo<T> valueType, T defaultValue, SettingConstraint<T> constraint,
                String humanReadableName, String description, Supplier<Optional<T>> override) {
        this.valueType = valueType;
        this.humanReadableName = humanReadableName;
        this.description = description;

        this.constraint = constraint;
        this.override = override;

        Preconditions.checkNotNull(defaultValue, "The default value for a Setting cannot be null.");

        if (isConstraintUnsatisfiedBy(defaultValue)) {
            throw new IllegalArgumentException("The default value must be a valid value. " +
                    "Check the logs for more information.");
        }

        this.defaultValue = defaultValue;
        this.value = this.defaultValue;
    }

    private void dispatchChangedEvent(T oldValue) {
        for (SettingChangeListener<T> subscriber : subscribers) {
            subscriber.onValueChanged(this, oldValue);
        }
    }

    private boolean isConstraintUnsatisfiedBy(T theValue) {
        if (constraint == null || constraint.isSatisfiedBy(theValue)) {
            return false;
        } else {
            constraint.warnUnsatisfiedBy(theValue);
            return true;
        }
    }

    @Override
    public boolean subscribe(SettingChangeListener<T> listener) {
        if (listener == null) {
            LOGGER.warn("Cannot add a null subscriber to a Setting.");
            return false;
        }

        if (subscribers.contains(listener)) {
            return false;
        }

        subscribers.add(listener);

        return true;
    }

    @Override
    public boolean unsubscribe(SettingChangeListener<T> listener) {
        if (!subscribers.contains(listener)) {
            return false;
        }

        subscribers.remove(listener);

        return true;
    }

    @Override
    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

    @Override
    public TypeInfo<T> getValueType() {
        return valueType;
    }

    @Override
    public SettingConstraint<T> getConstraint() {
        return constraint;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public T get() {
        return override.get().orElse(value);
    }

    @Override
    public boolean set(T newValue) {
        Preconditions.checkNotNull(newValue, "The value of a setting cannot be null.");

        if (override.get().isPresent()) {
            LOGGER.warn("An attempt was made to overwrite the value specified in the System property."
                    + " This will give nothing while the System Property value is supplied");
            return false;
        }

        if (isConstraintUnsatisfiedBy(newValue)) {
            return false;
        }

        if (newValue.equals(this.value)) {
            return false;
        }

        T oldValue = this.value;

        this.value = newValue;

        dispatchChangedEvent(oldValue);

        return true;
    }

    @Override
    public String getHumanReadableName() {
        return humanReadableName;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
