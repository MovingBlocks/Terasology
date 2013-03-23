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
