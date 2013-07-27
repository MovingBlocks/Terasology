/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.rendering.gui.windows.metricsScreen;

import gnu.trove.procedure.TObjectIntProcedure;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.gui.widgets.UILabel;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Immortius
 */
final class RunningThreadsMode extends MetricsMode {

    public RunningThreadsMode() {
        super("Running Threads", true, true);
    }

    @Override
    public void updateLines(List<UILabel> lines) {
        final SortedSet<String> threads = new TreeSet<>();
        PerformanceMonitor.getRunningThreads().forEachEntry(new TObjectIntProcedure<String>() {
            public boolean execute(String s, int i) {
                threads.add(String.format("%s (%d)", s, i));
                return true;
            }
        });
        int line = 0;
        for (String thread : threads) {
            lines.get(line).setVisible(true);
            lines.get(line).setText(thread);
            line++;
            if (line >= lines.size()) {
                break;
            }
        }
        for (; line < lines.size(); line++) {
            lines.get(line).setVisible(false);
        }
    }
}
