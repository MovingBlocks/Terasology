package org.terasology.entitySystem;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * @author Immortius <immortius@gmail.com>
 */
public abstract class AbstractComponent implements Component {

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

            for (Field field : getClass().getFields()) {
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
