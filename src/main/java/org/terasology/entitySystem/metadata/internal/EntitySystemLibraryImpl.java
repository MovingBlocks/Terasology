package org.terasology.entitySystem.metadata.internal;

import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibrary;

/**
 * @author Immortius
 */
public class EntitySystemLibraryImpl implements EntitySystemLibrary {
    private final TypeHandlerLibrary typeHandlerLibrary;
    private final ComponentLibrary componentLibrary;
    private final EventLibrary eventLibrary;

    public EntitySystemLibraryImpl(TypeHandlerLibrary typeHandlerLibrary) {
        this.typeHandlerLibrary = typeHandlerLibrary;
        this.componentLibrary = new ComponentLibraryImpl(typeHandlerLibrary);
        this.eventLibrary = new EventLibraryImpl(typeHandlerLibrary);
    }

    @Override
    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    @Override
    public EventLibrary getEventLibrary() {
        return eventLibrary;
    }

    @Override
    public TypeHandlerLibrary getTypeHandlerLibrary() {
        return typeHandlerLibrary;
    }

}
