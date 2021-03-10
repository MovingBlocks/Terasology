// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic.list;

import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;

@FunctionalInterface
public interface ListDecorator {
    ParagraphData wrapParagraph(ParagraphData paragraphData);
}
