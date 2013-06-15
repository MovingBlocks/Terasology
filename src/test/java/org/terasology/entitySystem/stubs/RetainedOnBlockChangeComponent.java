package org.terasology.entitySystem.stubs;

import org.terasology.entitySystem.Component;
import org.terasology.world.block.ForceBlockActive;

/**
 * @author Immortius
 */
@ForceBlockActive(retainUnalteredOnBlockChange = true)
public class RetainedOnBlockChangeComponent implements Component {
    public int value;

    public RetainedOnBlockChangeComponent() {
    }

    public RetainedOnBlockChangeComponent(int value) {
        this.value = value;
    }
}
