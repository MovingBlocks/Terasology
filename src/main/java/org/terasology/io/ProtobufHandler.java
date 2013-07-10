package org.terasology.io;

import com.google.protobuf.MessageOrBuilder;


/**
 * This is the basic interface for protobuf handlers.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public interface ProtobufHandler<T, M extends MessageOrBuilder> {

    public M encode(T value);
    
    public T decode(M message);
    
    public void decode(M message, T value);
    
}
