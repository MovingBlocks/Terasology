package org.terasology.entitySystem.pojo.persistence.core;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.pojo.persistence.AbstractTypeHandler;
import org.terasology.entitySystem.pojo.persistence.FieldInfo;
import org.terasology.protobuf.EntityData;

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
    private Map<String, FieldInfo> fields = Maps.newHashMap();

    public MappedContainerTypeHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void addField(FieldInfo info) {
        fields.put(info.getName().toLowerCase(Locale.ENGLISH), info);
    }
    
    public EntityData.Value serialize(T value) {
        if (value == null) return null;

        EntityData.Value.Builder result = EntityData.Value.newBuilder();

        try {
            for (FieldInfo fieldInfo : fields.values()) {
                Object rawValue = fieldInfo.getValue(value);
                if (rawValue == null)
                    continue;

                EntityData.Value fieldValue = fieldInfo.getSerializationHandler().serialize(rawValue);
                if (fieldValue != null) {
                    result.addNameValue(EntityData.NameValue.newBuilder().setName(fieldInfo.getName()).setValue(fieldValue).build());
                }
            }
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Unable to serialize field of " + value.getClass(), e);
        }
        return result.build();
    }

    public T deserialize(EntityData.Value value) {
        try {
            T result = clazz.newInstance();
            for (EntityData.NameValue entry : value.getNameValueList()) {
                FieldInfo fieldInfo = fields.get(entry.getName().toLowerCase(Locale.ENGLISH));
                if (fieldInfo != null) {
                    Object content = fieldInfo.getSerializationHandler().deserialize(entry.getValue());
                    if (content != null) {
                        fieldInfo.setValue(result, content);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to deserialize " + value.getClass(), e);
        }
        return null;
    }

}
