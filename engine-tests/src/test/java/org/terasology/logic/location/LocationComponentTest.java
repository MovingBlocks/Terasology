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

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.math.JomlUtil;
import org.terasology.math.TeraMath;
import org.terasology.testUtil.TeraAssert;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    Quaternionf yawRotation;
    Quaternionf pitchRotation;
    Quaternionf yawPitch;
    long nextFakeEntityId = 1;

    @BeforeEach
    public void setup() {
        loc = new LocationComponent();
        entity = createFakeEntityWith(loc);

        yawRotation = new Quaternionf().rotationYXZ(TeraMath.DEG_TO_RAD * 90, 0, 0);
        pitchRotation = new Quaternionf().rotationYXZ(0, TeraMath.DEG_TO_RAD * 45, 0);
        yawPitch = new Quaternionf().rotationYXZ(TeraMath.DEG_TO_RAD * 90, TeraMath.DEG_TO_RAD * 45, 0);
    }

    private EntityRef createFakeEntityWith(LocationComponent locationComponent) {
        EntityRef entRef = mock(EntityRef.class);
        when(entRef.getComponent(LocationComponent.class)).thenReturn(locationComponent);
        when(entRef.exists()).thenReturn(true);
        when(entRef.getId()).thenReturn(nextFakeEntityId++);
        return entRef;
    }

    @Test
    public void testSetLocalPosition() {
        loc.setLocalPosition(pos1);
        assertEquals(pos1, JomlUtil.from(loc.getLocalPosition()));
    }

    @Test
    public void testSetLocalRotation() {
        loc.setLocalRotation(yawRotation);
        TeraAssert.assertEquals(yawRotation, JomlUtil.from(loc.getLocalRotation()), 0.0001f);
    }

    @Test
    public void testUnparentedWorldLocationSameAsLocal() {
        loc.setLocalPosition(pos1);
        assertEquals(JomlUtil.from(loc.getLocalPosition()), loc.getWorldPosition(new Vector3f()));
    }

    @Test
    public void testOffsetParentAddsToWorldLocation() {
        LocationComponent parent = giveParent();
        loc.setLocalPosition(pos1);
        parent.setLocalPosition(pos2);
        assertEquals(pos1plus2, loc.getWorldPosition(new Vector3f()));
    }

    @Test
    public void testParentRotatesWorldLocation() {
        LocationComponent parent = giveParent();
        loc.setLocalPosition(pos1);
        parent.setLocalRotation(yawRotation);
        TeraAssert.assertEquals(new Vector3f(pos1.z, pos1.y, -pos1.x), loc.getWorldPosition(new Vector3f()), 0.00001f);
    }

    @Test
    public void testParentScalesWorldLocation() {
        LocationComponent parent = giveParent();
        loc.setLocalPosition(pos1);
        parent.setLocalScale(2.0f);

        assertEquals(new Vector3f(2, 4, 6), loc.getWorldPosition(new Vector3f()));
    }

    @Test
    public void testScaleRotateAndOffsetCombineCorrectlyForWorldPosition() {
        LocationComponent parent = giveParent();
        loc.setLocalPosition(pos1);
        parent.setLocalScale(2.0f);
        parent.setLocalPosition(pos2);
        parent.setLocalRotation(yawRotation);

        TeraAssert.assertEquals(new Vector3f(8, 7, 2), loc.getWorldPosition(new Vector3f()), 0.00001f);
    }

    @Test
    public void testWorldRotationSameAsLocalRotationWhenNoParent() {
        loc.setLocalRotation(yawRotation);
        assertEquals(JomlUtil.from(loc.getLocalRotation()), loc.getWorldRotation(new Quaternionf()));
    }

    @Test
    public void testWorldRotationCombinedWithParent() {
        LocationComponent parent = giveParent();
        loc.setLocalRotation(pitchRotation);
        parent.setLocalRotation(yawRotation);
        TeraAssert.assertEquals(yawPitch, loc.getWorldRotation(new Quaternionf()), 0.0001f);
    }

    @Test
    public void testWorldScaleSameAsLocalWhenNoParent() {
        loc.setLocalScale(2.0f);
        assertEquals(loc.getLocalScale(), loc.getWorldScale(), 0.00001f);
    }

    @Test
    public void testWorldScaleStacksWithParent() {
        LocationComponent parent = giveParent();
        loc.setLocalScale(2.f);
        parent.setLocalScale(2.f);
        assertEquals(4.f, loc.getWorldScale(), 0.000001f);
    }

    @Test
    public void testSetWorldPositionWorksWithNoParent() {
        loc.setWorldPosition(pos1);
        TeraAssert.assertEquals(pos1, loc.getWorldPosition(new Vector3f()), 0.0001f);
    }

    @Test
    public void testSetWorldPositionWorksWithOffsetParent() {
        LocationComponent parent = giveParent();
        parent.setLocalPosition(pos1);
        loc.setWorldPosition(pos1plus2);
        TeraAssert.assertEquals(pos2, JomlUtil.from(loc.getLocalPosition()), 0.0001f);
        assertEquals(pos1plus2, loc.getWorldPosition(new Vector3f()));
    }

    @Test
    public void testSetWorldPositionWorksWithScaledParent() {
        LocationComponent parent = giveParent();
        parent.setLocalScale(2.0f);
        loc.setWorldPosition(pos1);
        TeraAssert.assertEquals(pos1, loc.getWorldPosition(new Vector3f()), 0.0001f);
    }

    @Test
    public void testSetWorldPositionWorksWithRotatedParent() {
        LocationComponent parent = giveParent();
        parent.setLocalRotation(yawRotation);
        loc.setWorldPosition(pos1);
        TeraAssert.assertEquals(pos1, loc.getWorldPosition(new Vector3f()), 0.000001f);
    }

    @Test
    public void testSetWorldPositionWorksWithNestedRotatedParent() {
        LocationComponent first = new LocationComponent();
        EntityRef firstEntity = createFakeEntityWith(first);

        LocationComponent second = new LocationComponent();
        EntityRef secondEntity = createFakeEntityWith(second);

        LocationComponent third = new LocationComponent();
        EntityRef thirdEntity = createFakeEntityWith(third);

        Location.attachChild(firstEntity, secondEntity);
        second.setLocalPosition(new Vector3f(1, 0, 0));
        first.setLocalRotation(yawRotation);
        TeraAssert.assertEquals(new Vector3f(0, 0, -1), second.getWorldPosition(new Vector3f()), 0.000001f);
        Location.attachChild(secondEntity, thirdEntity);
        second.setLocalRotation(pitchRotation);
        third.setLocalPosition(new Vector3f(0, 0, 0));
        TeraAssert.assertEquals(new Vector3f(0, 0, -1), third.getWorldPosition(new Vector3f()), 0.000001f);
        third.setLocalPosition(new Vector3f(0, 0, 1));
        TeraAssert.assertEquals(new Vector3f(0.5f * (float) Math.sqrt(2), -0.5f * (float) Math.sqrt(2), -1), third.getWorldPosition(new Vector3f()), 0.000001f);

    }

    @Test
    public void testSetWorldPositionWorksWithComplexParent() {
        LocationComponent parent = giveParent();
        parent.setLocalRotation(yawRotation);
        parent.setLocalScale(2.0f);
        parent.setLocalPosition(pos2);
        loc.setWorldPosition(pos1);
        TeraAssert.assertEquals(pos1, loc.getWorldPosition(new Vector3f()), 0.000001f);
    }

    @Test
    public void testSetWorldScaleWorksWithNoParent() {
        loc.setWorldScale(4.0f);
        assertEquals(4.0f, loc.getWorldScale(), 0.000001f);
        assertEquals(4.0f, loc.getLocalScale(), 0.000001f);
    }

    @Test
    public void testSetWorldScaleWorksWithScaledParent() {
        LocationComponent parent = giveParent();
        parent.setLocalScale(4.0f);
        loc.setWorldScale(2.0f);
        assertEquals(2.0f, loc.getWorldScale(), 0.000001f);
    }

    @Test
    public void testSetWorldRotationWorksWithNoParent() {
        loc.setWorldRotation(yawRotation);
        TeraAssert.assertEquals(yawRotation, loc.getWorldRotation(new Quaternionf()), 0.0001f);
        TeraAssert.assertEquals(yawRotation, JomlUtil.from(loc.getLocalRotation()), 0.0001f);
    }

    @Test
    public void testSetWorldRotationWithRotatedParent() {
        LocationComponent parent = giveParent();
        parent.setLocalRotation(yawRotation);
        loc.setWorldRotation(yawPitch);
        TeraAssert.assertEquals(yawPitch, loc.getWorldRotation(new Quaternionf()), 0.0001f);
    }

    @Test
    public void testPositionMaintainedWhenAttachedToParent() {
        LocationComponent parent = new LocationComponent();
        EntityRef parentEntity = createFakeEntityWith(parent);
        parent.setWorldPosition(new Vector3f(1, 0, 0));

        loc.setWorldPosition(new Vector3f(2, 0, 0));
        Location.attachChild(parentEntity, entity);

        TeraAssert.assertEquals(new Vector3f(2, 0, 0), loc.getWorldPosition(new Vector3f()), 0.000001f);
    }

    @Test
    public void testPositionMaintainedWhenRemovedFromParent() {
        LocationComponent parent = new LocationComponent();
        EntityRef parentEntity = createFakeEntityWith(parent);
        parent.setWorldPosition(new Vector3f(1, 0, 0));

        loc.setWorldPosition(new Vector3f(2, 0, 0));
        Location.attachChild(parentEntity, entity);
        Location.removeChild(parentEntity, entity);

        TeraAssert.assertEquals(new Vector3f(2, 0, 0), loc.getWorldPosition(new Vector3f()), 0.000001f);
    }

    @Test
    public void testPositionMaintainedWhenParentDestroyed() {
        LocationComponent parent = new LocationComponent();
        EntityRef parentEntity = createFakeEntityWith(parent);
        parent.setWorldPosition(new Vector3f(1, 0, 0));

        loc.setWorldPosition(new Vector3f(2, 0, 0));
        Location.attachChild(parentEntity, entity);
        Location locationSystem = new Location();
        locationSystem.onDestroyed(BeforeRemoveComponent.newInstance(), parentEntity, parent);
        when(parentEntity.getComponent(LocationComponent.class)).thenReturn(null);
        when(parentEntity.exists()).thenReturn(false);

        TeraAssert.assertEquals(new Vector3f(2, 0, 0), loc.getWorldPosition(new Vector3f()), 0.000001f);
    }


    private LocationComponent giveParent() {
        LocationComponent parent = new LocationComponent();
        EntityRef parentEntity = createFakeEntityWith(parent);
        Location.attachChild(parentEntity, entity);
        return parent;
    }
}
