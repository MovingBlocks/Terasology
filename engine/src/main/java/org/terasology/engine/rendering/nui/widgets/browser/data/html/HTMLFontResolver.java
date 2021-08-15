// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html;

import org.terasology.nui.asset.font.Font;

@FunctionalInterface
public interface HTMLFontResolver {
    Font getFont(String name, boolean bold);
}
