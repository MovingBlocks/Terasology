package org.terasology.entitySystem.stubs;

import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.Component;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class StringComponent extends AbstractComponent {
    public String value;

    public StringComponent() {}

    public StringComponent(String value) {
        this.value = value;
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
