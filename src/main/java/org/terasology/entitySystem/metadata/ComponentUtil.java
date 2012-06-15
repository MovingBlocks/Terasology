package org.terasology.entitySystem.metadata;

import org.terasology.entitySystem.Component;

import java.util.Locale;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ComponentUtil {

    private ComponentUtil() {
    }

    public static String getComponentClassName(Component component) {
        return getComponentClassName(component.getClass());
    }

    public static String getComponentClassName(Class<? extends Component> componentClass) {
        String name = componentClass.getSimpleName();
        int index = name.toLowerCase(Locale.ENGLISH).lastIndexOf("component");
        if (index != -1) {
            return name.substring(0, index);
        }
        return name;
    }
}
