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
package org.terasology.telemetry.metrics;

import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import org.terasology.context.Context;
import org.terasology.engine.module.ModuleManager;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.telemetry.TelemetryCategory;
import org.terasology.telemetry.TelemetryField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is a metric for game modules in current context.
 * Metric includes module name and module version.
 */
@TelemetryCategory(id = "modules",
        displayName = "${engine:menu#telemetry-modules}",
        isOneMapMetric = true
)
public final class ModulesMetric extends Metric {

    public static final String SCHEMA_MODULES = "iglu:org.terasology/modules/jsonschema/1-0-0";

    @TelemetryField
    private List<Module> modules = new ArrayList<>();

    private Context context;

    public ModulesMetric(Context context) {
        this.context = context;
    }

    @Override
    public Optional<Unstructured> getUnstructuredMetric() {
        createTelemetryFieldToValue();
        return getUnstructuredMetric(SCHEMA_MODULES, telemetryFieldToValue);
    }

    @Override
    public Map<String, ?> createTelemetryFieldToValue() {
        updateModules();
        telemetryFieldToValue = new HashMap();
        for (Module module : modules) {
            telemetryFieldToValue.put(module.getId().toString(), module.getVersion().toString());
        }
        return telemetryFieldToValue;
    }

    private void updateModules() {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        ModuleEnvironment moduleEnvironment = moduleManager.getEnvironment();
        modules.clear();
        Iterator<Module> iterator =  moduleEnvironment.iterator();
        while (iterator.hasNext()) {
            Module next = iterator.next();
            modules.add(next);
        }
    }
}
