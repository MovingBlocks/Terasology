// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

import java.util.Locale;


public class NUIEditorConfig {
    private boolean disableIcons;
    private boolean disableAutosave;
    private Locale alternativeLocale;

    public boolean isDisableIcons() {
        return disableIcons;
    }

    public void setDisableIcons(boolean disableIcons) {
        this.disableIcons = disableIcons;
    }

    public Locale getAlternativeLocale() {
        return alternativeLocale;
    }

    public void setAlternativeLocale(Locale alternativeLocale) {
        this.alternativeLocale = alternativeLocale;
    }

    public boolean isDisableAutosave() {
        return disableAutosave;
    }

    public void setDisableAutosave(boolean disableAutosave) {
        this.disableAutosave = disableAutosave;
    }
}
