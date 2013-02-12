/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.rendering.gui.dialogs;

import com.google.common.collect.Lists;
import org.newdawn.slick.Color;
import org.terasology.config.ModConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.SelectionListener;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIDialog;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIList;
import org.terasology.rendering.gui.widgets.UIListItem;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Immortius
 */
public class UIDialogMods extends UIDialog {
    private static final Color ACTIVE_TEXT_COLOR = new Color(255, 220, 0);
    private static final Color ACTIVE_SELECTED_TEXT_COLOR = new Color(255, 255, 0);
    private static final Color INACTIVE_TEXT_COLOR = new Color(180, 180, 180);
    private static final Color INACTIVE_SELECTED_TEXT_COLOR = new Color(255, 255, 255);
    private static final String ACTIVATE_TEXT = "Activate";
    private static final String DEACTIVATE_TEXT = "Deactivate";
    private static final Color BLACK = new Color(0, 0, 0);

    private ModConfig modConfig;
    private ModConfig originalModConfig;
    private UIList modList;
    private UIButton toggleButton;
    private UILabel nameLabel;
    private UILabel descriptionLabel;
    private UIComposite detailPanel;
    private ModManager modManager = CoreRegistry.get(ModManager.class);

    public UIDialogMods(ModConfig modConfig) {
        super(new Vector2f(640f, 480f));
        this.modConfig = new ModConfig();
        this.originalModConfig = modConfig;
        this.modConfig.copy(modConfig);
        populateModList();
        setTitle("Select Mods...");

    }

