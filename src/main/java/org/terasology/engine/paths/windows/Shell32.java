package org.terasology.engine.paths.windows;

import com.sun.jna.Library;
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
