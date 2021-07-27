// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.clipboard;

import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.Share;

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
