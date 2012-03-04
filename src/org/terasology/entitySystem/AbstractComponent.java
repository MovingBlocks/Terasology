package org.terasology.entitySystem;

import java.lang.reflect.Field;
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
            return (Component) super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen
            throw new InternalError();
        }
    }
}
