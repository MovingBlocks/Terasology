// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data;

import org.terasology.engine.rendering.nui.widgets.browser.ui.style.DocumentRenderStyle;

import java.util.Collection;

public interface DocumentData {
    DocumentRenderStyle getDocumentRenderStyle();

    Collection<ParagraphData> getParagraphs();
}
