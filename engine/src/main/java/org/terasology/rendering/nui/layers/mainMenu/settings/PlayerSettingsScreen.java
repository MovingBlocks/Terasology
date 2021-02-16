/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.settings;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.i18n.TranslationProject;
import org.terasology.i18n.TranslationSystem;
import org.terasology.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.identity.storageServiceClient.StorageServiceWorkerStatus;
import org.terasology.nui.Color;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UICheckbox;
import org.terasology.nui.widgets.UIDropdownScrollable;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UISlider;
import org.terasology.nui.widgets.UIText;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.layers.mainMenu.StorageServiceLoginPopup;
import org.terasology.rendering.nui.layers.mainMenu.ThreeButtonPopup;
import org.terasology.utilities.Assets;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.terasology.identity.storageServiceClient.StatusMessageTranslator.getLocalizedButtonMessage;
import static org.terasology.identity.storageServiceClient.StatusMessageTranslator.getLocalizedStatusMessage;

public class PlayerSettingsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:PlayerMenuScreen");

    @In
    private Context context;
    @In
    private Config config;
    @In
    private TranslationSystem translationSystem;
    @In
    private StorageServiceWorker storageService;

    private final List<Color> colors = CieCamColors.L65C65;

    /**
     * Remove language x from this languagesExcluded table when it is ready for testing
     */
    private final Locale[] languagesExcluded =
            {Locale.forLanguageTag("zh"), // TODO: Chinese symbols not yet available
            Locale.forLanguageTag("hi"), // TODO: Hindi (Indian) symbols not yet available
            Locale.forLanguageTag("ar"), // TODO: Arabic symbols not yet available, no translated entries yet
            Locale.forLanguageTag("ko"), // TODO: Korean symbols not yet available
            Locale.forLanguageTag("fa")}; // TODO: Farsi (Persian) symbols not yet available

    private UIText nametext;
    private UISlider slider;
    private UILabel storageServiceStatus;
    private UIButton storageServiceAction;
    private UISlider heightSlider;
    private UISlider eyeHeightSlider;
    private UIImage img;
    private UICheckbox discordPresence;
    private UIDropdownScrollable<Locale> language;

    private StorageServiceWorkerStatus storageServiceWorkerStatus;

    @Override
    public void onOpened() {
        super.onOpened();
        if (nametext != null) {
            nametext.setText(config.getPlayer().getName());
        }
        if (slider != null) {
            Color color = config.getPlayer().getColor();
            slider.bindValue(new NotifyingBinding(findClosestIndex(color)));
        }
        if (heightSlider != null) {
            heightSlider.bindValue(new NotifyingBinding(config.getPlayer().getHeight()));
        }
        if (eyeHeightSlider != null) {
            eyeHeightSlider.bindValue(new NotifyingBinding(config.getPlayer().getEyeHeight()));
        }
        if (discordPresence != null) {
            discordPresence.setChecked(config.getPlayer().isDiscordPresence());
        }
        if (language != null) {
            language.setSelection(config.getSystem().getLocale());
        }
        updateImage();
    }

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        storageServiceStatus = find("storageServiceStatus", UILabel.class);
        storageServiceAction = find("storageServiceAction", UIButton.class);
        updateStorageServiceStatus();

        nametext = find("playername", UIText.class);
        if (nametext != null) {
            nametext.setTooltipDelay(0);
            nametext.bindTooltipString(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return validateScreen();
                }
            });
        }
        img = find("image", UIImage.class);
        if (img != null) {
            ResourceUrn uri = TextureUtil.getTextureUriForColor(Color.WHITE);
            Texture tex = Assets.get(uri, Texture.class).get();
            img.setImage(tex);
        }

        slider = find("tone", UISlider.class);
        if (slider != null) {
            slider.setIncrement(0.01f);
            Function<Object, String> constant = Functions.constant("  ");   // ensure a certain width
            slider.setLabelFunction(constant);
        }

        heightSlider = find("height", UISlider.class);
        if (heightSlider != null) {
            heightSlider.setMinimum(1.5f);
            heightSlider.setIncrement(0.1f);
            heightSlider.setRange(0.5f);
            heightSlider.setPrecision(1);
        }

        eyeHeightSlider = find("eye-height", UISlider.class);
        if (eyeHeightSlider != null) {
            eyeHeightSlider.setMinimum(0.5f);
            eyeHeightSlider.setIncrement(0.1f);
            eyeHeightSlider.setRange(1f);
            eyeHeightSlider.setPrecision(1);
        }

        discordPresence = find("discord-presence", UICheckbox.class);

        language = find("language", UIDropdownScrollable.class);
        if (language != null) {
            SimpleUri menuUri = new SimpleUri("engine:menu");
            TranslationProject menuProject = translationSystem.getProject(menuUri);
            List<Locale> locales = new ArrayList<>(menuProject.getAvailableLocales());
            for (Locale languageExcluded : languagesExcluded) {
                locales.remove(languageExcluded);
            }
            Collections.sort(locales, ((Object o1, Object o2) -> (o1.toString().compareTo(o2.toString()))));
            language.setOptions(Lists.newArrayList(locales));
            language.setVisibleOptions(5); // Set maximum number of options visible for scrolling
            language.setOptionRenderer(new LocaleRenderer(translationSystem));
        }

        WidgetUtil.trySubscribe(this, "close", button -> triggerBackAnimation());

        IdentityIOHelper identityIOHelper = new IdentityIOHelper(context);
        WidgetUtil.trySubscribe(this, "importIdentities", button -> identityIOHelper.importIdentities());
        WidgetUtil.trySubscribe(this, "exportIdentities", button -> identityIOHelper.exportIdentities());

        WidgetUtil.trySubscribe(this, "storageServiceAction", widget -> {
            if (storageService.getStatus() == StorageServiceWorkerStatus.LOGGED_IN) {
                ThreeButtonPopup logoutPopup = getManager().pushScreen(ThreeButtonPopup.ASSET_URI, ThreeButtonPopup.class);
                logoutPopup.setMessage(translationSystem.translate("${engine:menu#storage-service-log-out}"),
                        translationSystem.translate("${engine:menu#storage-service-log-out-popup}"));
                logoutPopup.setLeftButton(translationSystem.translate("${engine:menu#dialog-yes}"), () -> storageService.logout(true));
                logoutPopup.setCenterButton(translationSystem.translate("${engine:menu#dialog-no}"), () -> storageService.logout(false));
                logoutPopup.setRightButton(translationSystem.translate("${engine:menu#dialog-cancel}"), () -> { });
            } else if (storageService.getStatus() == StorageServiceWorkerStatus.LOGGED_OUT) {
                getManager().pushScreen(StorageServiceLoginPopup.ASSET_URI, StorageServiceLoginPopup.class);
            }
        });

        UIButton okButton = find("ok", UIButton.class);
        if (okButton != null) {
            okButton.subscribe(button -> {
                savePlayerSettings();
                triggerBackAnimation();
            });
            okButton.bindEnabled(new ReadOnlyBinding<Boolean>() {
                @Override
                public Boolean get() {
                    return Strings.isNullOrEmpty(validateScreen());
                }
            });
            okButton.setTooltipDelay(0);
            okButton.bindTooltipString(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return validateScreen();
                }
            });
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (storageService.getStatus() != storageServiceWorkerStatus) {
            updateStorageServiceStatus();
        }
    }

    private void updateStorageServiceStatus() {
        StorageServiceWorkerStatus stat = storageService.getStatus();
        storageServiceStatus.setText(getLocalizedStatusMessage(stat, translationSystem, storageService.getLoginName()));
        storageServiceAction.setText(getLocalizedButtonMessage(stat, translationSystem));
        storageServiceAction.setVisible(stat.isButtonEnabled());
        storageServiceWorkerStatus = stat;
    }

    private String validateScreen() {
        if (nametext != null) {
            if (Strings.isNullOrEmpty(nametext.getText()) || nametext.getText().trim().length() == 0) {
                return translationSystem.translate("${engine:menu#missing-name-message}");
            }
            if (nametext.getText().trim().length() > 100) {
                return translationSystem.translate("${engine:menu#validation-username-max-length}");
            }
        }
        return null;
    }

    private float findClosestIndex(Color color) {
        int best = 0;
        float minDist = Float.MAX_VALUE;
        for (int i = 0; i < colors.size(); i++) {
            Color other = colors.get(i);
            float dr = other.rf() - color.rf();
            float dg = other.gf() - color.gf();
            float db = other.bf() - color.bf();

            // there are certainly smarter ways to measure color distance,
            // but Euclidean distance is good enough for the purpose
            float dist = dr * dr + dg * dg + db * db;
            if (dist < minDist) {
                minDist = dist;
                best = i;
            }
        }

        float max = colors.size() - 1;
        return best / max;
    }

    private Color findClosestColor(float findex) {
        int index = DoubleMath.roundToInt(findex * (double) (colors.size() - 1), RoundingMode.HALF_UP);
        Color color = colors.get(index);
        return color;
    }

    private void updateImage() {
        Color color = getColor();
        if (img != null) {
            img.setTint(color);
        }
    }

    private Color getColor() {
        if (slider != null) {
            float index = slider.getValue();
            return findClosestColor(index);
        } else {
            return config.getPlayer().getColor();
        }
    }

    private Float getHeight() {
        if (heightSlider != null) {
            float index = heightSlider.getValue();
            return index;
        } else {
            return config.getPlayer().getHeight();
        }
    }

    private Float getEyeHeight() {
        if (eyeHeightSlider != null) {
            float index = eyeHeightSlider.getValue();
            return index;
        } else {
            return config.getPlayer().getEyeHeight();
        }
    }

    private void savePlayerSettings() {
        Color color = getColor();
        config.getPlayer().setColor(color);
        Float height = getHeight();
        config.getPlayer().setHeight(height);
        Float eyeHeight = getEyeHeight();
        config.getPlayer().setEyeHeight(eyeHeight);
        config.getPlayer().setDiscordPresence(discordPresence.isChecked());
        if (nametext != null) {
            config.getPlayer().setName(nametext.getText().trim());
            config.getPlayer().setHasEnteredUsername(true);
        }
        if (!config.getSystem().getLocale().equals(language.getSelection())) {
            config.getSystem().setLocale(language.getSelection());
            getManager().invalidate();
        }
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    /**
     * Calls update() in parent class when the slider value changes
     */
    private final class NotifyingBinding extends DefaultBinding<Float> {

        private NotifyingBinding(Float value) {
            super(value);
        }

        @Override
        public void set(Float v) {
            super.set(v);

            updateImage();
        }
    }
}
