package org.terasology.components;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.junit.Before;
import org.junit.Test;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.math.TeraMath;
import org.terasology.testUtil.TeraAssert;

import com.bulletphysics.linearmath.QuaternionUtil;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocationComponentTest {

    LocationComponent loc;
    EntityRef entity;
    Vector3f pos1 = new Vector3f(1,2,3);
    Vector3f pos2 = new Vector3f(2,3,4);
    Vector3f pos1plus2 = new Vector3f(3,5,7);
    Quat4f yawRotation = new Quat4f(0,0,0,1);
    Quat4f pitchRotation = new Quat4f(0,0,0,1);
    Quat4f pitchYaw = new Quat4f(0,0,0,1);

    @Before
    public void setup() {
        loc = new LocationComponent();
        entity = mock(EntityRef.class);
        when(entity.getComponent(LocationComponent.class)).thenReturn(loc);
        when(entity.exists()).thenReturn(true);
        QuaternionUtil.setEuler(yawRotation, TeraMath.DEG_TO_RAD * 90, 0, 0);
        QuaternionUtil.setEuler(pitchRotation, 0, TeraMath.DEG_TO_RAD * 45, 0);
        pitchYaw.mul(yawRotation, pitchRotation);
    }
    
    @Test
    public void setLocalPosition() {
        loc.setLocalPosition(pos1);
        assertEquals(pos1, loc.getLocalPosition());
    }

    @Test
    public void setLocalRotation() {
        loc.setLocalRotation(yawRotation);
        assertEquals(yawRotation, loc.getLocalRotation());
    }

    @Test
    public void unparentedWorldLocationSameAsLocal() {
        loc.setLocalPosition(pos1);
        assertEquals(loc.getLocalPosition(), loc.getWorldPosition());
    }
    
    @Test
    public void offsetParentAddsToWorldLocation() {
        LocationComponent parent = giveParent(loc);
        loc.setLocalPosition(pos1);
        parent.setLocalPosition(pos2);
        assertEquals(pos1plus2, loc.getWorldPosition());
    }

    @Test
    public void parentRotatesWorldLocation() {
        LocationComponent parent = giveParent(loc);
        loc.setLocalPosition(pos1);
        parent.setLocalRotation(yawRotation);
        TeraAssert.assertEquals(new Vector3f(pos1.z, pos1.y, -pos1.x), loc.getWorldPosition(), 0.00001f);
    }

    @Test
    public void parentScalesWorldLocation() {
        LocationComponent parent = giveParent(loc);
        loc.setLocalPosition(pos1);
        parent.setLocalScale(2.0f);

        assertEquals(new Vector3f(2,4,6), loc.getWorldPosition());
    }

    @Test
    public void scaleRotateAndOffsetCombineCorrectlyForWorldPosition() {
        LocationComponent parent = giveParent(loc);
        loc.setLocalPosition(pos1);
        parent.setLocalScale(2.0f);
        parent.setLocalPosition(pos2);
        parent.setLocalRotation(yawRotation);

        TeraAssert.assertEquals(new Vector3f(8, 7, 2), loc.getWorldPosition(), 0.00001f);
    }

    @Test
    public void worldRotationSameAsLocalRotationWhenNoParent() {
        loc.setLocalRotation(yawRotation);
        assertEquals(loc.getLocalRotation(), loc.getWorldRotation());
    }

    @Test
    public void worldRotationCombinedWithParent() {
        LocationComponent parent = giveParent(loc);
        loc.setLocalRotation(pitchRotation);
        parent.setLocalRotation(yawRotation);
        assertEquals(pitchYaw, loc.getWorldRotation());
    }

    @Test
    public void worldScaleSameAsLocalWhenNoParent() {
        loc.setLocalScale(2.0f);
        assertEquals(loc.getLocalScale(), loc.getWorldScale(), 0.00001f);
    }

    @Test
    public void worldScaleStacksWithParent() {
        LocationComponent parent = giveParent(loc);
        loc.setLocalScale(2.f);
        parent.setLocalScale(2.f);
        assertEquals(4.f, loc.getWorldScale(), 0.000001f);
    }

    @Test
    public void setWorldPositionWorksWithNoParent() {
        loc.setWorldPosition(pos1);
        assertEquals(pos1, loc.getWorldPosition());
    }

    @Test
    public void setWorldPositionWorksWithOffsetParent() {
        LocationComponent parent = giveParent(loc);
        parent.setLocalPosition(pos1);
        loc.setWorldPosition(pos1plus2);
        assertEquals(pos2, loc.getLocalPosition());
        assertEquals(pos1plus2, loc.getWorldPosition());
    }

    @Test
    public void setWorldPositionWorksWithScaledParent() {
        LocationComponent parent = giveParent(loc);
        parent.setLocalScale(2.0f);
        loc.setWorldPosition(pos1);
        assertEquals(pos1, loc.getWorldPosition());
    }

    @Test
    public void setWorldPositionWorksWithRotatedParent() {
        LocationComponent parent = giveParent(loc);
        parent.setLocalRotation(yawRotation);
        loc.setWorldPosition(pos1);
        TeraAssert.assertEquals(pos1, loc.getWorldPosition(), 0.000001f);
    }

    @Test
    public void setWorldPositionWorksWithComplexParent() {
        LocationComponent parent = giveParent(loc);
        parent.setLocalRotation(yawRotation);
        parent.setLocalScale(2.0f);
        parent.setLocalPosition(pos2);
        loc.setWorldPosition(pos1);
        TeraAssert.assertEquals(pos1, loc.getWorldPosition(), 0.000001f);
    }

    @Test
    public void setWorldScaleWorksWithNoParent() {
        loc.setWorldScale(4.0f);
        assertEquals(4.0f, loc.getWorldScale(), 0.000001f);
        assertEquals(4.0f, loc.getLocalScale(), 0.000001f);
    }

    @Test
    public void setWorldScaleWorksWithScaledParent() {
        LocationComponent parent = giveParent(loc);
        parent.setLocalScale(4.0f);
        loc.setWorldScale(2.0f);
        assertEquals(2.0f, loc.getWorldScale(), 0.000001f);
    }

    @Test
    public void setWorldRotationWorksWithNoParent() {
        loc.setWorldRotation(yawRotation);
        assertEquals(yawRotation, loc.getWorldRotation());
        assertEquals(yawRotation, loc.getLocalRotation());
    }

    @Test
    public void setWorldRotationWithRotatedParent() {
        LocationComponent parent = giveParent(loc);
        parent.setLocalRotation(yawRotation);
        loc.setWorldRotation(pitchYaw);
        TeraAssert.assertEquals(pitchYaw, loc.getWorldRotation(), 0.0000001f);
    }

    private LocationComponent giveParent(LocationComponent location) {
        LocationComponent parent = new LocationComponent();
        EntityRef parentEntity = mock(EntityRef.class);
        when(parentEntity.getComponent(LocationComponent.class)).thenReturn(parent);
        when(parentEntity.exists()).thenReturn(true);
        parent.addChild(entity, parentEntity);
        return parent;
    }
}
