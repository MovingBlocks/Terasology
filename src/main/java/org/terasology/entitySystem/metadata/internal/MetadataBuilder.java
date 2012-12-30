package org.terasology.entitySystem.metadata.internal;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.entitySystem.metadata.core.BooleanTypeHandler;
import org.terasology.entitySystem.metadata.core.ByteTypeHandler;
import org.terasology.entitySystem.metadata.core.DoubleTypeHandler;
import org.terasology.entitySystem.metadata.core.EnumTypeHandler;
import org.terasology.entitySystem.metadata.core.FloatTypeHandler;
import org.terasology.entitySystem.metadata.core.IntTypeHandler;
import org.terasology.entitySystem.metadata.core.ListTypeHandler;
import org.terasology.entitySystem.metadata.core.LongTypeHandler;
import org.terasology.entitySystem.metadata.core.MappedContainerTypeHandler;
import org.terasology.entitySystem.metadata.core.NumberTypeHandler;
import org.terasology.entitySystem.metadata.core.SetTypeHandler;
import org.terasology.entitySystem.metadata.core.StringMapTypeHandler;
import org.terasology.entitySystem.metadata.core.StringTypeHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class MetadataBuilder {
    private static final int MAX_SERIALIZATION_DEPTH = 1;
    private static final Logger logger = LoggerFactory.getLogger(MetadataBuilder.class);

    private Map<Class<?>, TypeHandler<?>> typeHandlers = Maps.newHashMap();

    public MetadataBuilder() {
        registerTypeHandler(Boolean.class, new BooleanTypeHandler());
        registerTypeHandler(Boolean.TYPE, new BooleanTypeHandler());
        registerTypeHandler(Byte.class, new ByteTypeHandler());
        registerTypeHandler(Byte.TYPE, new ByteTypeHandler());
        registerTypeHandler(Double.class, new DoubleTypeHandler());
        registerTypeHandler(Double.TYPE, new DoubleTypeHandler());
        registerTypeHandler(Float.class, new FloatTypeHandler());
        registerTypeHandler(Float.TYPE, new FloatTypeHandler());
        registerTypeHandler(Integer.class, new IntTypeHandler());
        registerTypeHandler(Integer.TYPE, new IntTypeHandler());
        registerTypeHandler(Long.class, new LongTypeHandler());
        registerTypeHandler(Long.TYPE, new LongTypeHandler());
        registerTypeHandler(String.class, new StringTypeHandler());
        registerTypeHandler(Number.class, new NumberTypeHandler());
    }

    public <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler) {
        typeHandlers.put(forClass, handler);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeHandler<? super T> getTypeHandler(Class<T> forClass) {
        return (TypeHandler<? super T>) typeHandlers.get(forClass);
    }

    public <T> ClassMetadata<T> build(Class<T> forClass) {
        try {
            // Check if constructor exists
            forClass.getConstructor();
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", forClass.getSimpleName());
            return null;
        }

        ClassMetadata<T> info = new ClassMetadata<T>(forClass);
        for (Field field : forClass.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
                continue;
            field.setAccessible(true);
            TypeHandler typeHandler = getHandlerFor(field.getGenericType(), 0);
            if (typeHandler == null) {
                logger.error("Unsupported field type in component type {}, {} : {}", forClass.getSimpleName(), field.getName(), field.getGenericType());
            } else {
                info.addField(new FieldMetadata(field, forClass, typeHandler));
            }
        }
        return info;
    }

    // TODO: Refactor
    private TypeHandler getHandlerFor(Type type, int depth) {
        Class typeClass;
        if (type instanceof Class) {
            typeClass = (Class) type;
        } else if (type instanceof ParameterizedType) {
            typeClass = (Class) ((ParameterizedType) type).getRawType();
        } else {
            logger.error("Cannot obtain class for type {}", type);
            return null;
        }

        if (Enum.class.isAssignableFrom(typeClass)) {
            return new EnumTypeHandler(typeClass);
        }
        // For lists, createEntityRef the handler for the contained type and wrap in a list type handler
        else if (List.class.isAssignableFrom(typeClass)) {
            Type parameter = getTypeParameter(type, 0);
            if (parameter != null) {
                TypeHandler innerHandler = getHandlerFor(parameter, depth);
                if (innerHandler != null) {
                    return new ListTypeHandler(innerHandler);
                }
            }
            logger.error("List field is not parameterized, or holds unsupported type");
            return null;
        }
        // For sets:
        else if (Set.class.isAssignableFrom(typeClass)) {
            Type parameter = getTypeParameter(type, 0);
            if (parameter != null) {
                TypeHandler innerHandler = getHandlerFor(parameter, depth);
                if (innerHandler != null) {
                    return new SetTypeHandler(innerHandler);
                }
            }
            logger.error("Set field is not parameterized, or holds unsupported type");
            return null;
        }
        // For Maps, createEntityRef the handler for the value type (and maybe key too?)
        else if (Map.class.isAssignableFrom(typeClass)) {
            Type keyParameter = getTypeParameter(type, 0);
            Type contentsParameter = getTypeParameter(type, 1);
            if (keyParameter != null && contentsParameter != null && String.class == keyParameter) {
                TypeHandler valueHandler = getHandlerFor(contentsParameter, depth);
                if (valueHandler != null) {
                    return new StringMapTypeHandler(valueHandler);
                }
            }
            logger.error("Map field is not parameterized, does not have a String key, or holds unsupported values");
        }
        // For know types, just use the handler
        else if (typeHandlers.containsKey(typeClass)) {
            return typeHandlers.get(typeClass);
        }
        // For unknown types of a limited depth, assume they are data holders and use them
        else if (depth <= MAX_SERIALIZATION_DEPTH && !Modifier.isAbstract(typeClass.getModifiers()) && !typeClass.isLocalClass() && !(typeClass.isMemberClass() && !Modifier.isStatic(typeClass.getModifiers()))) {
            try {
                // Check if constructor exists
                typeClass.getConstructor();
            } catch (NoSuchMethodException e) {
                logger.error("Unable to register field of type {}: no publicly accessible default constructor", typeClass.getSimpleName());
                return null;
            }

            logger.warn("Handling serialization of type {} via MappedContainer", typeClass);
            MappedContainerTypeHandler mappedHandler = new MappedContainerTypeHandler(typeClass);
            for (Field field : typeClass.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
                    continue;

                field.setAccessible(true);
                TypeHandler handler = getHandlerFor(field.getGenericType(), depth + 1);
                if (handler == null) {
                    logger.error("Unsupported field type in component type {}, {} : {}", typeClass.getSimpleName(), field.getName(), field.getGenericType());
                } else {
                    mappedHandler.addField(new FieldMetadata(field, typeClass, handler));
                }
            }
            return mappedHandler;
        }

        return null;
    }

    // TODO - Improve parameter lookup to go up the inheritance tree more
    private Type getTypeParameter(Type type, int parameter) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (parameterizedType.getActualTypeArguments().length < parameter + 1) {
            return null;
        }
        return parameterizedType.getActualTypeArguments()[parameter];
    }
}
