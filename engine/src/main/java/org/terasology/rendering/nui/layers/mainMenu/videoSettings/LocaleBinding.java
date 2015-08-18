/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.mainMenu.videoSettings;

import java.util.Locale;

import org.terasology.config.SystemConfig;
import org.terasology.context.Context;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.Binding;

/**
 * A Binding for {@link Locale} that updates the I18N-system.
 */
public class LocaleBinding implements Binding<Locale> {

    private SystemConfig config;
    private Context context;

    public LocaleBinding(Context context, SystemConfig config) {
        this.context = context;
        this.config = config;
    }

    @Override
    public Locale get() {
        return config.getLocale();
    }

    @Override
    public void set(Locale value) {
        config.setLocale(value);
        context.get(NUIManager.class).invalidate();
    }
}
