// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import org.terasology.context.annotation.API;
import org.terasology.gestalt.naming.Name;

/**
 * Uris are used to identify resources, like assets and systems introduced by mods. Uris can then be serialized/deserialized to and from Strings.
 * Uris are case-insensitive. They have a normalised form which is lower-case (using English casing).
 * Uris are immutable.
 * <br><br>
 * All uris include a module name as part of their structure.
 *
 */
@API
public interface Uri {
    /**
     * The character(s) use to separate the module name from other parts of the Uri
     */
    String MODULE_SEPARATOR = ":";

    /**
     * @return The name of the module the resource in question resides in.
     */
    Name getModuleName();

    /**
     * @return Whether this uri represents a valid, well formed uri.
     */
    boolean isValid();
}
