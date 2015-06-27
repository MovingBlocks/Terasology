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
import com.google.common.collect.Maps;

import org.terasology.config.Config;
import org.terasology.config.ModuleConfig;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.math.Vector2i;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.AbstractItemRenderer;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.ItemActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Immortius
 */
public class SelectModulesScreen extends CoreScreenLayer {

    @In
    private ModuleManager moduleManager;

    @In
    private Config config;

    private Map<Name, ModuleSelectionInfo> modulesLookup;
    private List<ModuleSelectionInfo> sortedModules;
    private DependencyResolver resolver;
    private ModuleListDownloader metaDownloader;
    private boolean needsUpdate = true;

    @Override
    public void onOpened() {
        super.onOpened();

        for (ModuleSelectionInfo info : sortedModules) {
            info.setExplicitSelection(config.getDefaultModSelection().hasModule(info.getMetadata().getId()));
        }

        refreshSelection();
    }

    @Override
    public void initialise() {
        metaDownloader = new ModuleListDownloader(config.getNetwork().getMasterServer());

        resolver = new DependencyResolver(moduleManager.getRegistry());
        modulesLookup = Maps.newHashMap();
        sortedModules = Lists.newArrayList();

        final UIList<ModuleSelectionInfo> moduleList = find("moduleList", UIList.class);
        if (moduleList != null) {
            moduleList.setList(sortedModules);
            moduleList.setItemRenderer(new AbstractItemRenderer<ModuleSelectionInfo>() {

                public String getString(ModuleSelectionInfo value) {
                    return value.getMetadata().getDisplayName().toString();
                }

                @Override
                public void draw(ModuleSelectionInfo value, Canvas canvas) {
                    if (isSelectedGameplayModule(value)) {
                        canvas.setMode("gameplay");
                    } else if (value.isSelected() && value.isExplicitSelection()) {
                        canvas.setMode("enabled");
                    } else if (value.isSelected()) {
                        canvas.setMode("dependency");
                    } else if (value.isValidToSelect()) {
                        canvas.setMode("disabled");
                    } else {
                        canvas.setMode("invalid");
                    }
                    canvas.drawText(getString(value), canvas.getRegion());
                }

                @Override
                public Vector2i getPreferredSize(ModuleSelectionInfo value, Canvas canvas) {
                    String text = getString(value);
                    return new Vector2i(canvas.getCurrentStyle().getFont().getWidth(text), canvas.getCurrentStyle().getFont().getLineHeight());
                }
            });

            //ItemActivateEventListener is triggered by double clicking
            moduleList.subscribe(new ItemActivateEventListener<ModuleSelectionInfo>() {
                @Override
                public void onItemActivated(UIWidget widget, ModuleSelectionInfo item) {
                    if (item.isSelected() && moduleList.getSelection().isExplicitSelection()) {
                        deselect(item);
                    } else if (item.isValidToSelect()) {
                        select(item);
                    }
                }
            });

            final Binding<ModuleMetadata> moduleInfoBinding = new ReadOnlyBinding<ModuleMetadata>() {
                @Override
                public ModuleMetadata get() {
                    if (moduleList.getSelection() != null) {
                        return moduleList.getSelection().getMetadata();
                    }
                    return null;
                }
            };

            UILabel name = find("name", UILabel.class);
            if (name != null) {
                name.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        if (moduleInfoBinding.get() != null) {
                            return moduleInfoBinding.get().getDisplayName().toString();
                        }
                        return "";
                    }
                });
            }

            UILabel version = find("version", UILabel.class);
            if (version != null) {
                version.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        ModuleSelectionInfo sel = moduleList.getSelection();
                        if (sel == null) {
                            return "";
                        }
                        ModuleMetadata info = sel.getMetadata();

                        String ver = info.getVersion().toString();

                        if (sel.getOnlineVersion() != null) {
                            ver += " (" + sel.getOnlineVersion().getVersion() + ")";
                        }

                        return ver;
                    }
                });
            }

            UILabel description = find("description", UILabel.class);
            if (description != null) {
                description.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        if (moduleInfoBinding.get() != null) {
                            return moduleInfoBinding.get().getDescription().toString();
                        }
                        return "";
                    }
                });
            }

            UILabel status = find("status", UILabel.class);
            if (status != null) {
                status.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        ModuleSelectionInfo info = moduleList.getSelection();
                        if (info != null) {
                            if (isSelectedGameplayModule(info)) {
                                return "gameplay";
                            } else if (info.isSelected() && info.isExplicitSelection()) {
                                return "enabled";
                            } else if (info.isSelected()) {
                                return "dependency";
                            } else if (info.isValidToSelect()) {
                                return "disabled";
                            } else {
                                return "invalid";
                            }
                        }
                        return "";
                    }
                });
            }

            UILabel error = find("errorMessage", UILabel.class);
            if (error != null) {
                error.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        if (moduleList.getSelection() != null) {
                            if (!moduleList.getSelection().isValidToSelect()) {
                                return "Incompatible with existing selection, or dependencies cannot be resolved";
                            }
                        }
                        return "";
                    }
                });
            }

            UIButton toggleActivate = find("toggleActivation", UIButton.class);
            if (toggleActivate != null) {
                toggleActivate.subscribe(new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget button) {
                        ModuleSelectionInfo info = moduleList.getSelection();
                        if (info != null) {
                            // Toggle
                            if (info.isSelected() && info.isExplicitSelection()) {
                                deselect(info);
                            } else if (info.isValidToSelect()) {
                                select(info);
                            }
                        }
                    }
                });
                toggleActivate.bindEnabled(new ReadOnlyBinding<Boolean>() {
                    @Override
                    public Boolean get() {
                        ModuleSelectionInfo info = moduleList.getSelection();
                        return info != null && info.isPresent()
                                && (info.isSelected() || info.isValidToSelect());
                    }
                });
                toggleActivate.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        if (moduleList.getSelection() != null) {
                            if (moduleList.getSelection().isExplicitSelection()) {
                                return "Deactivate";
                            } else {
                                return "Activate";
                            }
                        }
                        return "";
                    }
                });
            }

            UIButton downloadButton = find("download", UIButton.class);
            if (downloadButton != null) {
                downloadButton.subscribe(new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget button) {
                        if (moduleList.getSelection() != null) {

                            ModuleSelectionInfo info = moduleList.getSelection();
                        }
                    }
                });

                Predicate<ModuleSelectionInfo> canDownload = info -> info != null && !info.isPresent();
                Predicate<ModuleSelectionInfo> canUpdate = info -> {
                    if (info != null) {
                        Module online = info.getOnlineVersion();
                        if (online != null) {
                            return online.getVersion().compareTo(info.getLatestVersion().getVersion()) > 0;
                        }
                        return false;
                    }
                    return false;
                };

                downloadButton.bindEnabled(new ReadOnlyBinding<Boolean>() {
                    @Override
                    public Boolean get() {
                        ModuleSelectionInfo info = moduleList.getSelection();
                        if (canDownload.test(info)) {
                            return true;
                        }

                        return canUpdate.test(info);
                    }
                });
                downloadButton.bindText(new ReadOnlyBinding<String>() {

                    @Override
                    public String get() {
                        ModuleSelectionInfo info = moduleList.getSelection();
                        if (canDownload.test(info)) {
                            return "Download";
                        }
                        if (canUpdate.test(info)) {
                            return "Update";
                        }
                        return "-";
                    }
                });
            }

            UIButton disableAll = find("disableAll", UIButton.class);
            if (disableAll != null) {
                disableAll.subscribe(new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget button) {
                        for (ModuleSelectionInfo info : sortedModules) {
                            if (info.isSelected() && info.isExplicitSelection()) {
                                deselect(info);
                            }
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

    private void updateValidToSelect() {
        List<Name> selectedModules = Lists.newArrayList();
        for (ModuleSelectionInfo info : sortedModules) {
            if (info.isSelected()) {
                selectedModules.add(info.getMetadata().getId());
            }
        }
        Name[] selectedModulesArray = selectedModules.toArray(new Name[selectedModules.size()]);
        for (ModuleSelectionInfo info : sortedModules) {
            if (!info.isSelected()) {
                info.setValidToSelect(resolver.resolve(info.getMetadata().getId(), selectedModulesArray).isSuccess());
            }
        }
    }

    private void setSelectedVersions(ResolutionResult currentSelectionResults) {
        if (currentSelectionResults.isSuccess()) {
            for (Module module : currentSelectionResults.getModules()) {
                ModuleSelectionInfo info = modulesLookup.get(module.getId());

                // the engine module is not listed
                if (info != null) {
                    info.setSelectedVersion(module);
                }
            }
        }
    }

    private void updateModuleInformation() {
        modulesLookup.clear();
        sortedModules.clear();

        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module latestVersion = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (!latestVersion.isOnClasspath()) {
                ModuleSelectionInfo info = ModuleSelectionInfo.local(latestVersion);
                modulesLookup.put(info.getMetadata().getId(), info);
                sortedModules.add(info);
            }
        }

        for (RemoteModule remote : metaDownloader.getModules()) {
            ModuleSelectionInfo info = modulesLookup.get(remote.getId());
            if (info == null) {
                info = ModuleSelectionInfo.remote(remote);
                modulesLookup.put(remote.getId(), info);
                sortedModules.add(info);
            }
            info.setOnlineVersion(remote);
        }

        Collections.sort(sortedModules, (o1, o2) ->
            o1.getMetadata().getDisplayName().toString().compareTo(
            o2.getMetadata().getDisplayName().toString()));
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (needsUpdate) {
            if (metaDownloader.isDone()) {
                needsUpdate = false;
            }
            updateModuleInformation();
        }
    }

    @Override
    public void onClosed() {
        ModuleConfig moduleConfig = config.getDefaultModSelection();
        moduleConfig.clear();
        for (ModuleSelectionInfo info : sortedModules) {
            if (info.isSelected() && info.isExplicitSelection()) {
                moduleConfig.addModule(info.getMetadata().getId());
            }
        }
        if (!modulesLookup.get(config.getWorldGeneration().getDefaultGenerator().getModuleName()).isSelected()) {
            config.getWorldGeneration().setDefaultGenerator(new SimpleUri());
        }
        config.save();
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    private void select(ModuleSelectionInfo target) {
        if (target.isValidToSelect() && !target.isExplicitSelection()) {
            boolean previouslySelected = target.isSelected();
            target.setExplicitSelection(true);
            if (!previouslySelected) {
                refreshSelection();
            }
        }
    }

    private List<Name> getExplicitlySelectedModules() {
        List<Name> selectedModules = Lists.newArrayList();
        for (ModuleSelectionInfo info : sortedModules) {
            if (info.isExplicitSelection()) {
                selectedModules.add(info.getMetadata().getId());
            }
        }
        return selectedModules;
    }

    private void deselect(ModuleSelectionInfo target) {
        // only deselect if it is already selected and if it is not the currently selected gameplay module
        if (target.isExplicitSelection()
                && !isSelectedGameplayModule(target)) {
            target.setExplicitSelection(false);
            refreshSelection();
        }
    }

    private boolean isSelectedGameplayModule(ModuleSelectionInfo target) {
        return target.getMetadata().getId().equals(new Name(config.getDefaultModSelection().getDefaultGameplayModuleName()));
    }

    private void refreshSelection() {
        List<Name> selectedModules = getExplicitlySelectedModules();
        for (ModuleSelectionInfo info : sortedModules) {
            info.setSelectedVersion(null);
        }
        setSelectedVersions(resolver.resolve(selectedModules));
        updateValidToSelect();
    }


    private static final class ModuleSelectionInfo {
        private Module latestVersion;
        private Module selectedVersion;
        private Module onlineVersion;
        private boolean explicitSelection;
        private boolean validToSelect = true;

        private ModuleSelectionInfo(Module module) {
            this.latestVersion = module;
        }

        public static ModuleSelectionInfo remote(Module module) {
            ModuleSelectionInfo info = new ModuleSelectionInfo(null);
            info.setOnlineVersion(module);
            return info;
        }

        public static ModuleSelectionInfo local(Module module) {
            return new ModuleSelectionInfo(module);
        }

        public ModuleMetadata getMetadata() {
            if (selectedVersion != null) {
                return selectedVersion.getMetadata();
            } else if (latestVersion != null) {
                return latestVersion.getMetadata();
            } else if (onlineVersion != null) {
                return onlineVersion.getMetadata();
            }

            return null;
        }

        public boolean isPresent() {
            return latestVersion != null;
        }

        public boolean isSelected() {
            return selectedVersion != null;
        }

        public Module getOnlineVersion() {
            return onlineVersion;
        }

        public Module getLatestVersion() {
            return latestVersion;
        }

        public Module getSelectedVersion() {
            return selectedVersion;
        }

        public void setOnlineVersion(Module onlineVersion) {
            this.onlineVersion = onlineVersion;
        }

        public void setSelectedVersion(Module selectedVersion) {
            this.selectedVersion = selectedVersion;
        }

        public boolean isExplicitSelection() {
            return explicitSelection;
        }

        public void setExplicitSelection(boolean explicitSelection) {
            this.explicitSelection = explicitSelection;
        }

        public boolean isValidToSelect() {
            return validToSelect;
        }

        public void setValidToSelect(boolean validToSelect) {
            this.validToSelect = validToSelect;
        }
    }
}
