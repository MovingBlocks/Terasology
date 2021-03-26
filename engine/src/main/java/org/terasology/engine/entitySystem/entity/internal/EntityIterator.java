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
package org.terasology.engine.entitySystem.entity.internal;

import gnu.trove.iterator.TLongIterator;
import org.terasology.engine.entitySystem.entity.EntityPool;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.Iterator;

/**
 * Provides an iterator over EntityRefs, after being given an iterator over entity IDs.
 */
public class EntityIterator implements Iterator<EntityRef> {
    private TLongIterator idIterator;
    private EntityPool pool;

    EntityIterator(TLongIterator idIterator, EntityPool pool) {
        this.idIterator = idIterator;
        this.pool = pool;
    }

    @Override
    public boolean hasNext() {
        return idIterator.hasNext();
    }

    @Override
    public EntityRef next() {
        return pool.getEntity(idIterator.next());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
