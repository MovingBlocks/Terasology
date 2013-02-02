package org.terasology.entitySystem.metadata;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.metadata.core.EnumTypeHandler;
import org.terasology.entitySystem.metadata.core.ListTypeHandler;
import org.terasology.entitySystem.metadata.core.MappedContainerTypeHandler;
import org.terasology.entitySystem.metadata.core.SetTypeHandler;
import org.terasology.entitySystem.metadata.core.StringMapTypeHandler;
import org.terasology.network.NoReplicate;
import org.terasology.network.Replicate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class TypeHandlerLibrary implements Iterable<Map.Entry<Class<?>, TypeHandler<?>>> {
    private static final Logger logger = LoggerFactory.getLogger(TypeHandlerLibrary.class);
    private static final int MAX_SERIALIZATION_DEPTH = 1;

    private Map<Class<?>, TypeHandler<?>> typeHandlers;

    public TypeHandlerLibrary(Map<Class<?>, TypeHandler<?>> typeHandlers) {
        this.typeHandlers = ImmutableMap.copyOf(typeHandlers);
    }

    @SuppressWarnings("unchecked")
    public <T> TypeHandler<? super T> getTypeHandler(Class<T> forClass) {
        return (TypeHandler<? super T>) typeHandlers.get(forClass);
    }

    public <T> ClassMetadata<T> build(Class<T> forClass, boolean replicateFieldsByDefault, String... names) {
        ClassMetadata<T> info;
        try {
            info = new ClassMetadata<T>(forClass, names);
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register class {}: Default Constructor Required", forClass.getSimpleName(), e);
            return null;
        }

        populateFields(forClass, info, replicateFieldsByDefault);
        return info;
    }


    public <T> void populateFields(Class<T> forClass, ClassMetadata<T> info, boolean replicateFieldsByDefault) {
        for (Field field : Reflections.getAllFields(forClass, Predicates.alwaysTrue())) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
                continue;
            field.setAccessible(true);
            TypeHandler typeHandler = getHandlerFor(field.getGenericType(), 0);
            if (typeHandler == null) {
                logger.error("Unsupported field type in component type {}, {} : {}", forClass.getSimpleName(), field.getName(), field.getGenericType());
            } else {
                boolean replicate = replicateFieldsByDefault;
                if (field.getAnnotation(NoReplicate.class) != null) {
                    replicate = false;
                }
                if (field.getAnnotation(Replicate.class) != null) {
                    replicate = true;
                }
                info.addField(new FieldMetadata(field, forClass, typeHandler, replicate));
            }
        }
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
                    mappedHandler.addField(new FieldMetadata(field, typeClass, handler, true));
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

    @Override
    public Iterator<Map.Entry<Class<?>, TypeHandler<?>>> iterator() {
        return typeHandlers.entrySet().iterator();
    }
}
