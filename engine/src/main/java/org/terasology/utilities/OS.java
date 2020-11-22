/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.utilities;

/**
 * Utility for determinate OperationSystem(aka Platform)
 * Used instead {@link org.lwjgl.system.Platform}, because it invoke static of {@link org.lwjgl.system.Configuration} and load configs to static.
 * It stores System.getProperty and make writing lwjgl's path  - impossible.
 */
public enum OS {
    WINDOWS,
    MACOSX,
    LINUX;


    public static OS get() {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            return WINDOWS;
        } else if (osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("SunOS") || osName.startsWith("Unix")) {
            return LINUX;
        } else if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
            return MACOSX;
        } else {
            throw new IllegalStateException("Unknown platform: " + osName);
        }
    }
}
