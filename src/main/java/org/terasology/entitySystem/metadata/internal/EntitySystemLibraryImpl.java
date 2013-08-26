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

package org.terasology.entitySystem.metadata.internal;

import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.copying.CopyStrategyLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.reflect.ReflectFactory;
import org.terasology.persistence.typeSerialization.TypeSerializationLibrary;

/**
 * @author Immortius
 */
public class EntitySystemLibraryImpl implements EntitySystemLibrary {
    private final TypeSerializationLibrary typeSerializationLibrary;
    private final ComponentLibrary componentLibrary;
    private final EventLibrary eventLibrary;

    public EntitySystemLibraryImpl(ReflectFactory reflectFactory, CopyStrategyLibrary copyStrategies, TypeSerializationLibrary typeSerializationLibrary) {
        this.typeSerializationLibrary = typeSerializationLibrary;
        this.componentLibrary = new ComponentLibraryImpl(reflectFactory, copyStrategies);
        this.eventLibrary = new EventLibraryImpl(reflectFactory, copyStrategies);
    }

    @Override
    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    @Override
    public TypeSerializationLibrary getSerializationLibrary() {
        return typeSerializationLibrary;
    }

    @Override
    public EventLibrary getEventLibrary() {
        return eventLibrary;
    }

}
