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
package org.terasology.rendering.nui.editor.layers;

import com.google.common.collect.Lists;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.i18n.TranslationProject;
import org.terasology.i18n.TranslationSystem;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.layers.mainMenu.settings.LocaleRenderer;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * The screen to modify NUI screen/skin editor settings.
 */
@SuppressWarnings("unchecked")
public class NUIEditorSettingsScreen extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:nuiEditorSettingsScreen");

    @In
    private Config config;

    @In
    private TranslationSystem translationSystem;

    private UIDropdownScrollable<Locale> alternativeLocale;

    @Override
    public void initialise() {
        WidgetUtil.tryBindCheckbox(this, "disableAutosave", BindHelper.bindBeanProperty("disableAutosave", config.getNuiEditor(), Boolean.TYPE));
        WidgetUtil.tryBindCheckbox(this, "disableIcons", BindHelper.bindBeanProperty("disableIcons", config.getNuiEditor(), Boolean.TYPE));
        WidgetUtil.trySubscribe(this, "close", button -> getManager().closeScreen(ASSET_URI));

        alternativeLocale = find("alternativeLocale", UIDropdownScrollable.class);
        if (alternativeLocale != null) {
            // Build the list of available locales and set the dropdown's options to them.
            TranslationProject menuProject = translationSystem.getProject(new SimpleUri("engine:menu"));
            List<Locale> locales = new ArrayList<>(menuProject.getAvailableLocales());
            Collections.sort(locales, ((Object o1, Object o2) -> (o1.toString().compareTo(o2.toString()))));
            alternativeLocale.setOptions(Lists.newArrayList(locales));
            alternativeLocale.setVisibleOptions(5);
            alternativeLocale.setOptionRenderer(new LocaleRenderer(translationSystem));

            // If an alternative locale has been previously selected, select it; otherwise select the system locale.
            if (config.getNuiEditor().getAlternativeLocale() != null) {
                alternativeLocale.setSelection(config.getNuiEditor().getAlternativeLocale());
            } else {
                alternativeLocale.setSelection(config.getSystem().getLocale());
            }
        }
    }

    @Override
    public void onClosed() {
        if (!alternativeLocale.getSelection().equals(config.getNuiEditor().getAlternativeLocale())) {
            config.getNuiEditor().setAlternativeLocale(alternativeLocale.getSelection());
        }

        if (getManager().isOpen(NUIEditorScreen.ASSET_URI)) {
            ((NUIEditorScreen) getManager().getScreen(NUIEditorScreen.ASSET_URI)).updateConfig();
        }
        if (getManager().isOpen(NUISkinEditorScreen.ASSET_URI)) {
            ((NUISkinEditorScreen) getManager().getScreen(NUISkinEditorScreen.ASSET_URI)).updateConfig();
        }
    }
}
