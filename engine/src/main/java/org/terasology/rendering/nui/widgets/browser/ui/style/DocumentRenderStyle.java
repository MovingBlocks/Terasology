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
package org.terasology.rendering.nui.widgets.browser.ui.style;

import org.terasology.nui.Color;

public interface DocumentRenderStyle extends ParagraphRenderStyle {
    default ContainerInteger getDocumentMarginTop() {
        return null;
    }

    default ContainerInteger getDocumentMarginBottom() {
        return null;
    }

    default ContainerInteger getDocumentMarginLeft() {
        return null;
    }

    default ContainerInteger getDocumentMarginRight() {
        return null;
    }

    default Color getBackgroundColor() {
        return null;
    }
}
