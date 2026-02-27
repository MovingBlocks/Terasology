// Copyright The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui;

import org.junit.jupiter.api.Test;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystem;

import static org.mockito.Mockito.*;

public class CoreScreenLayerTestableDesignTest {

    static class TestScreenLayer extends CoreScreenLayer {

        TestScreenLayer(String id, MenuAnimationSystem animationSystem) {
            super(id, animationSystem);
        }

        @Override
        public void initialise() {
            // no-op for testing
        }
    }

    @Test
    public void onOpened_shouldTriggerAnimation() {

        MenuAnimationSystem animation = mock(MenuAnimationSystem.class);

        TestScreenLayer screen =
                new TestScreenLayer("test", animation);

        screen.onOpened();

        verify(animation, times(1)).triggerFromPrev();
    }
}