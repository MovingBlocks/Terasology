package org.terasology.entitySystem.metadata.core;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.metadata.AbstractTypeHandler;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.protobuf.EntityData;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class MappedContainerTypeHandler<T> extends AbstractTypeHandler<T> {

    private static Logger logger = Logger.getLogger(MappedContainerTypeHandler.class.getName());

    private Class<T> clazz;
    private Map<String, FieldMetadata> fields = Maps.newHashMap();

    public MappedContainerTypeHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void addField(FieldMetadata info) {
        fields.put(info.getName().toLowerCase(Locale.ENGLISH), info);
    }

    public EntityData.Value serialize(T value) {
        if (value == null) return null;

        EntityData.Value.Builder result = EntityData.Value.newBuilder();

        try {
            for (FieldMetadata fieldInfo : fields.values()) {
                Object rawValue = fieldInfo.getValue(value);
                if (rawValue == null)
                    continue;

                EntityData.Value fieldValue = fieldInfo.serialize(rawValue);
                if (fieldValue != null) {
                    result.addNameValue(EntityData.NameValue.newBuilder().setName(fieldInfo.getName()).setValue(fieldValue).build());
                }
            }
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Unable to serialize field of " + value.getClass(), e);
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE, "Unable to serialize field of " + value.getClass(), e);
        }
        return result.build();
    }

    public T deserialize(EntityData.Value value) {
        try {
            T result = clazz.newInstance();
            for (EntityData.NameValue entry : value.getNameValueList()) {
                FieldMetadata fieldInfo = fields.get(entry.getName().toLowerCase(Locale.ENGLISH));
                if (fieldInfo != null) {
                    Object content = fieldInfo.deserialize(entry.getValue());
                    if (content != null) {
                        fieldInfo.setValue(result, content);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to deserialize " + value, e);
        }
        return null;
    }

    public T copy(T value) {
        if (value != null) {
            try {
                T result = clazz.newInstance();
                for (FieldMetadata field : fields.values()) {
                    field.setValue(result, field.copy(field.getValue(value)));
                }
                return result;
            } catch (InstantiationException e) {
                logger.log(Level.SEVERE, "Unable to clone " + value.getClass(), e);
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Unable to clone " + value.getClass(), e);
            } catch (InvocationTargetException e) {
                logger.log(Level.SEVERE, "Unable to clone " + value.getClass(), e);
            }
        }
        return null;
    }

}
