// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings;

/**
 * Used to determine the display mode.
 * <ul>
 * <li>{@link #FULLSCREEN} - Sets the screen to fullscreen and prevents the mouse from moving out of the game.</li>
 * <li>{@link #WINDOWED_FULLSCREEN} - Sets the screen to borderless windowed fullscreen allowing the player to move the
 * mouse out of the game without minimizing.</li>
 * <li>{@link #WINDOWED} - Sets the screen to windowed with borders allowing the player to resize the window.</li>
 * </ul>
 */
public enum DisplayModeSetting {

    /**
     * Sets the screen to fullscreen and prevents the mouse from moving out of the game.
     */
    FULLSCREEN("${engine:menu#video-fullscreen}", true),

    /**
     * Sets the screen to borderless windowed fullscreen allowing the player to move the
     * mouse out of the game without minimizing.
     */
    WINDOWED_FULLSCREEN("${engine:menu#video-windowed-fullscreen}", false),

    /**
     * Sets the screen to windowed with borders allowing the player to resize the window.
     */
    WINDOWED("${engine:menu#video-windowed}", false);

    private String displayName;

    private boolean current;

    DisplayModeSetting(String displayName, boolean current) {
        this.displayName = displayName;
        this.current = current;
    }

    public DisplayModeSetting getCurrent() {
        for (DisplayModeSetting setting : values()) {
            if (setting.current) {
                return setting;
            }
        }
        return DisplayModeSetting.FULLSCREEN;
    }

    public boolean isCurrent() {
       return this.current;
    }

    public void setCurrent(boolean current) {
        for (DisplayModeSetting setting : values()) {
            setting.current = false;
        }
        this.current = current;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
