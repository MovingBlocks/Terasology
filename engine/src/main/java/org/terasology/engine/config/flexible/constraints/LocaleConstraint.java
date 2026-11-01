// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config.flexible.constraints;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class LocaleConstraint implements SettingConstraint<Locale> {

    private static final Logger logger = LoggerFactory.getLogger(LocaleConstraint.class);

    private final Set<Locale> locales;

    public LocaleConstraint(Set<Locale> locales) {
        this.locales = locales;
    }

    public LocaleConstraint(Locale... locales) {
        this.locales = Sets.newHashSet(locales);
    }

    @Override
    public boolean isSatisfiedBy(Locale value) {
        return locales.contains(value);
    }

    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public void warnUnsatisfiedBy(Locale value) {
        logger.warn("Locale {} should be one of {}",
                value,
                locales.stream()
                        .map(Locale::getLanguage)
                        .collect(Collectors.joining(",", "[", "]"))
        );
    }
}
