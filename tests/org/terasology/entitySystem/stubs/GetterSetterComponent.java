package org.terasology.entitySystem.stubs;

import org.terasology.entitySystem.AbstractComponent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class GetterSetterComponent extends AbstractComponent {
    private Vector3f value = new Vector3f(0,0,0);

    public transient boolean getterUsed = false;
    public transient boolean setterUsed = false;

    public Vector3f getValue() {
        getterUsed = true;
        return value;
    }

    public void setValue(Vector3f value) {
        this.value = value;
        setterUsed = true;
    }
}
