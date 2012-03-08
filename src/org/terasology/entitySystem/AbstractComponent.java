package org.terasology.entitySystem;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius <immortius@gmail.com>
 */
public abstract class AbstractComponent implements Component {
    
    private static Logger logger = Logger.getLogger(AbstractComponent.class.getName());

    public String getName() {
        String className = getClass().getSimpleName().toLowerCase();

        if (className.endsWith("component")) {
            return className.substring(0, className.lastIndexOf("component"));
        }

        return className;
    }

    public Component clone() {
        try {
            Component component = (Component)super.clone(); // shallow copy

            for (Field field : getClass().getDeclaredFields()) {
                Class fieldClass = field.getType();

                if (!Cloneable.class.isAssignableFrom(fieldClass)) { // field is not cloneable
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
