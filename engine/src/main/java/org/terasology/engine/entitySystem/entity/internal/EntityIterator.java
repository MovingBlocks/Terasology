// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
