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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.reflection.copy.CopyStrategy;

/**
 * This copy strategy return {@link DelayedEntityRef}s. See that class for more info.
 *
 * @author Florian <florian@fkoeberle.de>
 */
class DelayedEntityRefCopyStrategy implements CopyStrategy<EntityRef> {

    private DelayedEntityRefFactory delayedEntityRefFactory;

    DelayedEntityRefCopyStrategy(DelayedEntityRefFactory delayedEntityRefFactory) {
        this.delayedEntityRefFactory = delayedEntityRefFactory;
    }

    @Override
    public EntityRef copy(EntityRef value) {
        if (value != null) {
            return delayedEntityRefFactory.createDelayedEntityRef(value.getId());
        } else {
            return null;
        }
    }
}
