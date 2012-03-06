package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Component represent the location and facing of an entity in the world
 * @author Immortius <immortius@gmail.com>
 */
public final class LocationComponent extends AbstractComponent {
    // Standard position/rotation
    public Vector3f position = new Vector3f();
    // TODO: Represent as Euler angles instead?
    public Quat4f rotation = new Quat4f(0,0,0,1);
    // TODO: Should this be here? Probably needs to be common as it affects several other components
    public float scale = 1.0f;

    // Relative to
    public EntityRef parent = null;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationComponent component = (LocationComponent) o;

        if (Float.compare(component.scale, scale) != 0) return false;
        if (parent != null ? !parent.equals(component.parent) : component.parent != null) return false;
        if (position != null ? !position.equals(component.position) : component.position != null) return false;
        if (rotation != null ? !rotation.equals(component.rotation) : component.rotation != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = position != null ? position.hashCode() : 0;
        result = 31 * result + (rotation != null ? rotation.hashCode() : 0);
        result = 31 * result + (scale != +0.0f ? Float.floatToIntBits(scale) : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }
}
