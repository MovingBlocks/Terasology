// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence;

import org.terasology.context.annotation.API;

/**
 * Transforms the input text that contains markers (e.g. <code>${text}</code> expressions).
 */
@API
@FunctionalInterface
public interface TemplateEngine {

    /**
     * Transforms the input text and applies text mappings.
     * @param text the input text
     * @return the transformed text
     */
    String transform(String text);
}
