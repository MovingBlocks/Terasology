// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.clipboard;

import org.terasology.context.annotation.API;

@API // Temporarily in base permission set, until fixed - (permissionSet = "clipboard")
public interface ClipboardManager {
    String getClipboardContentsAsString();

    boolean setClipboardContents(String value);
}
