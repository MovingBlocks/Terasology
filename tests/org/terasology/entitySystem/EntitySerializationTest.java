package org.terasology.entitySystem;

import org.junit.Test;
import org.terasology.entitySystem.pojo.PojoEntityManager;
import org.terasology.entitySystem.pojo.persistence.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.stubs.GetterSetterComponent;

import javax.vecmath.Vector3f;

import static org.junit.Assert.assertTrue;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntitySerializationTest {
    @Test
    public void testGetterSetterUtilization() {
        PojoEntityManager entityManager = new PojoEntityManager();
        entityManager.registerTypeHandler(Vector3f.class, new Vector3fTypeHandler());
        entityManager.registerComponentClass(GetterSetterComponent.class);

        GetterSetterComponent comp = new GetterSetterComponent();
        GetterSetterComponent newComp = (GetterSetterComponent) entityManager.copyComponent(comp);
        assertTrue(comp.getterUsed);
        assertTrue(newComp.setterUsed);
    }
}
