// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html;

import org.xml.sax.Attributes;

@FunctionalInterface
public interface HTMLDocumentBuilderFactory {
    HTMLDocumentBuilder create(Attributes attributes);
}
