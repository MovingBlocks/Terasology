package org.terasology.entitySystem.stubs;

import org.terasology.entitySystem.Component;
import org.terasology.persistence.StorageReader;
import org.terasology.persistence.StorageWriter;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class StringComponent implements Component {
    public String value;

    public void store(StorageWriter writer) {
        writer.write("value", value);
    }

    public void retrieve(StorageReader reader) {
        value = reader.read("value", String.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringComponent that = (StringComponent) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
