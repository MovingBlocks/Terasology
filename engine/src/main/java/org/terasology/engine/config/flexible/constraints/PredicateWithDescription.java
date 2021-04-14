// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config.flexible.constraints;

import java.util.function.Predicate;

public class PredicateWithDescription<T> implements Predicate<T> {
    private final Predicate<T> predicate;
    private final String description;

    public PredicateWithDescription(String description, Predicate<T> predicate) {
        this.predicate = predicate;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean test(T s) {
        return predicate.test(s);
    }
}
