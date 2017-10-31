/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.selectModulesScreen;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.SelectModulesConfig;
import org.terasology.config.ModuleConfig;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.DependencyResolutionFailedException;
import org.terasology.engine.module.ModuleInstaller;
import org.terasology.engine.module.ModuleManager;
import org.terasology.i18n.TranslationSystem;
import org.terasology.math.geom.Vector2i;
import org.terasology.module.DependencyInfo;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.AbstractItemRenderer;
import org.terasology.rendering.nui.layers.mainMenu.ConfirmPopup;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.rendering.nui.layers.mainMenu.WaitPopup;
import org.terasology.rendering.nui.widgets.ResettableUIText;
import org.terasology.rendering.nui.widgets.TextChangeEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UICheckbox;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.world.generator.internal.WorldGeneratorManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 */
public class SelectModulesScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:selectModsScreen");

    private static final Logger logger = LoggerFactory.getLogger(SelectModulesScreen.class);
    private final Comparator<? super ModuleSelectionInfo> moduleInfoComparator = (o1, o2) ->
            o1.getMetadata().getDisplayName().toString().compareTo(
                    o2.getMetadata().getDisplayName().toString());
    @In
    private ModuleManager moduleManager;
    @In
    private Config config;

    private SelectModulesConfig selectModulesConfig;
    @In
    private WorldGeneratorManager worldGenManager;
    @In
    private TranslationSystem translationSystem;
    private Map<Name, ModuleSelectionInfo> modulesLookup;
    private List<ModuleSelectionInfo> sortedModules;
    private List<ModuleSelectionInfo> allSortedModules;
    private DependencyResolver resolver;
    private Future<Void> remoteModuleRegistryUpdater;
    private UICheckbox localOnlyCheckbox;
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
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());
        remoteModuleRegistryUpdater = Executors.newSingleThreadExecutor().submit(moduleManager.getInstallManager().updateRemoteRegistry());

        selectModulesConfig = config.getSelectModulesConfig();

        resolver = new DependencyResolver(moduleManager.getRegistry());

        modulesLookup = Maps.newHashMap();
        sortedModules = Lists.newArrayList();
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module latestVersion = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (!latestVersion.isOnClasspath()) {
                ModuleSelectionInfo info = ModuleSelectionInfo.local(latestVersion);
                modulesLookup.put(info.getMetadata().getId(), info);
                sortedModules.add(info);
            }
        }

        Collections.sort(sortedModules, moduleInfoComparator);
        allSortedModules = new ArrayList<>(sortedModules);

        final UIList<ModuleSelectionInfo> moduleList = find("moduleList", UIList.class);
        if (moduleList != null) {
            moduleList.setList(sortedModules);
            moduleList.setItemRenderer(new AbstractItemRenderer<ModuleSelectionInfo>() {

                public String getString(ModuleSelectionInfo value) {
                    return value.getMetadata().getDisplayName().toString();
                }

                @Override
                public void draw(ModuleSelectionInfo value, Canvas canvas) {
                    if (isSelectedGameplayModule(value) && value.isValidToSelect()) {
                        canvas.setMode("gameplay");
                    } else if (value.isSelected() && value.isExplicitSelection()) {
                        canvas.setMode("enabled");
                    } else if (value.isSelected()) {
                        canvas.setMode("dependency");
                    } else if (!value.isPresent()) {
                        canvas.setMode("disabled");
                    } else if (!value.isValidToSelect()) {
                        canvas.setMode("invalid");
                    } else {
                        canvas.setMode("available");
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
            moduleList.subscribe((widget, item) -> {
                if (item.isSelected() && moduleList.getSelection().isExplicitSelection()) {
                    deselect(item);
                } else if (item.isValidToSelect()) {
                    select(item);
                }
            });

            ResettableUIText moduleSearch = find("moduleSearch", ResettableUIText.class);
            if (moduleSearch != null) {
                moduleSearch.subscribe(new TextChangeEventListener() {
                    @Override
                    public void onTextChange(String oldText, String newText) {
                        filterText(newText);
                    }
                });
            }

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

            UILabel installedVersion = find("installedVersion", UILabel.class);
            if (installedVersion != null) {
                installedVersion.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        ModuleSelectionInfo sel = moduleList.getSelection();
                        if (sel == null) {
                            return "";
                        }
                        return sel.isPresent() ? sel.getMetadata().getVersion().toString() : translationSystem.translate("${engine:menu#module-version-installed-none}");
                    }
                });
            }

            UILabel onlineVersion = find("onlineVersion", UILabel.class);
            if (onlineVersion != null) {
                onlineVersion.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        ModuleSelectionInfo sel = moduleList.getSelection();
                        if (sel == null) {
                            return "";
                        }
                        return (sel.getOnlineVersion() != null)
                                ? sel.getOnlineVersion().getVersion().toString() : "none";
                    }
                });
            }

            UILabel description = find("description", UILabel.class);
            if (description != null) {
                description.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        ModuleMetadata moduleMetadata = moduleInfoBinding.get();
                        if (moduleMetadata != null) {
                            String dependenciesNames;
                            List<DependencyInfo> dependencies = moduleMetadata.getDependencies();
                            if (dependencies != null && dependencies.size() > 0) {
                                dependenciesNames = translationSystem.translate("${engine:menu#module-dependencies-exist}") + ":" + '\n';
                                for (DependencyInfo dependency : dependencies) {
                                    dependenciesNames += "   " + dependency.getId().toString() + '\n';
                                }
                            } else {
                                dependenciesNames = translationSystem.translate("${engine:menu#module-dependencies-empty}") + ".";
                            }
                            return moduleMetadata.getDescription().toString() + '\n' + '\n' + dependenciesNames;
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
                                return translationSystem.translate("${engine:menu#module-status-activegameplay}");
                            } else if (info.isSelected() && info.isExplicitSelection()) {
                                return translationSystem.translate("${engine:menu#module-status-activated}");
                            } else if (info.isSelected()) {
                                return translationSystem.translate("${engine:menu#module-status-dependency}");
                            } else if (!info.isPresent()) {
                                return translationSystem.translate("${engine:menu#module-status-notpresent}");
                            } else if (info.isValidToSelect()) {
                                return translationSystem.translate("${engine:menu#module-status-available}");
                            } else {
                                return translationSystem.translate("${engine:menu#module-status-error}");
                            }
                        }
                        return "";
                    }
                });
            }

            UIButton toggleActivate = find("toggleActivation", UIButton.class);
            if (toggleActivate != null) {
                toggleActivate.subscribe(button -> {
                    ModuleSelectionInfo info = moduleList.getSelection();
                    if (info != null) {
                        // Toggle
                        if (info.isSelected() && info.isExplicitSelection()) {
                            deselect(info);
                        } else if (info.isValidToSelect()) {
                            select(info);
                        }
                    }
                });
                toggleActivate.bindEnabled(new ReadOnlyBinding<Boolean>() {
                    @Override
                    public Boolean get() {
                        ModuleSelectionInfo info = moduleList.getSelection();
                        return info != null && info.isPresent() && !isSelectedGameplayModule(info)
                                && (info.isSelected() || info.isValidToSelect());
                    }
                });
                toggleActivate.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        if (moduleList.getSelection() != null) {
                            if (moduleList.getSelection().isExplicitSelection()) {
                                return translationSystem.translate("${engine:menu#deactivate-module}");
                            } else {
                                return translationSystem.translate("${engine:menu#activate-module}");
                            }
                        }
                        return translationSystem.translate("${engine:menu#activate-module}");  // button should be disabled
                    }
                });
            }

            UIButton downloadButton = find("download", UIButton.class);
            if (downloadButton != null) {
                downloadButton.subscribe(button -> {
                    if (moduleList.getSelection() != null) {
                        ModuleSelectionInfo info = moduleList.getSelection();
                        startDownloadingNewestModulesRequiredFor(info);
                    }
                });
                downloadButton.bindEnabled(new ReadOnlyBinding<Boolean>() {
                    @Override
                    public Boolean get() {
                        try {
                            return moduleList.getSelection().getOnlineVersion() != null;
                        } catch (NullPointerException e) {
                            return false;
                        }
                    }
                });
                downloadButton.bindText(new ReadOnlyBinding<String>() {
                    @Override
                    public String get() {
                        ModuleSelectionInfo info = moduleList.getSelection();
                        if (info != null && !info.isPresent()) {
                            return translationSystem.translate("${engine:menu#download-module}");
                        } else {
                            return translationSystem.translate("${engine:menu#update-module}");
                        }
                    }
                });
            }

            UIButton disableAll = find("disableAll", UIButton.class);
            if (disableAll != null) {
                disableAll.subscribe(button -> sortedModules.stream()
                        .filter(info -> info.isSelected() && info.isExplicitSelection()).forEach(this::deselect));
            }

            localOnlyCheckbox = find("localOnly", UICheckbox.class);
            localOnlyCheckbox.bindChecked(
                    new Binding<Boolean>() {

                        @Override
                        public Boolean get() {
                            filterText(moduleSearch.getText());
                            prepareModuleList(selectModulesConfig.isChecked());
                            return selectModulesConfig.isChecked();
                        }

                        @Override
                        public void set(Boolean value) {
                            selectModulesConfig.setIsChecked(value);
                            filterText(moduleSearch.getText());
                            prepareModuleList(value);
                        }
                    }
            );
        }

        WidgetUtil.trySubscribe(this, "close", button -> triggerBackAnimation());
    }

    private void prepareModuleList(boolean checked) {
        if (selectModulesConfig.isChecked()) {
            Iterator<ModuleSelectionInfo> iter = sortedModules.iterator();
            while (iter.hasNext()) {
                if (!iter.next().isPresent()) {
                    iter.remove();
                }
            }
        }
    }

    private void filterText(String newText) {
        sortedModules.clear();
        for (ModuleSelectionInfo m : allSortedModules) {
            if (m.getMetadata().getDisplayName().toString().toLowerCase().contains(newText.toLowerCase())) {
                sortedModules.add(m);
            }
        }
    }

    private void startDownloadingNewestModulesRequiredFor(ModuleSelectionInfo moduleMetadata) {
        Set<Module> modulesToDownload;
        try {
            modulesToDownload = moduleManager.getInstallManager().getAllModulesToDownloadFor(moduleMetadata.getMetadata().getId());
        } catch (DependencyResolutionFailedException ex) {
            MessagePopup messagePopup = getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
            messagePopup.setMessage("Error", ex.getMessage());
            return;
        }
        ConfirmPopup confirmPopup = getManager().pushScreen(ConfirmPopup.ASSET_URI, ConfirmPopup.class);
        confirmPopup.setMessage("Confirm Download", modulesToDownload.size() + " modules will be downloaded");
        confirmPopup.setOkHandler(() -> downloadModules(modulesToDownload));
    }

    private void downloadModules(Iterable<Module> modulesToDownload) {
        final WaitPopup<List<Module>> popup = getManager().pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
        popup.onSuccess(newModules -> {
            for (Module module : newModules) {
                modulesLookup.get(module.getId()).setLocalVersion(module);
                updateValidToSelect();
            }
        });
        ModuleInstaller operation = moduleManager.getInstallManager().createInstaller(modulesToDownload, new DownloadPopupProgressListener(popup));
        popup.startOperation(operation, true);
    }

    private void updateValidToSelect() {
        List<Name> selectedModules = Lists.newArrayList();
        selectedModules.addAll(sortedModules.stream().filter(ModuleSelectionInfo::isSelected)
                .map(info -> info.getMetadata().getId()).collect(Collectors.toList()));
        Name[] selectedModulesArray = selectedModules.toArray(new Name[selectedModules.size()]);
        sortedModules.stream().filter(info -> !info.isSelected()).forEach(info ->
                info.setValidToSelect(resolver.resolve(info.getMetadata().getId(), selectedModulesArray).isSuccess()));
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
        Iterable<Module> remoteModuleRegistry = moduleManager.getInstallManager().getRemoteRegistry();
        Set<Name> filtered = ImmutableSet.of(TerasologyConstants.ENGINE_MODULE, new Name("engine-test"));
        for (Module remote : remoteModuleRegistry) {
            ModuleSelectionInfo info = modulesLookup.get(remote.getId());
            if (!filtered.contains(remote.getId())) {
                if (info == null) {
                    info = ModuleSelectionInfo.remote(remote);
                    modulesLookup.put(remote.getId(), info);
                    int pos = Collections.binarySearch(sortedModules, info, moduleInfoComparator);
                    if (pos < 0) {                             // not yet in the (sorted) list
                        sortedModules.add(-pos - 1, info);     // use "insertion point" to keep the list sorted
                        allSortedModules.clear();
                        allSortedModules.addAll(sortedModules);
                    }
                }
                info.setOnlineVersion(remote);
            }
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        
        if (needsUpdate && remoteModuleRegistryUpdater.isDone() && !selectModulesConfig.isChecked()) {
            needsUpdate = false;
            try {
                remoteModuleRegistryUpdater.get(); // it'a a Callable<Void> so just a null is returned, but it's used instead of a runnable because it can throw exceptions
            } catch (CancellationException | InterruptedException | ExecutionException ex) {
                logger.warn("Failed to retrieve module list from meta server", ex);
                MessagePopup message = getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
                message.setMessage("Warning", "Failed to retrieve a module list from the master server. Only locally installed modules are available.");
                return;
            }
            updateModuleInformation();
        }
    }

    @Override
    public void onClosed() {
        // moduleConfig passes the module collection to the Create Game Screen.
        ModuleConfig moduleConfig = config.getDefaultModSelection();
        moduleConfig.clear();
        // Fetch all the selected/activated modules using allSortedModules
        // instead of fetching only selected/activated modules from filtered collection of modules using sortedModules
        allSortedModules.stream().filter(info -> info.isSelected() && info.isExplicitSelection()).forEach(info ->
                moduleConfig.addModule(info.getMetadata().getId()));
        SimpleUri defaultGenerator = config.getWorldGeneration().getDefaultGenerator();
        ModuleSelectionInfo info = modulesLookup.get(defaultGenerator.getModuleName());
        if (info != null && !info.isSelected()) {
            config.getWorldGeneration().setDefaultGenerator(new SimpleUri());
        }

        worldGenManager.refresh();

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
        return sortedModules.stream().filter(ModuleSelectionInfo::isExplicitSelection).map(info ->
                info.getMetadata().getId()).collect(Collectors.toCollection(ArrayList::new));
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
}
