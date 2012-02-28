package org.terasology.entitySystem.pojo.persistence;

import java.lang.reflect.Field;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class FieldInfo {
    // TODO: Support getters + setters
    private Field field;
    private TypeHandler serializationHandler;

    public FieldInfo(Field field, TypeHandler handler) {
        this.field = field;
        this.serializationHandler = handler;
    }

    public TypeHandler getSerializationHandler() {
        return serializationHandler;
    }

    public String getName() {
        return field.getName();
    }

    public Object getValue(Object obj) throws IllegalAccessException {
        return field.get(obj);
    }

    public void setValue(Object target, Object value) throws IllegalAccessException {
        field.set(target, value);
    }
}
