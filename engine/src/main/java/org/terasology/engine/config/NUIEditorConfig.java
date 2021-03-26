/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.engine.config;

import java.util.Locale;

/**
 *
 */
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
