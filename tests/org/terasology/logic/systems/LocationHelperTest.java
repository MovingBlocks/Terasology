package org.terasology.logic.systems;

import org.junit.Before;
import org.junit.Test;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.math.TeraMath;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.terasology.testUtil.TeraAssert.*;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocationHelperTest {
    
    LocationComponent loc;
    Vector3f position;

    @Before
    public void setup() {
        loc = new LocationComponent();
        position = new Vector3f(1,2,3);
    }
    
    @Test
    public void localToWorldPosNoParentNoChange() {
        loc.position.set(1,2,3);
        Vector3f result = LocationHelper.localToWorldPos(loc);
        assertEquals(loc.position, result);
    }
    
    @Test
     public void localToWorldPosOffsetByParent() {
        LocationComponent parent = giveParent(loc);
        loc.position.set(1,2,3);
        parent.position.set(2,3,4);

        Vector3f expected = new Vector3f();
        expected.add(loc.position, parent.position);
        assertEquals(expected, LocationHelper.localToWorldPos(loc));
    }

    @Test
    public void localToWorldPosRotatedByParent() {
        LocationComponent parent = giveParent(loc);
        loc.position.set(1,2,3);
        parent.rotation.set(new AxisAngle4f(0, 1, 0, TeraMath.HALF_PI));

        assertEquals(new Vector3f(3,2,-1), LocationHelper.localToWorldPos(loc), 0.00001f);
    }
    
    @Test
    public void localToWorldPosScaledByParent() {
        LocationComponent parent = giveParent(loc);
        loc.position.set(1,2,3);
        parent.scale = 2.0f;
        
        assertEquals(new Vector3f(2,4,6), LocationHelper.localToWorldPos(loc));
    }

    @Test
    public void localToWorldPosScaleAndRotateBeforeOffset() {
        LocationComponent parent = giveParent(loc);
        loc.position.set(1,2,3);
        parent.scale = 2.0f;
        parent.position.set(1,2,3);
        parent.rotation.set(new AxisAngle4f(0, 1, 0, TeraMath.HALF_PI));

        assertEquals(new Vector3f(7, 6, 1), LocationHelper.localToWorldPos(loc), 0.00001f);
    }

    @Test
    public void worldToLocalPosNoParentNoChange() {
        Vector3f result = LocationHelper.worldToLocalPos(loc, position);
        assertEquals(position, result);
    }

    @Test
    public void worldToLocalPosOffsetByParent() {
        LocationComponent parent = giveParent(loc);
        parent.position.set(2,3,4);

        Vector3f expected = new Vector3f();
        expected.sub(position, parent.position);
        assertEquals(expected, LocationHelper.worldToLocalPos(loc, position));
    }

    @Test
    public void worldToLocalPosRotatedByParent() {
        LocationComponent parent = giveParent(loc);
        parent.rotation.set(new AxisAngle4f(0, 1, 0, TeraMath.HALF_PI));

        assertEquals(new Vector3f(-3,2,1), LocationHelper.worldToLocalPos(loc, position), 0.00001f);
    }

    @Test
    public void worldToLocalPosScaledByParent() {
        LocationComponent parent = giveParent(loc);
        parent.scale = 2.0f;

        assertEquals(new Vector3f(0.5f,1f,1.5f), LocationHelper.worldToLocalPos(loc, position));
    }

    @Test
    public void worldToLocalPosOffsetBeforeScaleAndRotate() {
        LocationComponent parent = giveParent(loc);
        parent.scale = 2.0f;
        parent.position.set(-1,-1,-1);
        parent.rotation.set(new AxisAngle4f(0, 1, 0, TeraMath.HALF_PI));

        assertEquals(new Vector3f(-2, 1.5f, 1), LocationHelper.worldToLocalPos(loc, position), 0.00001f);
    }
    
    private LocationComponent giveParent(LocationComponent location) {
        LocationComponent parent = new LocationComponent();
        EntityRef parentEntity = mock(EntityRef.class);
        when(parentEntity.getComponent(LocationComponent.class)).thenReturn(parent);
        location.parent = parentEntity;  
        return parent;
    }
}
