package org.terasology.entitySystem.metadata.internal;

import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.TypeHandler;

/**
 * @author Immortius
 */
public class EntitySystemLibraryImpl implements EntitySystemLibrary {
    private MetadataBuilder metadataBuilder = new MetadataBuilder();
    private ComponentLibrary componentLibrary = new ComponentLibraryImpl(metadataBuilder);
    private EventLibrary eventLibrary = new EventLibraryImpl(metadataBuilder);

    @Override
    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    @Override
    public EventLibrary getEventLibrary() {
        return eventLibrary;
    }

    @Override
    public <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler) {
        metadataBuilder.registerTypeHandler(forClass, handler);
    }

    @Override
    public <T> TypeHandler<? super T> getTypeHandler(Class<T> forClass) {
        return metadataBuilder.getTypeHandler(forClass);
    }
}
