/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.behavior.nui;

import com.google.common.collect.Lists;
import org.terasology.engine.CoreRegistry;
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.BehaviorSystem;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.UIScreenUtil;
import org.terasology.rendering.nui.baseLayouts.PropertyLayout;
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UIDropdown;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.properties.PropertyProvider;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.List;

/**
 * @author synopia
 */
public class BehaviorEditorScreen extends UIScreen {
    @Override
    public void initialise() {
        PropertyLayout entityProperties = find("entity_properties", PropertyLayout.class);
        entityProperties.addPropertyProvider("Location", new PropertyProvider<>(new LocationComponent()));
        entityProperties.addPropertyProvider("Movement", new PropertyProvider<>(new CharacterMovementComponent()));
        find("tree", BehaviorEditor.class).bindSelection(new Binding<RenderableNode>() {
            @Override
            public RenderableNode get() {
                return null;
            }

            @Override
            public void set(RenderableNode value) {
                PropertyProvider<?> provider = new PropertyProvider<>(value.getNode());
                PropertyLayout properties = find("properties", PropertyLayout.class);
                properties.clear();
                properties.addPropertyProvider("Behavior Node", provider);
            }
        });
        find("select_tree", UIDropdown.class).bindSelection(new Binding<BehaviorTree>() {
            @Override
            public BehaviorTree get() {
                return null;
            }

            @Override
            public void set(BehaviorTree value) {
                find("tree", BehaviorEditor.class).setTree(value);
            }
        });
        find("select_tree", UIDropdown.class).bindOptions(new Binding<List<BehaviorTree>>() {
            @Override
            public List<BehaviorTree> get() {
                return Lists.newArrayList(CoreRegistry.get(BehaviorSystem.class).getTrees());
            }

            @Override
            public void set(List<BehaviorTree> value) {
            }
        });

        find("select_entity", UIDropdown.class).bindOptions(new Binding<List<Interpreter>>() {
            @Override
            public List<Interpreter> get() {
                BehaviorTree selection = (BehaviorTree) find("select_tree", UIDropdown.class).getSelection();
                if (selection != null) {
                    return CoreRegistry.get(BehaviorSystem.class).getInterpreter(selection);
                } else {
                    return Arrays.asList();
                }
            }

            @Override
            public void set(List<Interpreter> value) {
            }
        });

        find("palette", UIDropdown.class).bindOptions(new Binding<List<BehaviorNodeComponent>>() {
            @Override
            public List<BehaviorNodeComponent> get() {
                return Lists.newArrayList(CoreRegistry.get(BehaviorNodeFactory.class).getNodeComponents());
            }

            @Override
            public void set(List<BehaviorNodeComponent> value) {

            }
        });

        UIScreenUtil.trySubscribe(this, "create", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                find("tree", BehaviorEditor.class).createNode((BehaviorNodeComponent) find("palette", UIDropdown.class).getSelection());
            }
        });

        UIScreenUtil.trySubscribe(this, "copy", new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                String tree = find("tree", BehaviorEditor.class).save();
                StringSelection contents = new StringSelection(tree);
                systemClipboard.setContents(contents, contents);
            }
        });
    }
}
