// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.coreTypes;

import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

import java.util.Locale;

/**
 * Serializes objects of type {@link java.util.Locale}
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
