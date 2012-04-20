package org.terasology.entitySystem.persistence;

import org.terasology.entitySystem.Component;

import java.util.Locale;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PersistenceUtil {

    private PersistenceUtil() {}

    public static String getComponentClassName(Class<? extends Component> componentClass) {
        String name = componentClass.getSimpleName();
        int index = name.toLowerCase(Locale.ENGLISH).lastIndexOf("component");
        if (index != -1) {
            return name.substring(0, index);
        }
        return name;
    }
}
