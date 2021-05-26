// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.module;

import java.util.Locale;

public final class UriUtil {

    private UriUtil() {
    }

    /**
     * Normalises a uri or uri part. The normal form is used for comparison/string matching.
     * This process includes lower-casing the uri.
     *
     * @param value A uri or uri part
     * @return The normal form of the given value.
     */
    public static String normalise(String value) {
        return value.toLowerCase(Locale.ENGLISH);
    }
}
