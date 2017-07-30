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
package org.terasology.telemetry;

import com.google.common.collect.Maps;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.ServerInfo;
import org.terasology.engine.module.ModuleManager;
import org.terasology.i18n.TranslationSystem;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.module.predicates.FromModule;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.layers.mainMenu.AddServerPopup;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.ScrollableArea;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.telemetry.logstash.TelemetryLogstashAppender;
import org.terasology.telemetry.metrics.Metric;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The metrics menu lists the telemetry field names and values that will be sent to the server.
 * Users can enable or disable telemetry function in this menu.
 */
public class TelemetryScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:telemetryScreen");

    private static final Logger logger = LoggerFactory.getLogger(TelemetryScreen.class);

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

    private final int horizontalSpacing = 12;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());
        refreshContent();

        WidgetUtil.trySubscribe(this, "back", button -> triggerBackAnimation());
        WidgetUtil.tryBindCheckBoxWithListener(this, "telemetryEnabled", BindHelper.bindBeanProperty("telemetryEnabled", config.getTelemetryConfig(), Boolean.TYPE),(checkbox) -> {
            if (config.getTelemetryConfig().isTelemetryEnabled()) {
                pushAddServerPopupAndStartEmitter();
            }
        });
        WidgetUtil.tryBindCheckBoxWithListener(this, "errorReportingEnabled", BindHelper.bindBeanProperty("errorReportingEnabled", config.getTelemetryConfig(), Boolean.TYPE), (checkbox) -> {
            if (config.getTelemetryConfig().isErrorReportingEnabled()) {
                pushAddServerPopupAndStartLogBackAppender();
            } else {
                TelemetryLogstashAppender telemetryLogstashAppender = TelemetryUtils.fetchTelemetryLogstashAppender();
                telemetryLogstashAppender.stop();
            }
        });
    }

    @Override
    public void onOpened() {
        super.onOpened();
        refreshContent();
    }

    private void refreshContent() {
        ColumnLayout mainLayout = new ColumnLayout();
        mainLayout.setHorizontalSpacing(8);
        mainLayout.setVerticalSpacing(8);
        Map<TelemetryCategory, Class> telemetryCategories = fetchTelemetryCategoriesFromEnvironment();

        for (Map.Entry<TelemetryCategory, Class> telemetryCategory: telemetryCategories.entrySet()) {
            Class metricClass = telemetryCategory.getValue();
            Optional<Metric> optional = metrics.getMetric(metricClass);
            if (optional.isPresent()) {
                Metric metric = optional.get();
                Map<String, ?> map = metric.getFieldValueMap();
                if (map != null) {
                    addTelemetrySection(telemetryCategory.getKey(), mainLayout, map);
                }
            }
        }

        ScrollableArea area = find("area", ScrollableArea.class);
        area.setContent(mainLayout);
    }

    private void pushAddServerPopupAndStartEmitter() {
        AddServerPopup addServerPopup = nuiManager.pushScreen(AddServerPopup.ASSET_URI, AddServerPopup.class);
        addServerPopup.removeTip();
        ServerInfo serverInfo = new ServerInfo("TelemetryCollector", TelemetryEmitter.DEFAULT_COLLECTOR_HOST, TelemetryEmitter.DEFAULT_COLLECTOR_PORT);
        serverInfo.setOwner(TelemetryEmitter.DEFAULT_COLLECTOR_OWNER);
        addServerPopup.setServerInfo(serverInfo);
        addServerPopup.onSuccess((item) -> {
            TelemetryEmitter telemetryEmitter = (TelemetryEmitter) emitter;
            Optional<URL> optionalURL = item.getURL("http");
            if (optionalURL.isPresent()) {
                telemetryEmitter.changeUrl(optionalURL.get());

                // Save the telemetry destination
                config.getTelemetryConfig().setTelemetryDestination(optionalURL.get().toString());
            }
        });
        addServerPopup.onCancel((button) -> {
            config.getTelemetryConfig().setTelemetryEnabled(false);
        });
    }

    private void pushAddServerPopupAndStartLogBackAppender() {

        AddServerPopup addServerPopup = nuiManager.pushScreen(AddServerPopup.ASSET_URI, AddServerPopup.class);
        addServerPopup.removeTip();
        ServerInfo serverInfo = new ServerInfo("TelemetryCollector", TelemetryLogstashAppender.DEFAULT_LOGSTASH_HOST, TelemetryLogstashAppender.DEFAULT_LOGSTASH_PORT);
        serverInfo.setOwner(TelemetryLogstashAppender.DEFAULT_LOGSTASH_OWNER);
        addServerPopup.setServerInfo(serverInfo);
        addServerPopup.onSuccess((item) -> {
            StringBuilder destinationLogstash = new StringBuilder();
            destinationLogstash.append(item.getAddress());
            destinationLogstash.append(":");
            destinationLogstash.append(item.getPort());
            TelemetryLogstashAppender telemetryLogstashAppender = TelemetryUtils.fetchTelemetryLogstashAppender();
            telemetryLogstashAppender.addDestination(destinationLogstash.toString());
            telemetryLogstashAppender.start();

            // Save the destination
            config.getTelemetryConfig().setErrorReportingDestination(destinationLogstash.toString());
        });
        addServerPopup.onCancel((button) -> {
            config.getTelemetryConfig().setErrorReportingEnabled(false);
        });
    }

    private Map<TelemetryCategory, Class> fetchTelemetryCategoriesFromEnvironment() {
        Map<TelemetryCategory, Class> telemetryCategories = Maps.newHashMap();

        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (module.isCodeModule()) {
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    try (ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), false)) {
                        for (Class<?> holdingType : environment.getTypesAnnotatedWith(TelemetryCategory.class, new FromModule(environment, moduleId))) {
                            TelemetryCategory telemetryCategory = holdingType.getAnnotation(TelemetryCategory.class);
                            telemetryCategories.put(telemetryCategory, holdingType);
                        }
                    }
                }
            }
        }

        return telemetryCategories;
    }

    /**
     * Add a new section with represents a new metrics type.
     * @param telemetryCategory the annotation of the new metric
     * @param layout the layout where the new section will be added
     * @param map the map which includes the telemetry field name and value
     */
    private void addTelemetrySection(TelemetryCategory telemetryCategory, ColumnLayout layout, Map<String, ?> map) {

        UILabel categoryHeader = new UILabel(translationSystem.translate(telemetryCategory.displayName()));
        categoryHeader.setFamily("subheading");
        layout.addWidget(categoryHeader);
        List<Map.Entry> telemetryFields = sortFields(map);
        for (Map.Entry entry : telemetryFields) {
            Object value = entry.getValue();
            if (value == null) {
                value = "Value Unknown";
            }
            if (value instanceof List) {
                List list = (List) value;
                addTelemetryField(entry.getKey().toString(), list, layout);
            } else {
                addTelemetryField(entry.getKey().toString(), value, layout);
            }
        }
    }

    /**
     * Add a new row in the menu, the new row includes the field name and value.
     * @param type the type(name) of the this field
     * @param value the value of this field
     * @param layout the layout where the new line will be added
     */
    private void addTelemetryField(String type, Object value, ColumnLayout layout) {
        RowLayout newRow = new RowLayout(new UILabel(type), new UILabel(value.toString()))
                .setColumnRatios(0.4f)
                .setHorizontalSpacing(horizontalSpacing);

        layout.addWidget(newRow);
    }

    /**
     * If the field value is a list, then will add more than one rows.
     * Each new line includes the field name with index and its value.
     * @param type the type(name) of the this field
     * @param value the value of this field (a List)
     * @param layout the layout where the new line will be added
     */
    private void addTelemetryField(String type, List value, ColumnLayout layout) {
        int moduleCount = 1;
        for (Object o : value) {
            StringBuilder sb = new StringBuilder();
            sb.append(type);
            sb.append(" ");
            sb.append(moduleCount);
            addTelemetryField(sb.toString(), o, layout);
            moduleCount = moduleCount + 1;
        }
    }

    /**
     * Sorts the fields by the name of each fields.
     * @param map the map that will be sorted
     * @return a list of map entry that is ordered by fields' names
     */
    private List<Map.Entry> sortFields(Map<String, ?> map) {
        List<Map.Entry> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry>() {
            @Override
            public int compare(Map.Entry o1, Map.Entry o2) {
                return o1.getKey().toString().compareTo(o2.getKey().toString());
            }
        });

        return list;
    }
}
