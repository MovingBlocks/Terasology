package org.terasology.entitySystem;

import org.terasology.entitySystem.pojo.persistence.PersistenceUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius <immortius@gmail.com>
 */
public abstract class AbstractComponent implements Component {
    
    private static Logger logger = Logger.getLogger(AbstractComponent.class.getName());

    public String getName() {
        return PersistenceUtil.getComponentClassName(getClass());
    }

    public Component clone() {
        try {
            Component component = (Component)super.clone(); // shallow copy

            for (Field field : getClass().getDeclaredFields()) {
                Class fieldClass = field.getType();

                if (!Cloneable.class.isAssignableFrom(fieldClass)) { // field is not cloneable
                    logger.log(Level.SEVERE, "Failed to clone component field: " + fieldClass);
                     continue;
                }

                field.setAccessible(true);

                try {
                    Method method = fieldClass.getMethod("clone");
                    
                    Object clonedField = method.invoke(field.get(component));

                    field.set(component, clonedField);
                } catch (Throwable e) {
                    logger.log(Level.SEVERE, "Failed to clone field", e);
                    // do nothing
                }
            }

            return component;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen
            throw new InternalError();
        }
    }

}
