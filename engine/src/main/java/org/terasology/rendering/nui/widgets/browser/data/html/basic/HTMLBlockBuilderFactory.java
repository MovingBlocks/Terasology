// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic;

import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLBlockBuilder;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLFontResolver;
import org.xml.sax.Attributes;

@FunctionalInterface
public interface HTMLBlockBuilderFactory {
    HTMLBlockBuilder create(HTMLFontResolver htmlFontResolver, Attributes attributes);
}
