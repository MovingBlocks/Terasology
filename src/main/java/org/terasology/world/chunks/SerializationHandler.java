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

package org.terasology.world.chunks;

import java.nio.ByteBuffer;

/**
 * The basic interface for serialization handlers.
 *
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 */
public interface SerializationHandler<T> {

    public boolean canHandle(Class<?> clazz);

    public int computeMinimumBufferSize(T object);

    public ByteBuffer serialize(T object, ByteBuffer buffer);

    public T deserialize(ByteBuffer buffer);
}
