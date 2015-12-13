/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.location;

import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.testUtil.TeraAssert;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 */
public class LocationComponentTest extends TerasologyTestingEnvironment {

    LocationComponent loc;
    EntityRef entity;
    Vector3f pos1 = new Vector3f(1, 2, 3);
    Vector3f pos2 = new Vector3f(2, 3, 4);
    Vector3f pos1plus2 = new Vector3f(3, 5, 7);
    Quat4f yawRotation;
    Quat4f pitchRotation;
    Quat4f yawPitch;
    long nextFakeEntityId = 1;

    @Before
    public void setup() {
        loc = new LocationComponent();
        entity = createFakeEntityWith(loc);

        yawRotation = new Quat4f(TeraMath.DEG_TO_RAD * 90, 0, 0);
        pitchRotation = new Quat4f(0, TeraMath.DEG_TO_RAD * 45, 0);
        yawPitch = new Quat4f(TeraMath.DEG_TO_RAD * 90, TeraMath.DEG_TO_RAD * 45, 0);
    }

    private EntityRef createFakeEntityWith(LocationComponent locationComponent) {
        EntityRef entRef = mock(EntityRef.class);
        when(entRef.getComponent(LocationComponent.class)).thenReturn(locationComponent);
        when(entRef.exists()).thenReturn(true);
        when(entRef.getId()).thenReturn(nextFakeEntityId++);
        return entRef;
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
        LocationComponent parent = giveParent();
        loc.setLocalPosition(pos1);
        parent.setLocalPosition(pos2);
        assertEquals(pos1plus2, loc.getWorldPosition());
    }

    @Test
    public void parentRotatesWorldLocation() {
        LocationComponent parent = giveParent();
        loc.setLocalPosition(pos1);
        parent.setLocalRotation(yawRotation);
        TeraAssert.assertEquals(new Vector3f(pos1.z, pos1.y, -pos1.x), loc.getWorldPosition(), 0.00001f);
    }

    @Test
    public void parentScalesWorldLocation() {
        LocationComponent parent = giveParent();
        loc.setLocalPosition(pos1);
        parent.setLocalScale(2.0f);

        assertEquals(new Vector3f(2, 4, 6), loc.getWorldPosition());
    }

    @Test
    public void scaleRotateAndOffsetCombineCorrectlyForWorldPosition() {
        LocationComponent parent = giveParent();
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
        LocationComponent parent = giveParent();
        loc.setLocalRotation(pitchRotation);
        parent.setLocalRotation(yawRotation);
        assertEquals(yawPitch, loc.getWorldRotation());
    }

    @Test
    public void worldScaleSameAsLocalWhenNoParent() {
        loc.setLocalScale(2.0f);
        assertEquals(loc.getLocalScale(), loc.getWorldScale(), 0.00001f);
    }

    @Test
    public void worldScaleStacksWithParent() {
        LocationComponent parent = giveParent();
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
        LocationComponent parent = giveParent();
        parent.setLocalPosition(pos1);
        loc.setWorldPosition(pos1plus2);
        assertEquals(pos2, loc.getLocalPosition());
        assertEquals(pos1plus2, loc.getWorldPosition());
    }

    @Test
    public void setWorldPositionWorksWithScaledParent() {
        LocationComponent parent = giveParent();
        parent.setLocalScale(2.0f);
        loc.setWorldPosition(pos1);
        assertEquals(pos1, loc.getWorldPosition());
    }

    @Test
    public void setWorldPositionWorksWithRotatedParent() {
        LocationComponent parent = giveParent();
        parent.setLocalRotation(yawRotation);
        loc.setWorldPosition(pos1);
        TeraAssert.assertEquals(pos1, loc.getWorldPosition(), 0.000001f);
    }

    @Test
    public void setWorldPositionWorksWithNestedRotatedParent() {
        LocationComponent first = new LocationComponent();
        EntityRef firstEntity = createFakeEntityWith(first);

        LocationComponent second = new LocationComponent();
        EntityRef secondEntity = createFakeEntityWith(second);

        LocationComponent third = new LocationComponent();
        EntityRef thirdEntity = createFakeEntityWith(third);

        Location.attachChild(firstEntity, secondEntity);
        second.setLocalPosition(new Vector3f(1, 0, 0));
        first.setLocalRotation(yawRotation);
        TeraAssert.assertEquals(new Vector3f(0, 0, -1), second.getWorldPosition(), 0.000001f);
        Location.attachChild(secondEntity, thirdEntity);
        second.setLocalRotation(pitchRotation);
        third.setLocalPosition(new Vector3f(0, 0, 0));
        TeraAssert.assertEquals(new Vector3f(0, 0, -1), third.getWorldPosition(), 0.000001f);
        third.setLocalPosition(new Vector3f(0, 0, 1));
        TeraAssert.assertEquals(new Vector3f(0.5f * (float) Math.sqrt(2), -0.5f * (float) Math.sqrt(2), -1), third.getWorldPosition(), 0.000001f);

    }

    @Test
    public void setWorldPositionWorksWithComplexParent() {
        LocationComponent parent = giveParent();
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
        LocationComponent parent = giveParent();
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
        LocationComponent parent = giveParent();
        parent.setLocalRotation(yawRotation);
        loc.setWorldRotation(yawPitch);
        TeraAssert.assertEquals(yawPitch, loc.getWorldRotation(), 0.0000001f);
    }

    @Test
    public void positionMaintainedWhenAttachedToParent() {
        LocationComponent parent = new LocationComponent();
        EntityRef parentEntity = createFakeEntityWith(parent);
        parent.setWorldPosition(new Vector3f(1, 0, 0));

        loc.setWorldPosition(new Vector3f(2, 0, 0));
        Location.attachChild(parentEntity, entity);

        TeraAssert.assertEquals(new Vector3f(2, 0, 0), loc.getWorldPosition(), 0.000001f);
    }

    @Test
    public void positionMaintainedWhenRemovedFromParent() {
        LocationComponent parent = new LocationComponent();
        EntityRef parentEntity = createFakeEntityWith(parent);
        parent.setWorldPosition(new Vector3f(1, 0, 0));

        loc.setWorldPosition(new Vector3f(2, 0, 0));
        Location.attachChild(parentEntity, entity);
        Location.removeChild(parentEntity, entity);

        TeraAssert.assertEquals(new Vector3f(2, 0, 0), loc.getWorldPosition(), 0.000001f);
    }

    @Test
    public void positionMaintainedWhenParentDestroyed() {
        LocationComponent parent = new LocationComponent();
        EntityRef parentEntity = createFakeEntityWith(parent);
        parent.setWorldPosition(new Vector3f(1, 0, 0));

        loc.setWorldPosition(new Vector3f(2, 0, 0));
        Location.attachChild(parentEntity, entity);
        Location locationSystem = new Location();
        locationSystem.onDestroyed(BeforeRemoveComponent.newInstance(), parentEntity, parent);
        when(parentEntity.getComponent(LocationComponent.class)).thenReturn(null);
        when(parentEntity.exists()).thenReturn(false);

        TeraAssert.assertEquals(new Vector3f(2, 0, 0), loc.getWorldPosition(), 0.000001f);
    }


    private LocationComponent giveParent() {
        LocationComponent parent = new LocationComponent();
        EntityRef parentEntity = createFakeEntityWith(parent);
        Location.attachChild(parentEntity, entity);
        return parent;
    }
}
