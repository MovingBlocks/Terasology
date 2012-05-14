package org.terasology.entitySystem;

import com.google.common.base.Objects;

/**
 * @todo javadoc
 */
public abstract class AbstractPrefab implements Prefab {

    private String name;

    protected AbstractPrefab(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Prefab) {
            return Objects.equal(name, ((Prefab) o).getName());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "Prefab(" + name + "){ components: " + this.listOwnComponents() + ", parents: " + this.getParents() + " }";
    }
}
