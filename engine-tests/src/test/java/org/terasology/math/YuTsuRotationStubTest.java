// Copyright The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.math;

import org.junit.jupiter.api.Test;
import org.terasology.engine.math.Side;

import static org.junit.jupiter.api.Assertions.assertEquals;

// part 5: stub implementation by Yu-Tsu Chang
public class YuTsuRotationStubTest {

    interface Rotatable {
        Side rotate(Side side);
    }

    static class StubRotatable implements Rotatable {
        @Override
        public Side rotate(Side side) {
            return Side.TOP; // deterministic stub behavior
        }
    }

    static class SideTransformer {
        private final Rotatable rotatable;

        SideTransformer(Rotatable rotatable) {
            this.rotatable = rotatable;
        }

        Side transform(Side input) {
            return rotatable.rotate(input);
        }
    }

    @Test
    public void testStubbedRotationUsedInsteadOfRealLogic() {
        Rotatable stub = new StubRotatable();
        SideTransformer transformer = new SideTransformer(stub);

        assertEquals(Side.TOP, transformer.transform(Side.FRONT));
        assertEquals(Side.TOP, transformer.transform(Side.LEFT));
    }
}