package org.terasology.entitySystem.metadata.internal;

import com.google.common.collect.ImmutableList;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.metadata.EventLibrary;

import java.util.List;

/**
 * @author Immortius
 */
public class EventLibraryImpl extends BaseLibraryImpl<Event> implements EventLibrary {

    public EventLibraryImpl(MetadataBuilder metadataBuilder) {
        super(metadataBuilder);
    }

    @Override
    public List<String> getNamesFor(Class<? extends Event> clazz) {
        return ImmutableList.<String>builder().add(clazz.getSimpleName()).build();
    }
}
