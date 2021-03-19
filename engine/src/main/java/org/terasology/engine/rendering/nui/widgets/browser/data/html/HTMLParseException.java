// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html;

public class HTMLParseException extends RuntimeException {

    private static final long serialVersionUID = -4027037623808771428L;

    public HTMLParseException(String message) {
        super(message);
    }

    public HTMLParseException(Throwable cause) {
        super(cause);
    }
}
