/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.entitySystem;

import gnu.trove.set.TIntSet;
import org.terasology.entitySystem.event.EventSystem;

import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface EngineEntityManager extends EntityManager {

    // Persistence enabling methods

    EntityRef createEntityWithId(int id, Iterable<Component> components);

    EntityRef createEntityRefWithId(int id);

    int getNextId();

    void setNextId(int id);

    TIntSet getFreedIds();

    void clear();

    void removedForStoring(EntityRef entity);

    /**
     * Subscribes to all changes related to entities (for internal use)
     * @param subscriber
     */
    void subscribe(EntityChangeSubscriber subscriber);

    void unsubscribe(EntityChangeSubscriber subscriber);

    void setEventSystem(EventSystem system);


}
