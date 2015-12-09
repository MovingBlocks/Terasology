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

package org.terasology.entitySystem.metadata;

import org.terasology.context.Context;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;

/**
 * The set of metadata libraries used by the entity system
 *
 */
public class EntitySystemLibrary {

    private final TypeSerializationLibrary typeSerializationLibrary;
    private final ComponentLibrary componentLibrary;
    private final EventLibrary eventLibrary;

    public EntitySystemLibrary(Context context, TypeSerializationLibrary typeSerializationLibrary) {
        this.typeSerializationLibrary = typeSerializationLibrary;
        this.componentLibrary = new ComponentLibrary(context);
        this.eventLibrary = new EventLibrary(context);

    }

    /**
     * @return The library of component metadata
     */
    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    /**
     * @return The library of serializers
     */
    public TypeSerializationLibrary getSerializationLibrary() {
        return typeSerializationLibrary;
    }

    /**
     * @return The library of event metadata
     */
    public EventLibrary getEventLibrary() {
        return eventLibrary;
    }

}
