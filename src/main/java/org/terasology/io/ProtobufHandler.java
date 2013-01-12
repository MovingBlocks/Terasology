package org.terasology.io;

import com.google.protobuf.MessageOrBuilder;

public interface ProtobufHandler<T, M extends MessageOrBuilder> {

    public M encode(T value);
    
    public T decode(M message);
    
}
