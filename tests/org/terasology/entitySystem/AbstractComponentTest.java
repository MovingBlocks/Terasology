package org.terasology.entitySystem;

import org.junit.Test;
import org.terasology.entitySystem.stubs.StringComponent;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class AbstractComponentTest {

    @Test
    public void getName() {
        StringComponent comp = new StringComponent();
        assertEquals("string", comp.getName());
    }
}
