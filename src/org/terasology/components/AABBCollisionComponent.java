package org.terasology.components;

import org.terasology.entitySystem.Component;
import org.terasology.model.structures.AABB;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

import javax.vecmath.Vector3d;

/**
 * Component describing the collision of the entity in terms of an AABB
 * @author Immortius <immortius@gmail.com>
 */
// TODO: Actually should support something better than just AABB collision for entities, via JBullet.
// NOTE: May want to use a flyweight pattern - define each AABBCollisionComponent once, reuse component for each entity that needs
// it. Will mean only need to replicate it once too.
public class AABBCollisionComponent implements Component {
    public AABB aabb = new AABB(new Vector3d(), new Vector3d());

    public void store(StorageWriter writer) {
        writer.write("aabb", aabb);
    }

    public void retrieve(StorageReader reader) {
        aabb = reader.read("aabb", AABB.class, aabb);
    }
}
