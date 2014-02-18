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
package org.terasology.rendering.nui.layers.mainMenu;

import com.google.common.collect.Lists;
import org.terasology.config.Config;
import org.terasology.config.ModuleConfig;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleInfo;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleSelection;
import org.terasology.math.Vector2i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.AbstractItemRenderer;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.ItemActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Immortius
 */
public class SelectModulesScreen extends CoreScreenLayer {

    @In
    private ModuleManager moduleManager;

    @In
    private Config config;

    private ModuleSelection selection;

    @Override
    public void initialise() {

        //get all the default modes and load them up in selection
        //  checking if it's valid along the way
        selection = new ModuleSelection(moduleManager);
        for (String moduleId : config.getDefaultModSelection().listModules()) {
            ModuleSelection newSelection = selection.add(moduleId);
            if (newSelection.isValid()) {
                selection = newSelection;
            }
        }

        //get all the modules id's, except engine
        List<String> moduleIds = Lists.newArrayList(moduleManager.getModuleIds());
        moduleIds.remove("engine");

        //create our own copy of the modules
        List<Module> modules = Lists.newArrayListWithCapacity(moduleIds.size());
        for (String id : moduleIds) {
            modules.add(moduleManager.getLatestModuleVersion(id));
        }

        //sort by name
        Collections.sort(modules, new Comparator<Module>() {
            @Override
            public int compare(Module o1, Module o2) {
                return o1.getModuleInfo().getDisplayName().compareTo(o2.getModuleInfo().getDisplayName());
            }
        });


        final UIList<Module> moduleList = find("moduleList", UIList.class);
        if (moduleList != null) {
            moduleList.setList(modules);
            moduleList.setItemRenderer(new AbstractItemRenderer<Module>() {

                public String getString(Module value) {
                    return value.getModuleInfo().getDisplayName();
                }

                @Override
                public void draw(Module value, Canvas canvas) {
                    if (selection.contains(value.getId())) {
                        canvas.setMode("enabled");
                    } else {
                        canvas.setMode("disabled");
                    }
                    canvas.drawText(getString(value), canvas.getRegion());
                }

                @Override
                public Vector2i getPreferredSize(Module value, Canvas canvas) {
                    String text = getString(value);
                    return new Vector2i(canvas.getCurrentStyle().getFont().getWidth(text), canvas.getCurrentStyle().getFont().getLineHeight());
                }
            });

            //ItemActivateEventListener is triggered by double clicking
            moduleList.subscribe(new ItemActivateEventListener<Module>() {
                @Override
                public void onItemActivated(UIWidget widget, Module item) {
                    String id = item.getId();

                    // The Core module is mandatory - ignore toggle requests
                    if (id.equals("core")) {
                        return;
                    }

                    // Toggle
                    if (selection.contains(id)) {
                        ModuleSelection newSelection = selection.remove(id);
                        if (newSelection.isValid()) {
                            selection = newSelection;
                        }
                    } else {
                        ModuleSelection newSelection = selection.add(id);
                        if (newSelection.isValid()) {
                            selection = newSelection;
                        }
                    }
                }
            });


            Binding<ModuleInfo> moduleInfoBinding = new ReadOnlyBinding<ModuleInfo>() {
                @Override
                public ModuleInfo get() {
                    if (moduleList.getSelection() != null) {
                        return moduleList.getSelection().getModuleInfo();
                    }
                    return null;
                }
            };

            UILabel name = find("name", UILabel.class);
            if (name != null) {
                name.bindText(BindHelper.bindBoundBeanProperty("displayName", moduleInfoBinding, ModuleInfo.class, String.class));
            }

            UILabel version = find("version", UILabel.class);
            if (version != null) {
                version.bindText(BindHelper.bindBoundBeanProperty("version", moduleInfoBinding, ModuleInfo.class, String.class));
            }

            UILabel description = find("description", UILabel.class);
            if (description != null) {
                description.bindText(BindHelper.bindBoundBeanProperty("description", moduleInfoBinding, ModuleInfo.class, String.class));
            }

            UILabel error = find("errorMessage", UILabel.class);
            if (error != null) {
                error.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        if (moduleList.getSelection() != null) {
                            ModuleSelection newModuleSelection = selection.add(moduleList.getSelection().getId());
                            if (!newModuleSelection.isValid()) {
                                return newModuleSelection.getValidationMessages().get(0);
                            }
                        }
                        return "";
                    }
                });
            }

            UIButton toggle = find("toggleActivation", UIButton.class);
            if (toggle != null) {
                toggle.subscribe(new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget button) {
                        if (moduleList.getSelection() != null) {
                            String id = moduleList.getSelection().getId();

                            // The Core module is mandatory - ignore toggle requests
                            if (id.equals("core")) {
                                return;
                            }

                            // Toggle
                            if (selection.contains(id)) {
                                ModuleSelection newSelection = selection.remove(id);
                                if (newSelection.isValid()) {
                                    selection = newSelection;
                                }
                            } else {
                                ModuleSelection newSelection = selection.add(id);
                                if (newSelection.isValid()) {
                                    selection = newSelection;
                                }
                            }
                        }
                    }
                });
                toggle.bindVisible(new ReadOnlyBinding<Boolean>() {
                    @Override
                    public Boolean get() {
                        return moduleList.getSelection() != null
                                && (selection.contains(moduleList.getSelection().getId()) || selection.add(moduleList.getSelection().getId()).isValid());
                    }
                });
                toggle.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        if (moduleList.getSelection() != null) {
                            String id = moduleList.getSelection().getId();
                            if (selection.contains(id)) {
                                return "Deactivate";
                            } else {
                                return "Activate";
                            }
                        }
                        return "";
                    }
                });
            }

            UIButton enableAll = find("enableAll", UIButton.class);
            if (enableAll != null) {
                enableAll.subscribe(new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget button) {
                        for (Module m : moduleList.getList()) {
                            ModuleSelection newSelection = selection.add(m);
                            if (newSelection.isValid()) {
                                selection = newSelection;
                            }
                        }
                    }
                });
            }

            UIButton disableAll = find("disableAll", UIButton.class);
            if (disableAll != null) {
                disableAll.subscribe(new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget button) {
                        for (Module m : moduleList.getList()) {
                            // The Core module is mandatory - ignore trying to disable it
                            if (m.getId().equals("core")) {
                                continue;
                            }

                            //skipping the valid checks - the only one that should be left is the core module
                            //TODO: Add a remove method that takes the module
                            ModuleSelection newSelection = selection.remove(m.getId());
                            selection = newSelection;
                        }
                    }
                });
            }
        }


        WidgetUtil.trySubscribe(this, "close", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().popScreen();
            }
        });
    }

    @Override
    public void onClosed() {
        ModuleConfig moduleConfig = config.getDefaultModSelection();
        moduleConfig.clear();
        for (Module module : selection.getSelection()) {
            moduleConfig.addModule(module.getId());
        }
        if (!moduleConfig.hasModule(config.getWorldGeneration().getDefaultGenerator().getModuleName())) {
            config.getWorldGeneration().setDefaultGenerator(new SimpleUri());
        }
        config.save();
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

}
