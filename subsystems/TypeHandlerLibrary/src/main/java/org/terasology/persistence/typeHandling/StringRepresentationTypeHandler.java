// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class StringRepresentationTypeHandler<T> extends TypeHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(StringRepresentationTypeHandler.class);

    /**
     * Per-instance rather than static: a single damaged reference can appear on thousands of
     * entities, so this reports once and then goes quiet - but MTE builds a fresh handler library
     * per environment, and a static flag would silence every test after the first.
     */
    private final AtomicBoolean warnedNullContent = new AtomicBoolean();

    public abstract String getAsString(T item);

    public abstract T getFromString(String representation);

    @Override
    public PersistedData serializeNonNull(T value, PersistedDataSerializer serializer) {
        String stringValue = getAsString(value);
        return serializer.serialize(stringValue);
    }

    @Override
    public Optional<T> deserialize(PersistedData data) {
        if (data.isString()) {
            String value = data.getAsString();
            if (value == null) {
                // PersistedString reports isString() unconditionally, so stale save data can
                // arrive with null content - treat as absent rather than call getFromString(null).
                if (warnedNullContent.compareAndSet(false, true)) {
                    logger.warn("{}: persisted data reported isString() but held null content -"
                                    + " treating it as absent. Further occurrences from this handler"
                                    + " are silent. Usually a stale or renamed reference in older"
                                    + " save data.",
                            getClass().getSimpleName());
                }
                return Optional.empty();
            }
            return Optional.ofNullable(getFromString(value));
        }
        return Optional.empty();
    }

}
