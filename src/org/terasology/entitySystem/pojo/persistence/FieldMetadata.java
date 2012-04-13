package org.terasology.entitySystem.pojo.persistence;

import org.terasology.protobuf.EntityData;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class FieldMetadata {
    private Field field;
    private Method getter;
    private Method setter;
    private TypeHandler serializationHandler;

    public FieldMetadata(Field field, Class type, TypeHandler handler) {
        this.field = field;
        this.serializationHandler = handler;
        getter = findGetter(type, field);
        setter = findSetter(type, field);
    }

    public EntityData.Value serialize(Object field) {
        return serializationHandler.serialize(field);
    }

    public Object deserialize(EntityData.Value value) {
        return serializationHandler.deserialize(value);
    }

    public Object copy(Object field) {
        return serializationHandler.copy(field);
    }

    public String getName() {
        return field.getName();
    }

    public Object getValue(Object obj) throws IllegalAccessException, InvocationTargetException {
        if (getter != null) {
            return getter.invoke(obj);
        }
        return field.get(obj);
    }

    public void setValue(Object target, Object value) throws IllegalAccessException, InvocationTargetException {
        if (setter != null) {
            setter.invoke(target, value);
        } else {
            field.set(target, value);
        }
    }

    private Method findGetter(Class type, Field field) {
        Method result = findMethod(type, "get" + field.getName().substring(0,1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1));
        if (result != null && field.getType().equals(result.getReturnType())) {
            return result;
        }
        result = findMethod(type, "is" + field.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1));
        if (result != null && field.getType().equals(result.getReturnType())) {
            return result;
        }
        return null;
    }

    private Method findSetter(Class type, Field field) {
        return findMethod(type, "set" + field.getName().substring(0,1).toUpperCase(Locale.ENGLISH) + field.getName().substring(1), field.getType());
    }

    private Method findMethod(Class type, String methodName, Class<?> ... parameters) {
        try {
            return type.getMethod(methodName, parameters);
        } catch (NoSuchMethodException nsme) {
            // Not really that exceptional
        }
        return null;
    }
}
