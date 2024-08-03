// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry;

import com.google.common.collect.Maps;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.ServerInfo;
import org.terasology.engine.config.TelemetryConfig;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.AddServerPopup;
import org.terasology.engine.telemetry.logstash.TelemetryLogstashAppender;
import org.terasology.engine.telemetry.metrics.Metric;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.BindHelper;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.layouts.ColumnLayout;
import org.terasology.nui.layouts.RowLayout;
import org.terasology.nui.layouts.ScrollableArea;
import org.terasology.nui.widgets.UICheckbox;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UISpace;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The metrics menu lists the telemetry field names and values that will be sent to the server. Users can enable or
 * disable telemetry function in this menu.
 */
public class TelemetryScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:telemetryScreen");

    private static final Logger logger = LoggerFactory.getLogger(TelemetryScreen.class);
    private static final int HORIZONTAL_SPACING = 12;

    @In
    private Config config;

    @In
    private ModuleManager moduleManager;

    @In
    private TranslationSystem translationSystem;

    @In
    private Metrics metrics;

    @In
    private NUIManager nuiManager;

    @In
    private Emitter emitter;


    private Map<TelemetryCategory, Class> telemetryCategories;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());
        refreshContent();

        WidgetUtil.trySubscribe(this, "back", button -> triggerBackAnimation());
        WidgetUtil.tryBindCheckBoxWithListener(this, "telemetryEnabled", BindHelper.bindBeanProperty(
                "telemetryEnabled", config.getTelemetryConfig(), Boolean.TYPE), (checkbox) -> {
            if (config.getTelemetryConfig().isTelemetryEnabled()) {
                pushAddServerPopupAndStartEmitter();
            }
        });
        WidgetUtil.tryBindCheckBoxWithListener(this, "errorReportingEnabled", BindHelper.bindBeanProperty(
                "errorReportingEnabled", config.getTelemetryConfig(), Boolean.TYPE), (checkbox) -> {
            if (config.getTelemetryConfig().isErrorReportingEnabled()) {
                pushAddServerPopupAndStartLogBackAppender();
            } else {
                TelemetryLogstashAppender telemetryLogstashAppender = TelemetryUtils.fetchTelemetryLogstashAppender();
                if (telemetryLogstashAppender != null) {
                    telemetryLogstashAppender.stop();
                }
            }
        });

        addEnablingAllTelemetryListener();
    }

    @Override
    public void onOpened() {
        super.onOpened();
        refreshContent();

        addGroupEnablingListener();
    }

    /**
     * Add a listener to the telemetryEnable checkbox. If the checkbox os enabled/disabled, it will enable/disable all
     * the telemetry field.
     */
    private void addEnablingAllTelemetryListener() {
        UICheckbox uiCheckbox = this.find("telemetryEnabled", UICheckbox.class);
        if (uiCheckbox != null) {
            uiCheckbox.subscribe((checkbox) -> {
                boolean telemetryEnabled = config.getTelemetryConfig().isTelemetryEnabled();
                Map<String, Boolean> bindingMap =
                        config.getTelemetryConfig().getMetricsUserPermissionConfig().getBindingMap();
                for (Map.Entry<String, Boolean> entry : bindingMap.entrySet()) {
                    entry.setValue(telemetryEnabled);
                }

                fetchTelemetryCategoriesFromEngineOnlyEnvironment();
                for (Map.Entry<TelemetryCategory, Class> telemetryCategory : telemetryCategories.entrySet()) {
                    UICheckbox categoryBox = this.find(telemetryCategory.getKey().id(), UICheckbox.class);
                    if (categoryBox != null) {
                        categoryBox.setEnabled(telemetryEnabled);
                    }
                    Set<Field> fields = ReflectionUtils.getFields(telemetryCategory.getValue(),
                            ReflectionUtils.withAnnotation(TelemetryField.class));
                    for (Field field : fields) {
                        String fieldName = telemetryCategory.getKey().id() + ":" + field.getName();
                        UICheckbox fieldBox = this.find(fieldName, UICheckbox.class);
                        if (fieldBox != null) {
                            fieldBox.setEnabled(telemetryEnabled);
                        }
                    }
                }
            });
        }
    }

    /**
     * Add a listener to the checkbox in the telemetry category row. If this checkbox is checked, all the sub telemetry
     * fields will be enabled/disabled.
     */
    private void addGroupEnablingListener() {
        fetchTelemetryCategoriesFromEngineOnlyEnvironment();
        for (Map.Entry<TelemetryCategory, Class> telemetryCategory : telemetryCategories.entrySet()) {
            if (!telemetryCategory.getKey().isOneMapMetric()) {
                UICheckbox uiCheckbox = this.find(telemetryCategory.getKey().id(), UICheckbox.class);
                if (uiCheckbox == null) {
                    continue;
                }
                uiCheckbox.subscribe((checkbox) -> {
                    Map<String, Boolean> bindingMap =
                            config.getTelemetryConfig().getMetricsUserPermissionConfig().getBindingMap();
                    if (bindingMap.containsKey(telemetryCategory.getKey().id())) {
                        boolean isGroupEnable = bindingMap.get(telemetryCategory.getKey().id());
                        Set<Field> fields = ReflectionUtils.getFields(telemetryCategory.getValue(),
                                ReflectionUtils.withAnnotation(TelemetryField.class));
                        for (Field field : fields) {
                            String fieldName = telemetryCategory.getKey().id() + ":" + field.getName();
                            bindingMap.put(fieldName, isGroupEnable);
                        }
                    }
                });
            }
        }
    }

    private void refreshContent() {
        ColumnLayout mainLayout = new ColumnLayout();
        mainLayout.setHorizontalSpacing(8);
        mainLayout.setVerticalSpacing(8);
        fetchTelemetryCategoriesFromEngineOnlyEnvironment();

        for (Map.Entry<TelemetryCategory, Class> telemetryCategory : telemetryCategories.entrySet()) {
            Class metricClass = telemetryCategory.getValue();
            Optional<Metric> optional = metrics.getMetric(metricClass);
            if (optional.isPresent()) {
                Metric metric = optional.get();
                Map<String, ?> map = metric.createTelemetryFieldToValue();
                if (map != null) {
                    addTelemetrySection(telemetryCategory.getKey(), mainLayout, map);
                }
            }
        }

        ScrollableArea area = find("area", ScrollableArea.class);
        if (area != null) {
            area.setContent(mainLayout);
        }
    }

    private void pushAddServerPopupAndStartEmitter() {
        AddServerPopup addServerPopup = nuiManager.pushScreen(AddServerPopup.ASSET_URI, AddServerPopup.class);
        addServerPopup.removeTip();
        ServerInfo serverInfo;
        TelemetryConfig telemetryConfig = config.getTelemetryConfig();
        String telemetryDestination = telemetryConfig.getTelemetryDestination();
        if (telemetryDestination != null) {
            try {
                URL url = new URL(telemetryDestination);
                String address = url.getHost();
                int port = url.getPort();
                serverInfo = new ServerInfo(telemetryConfig.getTelemetryServerName(), address, port);
                serverInfo.setOwner(telemetryConfig.getTelemetryServerOwner());
            } catch (Exception e) {
                logger.error("Exception when get telemetry server information", e);
                serverInfo = new ServerInfo(TelemetryEmitter.DEFAULT_COLLECTOR_NAME,
                        TelemetryEmitter.DEFAULT_COLLECTOR_HOST, TelemetryEmitter.DEFAULT_COLLECTOR_PORT);
                serverInfo.setOwner(TelemetryEmitter.DEFAULT_COLLECTOR_OWNER);
            }
        } else {
            serverInfo = new ServerInfo(TelemetryEmitter.DEFAULT_COLLECTOR_NAME,
                    TelemetryEmitter.DEFAULT_COLLECTOR_HOST, TelemetryEmitter.DEFAULT_COLLECTOR_PORT);
            serverInfo.setOwner(TelemetryEmitter.DEFAULT_COLLECTOR_OWNER);
        }
        addServerPopup.setServerInfo(serverInfo);
        addServerPopup.onSuccess((item) -> {
            TelemetryEmitter telemetryEmitter = (TelemetryEmitter) emitter;
            Optional<URL> optionalURL = item.getURL("http");
            if (optionalURL.isPresent()) {
                telemetryEmitter.changeUrl(optionalURL.get());

                // Save the telemetry destination
                telemetryConfig.setTelemetryDestination(optionalURL.get().toString());
                telemetryConfig.setTelemetryServerName(item.getName());
                telemetryConfig.setTelemetryServerOwner(item.getOwner());
            }
        });
        addServerPopup.onCancel((button) -> config.getTelemetryConfig().setTelemetryEnabled(false));
    }

    private void pushAddServerPopupAndStartLogBackAppender() {

        AddServerPopup addServerPopup = nuiManager.pushScreen(AddServerPopup.ASSET_URI, AddServerPopup.class);
        addServerPopup.removeTip();
        ServerInfo serverInfo;
        TelemetryConfig telemetryConfig = config.getTelemetryConfig();
        if (telemetryConfig.getErrorReportingDestination() != null) {
            try {
                URL url = new URL("http://" + telemetryConfig.getErrorReportingDestination());
                serverInfo = new ServerInfo(telemetryConfig.getErrorReportingServerName(), url.getHost(),
                        url.getPort());
                serverInfo.setOwner(telemetryConfig.getErrorReportingServerOwner());
            } catch (Exception e) {
                logger.error("Exception when get telemetry server information", e);
                serverInfo = new ServerInfo(TelemetryLogstashAppender.DEFAULT_LOGSTASH_NAME,
                        TelemetryLogstashAppender.DEFAULT_LOGSTASH_HOST,
                        TelemetryLogstashAppender.DEFAULT_LOGSTASH_PORT);
                serverInfo.setOwner(TelemetryLogstashAppender.DEFAULT_LOGSTASH_OWNER);
            }
        } else {
            serverInfo = new ServerInfo(TelemetryLogstashAppender.DEFAULT_LOGSTASH_NAME,
                    TelemetryLogstashAppender.DEFAULT_LOGSTASH_HOST,
                    TelemetryLogstashAppender.DEFAULT_LOGSTASH_PORT);
            serverInfo.setOwner(TelemetryLogstashAppender.DEFAULT_LOGSTASH_OWNER);
        }
        addServerPopup.setServerInfo(serverInfo);
        addServerPopup.onSuccess((item) -> {
            String destinationLogstash = item.getAddress() + ":" + item.getPort();
            TelemetryLogstashAppender telemetryLogstashAppender = TelemetryUtils.fetchTelemetryLogstashAppender();
            if (telemetryLogstashAppender != null) {
                telemetryLogstashAppender.addDestination(destinationLogstash);
                telemetryLogstashAppender.start();
            }

            // Save the destination
            telemetryConfig.setErrorReportingDestination(destinationLogstash);
            telemetryConfig.setErrorReportingServerName(item.getName());
            telemetryConfig.setErrorReportingServerOwner(item.getOwner());
        });
        addServerPopup.onCancel((button) -> telemetryConfig.setErrorReportingEnabled(false));
    }

    /**
     * refresh the telemetryCategories map.
     */
    private void fetchTelemetryCategoriesFromEngineOnlyEnvironment() {
        telemetryCategories = Maps.newHashMap();
        try (ModuleEnvironment environment = moduleManager.loadEnvironment(Collections.emptySet(), false)) {
            for (Class<?> holdingType : environment.getTypesAnnotatedWith(TelemetryCategory.class)) {
                TelemetryCategory telemetryCategory = holdingType.getAnnotation(TelemetryCategory.class);
                telemetryCategories.put(telemetryCategory, holdingType);
            }
        }
    }

    /**
     * Add a new section with represents a new metrics type.
     *
     * @param telemetryCategory the annotation of the new metric
     * @param layout the layout where the new section will be added
     * @param map the map which includes the telemetry field name and value
     */
    private void addTelemetrySection(TelemetryCategory telemetryCategory, ColumnLayout layout, Map<String, ?> map) {
        UILabel categoryHeader = new UILabel(translationSystem.translate(telemetryCategory.displayName()));
        categoryHeader.setFamily("subheading");
        UICheckbox uiCheckbox = new UICheckbox(telemetryCategory.id());
        Map<String, Boolean> bindingMap = config.getTelemetryConfig().getMetricsUserPermissionConfig().getBindingMap();
        if (!bindingMap.containsKey(telemetryCategory.id())) {
            bindingMap.put(telemetryCategory.id(), config.getTelemetryConfig().isTelemetryEnabled());
        }
        Binding<Boolean> binding = getBindingFromBooleanMap(bindingMap, telemetryCategory.id());
        uiCheckbox.bindChecked(binding);
        RowLayout newRow = new RowLayout(categoryHeader, new UISpace(), uiCheckbox)
                .setColumnRatios(0.4f, 0.5f, 0.1f)
                .setHorizontalSpacing(HORIZONTAL_SPACING);
        layout.addWidget(newRow);

        List<Map.Entry<String, ?>> telemetryFields = sortFields(map);
        for (Map.Entry entry : telemetryFields) {
            Object value = entry.getValue();
            if (value == null) {
                value = "Value unknown yet";
            }
            boolean isWithCheckbox = !telemetryCategory.isOneMapMetric();
            if (value instanceof List) {
                List list = (List) value;
                addTelemetryField(entry.getKey().toString(), list, layout, isWithCheckbox, telemetryCategory);
            } else {
                addTelemetryField(entry.getKey().toString(), value, layout, isWithCheckbox, telemetryCategory);
            }
        }
    }

    /**
     * Get a binding to a map boolean value.
     *
     * @param bindingMap the map.
     * @param fieldName the key associate to the binding value in the map.
     * @return
     */
    private Binding<Boolean> getBindingFromBooleanMap(Map<String, Boolean> bindingMap, String fieldName) {
        return new Binding<Boolean>() {
            @Override
            public Boolean get() {
                return bindingMap.get(fieldName);
            }

            @Override
            public void set(Boolean value) {
                bindingMap.put(fieldName, value);
            }
        };
    }

    /**
     * Add a new row in the menu, the new row includes the field name and value.
     *
     * @param type the type(name) of the this field
     * @param value the value of this field
     * @param layout the layout where the new line will be added
     * @param isWithCheckbox whether add a check box in the line
     * @param telemetryCategory the TelemetryCategory that this field belongs to
     */
    private void addTelemetryField(String type, Object value, ColumnLayout layout, boolean isWithCheckbox,
                                   TelemetryCategory telemetryCategory) {
        RowLayout newRow;
        if (isWithCheckbox) {
            String fieldName = telemetryCategory.id() + ":" + type;
            UICheckbox uiCheckbox = new UICheckbox(fieldName);
            Map<String, Boolean> bindingMap =
                    config.getTelemetryConfig().getMetricsUserPermissionConfig().getBindingMap();
            if (!bindingMap.containsKey(fieldName)) {
                bindingMap.put(fieldName, config.getTelemetryConfig().isTelemetryEnabled());
            }
            Binding<Boolean> binding = getBindingFromBooleanMap(bindingMap, fieldName);
            uiCheckbox.bindChecked(binding);
            uiCheckbox.subscribe((checkbox) -> {
                if (bindingMap.get(fieldName)) {
                    bindingMap.put(telemetryCategory.id(), true);
                } else {
                    Set<Field> fields = ReflectionUtils.getFields(telemetryCategories.get(telemetryCategory),
                            ReflectionUtils.withAnnotation(TelemetryField.class));
                    boolean isOneEnabled = false;
                    for (Field field : fields) {
                        isOneEnabled = isOneEnabled || bindingMap.get(telemetryCategory.id() + ":" + field.getName());
                    }
                    if (!isOneEnabled) {
                        bindingMap.put(telemetryCategory.id(), false);
                    }
                }
            });
            newRow = new RowLayout(new UILabel(type), new UILabel(value.toString()), uiCheckbox)
                    .setColumnRatios(0.4f, 0.5f, 0.1f)
                    .setHorizontalSpacing(HORIZONTAL_SPACING);
        } else {
            newRow = new RowLayout(new UILabel(type), new UILabel(value.toString()))
                    .setColumnRatios(0.4f, 0.5f)
                    .setHorizontalSpacing(HORIZONTAL_SPACING);
        }

        layout.addWidget(newRow);
    }

    /**
     * If the field value is a list, then will add more than one rows. Each new line includes the field name with index
     * and its value.
     *
     * @param type the type(name) of the this field
     * @param value the value of this field (a List)
     * @param layout the layout where the new line will be added
     * @param isWithCheckbox whether add a check box in the line
     */
    private void addTelemetryField(String type, List value, ColumnLayout layout, boolean isWithCheckbox,
                                   TelemetryCategory telemetryCategory) {
        int moduleCount = 1;
        for (Object o : value) {
            StringBuilder sb = new StringBuilder();
            sb.append(type);
            sb.append(" ");
            sb.append(moduleCount);
            addTelemetryField(sb.toString(), o, layout, isWithCheckbox, telemetryCategory);
            moduleCount = moduleCount + 1;
        }
    }

    /**
     * Sorts the fields by the name of each fields.
     *
     * @param map the map that will be sorted
     * @return a list of map entry that is ordered by fields' names
     */
    private List<Map.Entry<String, ?>> sortFields(Map<String, ?> map) {
        List<Map.Entry<String, ?>> list = new ArrayList<>(map.entrySet());
        list.sort(Comparator.comparing(Map.Entry::getKey));
        return list;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
