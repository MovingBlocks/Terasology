package org.terasology.entitySystem;

import org.junit.Test;
import org.terasology.entitySystem.pojo.PojoEntityManager;
import org.terasology.entitySystem.pojo.persistence.FieldInfo;
import org.terasology.entitySystem.pojo.persistence.SerializationInfo;
import org.terasology.entitySystem.pojo.persistence.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.stubs.GetterSetterComponent;

import javax.vecmath.Vector3f;

import static org.junit.Assert.assertTrue;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntitySerializationTest {
    @Test
    public void testGetterSetterUtilization() throws Exception {
        SerializationInfo info = new SerializationInfo(GetterSetterComponent.class);
        info.addField(new FieldInfo(GetterSetterComponent.class.getDeclaredField("value"), GetterSetterComponent.class, new Vector3fTypeHandler()));

        GetterSetterComponent comp = new GetterSetterComponent();
        GetterSetterComponent newComp = (GetterSetterComponent) info.deserialize(info.serialize(comp));
        assertTrue(comp.getterUsed);
        assertTrue(newComp.setterUsed);
    }
}
