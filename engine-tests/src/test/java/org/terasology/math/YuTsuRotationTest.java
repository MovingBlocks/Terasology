// Copyright The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.math;

import org.junit.jupiter.api.Test;
import org.terasology.engine.math.Pitch;
import org.terasology.engine.math.Roll;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Side;
import org.terasology.engine.math.Yaw;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class YuTsuRotationTest {

    @Test
    public void testFlyweightSameInstanceForSameAngles() {
        Rotation a = Rotation.rotate(Yaw.CLOCKWISE_90, Pitch.CLOCKWISE_180, Roll.CLOCKWISE_270);
        Rotation b = Rotation.rotate(Yaw.CLOCKWISE_90, Pitch.CLOCKWISE_180, Roll.CLOCKWISE_270);

        // flyweight/cache: same yaw/pitch/roll should return the same cached instance
        assertSame(a, b);
    }

    @Test
    public void testGettersReturnConstructorAngles() {
        Rotation r = Rotation.rotate(Yaw.CLOCKWISE_180, Pitch.CLOCKWISE_90, Roll.CLOCKWISE_270);

        assertEquals(Yaw.CLOCKWISE_180, r.getYaw());
        assertEquals(Pitch.CLOCKWISE_90, r.getPitch());
        assertEquals(Roll.CLOCKWISE_270, r.getRoll());
    }

    @Test
    public void testEqualsBranchesAndHashCode() {
        Rotation r1 = Rotation.rotate(Yaw.CLOCKWISE_90, Pitch.NONE, Roll.NONE);

        // Branch 1: this == obj
        assertTrue(r1.equals(r1));

        // Branch 2: obj instanceof Rotation and same index (same rotation), but different reference possible in theory
        // In this implementation it's cached so reference is same; still exercises the instanceof Rotation path.
        Rotation r2 = Rotation.rotate(Yaw.CLOCKWISE_90, Pitch.NONE, Roll.NONE);
        assertEquals(r1, r2);

        // hashCode returns index, so equal objects should have same hashCode
        assertEquals(r1.hashCode(), r2.hashCode());

        // Branch 3: obj not a Rotation
        assertFalse(r1.equals("not-a-rotation"));
        assertFalse(r1.equals(null));
    }

    @Test
    public void testFindReverseNullGuard() {
        // Preconditions.checkNotNull(rotation) should throw NPE for null input
        assertThrows(NullPointerException.class, () -> Rotation.findReverse(null));
    }

    @Test
    public void testHorizontalRotationsContentAndOrder() {
        List<Rotation> horizontals = Rotation.horizontalRotations();

        // expected size and order: NONE, 90, 180, 270 yaw rotations (pitch/roll NONE)
        assertEquals(4, horizontals.size());

        assertSame(Rotation.none(), horizontals.get(0));
        assertSame(Rotation.rotate(Yaw.CLOCKWISE_90), horizontals.get(1));
        assertSame(Rotation.rotate(Yaw.CLOCKWISE_180), horizontals.get(2));
        assertSame(Rotation.rotate(Yaw.CLOCKWISE_270), horizontals.get(3));
    }

    @Test
    public void testRotateOverloadsReturnExpectedInstance() {
        // These calls exist as different overloads; we verify they map to the same canonical rotation.
        assertSame(Rotation.rotate(Yaw.NONE, Pitch.NONE, Roll.NONE), Rotation.none());

        assertSame(Rotation.rotate(Yaw.CLOCKWISE_90, Pitch.NONE, Roll.NONE), Rotation.rotate(Yaw.CLOCKWISE_90));
        assertSame(Rotation.rotate(Yaw.NONE, Pitch.CLOCKWISE_90, Roll.NONE), Rotation.rotate(Pitch.CLOCKWISE_90));
        assertSame(Rotation.rotate(Yaw.NONE, Pitch.NONE, Roll.CLOCKWISE_90), Rotation.rotate(Roll.CLOCKWISE_90));

        assertSame(
                Rotation.rotate(Yaw.CLOCKWISE_180, Pitch.CLOCKWISE_90, Roll.NONE),
                Rotation.rotate(Yaw.CLOCKWISE_180, Pitch.CLOCKWISE_90)
        );

        assertSame(
                Rotation.rotate(Yaw.NONE, Pitch.CLOCKWISE_180, Roll.CLOCKWISE_270),
                Rotation.rotate(Pitch.CLOCKWISE_180, Roll.CLOCKWISE_270)
        );

        assertSame(
                Rotation.rotate(Yaw.CLOCKWISE_270, Pitch.NONE, Roll.CLOCKWISE_180),
                Rotation.rotate(Yaw.CLOCKWISE_270, Roll.CLOCKWISE_180)
        );
    }

    @Test
    public void testRotateAppliesRollThenPitchThenYawOrder() {
        // Rotation.rotate(side) is implemented as: roll -> pitch -> yaw (in that order)
        Rotation r = Rotation.rotate(Yaw.CLOCKWISE_90, Pitch.CLOCKWISE_90, Roll.CLOCKWISE_90);

        Side input = Side.FRONT;

        Side expected = input
                .rollClockwise(r.getRoll().getIncrements())
                .pitchClockwise(r.getPitch().getIncrements())
                .yawClockwise(r.getYaw().getIncrements());

        assertEquals(expected, r.rotate(input));
    }

    @Test
    public void testValuesAreUniqueByTransformationFrontTop() {
        // Rotation.values() should contain only unique transformations (24),
        // even though allValues() includes 64 yaw/pitch/roll combinations.
        Set<String> seen = new HashSet<>();

        for (Rotation r : Rotation.values()) {
            Side front = r.rotate(Side.FRONT);
            Side top = r.rotate(Side.TOP);

            // a rotation is uniquely determined by where FRONT and TOP end up
            String key = front.name() + "|" + top.name();
            assertFalse(seen.contains(key), "Duplicate transformation found in Rotation.values(): " + key);
            seen.add(key);
        }

        assertEquals(24, seen.size());
    }

    @Test
    public void testAllValuesMayContainDuplicatesByTransformation() {
        // allValues() includes 64 combinations; some are duplicates by transformation.
        // We don't assert the exact number of duplicates (implementation detail),
        // but we do prove that the set of transformations is <= 24.
        Set<String> seen = new HashSet<>();

        for (Rotation r : Rotation.allValues()) {
            Side front = r.rotate(Side.FRONT);
            Side top = r.rotate(Side.TOP);
            seen.add(front.name() + "|" + top.name());
        }

        assertTrue(seen.size() <= 24);
        assertEquals(24, seen.size(), "All transformations should still collapse to 24 unique mappings");
    }
}
