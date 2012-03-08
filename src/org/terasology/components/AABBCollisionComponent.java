package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.Component;
import org.terasology.model.structures.AABB;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Component describing the collision of the entity in terms of an AABB
 * Makes an assumption the AABB is centered on the entity's location
 * @author Immortius <immortius@gmail.com>
 */
// TODO: Actually should support something better than just AABB collision for entities, via JBullet.
// NOTE: May want to use a flyweight pattern - define each AABBCollisionComponent once, reuse component for each entity that needs
// it. Will mean only need to replicate it once too.
public final class AABBCollisionComponent extends AbstractComponent {

    private Vector3f extents = new Vector3f();

    public Vector3f getExtents() {
        return extents;
    }
    
    public void setExtents(Tuple3f newExtents) {
        extents.set(newExtents);
    }

}