    private void populateModList() {


        List<Mod> mods = Lists.newArrayList(modManager.getMods());
        Collections.sort(mods, new Comparator<Mod>() {
            @Override
            public int compare(Mod o1, Mod o2) {
                return o1.getModInfo().getDisplayName().compareTo(o2.getModInfo().getDisplayName());
            }
        });

        for (Mod mod : mods) {
            UIListItem item = new UIListItem(mod.getModInfo().getDisplayName(), mod);
            item.setPadding(new Vector4f(2f, 5f, 2f, 5f));
            if (modConfig.hasMod(mod.getModInfo().getId())) {
                item.setTextColor(ACTIVE_TEXT_COLOR);
                item.setTextSelectionColor(ACTIVE_SELECTED_TEXT_COLOR);
            } else {
                item.setTextColor(INACTIVE_TEXT_COLOR);
                item.setTextSelectionColor(INACTIVE_SELECTED_TEXT_COLOR);
            }
            modList.addItem(item);
        }
        modList.addSelectionListener(new SelectionListener() {
            @Override
            public void changed(UIDisplayElement element) {
                Mod mod = (Mod) modList.getSelection().getValue();
                detailPanel.setVisible(true);
                nameLabel.setText(mod.getModInfo().getDisplayName());
                descriptionLabel.setText(mod.getModInfo().getDescription());
                boolean active = modConfig.hasMod(mod.getModInfo().getId());
                if (active) {
                    toggleButton.getLabel().setText(DEACTIVATE_TEXT);
                } else {
                    toggleButton.getLabel().setText(ACTIVATE_TEXT);
                }
                toggleButton.setVisible(!mod.getModInfo().getId().equals("core"));
            }
        });

        toggleButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                Mod selectedMod = (Mod) modList.getSelection().getValue();
                if (modConfig.hasMod(selectedMod.getModInfo().getId())) {
                    deactivateMod(selectedMod);
                    toggleButton.getLabel().setText(ACTIVATE_TEXT);
                } else {
                    activateMod(selectedMod);
                    toggleButton.getLabel().setText(DEACTIVATE_TEXT);
                }
                refreshListItemActivation();
            }
        });
    }

    private void refreshListItemActivation() {
        for (UIListItem item : modList.getItems()) {
            Mod mod = (Mod) item.getValue();
            if (modConfig.hasMod(mod.getModInfo().getId())) {
                item.setTextColor(ACTIVE_TEXT_COLOR);
                item.setTextSelectionColor(ACTIVE_SELECTED_TEXT_COLOR);
            } else {
                item.setTextColor(INACTIVE_TEXT_COLOR);
                item.setTextSelectionColor(INACTIVE_SELECTED_TEXT_COLOR);
            }
        }
    }

    private void deactivateMod(Mod mod) {
        modConfig.removeMod(mod.getModInfo().getId());
        for (String activeModName : Lists.newArrayList(modConfig.listMods())) {
            Mod activeMod = modManager.getMod(activeModName);
            if (activeMod != null && activeMod.getModInfo().getDependencies().contains(mod.getModInfo().getId())) {
                deactivateMod(activeMod);
            }
        }
    }

    private void activateMod(Mod mod) {
        modConfig.addMod(mod.getModInfo().getId());
        for (String dependencyName : mod.getModInfo().getDependencies()) {
            Mod dependency = modManager.getMod(dependencyName);
            if (dependency != null) {
                activateMod(dependency);
            }
        }
    }



    @Override
    protected void createDialogArea(UIDisplayContainer parent) {

        UIComposite modPanel = new UIComposite();
        modPanel.setPosition(new Vector2f(15, 50f));
        modPanel.setSize(new Vector2f(320f, 400f));
        modPanel.setVisible(true);

        detailPanel = new UIComposite();
        detailPanel.setPosition(new Vector2f(340, 50));
        detailPanel.setSize(new Vector2f(320, 400));
        detailPanel.setVisible(true);

        modList = new UIList();
        modList.setVisible(true);
        modList.setSize(new Vector2f(300f, 350f));
        modList.setPadding(new Vector4f(10f, 5f, 10f, 5f));
        modList.setBackgroundImage("engine:gui_menu", new Vector2f(264f, 18f), new Vector2f(159f, 63f));
        modList.setBorderImage("engine:gui_menu", new Vector2f(256f, 0f), new Vector2f(175f, 88f), new Vector4f(16f, 7f, 7f, 7f));

        modPanel.addDisplayElement(modList);
        modPanel.layout();

        UILabel label = new UILabel("Name:");
        label.setVisible(true);
        label.setPosition(new Vector2f(0, 0));
        label.setColor(BLACK);
        detailPanel.addDisplayElement(label);
        nameLabel = new UILabel();
        nameLabel.setVisible(true);
        nameLabel.setColor(BLACK);
        nameLabel.setTextShadow(false);
        nameLabel.setPosition(new Vector2f(label.getPosition().x + label.getSize().x + 10f, label.getPosition().y));
        detailPanel.addDisplayElement(nameLabel);
        label = new UILabel("Description:");
        label.setVisible(true);
        label.setPosition(new Vector2f(0, nameLabel.getPosition().y + nameLabel.getSize().y + 8f));
        label.setColor(BLACK);
        detailPanel.addDisplayElement(label);
        descriptionLabel = new UILabel();
        descriptionLabel.setColor(BLACK);
        descriptionLabel.setVisible(true);
        descriptionLabel.setWrap(true);
        descriptionLabel.setTextShadow(false);
        descriptionLabel.setPosition(new Vector2f(0, label.getPosition().y + label.getSize().y + 8f));
        descriptionLabel.setSize(new Vector2f(300, descriptionLabel.getSize().y));
        detailPanel.addDisplayElement(descriptionLabel);

        toggleButton = new UIButton(new Vector2f(128f, 32), UIButton.ButtonType.NORMAL);
        toggleButton.setPosition(new Vector2f(0, 240f));
        toggleButton.setVisible(true);

        detailPanel.addDisplayElement(toggleButton);
        detailPanel.setVisible(false);

        addDisplayElement(modPanel);
        addDisplayElement(detailPanel);

    }

    @Override
    protected void createButtons(UIDisplayContainer parent) {
        UIButton okButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        okButton.getLabel().setText("Ok");
        okButton.setPosition(new Vector2f(getSize().x / 2 - okButton.getSize().x - 16f, getSize().y - okButton.getSize().y - 10));
        okButton.setVisible(true);

        okButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                originalModConfig.copy(modConfig);
                close();
            }
        });

        UIButton cancelButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        cancelButton.setPosition(new Vector2f(okButton.getPosition().x + okButton.getSize().x + 16f, okButton.getPosition().y));
        cancelButton.getLabel().setText("Cancel");
        cancelButton.setVisible(true);

        cancelButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                close();
            }
        });

        parent.addDisplayElement(okButton);
        parent.addDisplayElement(cancelButton);
    }
}
