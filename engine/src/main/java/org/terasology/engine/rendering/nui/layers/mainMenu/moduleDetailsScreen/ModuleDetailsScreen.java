// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.moduleDetailsScreen;

import org.codehaus.plexus.util.StringUtils;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.module.DependencyResolutionFailedException;
import org.terasology.engine.core.module.ExtraDataModuleExtension;
import org.terasology.engine.core.module.ModuleInstaller;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.module.RemoteModuleExtension;
import org.terasology.engine.core.module.StandardModuleExtension;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.ConfirmPopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.WaitPopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.advancedGameSetupScreen.DownloadPopupProgressListener;
import org.terasology.engine.rendering.nui.widgets.UIButtonWebBrowser;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.dependencyresolution.DependencyInfo;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.Version;
import org.terasology.nui.Canvas;
import org.terasology.nui.Color;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.itemRendering.AbstractItemRenderer;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIList;
import org.terasology.nui.widgets.UIText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Shows detailed information about modules.
 */
public class ModuleDetailsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:moduleDetailsScreen");

    private static final Logger logger = LoggerFactory.getLogger(ModuleDetailsScreen.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_GITHUB_MODULE_URL = "https://github.com/Terasology/";
    private static final List<String> INTERNAL_MODULES = Arrays.asList("engine");
    @In
    private ModuleManager moduleManager;
    @In
    private TranslationSystem translationSystem;
    @In
    private Context context;

    private UIList<DependencyInfo> dependencies;
    private final Binding<DependencyInfo> dependencyInfoBinding = new ReadOnlyBinding<DependencyInfo>() {
        @Override
        public DependencyInfo get() {
            if (dependencies.getSelection() != null) {
                return dependencies.getSelection();
            }
            return null;
        }
    };

    private UIList<Module> modules;
    private final Binding<Module> moduleInfoBinding = new ReadOnlyBinding<Module>() {
        @Override
        public Module get() {
            if (modules.getSelection() != null) {
                return modules.getSelection();
            }
            return null;
        }
    };
    private UILabel moduleName;
    private UILabel installedVersion;
    private UILabel minSupportedVersion;
    private UILabel maxSupportedVersion;
    private UILabel onlineVersion;
    private UILabel required;
    private UIButtonWebBrowser openInBrowser;
    private UIButton updateModuleButton;
    private UIText description;
    private UIButton close;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        initWidgets();

        if (isScreenValid()) {

            setUpModules();
            setUpDependencies();
            setUpUpdateModuleButton();
            bindDependencyDescription();

            close.subscribe(button -> triggerBackAnimation());

            description.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    if (moduleInfoBinding.get() != null) {
                        return getModuleDescription(moduleInfoBinding.get());
                    }
                    return "";
                }
            });
        }
    }

    private void initWidgets() {
        moduleName = find("moduleName", UILabel.class);
        installedVersion = find("installedVersion", UILabel.class);
        minSupportedVersion = find("minSupportedVersion", UILabel.class);
        maxSupportedVersion = find("maxSupportedVersion", UILabel.class);
        onlineVersion = find("onlineVersion", UILabel.class);
        required = find("required", UILabel.class);
        modules = find("modules", UIList.class);
        dependencies = find("dependencies", UIList.class);
        openInBrowser = find("openInBrowser", UIButtonWebBrowser.class);
        updateModuleButton = find("update", UIButton.class);
        description = find("description", UIText.class);
        close = find("close", UIButton.class);
    }

    @Override
    public void onOpened() {
        super.onOpened();

        if (!isScreenValid()) {
            final MessagePopup popup = getManager().createScreen(MessagePopup.ASSET_URI, MessagePopup.class);
            popup.setMessage(
                    translationSystem.translate("${engine:menu#game-details-errors-message-title}"),
                    translationSystem.translate("${engine:menu#game-details-errors-message-body}"));
            popup.subscribeButton(e -> triggerBackAnimation());
            getManager().pushScreen(popup);
            // disable child widgets
            setEnabled(false);
        }
    }

    private void setUpModules() {
        modules.subscribeSelection(((widget, item) -> {
            if (item != null) {
                dependencies.setList(getSortedDependencies(item));
                dependencies.setSelection(null);
                dependencies.select(0);
                this.updateOpenInBrowserButton();
            }
        }));
        modules.setItemRenderer(new StringTextRenderer<Module>() {
            @Override
            public String getString(Module value) {
                if (value != null) {
                    return value.getMetadata().getDisplayName().toString();
                }
                return "";
            }

            @Override
            public void draw(Module value, Canvas canvas) {
                if (!validateModuleDependencies(value.getId())) {
                    canvas.setMode(("invalid"));
                } else {
                    canvas.setMode("available");
                }
                canvas.drawText(getString(value), canvas.getRegion());
            }

            @Override
            public Vector2i getPreferredSize(Module value, Canvas canvas) {
                String text = getString(value);
                return new Vector2i(
                        canvas.getCurrentStyle().getFont().getWidth(text),
                        canvas.getCurrentStyle().getFont().getLineHeight());
            }
        });
    }

    private List<DependencyInfo> getSortedDependencies(final Module module) {
        return module.getMetadata()
                .getDependencies().stream()
                .sorted(Comparator.comparing(DependencyInfo::getId))
                .collect(Collectors.toList());
    }

    private void updateOpenInBrowserButton() {
        openInBrowser.setEnabled(false);
        final Module module = moduleInfoBinding.get();
        if (module == null) {
            return;
        }
        final String moduleOrigin = getOriginModuleUrl(moduleManager.getRegistry().getLatestModuleVersion(module.getMetadata().getId()));
        if (StringUtils.isNotBlank(moduleOrigin)) {
            openInBrowser.setEnabled(true);
            openInBrowser.setUrl(moduleOrigin)
                    .setNuiManager(getManager())
                    .setTranslationSystem(translationSystem);
        }
    }

    private void bindDependencyDescription() {

        moduleName.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                if (dependencyInfoBinding.get() != null) {
                    return String.valueOf(dependencyInfoBinding.get().getId());
                }
                return "";
            }
        });

        installedVersion.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                if (dependencyInfoBinding.get() != null
                        && moduleManager.getRegistry().getLatestModuleVersion(dependencyInfoBinding.get().getId()) != null) {
                    return String.valueOf(moduleManager.getRegistry().getLatestModuleVersion(dependencyInfoBinding.get().getId()).getVersion());
                }
                return "";
            }
        });

        minSupportedVersion.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                if (dependencyInfoBinding.get() != null) {
                    return String.valueOf(dependencyInfoBinding.get().getMinVersion());
                }
                return "";
            }
        });

        maxSupportedVersion.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                if (dependencyInfoBinding.get() != null) {
                    return String.valueOf(dependencyInfoBinding.get().getMaxVersion());
                }
                return "";
            }
        });

        onlineVersion.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                if (dependencyInfoBinding.get() != null) {
                    return getOnlineVersion(dependencyInfoBinding.get());
                }
                return "";
            }
        });

        required.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                if (dependencyInfoBinding.get() != null) {
                    return String.valueOf(!dependencyInfoBinding.get().isOptional());
                }
                return "";
            }
        });
    }

    private boolean validateModuleDependencies(Name moduleName) {
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        return resolver.resolve(moduleName).isSuccess();
    }

    private void setUpDependencies() {
        dependencies.setList(Collections.emptyList());
        dependencies.setItemRenderer(new AbstractItemRenderer<DependencyInfo>() {
            private String getString(DependencyInfo value) {
                if (value != null) {
                    return value.getId().toString();
                }
                return "";
            }

            @Override
            public void draw(DependencyInfo value, Canvas canvas) {
                Module module = moduleManager.getRegistry().getLatestModuleVersion(value.getId());

                if (module == null || !value.versionRange().contains(module.getVersion())) {
                    canvas.setMode("invalid");
                } else {
                    canvas.setMode("available");
                }

                canvas.drawText(getString(value), canvas.getRegion());
            }

            @Override
            public Vector2i getPreferredSize(DependencyInfo value, Canvas canvas) {
                String text = getString(value);
                canvas.getCurrentStyle().setTextColor(Color.RED);
                return new Vector2i(
                        canvas.getCurrentStyle().getFont().getWidth(text),
                        canvas.getCurrentStyle().getFont().getLineHeight());
            }
        });
        dependencies.subscribe(((widget, item) -> {
            if (item != null) {
                modules.getList().stream()
                        .filter(m -> item.getId().equals(m.getId()))
                        .findFirst()
                        .ifPresent(m -> modules.setSelection(m));
            }
        }));
    }

    private void setUpUpdateModuleButton() {

        updateModuleButton.bindEnabled(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                final String online = onlineVersion.getText();
                final String installed = installedVersion.getText();
                if (StringUtils.isNotBlank(online) && StringUtils.isNotBlank(installed)) {
                    return new Version(online).compareTo(new Version(installed)) > 0;
                }
                return false;
            }
        });

        updateModuleButton.subscribe((button -> {
            if (dependencyInfoBinding.get() != null) {
                startDownloadingNewestModulesRequiredFor(dependencyInfoBinding.get());
            }
        }));
    }

    private void startDownloadingNewestModulesRequiredFor(final DependencyInfo dependencyInfo) {
        final Set<Module> modulesToDownload;
        try {
            modulesToDownload = moduleManager.getInstallManager().getAllModulesToDownloadFor(dependencyInfo.getId());
        } catch (DependencyResolutionFailedException ex) {
            MessagePopup messagePopup = getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
            messagePopup.setMessage("Error", ex.getMessage());
            return;
        }
        final ConfirmPopup confirmPopup = getManager().pushScreen(ConfirmPopup.ASSET_URI, ConfirmPopup.class);
        confirmPopup.setMessage("Confirm Download", modulesToDownload.size() + " modules will be downloaded");
        confirmPopup.setOkHandler(() -> downloadModules(modulesToDownload));
    }

    private void downloadModules(Iterable<Module> modulesToDownload) {
        final WaitPopup<List<Module>> popup = getManager().pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
        ModuleInstaller operation = moduleManager.getInstallManager().createInstaller(modulesToDownload,
                new DownloadPopupProgressListener(popup));
        popup.startOperation(operation, true);
    }

    private Set<Module> getAllModuleDependencies(final Collection<Module> modules) {
        return modules.stream()
                .filter(Objects::nonNull)
                .map(Module::getMetadata)
                .map(ModuleMetadata::getDependencies)
                .flatMap(Collection::stream)
                .filter(dep -> Objects.nonNull(dep.getId()))
                .map(dep -> moduleManager.getRegistry().getLatestModuleVersion(dep.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void setModules(final Collection<Module> modules) {
        if (modules != null) {
            final Set<Module> mods = new HashSet<>(modules);
            mods.addAll(getAllModuleDependencies(modules));
            this.modules.setList(
                    mods.stream()
                            .sorted(Comparator.comparing(Module::getId))
                            .collect(Collectors.toList())
            );
        }
    }

    public void setSelectedModule(final Module selectedModule) {
        if (selectedModule != null) {
            this.modules.setSelection(selectedModule);
        } else {
            this.modules.select(0);
        }
    }

    private String getModuleDescription(final Module module) {
        if (module == null || module.getMetadata() == null) {
            return "";
        }
        final ModuleMetadata metadata = module.getMetadata();

        return translationSystem.translate("${engine:menu#game-details-module-id}") + ": " +
                metadata.getId() +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-display-name}") + ": " +
                metadata.getDisplayName() +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-version}") + ": " +
                metadata.getVersion() +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-description}") + ": " +
                metadata.getDescription() +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-permissions}") + ": " +
                String.join(", ", metadata.getRequiredPermissions()) +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-github}") + ": " +
                getOriginModuleUrl(module) +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-author}") + ": " +
                ExtraDataModuleExtension.getAuthor(module) +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-size}") + ": " +
                getRemoteSize(module) +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-last-update-date}") + ": " +
                getLastUpdateDate(module) +
                '\n' +
                translationSystem.translate("${engine:menu#game-details-module-categories}") + ": " +
                getModuleTags(module);
    }

    private String getOriginModuleUrl(Module module) {
        final String origin = ExtraDataModuleExtension.getOrigin(module);
        if (StringUtils.isBlank(origin) && !INTERNAL_MODULES.contains(module.getId().toString())) {
            return DEFAULT_GITHUB_MODULE_URL + module.getId();
        }
        return origin;
    }

    private String getOnlineVersion(final DependencyInfo dependencyInfo) {
        return moduleManager.getInstallManager().getRemoteRegistry().stream()
                .filter(module -> module.getId().equals(dependencyInfo.getId()))
                .findFirst()
                .map(Module::getVersion)
                .map(String::valueOf)
                .orElse("");
    }

    private String getRemoteSize(final Module module) {
        return moduleManager.getInstallManager().getRemoteRegistry().stream()
                .filter(m -> m.getId().equals(module.getId()))
                .findFirst()
                .map(Module::getMetadata)
                .map(RemoteModuleExtension::getArtifactSize)
                .map(m -> m + " bytes")
                .orElse("");
    }

    private String getLastUpdateDate(final Module module) {
        return moduleManager.getInstallManager().getRemoteRegistry().stream()
                .filter(m -> m.getId().equals(module.getId()))
                .findFirst()
                .map(Module::getMetadata)
                .map(RemoteModuleExtension::getLastUpdated)
                .map(DATE_FORMAT::format)
                .orElse("");
    }

    private String getModuleTags(final Module module) {
        return StandardModuleExtension.booleanPropertySet().stream()
                .filter(ext -> ext.isProvidedBy(module))
                .map(StandardModuleExtension::getKey)
                .collect(Collectors.joining(", "));
    }

    private boolean isScreenValid() {
        if (Stream.of(moduleName, installedVersion, minSupportedVersion, maxSupportedVersion, close,
                onlineVersion, required, dependencies, modules, openInBrowser, updateModuleButton, description)
                .anyMatch(Objects::isNull)) {
            logger.error("Can't initialize screen correctly. At least one widget was missed!");
            return false;
        }
        return true;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
