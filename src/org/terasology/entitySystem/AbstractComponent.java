package org.terasology.entitySystem;

/**
 * @author Immortius <immortius@gmail.com>
 */
public abstract class AbstractComponent implements Component {
    public String getName() {
        int index = getClass().getSimpleName().lastIndexOf("Component");
        if (index != -1) {
            return getClass().getSimpleName().substring(0, index).toLowerCase();
        }
        return getClass().getSimpleName().toLowerCase();
    }
    
}
