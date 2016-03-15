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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.ModuleConfig;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.RemoteModuleExtension;
import org.terasology.engine.paths.PathManager;
import org.terasology.i18n.TranslationSystem;
import org.terasology.math.geom.Vector2i;
import org.terasology.module.DependencyInfo;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleLoader;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.AbstractItemRenderer;
import org.terasology.rendering.nui.widgets.TextChangeEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.world.generator.internal.WorldGeneratorManager;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 */
public class SelectModulesScreen extends CoreScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(SelectModulesScreen.class);
    private static final Name ENGINE_MODULE_NAME = new Name("engine");

    @In
    private ModuleManager moduleManager;

    @In
    private Config config;

    @In
    private WorldGeneratorManager worldGenManager;

    @In
    private TranslationSystem translationSystem;

    private Map<Name, ModuleSelectionInfo> modulesLookup;
    private List<ModuleSelectionInfo> sortedModules;
    private List<ModuleSelectionInfo> allSortedModules;
    private DependencyResolver resolver;
    private ModuleListDownloader metaDownloader;
    private boolean needsUpdate = true;

    private final Comparator<? super ModuleSelectionInfo> moduleInfoComparator = (o1, o2) ->
            o1.getMetadata().getDisplayName().toString().compareTo(
            o2.getMetadata().getDisplayName().toString());

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
                    if (isSelectedGameplayModule(value)) {
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

            UIText moduleSearch = find("moduleSearch", UIText.class);
            if (moduleSearch != null) {
                    moduleSearch.subscribe(new TextChangeEventListener() {
                            @Override
                            public void onTextChange(String oldText, String newText) {
                                    sortedModules.clear();
                                    for (ModuleSelectionInfo m : allSortedModules) {
                                            if (m.getMetadata().getDisplayName().toString().toLowerCase().contains(newText.toLowerCase())) {
                                                    sortedModules.add(m);
                                                }
                                        }
                                    moduleList.update(0.1f);
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
        }


        WidgetUtil.trySubscribe(this, "close", button -> getManager().popScreen());
    }

    private void startDownloadingNewestModulesRequiredFor(ModuleSelectionInfo moduleMetadata) {
        List<ModuleSelectionInfo> modulesToDownload;
        try {
            modulesToDownload = getModulesRequiredToDownloadFor(moduleMetadata);
        } catch (DependencyResolutionFailed e) {
            MessagePopup messagePopup = getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
            messagePopup.setMessage("Depedency resolution failed", e.getMessage());
            return;
        }

        Map<URL, Path> urlToTargetMap = determineDownloadUrlsFor(modulesToDownload);

        ConfirmPopup confirmPopup = getManager().pushScreen(ConfirmPopup.ASSET_URI, ConfirmPopup.class);
        confirmPopup.setMessage("Confirm Download", modulesToDownload.size()  + " modules will be downloaded");
        confirmPopup.setOkHandler(() -> downloadModules(urlToTargetMap));
    }

    private void downloadModules(Map<URL, Path> urlToTargetMap) {
        final WaitPopup<List<Path>> popup = getManager().pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
        ModuleLoader loader = new ModuleLoader(moduleManager.getModuleMetadataReader());
        loader.setModuleInfoPath(TerasologyConstants.MODULE_INFO_FILENAME);
        popup.onSuccess(paths -> {
                for (Path filePath: paths) {
                    try {
                        Module module = loader.load(filePath);
                        modulesLookup.get(module.getId()).setLocalVersion(module);
                        moduleManager.getRegistry().add(module);
                    } catch (IOException e) {
                        logger.warn("Could not load module {}", filePath.getFileName(), e);
                        return;
                    }
                    updateValidToSelect();
                }
            });
        ProgressListener progressListener = progress ->
                popup.setMessage("Downloading required modules", String.format("Please wait ... %d%%", (int) (progress * 100f)));
        // to ensure that the initial message gets set:
        progressListener.onProgress(0);
        MultiFileDownloader operation = new MultiFileDownloader(urlToTargetMap, progressListener);
        popup.startOperation(operation, true);
    }

    private Map<URL, Path> determineDownloadUrlsFor(List<ModuleSelectionInfo> modulesToDownload) {
        Map<URL, Path> urlToTargetMap = Maps.newLinkedHashMap();
        for (ModuleSelectionInfo moduleSelectionInfo: modulesToDownload) {
            ModuleMetadata metaData = moduleSelectionInfo.getOnlineVersion().getMetadata();
            String version = metaData.getVersion().toString();
            String id = metaData.getId().toString();
            URL url = RemoteModuleExtension.getDownloadUrl(metaData);
            String fileName = String.format("%s-%s.jar", id, version);
            Path folder = PathManager.getInstance().getHomeModPath().normalize();
            Path target = folder.resolve(fileName);
            urlToTargetMap.put(url, target);
        }
        return urlToTargetMap;
    }

    /**
     * @return All modules that are required to play the online version of the specified module. The list contains the
     * passed module too.
     */
    private List<ModuleSelectionInfo> getModulesRequiredFor(ModuleSelectionInfo mainModuleInfo) throws DependencyResolutionFailed {
        ModuleMetadata mainModuleMetadata = mainModuleInfo.getOnlineVersion().getMetadata();
        LinkedList<Name> idsToCheck = Lists.newLinkedList();
        idsToCheck.add(mainModuleMetadata.getId());
        Map<Name, ModuleSelectionInfo> requiredIdToMetaDataMap = Maps.newLinkedHashMap();
        requiredIdToMetaDataMap.put(mainModuleMetadata.getId(), mainModuleInfo);
        while (!idsToCheck.isEmpty()) {
            Name moduleToCheck = idsToCheck.removeFirst();
            ModuleSelectionInfo moduleToCheckInfo = requiredIdToMetaDataMap.get(moduleToCheck);
            ModuleMetadata metaDataOfModuleToCheck = moduleToCheckInfo.getOnlineVersion().getMetadata();

            for (DependencyInfo dependencyInfo : metaDataOfModuleToCheck.getDependencies()) {
                Name depName = dependencyInfo.getId();

                ModuleMetadata depMetaData;
                if (depName.equals(ENGINE_MODULE_NAME)) {
                    depMetaData = moduleManager.getRegistry().getLatestModuleVersion(ENGINE_MODULE_NAME).getMetadata();
                    if (!dependencyInfo.versionRange().contains(depMetaData.getVersion())) {
                        throw new DependencyResolutionFailed(String.format(
                                "Module %s %s requires %s in version range %s, but you are using version %s",
                                moduleToCheck, metaDataOfModuleToCheck.getVersion(), depName, dependencyInfo.versionRange(),
                                depMetaData.getVersion()));
                    }
                } else {
                    ModuleSelectionInfo depInfo = modulesLookup.get(depName);
                    if (depInfo == null) {
                        throw new DependencyResolutionFailed(String.format("%s requires %s which is missing", moduleToCheck,
                                depName));
                    }
                    depMetaData = depInfo.getOnlineVersion().getMetadata();

                    if (!dependencyInfo.versionRange().contains(depMetaData.getVersion())) {
                        throw new DependencyResolutionFailed(String.format(
                                "Module %s %s requires %s in version range %s, but the online version has version %s",
                                moduleToCheck, metaDataOfModuleToCheck.getVersion(), depName, dependencyInfo.versionRange(),
                                depMetaData.getVersion()));
                    }
                    if (!requiredIdToMetaDataMap.containsKey(depName)) {
                        idsToCheck.add(depName);
                        requiredIdToMetaDataMap.put(depName, depInfo);
                    }
                }
            }
        }
        List<ModuleSelectionInfo> sortedDependencies = Lists.newArrayList(requiredIdToMetaDataMap.values());
        return sortedDependencies;
    }

    /**
     *
     * @return all modules that need to be downloaded to use the newest version of the specified module and all its
     * dependencies.
     */
    private List<ModuleSelectionInfo> getModulesRequiredToDownloadFor(ModuleSelectionInfo mainModuleInfo)
            throws DependencyResolutionFailed {
        List<ModuleSelectionInfo> requiredModules = getModulesRequiredFor(mainModuleInfo);

        List<ModuleSelectionInfo> modulesToDownload = requiredModules.stream().filter(ModuleSelectionInfo::isOnlineVersionNewer)
                .collect(Collectors.toList());
        return modulesToDownload;
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

        Set<Name> filtered = ImmutableSet.of(ENGINE_MODULE_NAME, new Name("engine-test"));
        for (RemoteModule remote : metaDownloader.getModules()) {
            ModuleSelectionInfo info = modulesLookup.get(remote.getId());
            if (!filtered.contains(remote.getId())) {
                if (info == null) {
                    info = ModuleSelectionInfo.remote(remote);
                    modulesLookup.put(remote.getId(), info);
                    int pos = Collections.binarySearch(sortedModules, info, moduleInfoComparator);
                    if (pos < 0) {                             // not yet in the (sorted) list
                        sortedModules.add(-pos - 1, info);     // use "insertion point" to keep the list sorted
                    }
                }
                info.setOnlineVersion(remote);
            }
        }
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
        sortedModules.stream().filter(info -> info.isSelected() && info.isExplicitSelection()).forEach(info ->
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

    private static final class DependencyResolutionFailed extends Exception {

        private static final long serialVersionUID = -2098680881126171195L;

        DependencyResolutionFailed(String message) {
            super(message);
        }
    }

    private static class MultiFileDownloader implements Callable<List<Path>> {
        private Map<URL, Path> urlToTargetMap;
        private ProgressListener progressListener;

        public MultiFileDownloader(Map<URL, Path> urlToTargetMap, ProgressListener progressListener) {
            this.urlToTargetMap = urlToTargetMap;
            this.progressListener = progressListener;
        }

        @Override
        public List<Path> call() throws Exception {
            List<Path> downloadedFiles = new ArrayList<>();
            float fractionPerFile = (float) 1 / urlToTargetMap.size();
            int index = 0;
            for (Map.Entry<URL, Path> entry: urlToTargetMap.entrySet()) {
                float progressWithFiles = fractionPerFile * index;
                ProgressListener singleDownloadListener = fraction -> {
                    float totalPrecentDone = progressWithFiles + (fraction / urlToTargetMap.size());
                    progressListener.onProgress(totalPrecentDone);
                };
                FileDownloader fileDownloader = new FileDownloader(entry.getKey(), entry.getValue(),
                        singleDownloadListener);
                downloadedFiles.add(fileDownloader.call());
                index++;
            }
            return downloadedFiles;
        }
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

        public void setLocalVersion(Module module) {
            latestVersion = module;
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

        public boolean isOnlineVersionNewer() {
            if (onlineVersion == null) {
                return false;
            }
            if (latestVersion == null) {
                return true;
            }
            int versionCompare = onlineVersion.getVersion().compareTo(latestVersion.getVersion());
            if (versionCompare > 0) {
                return true;
            } else if (versionCompare == 0) {
                /*
                 * Multiple binaries get released as the same snapshot version, A version name match thus does not
                 * gurantee that we have the newest version already if it is a snapshot version.
                 *
                 * Having the user redownload the same binary again is not ideal, but it is better then ahving the user
                 * being stuck on an outdated snapshot binary.
                 */
                return onlineVersion.getVersion().isSnapshot();
            } else {
                return false;
            }
        }
    }
}
