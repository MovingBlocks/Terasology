package org.terasology.engine.paths.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * @author Immortius
 */
public interface Ole32 extends StdCallLibrary{

    Ole32 INSTANCE = (Ole32) Native.loadLibrary(
            "Ole32", Ole32.class, W32APIOptions.UNICODE_OPTIONS);

    void CoTaskMemFree(Pointer pv);
}
