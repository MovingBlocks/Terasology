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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
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
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.ScrollableArea;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.telemetry.logstash.TelemetryLogstashAppender;
import org.terasology.telemetry.metrics.Metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The metric menu.
 */
public class TelemetryScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:telemetryScreen");

    @In
    private Config config;

    @In
    private ModuleManager moduleManager;

    @In
    private TranslationSystem translationSystem;

    @In
    private Metrics metrics;

    private static final Logger logger = LoggerFactory.getLogger(TelemetryScreen.class);


    private final int horizontalSpacing = 12;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());
        ColumnLayout mainLayout = new ColumnLayout();
        mainLayout.setHorizontalSpacing(8);
        mainLayout.setVerticalSpacing(8);

        Map<TelemetryCategory, Class> telemetryCategories = fetchTelemetryCategoriesFromEnvironment();

        for (Map.Entry<TelemetryCategory,Class> telemetryCategory: telemetryCategories.entrySet()) {
            Class metricClass = telemetryCategory.getValue();
            Metric metricType = metrics.getMap().get(metricClass);
            Map<String,Object> map = metricType.getFieldValueMap();

            addTelemetrySection(telemetryCategory.getKey(),mainLayout,map);
        }

        ScrollableArea area = find("area", ScrollableArea.class);
        area.setContent(mainLayout);

        WidgetUtil.trySubscribe(this, "back", button -> triggerBackAnimation());
        WidgetUtil.tryBindCheckbox(this, "telemetryEnabled", BindHelper.bindBeanProperty("telemetryEnabled", config.getTelemetryConfig(), Boolean.TYPE));
        WidgetUtil.tryBindCheckBoxWithListener(this, "errorReportingEnabled",BindHelper.bindBeanProperty("errorReportingEnabled",config.getTelemetryConfig(),Boolean.TYPE), (checkbox) -> {
            TelemetryLogstashAppender appender = TelemetryUtils.fetchTelemetryLogstashAppender();
            if (config.getTelemetryConfig().isErrorReportingEnabled()) {
                appender.turnOnErrorReporting();
            } else {
                appender.turnOffErrorReporting();
            }
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

    private void addTelemetrySection(TelemetryCategory telemetryCategory, ColumnLayout layout, Map<String,Object> map) {

        UILabel categoryHeader = new UILabel(translationSystem.translate(telemetryCategory.displayName()));
        categoryHeader.setFamily("subheading");
        layout.addWidget(categoryHeader);
        List<Map.Entry> telemetryFields = sortFields(map);
        for (Map.Entry entry : telemetryFields) {
            Object value = entry.getValue();
            if (value instanceof List) {
                List list = (List) value;
                addTelemetryField(entry.getKey().toString(),list,layout);
            }
            else {
                addTelemetryField(entry.getKey().toString(),value,layout);
            }
        }
    }

    private void addTelemetryField(String type, Object value, ColumnLayout layout) {
        RowLayout newRow = new RowLayout(new UILabel(type),new UILabel(value.toString()))
                .setColumnRatios(0.4f)
                .setHorizontalSpacing(horizontalSpacing);

        layout.addWidget(newRow);
    }

    // Will be used if the value is a List
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

    private List<Map.Entry> sortFields(Map<String,Object> map) {
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
