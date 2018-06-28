/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.persistence.typeHandling.coreTypes;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.SimpleTypeHandler;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class QueueTypeHandler<E> extends SimpleTypeHandler<Queue<E>> {
    private TypeHandler<E> contentsType;

    public QueueTypeHandler(TypeHandler<E> type) {
        this.contentsType = type;
    }

    @Override
    public PersistedData serialize(Queue<E> value, SerializationContext context) {
        if (value.size() > 0) {
            return contentsType.serializeCollection(value, context);
        }
        return context.createNull();
    }

    @Override
    public Queue<E> deserialize(PersistedData data, DeserializationContext context) {
        List<E> list = contentsType.deserializeCollection(data, context);
        return new LinkedList<>(list);
    }

}
