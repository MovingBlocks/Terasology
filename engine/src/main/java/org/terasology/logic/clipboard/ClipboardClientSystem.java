/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.clipboard;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.Share;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

@RegisterSystem(RegisterMode.CLIENT)
@Share(ClipboardManager.class)
public class ClipboardClientSystem extends BaseComponentSystem implements ClipboardManager {
    @Override
    public String getClipboardContentsAsString() {
        return AccessController.doPrivileged(
                (PrivilegedAction<String>) () -> {
                    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

                    try {
                        if (t != null) {
                            return (String) t.getTransferData(DataFlavor.stringFlavor);
                        }
                    } catch (UnsupportedFlavorException | IOException e) {
                        return null;
                    }
                    return null;
                });
    }

    @Override
    public boolean setClipboardContents(String value) {
        return AccessController.doPrivileged(
                (PrivilegedAction<Boolean>) () -> {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    try {
                        clipboard.setContents(new StringSelection(value), null);
                    } catch (IllegalStateException exp) {
                        // Some OSs might lock out access to clipboard, if another application uses it.
                        // In this case, this exception is thrown
                        return false;
                    }
                    return true;
                });
    }
}
