// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import org.junit.jupiter.api.Test;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.core.subsystem.DisplayDevice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScreenGrabberTest {

    private static ScreenGrabber screenGrabber(boolean debugEnabled) {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.debugEnabled.set(debugEnabled);
        Config config = new Config();
        config.loadDefaults();
        return new ScreenGrabber(config, systemConfig);
    }

    @Test
    public void saveScreenshotLeavesCompositedCapturePendingWhenDebugEnabled() {
        ScreenGrabber screenGrabber = screenGrabber(true);
        screenGrabber.takeScreenshot();
        screenGrabber.saveScreenshot();

        assertTrue(screenGrabber.isTakingScreenshot());
        assertTrue(screenGrabber.capturesCompositedFramebuffer());
    }

    @Test
    public void screenshotReadsSceneBufferWhenDebugDisabled() {
        ScreenGrabber screenGrabber = screenGrabber(false);
        screenGrabber.takeScreenshot();

        assertTrue(screenGrabber.isTakingScreenshot());
        assertFalse(screenGrabber.capturesCompositedFramebuffer());
    }

    @Test
    public void noCompositedCaptureWithoutPendingScreenshot() {
        assertFalse(screenGrabber(true).capturesCompositedFramebuffer());
    }

    @Test
    public void headlessCompositedCaptureClearsPendingScreenshot() {
        ScreenGrabber screenGrabber = screenGrabber(true);
        screenGrabber.takeScreenshot();
        DisplayDevice display = mock(DisplayDevice.class);
        when(display.isHeadless()).thenReturn(true);

        screenGrabber.saveCompositedScreenshot(display);

        assertFalse(screenGrabber.isTakingScreenshot());
        assertFalse(screenGrabber.capturesCompositedFramebuffer());
    }
}
