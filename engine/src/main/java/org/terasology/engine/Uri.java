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
package org.terasology.engine;

import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;

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
