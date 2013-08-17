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

package org.terasology.engine.paths.windows;

import com.google.common.collect.ImmutableMap;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32Util;
import com.sun.jna.ptr.PointerByReference;

import java.util.Map;

/**
 * @author Immortius
 */
public class SavedGamesPathFinder {

    private static final Map<String, Integer> FOLDER_ID_TO_CSIDL = ImmutableMap.of(Shell32.FOLDERID_DOCUMENTS, 0x0005);

    public static String findSavedGamesPath() {
        return findWindowsPath(Shell32.FOLDERID_SAVED_GAMES);
    }

    public static String findDocumentsPath() {
        return findWindowsPath(Shell32.FOLDERID_DOCUMENTS);
    }

    private static String findWindowsPath(String folderId) {
        try {
            Guid.GUID pathGUID = Ole32Util.getGUIDFromString(folderId);
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
            return findWindowsPathFallback(folderId);
        }
        return null;
    }

    private static String findWindowsPathFallback(String folderId) {
        Integer csidlId = FOLDER_ID_TO_CSIDL.get(folderId);
        if (csidlId == null) {
            return null;
        }
        try {
            char[] outPath = new char[255];
            int hResult = Shell32.INSTANCE.SHGetFolderPath(null, csidlId, null, 0, outPath);
            if (hResult == 0) {
                int end = 0;
                while (end < outPath.length && outPath[end] != 0) {
                    end++;
                }
                return new String(outPath, 0, end);
            }
        } catch (UnsatisfiedLinkError e) {
            System.out.println("SHGetFolderPath not available");
        }
        return null;
    }
}
