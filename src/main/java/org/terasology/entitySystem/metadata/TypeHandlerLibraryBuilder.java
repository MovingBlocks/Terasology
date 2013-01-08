package org.terasology.entitySystem.metadata;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.metadata.core.BooleanTypeHandler;
import org.terasology.entitySystem.metadata.core.ByteTypeHandler;
import org.terasology.entitySystem.metadata.core.DoubleTypeHandler;
import org.terasology.entitySystem.metadata.core.FloatTypeHandler;
import org.terasology.entitySystem.metadata.core.IntTypeHandler;
import org.terasology.entitySystem.metadata.core.LongTypeHandler;
import org.terasology.entitySystem.metadata.core.NumberTypeHandler;
import org.terasology.entitySystem.metadata.core.StringTypeHandler;

import java.util.Map;

/**
 * @author Immortius
 */
public class TypeHandlerLibraryBuilder {

    private Map<Class<?>, TypeHandler<?>> typeHandlers = Maps.newHashMap();

    public TypeHandlerLibraryBuilder() {
        add(Boolean.class, new BooleanTypeHandler());
        add(Boolean.TYPE, new BooleanTypeHandler());
        add(Byte.class, new ByteTypeHandler());
        add(Byte.TYPE, new ByteTypeHandler());
        add(Double.class, new DoubleTypeHandler());
        add(Double.TYPE, new DoubleTypeHandler());
        add(Float.class, new FloatTypeHandler());
        add(Float.TYPE, new FloatTypeHandler());
        add(Integer.class, new IntTypeHandler());
        add(Integer.TYPE, new IntTypeHandler());
        add(Long.class, new LongTypeHandler());
        add(Long.TYPE, new LongTypeHandler());
        add(String.class, new StringTypeHandler());
        add(Number.class, new NumberTypeHandler());
    }

    public <T> TypeHandlerLibraryBuilder add(Class<? extends T> forClass, TypeHandler<T> handler) {
        typeHandlers.put(forClass, handler);
        return this;
    }

    public TypeHandlerLibraryBuilder addRaw(Class forClass, TypeHandler handler) {
        typeHandlers.put(forClass, handler);
        return this;
    }

    public TypeHandlerLibrary build() {
        return new TypeHandlerLibrary(typeHandlers);
    }
}
