/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.game.paths.windows;

import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32Util;
import com.sun.jna.ptr.PointerByReference;

/**
 * @author Immortius
 */
public class SavedGamesPathFinder {

    public static String findSavedGamesPath() {
        try {
            Guid.GUID pathGUID = Ole32Util.getGUIDFromString(Shell32.FOLDERID_SAVED_GAMES);
            PointerByReference outPath = new PointerByReference();
            int hResult = Shell32.INSTANCE.SHGetKnownFolderPath(pathGUID, 0, null, outPath);
            if (hResult == 0) {
                char[] pathChars = outPath.getValue().getCharArray(0, 255);
                int count = 0;
                while (count < pathChars.length && pathChars[count] != 0) {
                    count++;
                }
                String path = String.copyValueOf(pathChars, 0, count);
                Ole32.INSTANCE.CoTaskMemFree(outPath.getValue());
                return path;
            }
        } catch (UnsatisfiedLinkError e) {
            // This is expected behaviour on versions of Windows preceding Vista.
        }
        return null;
    }
}
