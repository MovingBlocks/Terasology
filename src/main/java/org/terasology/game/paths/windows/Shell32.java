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

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Access to the Windows Shell32 library, for the purpose of looking up special directory paths (Windows Vista/7 only)
 *
 * @author Immortius
 */
public interface Shell32 extends StdCallLibrary {

    public static final String FOLDERID_SAVED_GAMES = "{4C5C32FF-BB9D-43b0-B5B4-2D72E54EAAA4}";

    static Shell32 INSTANCE = (Shell32) Native.loadLibrary("shell32",
            Shell32.class, W32APIOptions.UNICODE_OPTIONS);

    public int SHGetKnownFolderPath(Guid.GUID rfid, int dwFlags, WinNT.HANDLE hToken,
                                    PointerByReference pszPath);
}
