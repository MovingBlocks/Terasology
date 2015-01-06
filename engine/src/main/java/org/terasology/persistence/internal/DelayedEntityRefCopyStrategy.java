/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.persistence.internal;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.reflection.copy.CopyStrategy;

/**
 * This copy strategy return {@link EntityRef}s that will use another {@link EntityManager} once they get accessed
 * for the first time. This makes it possible to create copies of components ont he main thread which contain
 * entity refs which will use a entity manager that is private to the saving thread.
 *
 * @author Florian <florian@fkoeberle.de>
 */
class DelayedEntityRefCopyStrategy implements CopyStrategy<EntityRef> {

    private EntityManager entityManager;

    /**
     *
     * @param entityManager the entity manager that the copies will use.
     */
    DelayedEntityRefCopyStrategy(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public EntityRef copy(EntityRef value) {
        return new DelayedEntityRef(value.getId(), entityManager);
    }
}
