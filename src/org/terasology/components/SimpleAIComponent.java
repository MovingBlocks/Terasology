package org.terasology.components;

import org.terasology.entitySystem.Component;
import org.terasology.game.Terasology;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class SimpleAIComponent implements Component {

    public long lastChangeOfDirectionAt = 0;
    public Vector3f movementTarget = new Vector3f();
    public boolean followingPlayer = false;

    public void store(StorageWriter writer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void retrieve(StorageReader reader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
