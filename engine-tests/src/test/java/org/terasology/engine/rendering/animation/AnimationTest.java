// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.animation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link Animation} class
 */
public class AnimationTest {

    private static final float EPS = 0.001f;

    private Container c = new Container();

    @Test
    public void testStartEndOnce() {
        Animation ani = once();
        AnimationListener listener = Mockito.mock(AnimationListener.class);
        ani.addListener(listener);
        ani.start();
        ani.update(2.5f);
        Mockito.verify(listener, Mockito.times(1)).onStart();
        Mockito.verify(listener, Mockito.times(1)).onEnd();
    }

    @Test
    public void testStartEndValuesOnce() {
        Animation ani = once().start();
        assertEquals(0.0f, c.value, 0.0f);
        ani.update(2.5f);
        assertEquals(1.0f, c.value, 0.0f);
    }

    @Test
    public void testStartEndValuesInfinite() {
        Animation ani = infinite().start();
        assertEquals(0.0f, c.value, 0.0f);
        ani.update(2.5f);
        assertEquals(.25f, c.value, EPS); // (2.5 % 2) / 2
    }

    @Test
    public void testOverflowInfinite() {
        Animation ani = infinite().start();
        assertEquals(0.0f, c.value, 0.0f);
        ani.update(112.5f);
        assertEquals(.25f, c.value, EPS); // (112.5 % 2) / 2
    }

    @Test
    public void testUpdates() {
        Animation ani = once();
        ani.update(2.5f);  // ignored
        assertEquals(0f, c.value, 0f);
        ani.start();
        ani.update(0.5f);
        assertEquals(0.25f, c.value, EPS); // 0.5 / 2
        ani.pause();
        ani.update(0.5f);  // ignored
        assertEquals(0.25f, c.value, EPS); // same
        ani.resume();
        ani.update(1.0f);
        assertEquals(0.75f, c.value, EPS); // 1.5 / 2
        ani.update(1.0f);
        assertEquals(1.00f, c.value, 0f);  // 2.5 / 2 -> capped
        ani.update(1.0f);  // ignored
        assertEquals(1.00f, c.value, 0f);  // same
    }

    @Test
    public void testStartEndOnceReverse() {
        Animation ani = once().setReverseMode();
        AnimationListener listener = Mockito.mock(AnimationListener.class);
        ani.addListener(listener);
        ani.start();
        ani.update(2.5f);
        Mockito.verify(listener, Mockito.times(1)).onStart();
        Mockito.verify(listener, Mockito.times(1)).onEnd();
    }

    @Test
    public void testUpdatesReverse() {
        Animation ani = once().setReverseMode();
        ani.update(2.5f);  // ignored
        assertEquals(0f, c.value, 0f);
        ani.start();
        ani.update(0.5f);
        assertEquals(0.75f, c.value, EPS); // 1 - 0.5 / 2
        ani.pause();
        ani.update(0.5f);  // ignored
        assertEquals(0.75f, c.value, EPS); // same
        ani.resume();
        ani.update(1.0f);
        assertEquals(0.25f, c.value, EPS); // 1 - 1.5 / 2
        ani.update(1.0f);
        assertEquals(0.00f, c.value, 0f);  // 1 - 2.5 / 2 -> capped
        ani.update(1.0f);  // ignored
        assertEquals(0.00f, c.value, 0f);  // same
    }

    private Animation once() {
        return Animation.once(v -> c.value = v, 2f, TimeModifiers.linear());
    }

    private Animation infinite() {
        return Animation.infinite(v -> c.value = v, 2f, TimeModifiers.linear());
    }

    private static class Container {
        float value;
    }
}
