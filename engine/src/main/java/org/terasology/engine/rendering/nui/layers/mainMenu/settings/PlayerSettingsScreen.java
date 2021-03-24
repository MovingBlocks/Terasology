// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.settings;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.math.DoubleMath;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.config.PlayerConfig;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorkerStatus;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureUtil;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.StorageServiceLoginPopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.ThreeButtonPopup;
import org.terasology.engine.utilities.Assets;
import org.terasology.nui.Color;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UISlider;

import java.math.RoundingMode;
import java.util.List;

import static org.terasology.engine.identity.storageServiceClient.StatusMessageTranslator.getLocalizedButtonMessage;
import static org.terasology.engine.identity.storageServiceClient.StatusMessageTranslator.getLocalizedStatusMessage;

public class PlayerSettingsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:PlayerMenuScreen");

    @In
    private Context context;
    @In
    private PlayerConfig config;
    @In
    private SystemConfig systemConfig;
    @In
    private TranslationSystem translationSystem;
    @In
    private StorageServiceWorker storageService;

    private final List<Color> colors = CieCamColors.L65C65;

    private UISlider slider;
    private UILabel storageServiceStatus;
    private UIButton storageServiceAction;
    private UIImage img;

    private StorageServiceWorkerStatus storageServiceWorkerStatus;

    @Override
    public void onOpened() {
        super.onOpened();
    }


    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        storageServiceStatus = find("storageServiceStatus", UILabel.class);
        storageServiceAction = find("storageServiceAction", UIButton.class);
        updateStorageServiceStatus();

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


    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
