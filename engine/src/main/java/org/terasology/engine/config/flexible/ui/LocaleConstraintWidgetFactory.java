// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config.flexible.ui;

import com.google.common.collect.Lists;
import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.constraints.LocaleConstraint;
import org.terasology.engine.i18n.TranslationProject;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.rendering.nui.layers.mainMenu.settings.LocaleRenderer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.UIDropdownScrollable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class LocaleConstraintWidgetFactory extends ConstraintWidgetFactory<Locale, LocaleConstraint> {

    /**
     * Remove language x from this languagesExcluded table when it is ready for testing
     */
    private final Locale[] languagesExcluded =
            {Locale.forLanguageTag("zh"), // TODO: Chinese symbols not yet available
                    Locale.forLanguageTag("hi"), // TODO: Hindi (Indian) symbols not yet available
                    Locale.forLanguageTag("ar"), // TODO: Arabic symbols not yet available, no translated entries yet
                    Locale.forLanguageTag("ko"), // TODO: Korean symbols not yet available
                    Locale.forLanguageTag("fa")}; // TODO: Farsi (Persian) symbols not yet available

    
    private final TranslationSystem translationSystem;

    public LocaleConstraintWidgetFactory(TranslationSystem translationSystem) {
        this.translationSystem = translationSystem;
    }

    @Override
    protected Optional<UIWidget> buildWidget() {
        Setting<Locale> setting = getSetting();
        
        Binding<Locale> binding = new Binding<Locale>() {
            @Override
            public Locale get() {
                return setting.get();
            }

            @Override
            public void set(Locale value) {
                setting.set(value);
            }
        };

        UIDropdownScrollable<Locale> dropdownScrollable = new UIDropdownScrollable<>();
        ResourceUrn menuUrn = new ResourceUrn("engine:menu");
        TranslationProject menuProject = translationSystem.getProject(menuUrn);
        List<Locale> locales = new ArrayList<>(menuProject.getAvailableLocales());
        for (Locale languageExcluded : languagesExcluded) {
            locales.remove(languageExcluded);
        }
        Collections.sort(locales, (Comparator.comparing((Function<Object, String>) Object::toString)));
        dropdownScrollable.setOptions(Lists.newArrayList(locales));
        dropdownScrollable.setVisibleOptions(5); // Set maximum number of options visible for scrolling
        dropdownScrollable.bindSelection(binding);
       
        dropdownScrollable.setOptionRenderer(new LocaleRenderer(translationSystem));

        return Optional.of(dropdownScrollable);
    }
}
