package org.terasology.entitySystem.metadata.internal;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.metadata.ClassLibrary;
import org.terasology.entitySystem.metadata.ClassMetadata;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Immortius
 */
public abstract class BaseLibraryImpl<T> implements ClassLibrary<T> {

    private Map<Class<? extends T>, ClassMetadata<? extends T>> serializationLookup = Maps.newHashMap();
    private Map<String, Class<? extends T>> typeLookup = Maps.newHashMap();

    private MetadataBuilder metadataBuilder;

    public BaseLibraryImpl(MetadataBuilder metadataBuilder) {
        this.metadataBuilder = metadataBuilder;
    }

    public abstract List<String> getNamesFor(Class<? extends T> clazz);

    @Override
    public void register(Class<? extends T> clazz) {
        ClassMetadata<? extends T> metadata = metadataBuilder.build(clazz);

        serializationLookup.put(clazz, metadata);

        for (String name : getNamesFor(clazz)) {
            typeLookup.put(name.toLowerCase(Locale.ENGLISH), clazz);
        }
    }

    @Override
    public <U extends T> ClassMetadata<U> getMetadata(Class<U> clazz) {
        if (clazz == null) {
            return null;
        }
        return (ClassMetadata<U>) serializationLookup.get(clazz);
    }

    @Override
    public ClassMetadata<? extends T> getMetadata(T object) {
        if (object != null) {
            return serializationLookup.get(object.getClass());
        }
        return null;
    }

    @Override
    public <TYPE extends T> TYPE copy(TYPE object) {
        ClassMetadata<TYPE> info = (ClassMetadata<TYPE>) getMetadata(object);
        if (info != null) {
            return info.clone(object);
        }
        return null;
    }

    @Override
    public ClassMetadata<? extends T> getMetadata(String className) {
        return getMetadata(typeLookup.get(className.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Iterator<ClassMetadata<? extends T>> iterator() {
        return serializationLookup.values().iterator();
    }
}
