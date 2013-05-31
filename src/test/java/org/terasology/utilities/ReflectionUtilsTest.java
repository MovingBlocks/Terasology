package org.terasology.utilities;

import org.junit.Test;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.location.LocationComponent;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class ReflectionUtilsTest {

    @Test
    public void testGetParameterForField() throws Exception {
        assertEquals(EntityRef.class, ReflectionUtil.getTypeParameter(LocationComponent.class.getDeclaredField("children").getGenericType(), 0));
    }

}
