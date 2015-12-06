/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.module;

import java.util.Locale;

/**
 */
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
