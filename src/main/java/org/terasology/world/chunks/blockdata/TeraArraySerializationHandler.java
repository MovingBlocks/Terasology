package org.terasology.world.chunks.blockdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is the interface for serialization handlers for tera arrays. Every tera array is required to implement
 * a serialization handler. It is recommended to subclass {@link org.terasology.world.chunks.blockdata.TeraArray.SerializationHandler TeraArray.SerializationHandler}
 * instead of using this interface directly. It should be implemented as a static subclass of the corresponding tera array class. 
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 * @see org.terasology.world.chunks.blockdata.TeraArray.SerializationHandler
 */
public interface TeraArraySerializationHandler<T extends TeraArray> {

    public Class<T> getArrayClass();

    public void serialize(T array, OutputStream out) throws IOException;
    
    public T deserialize(TeraArrayFactory<T> factory, InputStream in) throws IOException;
    
}
