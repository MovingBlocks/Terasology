package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.Component;
import org.terasology.game.Terasology;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class SimpleAIComponent extends AbstractComponent {

    public long lastChangeOfDirectionAt = 0;
    public Vector3f movementTarget = new Vector3f();
    public boolean followingPlayer = false;

}
