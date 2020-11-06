// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.config.flexible.typehandling;

import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

import java.util.Locale;

/**
 * Locale type handler for {@link org.terasology.config.SystemConfig#locale}
 */
public class LocaleTypeHandler extends StringRepresentationTypeHandler<Locale> {
    @Override
    public String getAsString(Locale item) {
        return item.toLanguageTag();
    }

    @Override
    public Locale getFromString(String representation) {
        return Locale.forLanguageTag(representation);
    }
}
