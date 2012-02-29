package org.terasology.entitySystem.stubs;

import org.terasology.entitySystem.Component;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class IntegerComponent implements Component {
    int value;

    public void store(StorageWriter writer) {
        writer.write("value", value);
    }

    public void retrieve(StorageReader reader) {
        value = reader.readInt("value", 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntegerComponent that = (IntegerComponent) o;

        if (value != that.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
