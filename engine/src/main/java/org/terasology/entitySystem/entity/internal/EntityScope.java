/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.entitySystem.entity.internal;

import org.terasology.gestalt.module.sandbox.API;

@API
public enum EntityScope {
    GLOBAL(true),
    SECTOR(true),
    CHUNK(false);

    private boolean alwaysRelevant;

    EntityScope(boolean alwaysRelevant) {
        this.alwaysRelevant = alwaysRelevant;
    }

    public boolean getAlwaysRelevant() {
        return alwaysRelevant;
    }

    public static EntityScope getDefaultScope() {
        return CHUNK;
    }
}
