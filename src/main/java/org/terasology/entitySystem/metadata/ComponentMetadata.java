package org.terasology.entitySystem.metadata;

import org.terasology.entitySystem.Component;
import org.terasology.network.NoReplicate;

/**
 * @author Immortius
 */
public class ComponentMetadata<T extends Component> extends ClassMetadata<T> {

    private boolean noReplicate = false;

    public ComponentMetadata(Class<T> simpleClass, String... names) throws NoSuchMethodException {
        super(simpleClass, names);
        noReplicate = simpleClass.getAnnotation(NoReplicate.class) != null;
    }

    public boolean isNoReplicate() {
        return noReplicate;
    }
}
