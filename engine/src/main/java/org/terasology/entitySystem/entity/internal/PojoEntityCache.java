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

import com.google.common.collect.MapMaker;
import org.terasology.entitySystem.entity.EntityCache;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.Map;

/**
 */
public class PojoEntityCache implements EntityCache {
    private Map<Long, BaseEntityRef> entityCache = new MapMaker().weakValues().concurrencyLevel(4).initialCapacity(1000).makeMap();
    private ComponentTable store = new ComponentTable();

    public EntityRef create() {
        //Todo: implement
        return EntityRef.NULL;
    }

    public Map<Long, BaseEntityRef> getEntityCache() {
        return entityCache;
    }

    public ComponentTable getStore() {
        return store;
    }
}
