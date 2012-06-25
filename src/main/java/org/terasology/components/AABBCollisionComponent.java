package org.terasology.components;

import org.terasology.entitySystem.Component;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

/**
 * Component describing the collision of the entity in terms of an AABB
 * Makes an assumption the AABB is centered on the entity's location
 *
 * @author Immortius <immortius@gmail.com>
 */
// TODO: Actually should support something better than just AABB collision for entities, via JBullet.
public final class AABBCollisionComponent implements Component {

    private Vector3f extents = new Vector3f();

    public Vector3f getExtents() {
        return extents;
    }

    public void setExtents(Tuple3f newExtents) {
        extents.set(newExtents);
    }

}
