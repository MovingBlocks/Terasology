package org.terasology.engine.paths.windows;

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
