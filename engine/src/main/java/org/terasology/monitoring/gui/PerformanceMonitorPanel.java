/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.monitoring.gui;

import com.google.common.base.Preconditions;
import gnu.trove.map.TObjectDoubleMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;

import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.AbstractListModel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("serial")
public class PerformanceMonitorPanel extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceMonitorPanel.class);

    // If true, the active monitoring thread in executor service should stop.
    private boolean stopThread;

    public PerformanceMonitorPanel() {
        setLayout(new BorderLayout());
        HeaderPanel header = new HeaderPanel();
        JList list = new JList(new PerformanceListModel());
        list.setCellRenderer(new PerformanceListRenderer(header));
        list.setVisible(true);
        add(header, BorderLayout.PAGE_START);
        add(list, BorderLayout.CENTER);
    }

    public void stopThread() {
        stopThread = true;
    }

    private static class HeaderPanel extends JPanel {

        private final JLabel lName = new JLabel("Title");
        private final JLabel lMean = new JLabel("Running Means");
        private final JLabel lSpike = new JLabel("Decaying Spikes");

        HeaderPanel() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

            add(lName);
            add(lMean);
            add(lSpike);
        }

        public void setNameSize(Dimension d) {
            lName.setPreferredSize(d);
            doLayout();
        }
    }

    private static class Entry implements Comparable<Entry> {

        public final String name;
        public boolean active;
        public double mean;
        public double spike;

        Entry(String name) {
            this.name = (name == null) ? "" : name;
        }

        @Override
        public int compareTo(Entry o) {
            return name.compareTo(o.name);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Entry) {
                return Objects.equals(name, ((Entry) obj).name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    private static class PerformanceListRenderer implements ListCellRenderer {

        private final MyRenderer renderer;

        PerformanceListRenderer(HeaderPanel header) {
            renderer = new MyRenderer(header);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Entry) {
                renderer.setEntry((Entry) value);
            } else {
                renderer.setEntry(null);
            }
            return renderer;
        }

        private static class MyRenderer extends JPanel {

            private final HeaderPanel header;
            private final DecimalFormat format = new DecimalFormat("#####0.00");
            private final JLabel lName = new JLabel();
            private final JLabel lMean = new JLabel();
            private final JLabel lSpike = new JLabel();

            private Dimension dName = new Dimension(0, 0);

            MyRenderer(HeaderPanel header) {
                this.header = Preconditions.checkNotNull(header, "The parameter 'header' must not be null");

                setBackground(Color.WHITE);
                setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

                lMean.setHorizontalAlignment(SwingConstants.RIGHT);
                lMean.setForeground(Color.GRAY);
                lMean.setPreferredSize(header.lMean.getPreferredSize());

                lSpike.setHorizontalAlignment(SwingConstants.RIGHT);
                lSpike.setForeground(Color.GRAY);
                lSpike.setPreferredSize(header.lSpike.getPreferredSize());

                add(lName);
                add(lMean);
                add(lSpike);
            }

            public void setEntry(Entry entry) {
                if (entry != null) {
                    lName.setPreferredSize(null);
                    lName.setForeground(entry.active ? Color.BLUE : Color.GRAY);
                    lName.setText(entry.name);
                    Dimension tmp = lName.getPreferredSize();
                    if (tmp.width > dName.width || tmp.height > dName.height) {
                        dName = tmp;
                        header.setNameSize(dName);
                    }
                    lName.setPreferredSize(dName);

                    lMean.setText("  " + format.format(entry.mean) + " ms");
                    lSpike.setText("  " + format.format(entry.spike) + " ms");
                } else {
                    lName.setText("");
                    lMean.setText("");
                    lSpike.setText("");
                }
            }
        }
    }

    private final class PerformanceListModel extends AbstractListModel {

        private final List<Entry> list = new ArrayList<>();
        private final Map<String, Entry> map = new HashMap<>();
        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        private PerformanceListModel() {
            executor.execute(() -> {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                try {
                    while (!stopThread) {
                        Thread.sleep(1000);
                        try (ThreadActivity ignored = ThreadMonitor.startThreadActivity("Poll")) {
                            updateEntries(PerformanceMonitor.getRunningMean(), PerformanceMonitor.getDecayingSpikes());
                        }
                    }
                } catch (Exception e) {
                    ThreadMonitor.addError(e);
                    LOGGER.error("Error executing performance monitor update", e);
                }

                executor.shutdownNow();
            });
        }

        private void invokeIntervalAdded(final int a, final int b) {
            final Object source = this;
            SwingUtilities.invokeLater(() -> fireIntervalAdded(source, a, b));
        }

        private void invokeContentsChanged(final int a, final int b) {
            final Object source = this;
            SwingUtilities.invokeLater(() -> fireContentsChanged(source, a, b));
        }

        private void updateEntries(TObjectDoubleMap<String> means, TObjectDoubleMap<String> spikes) {
            if (means != null) {
                for (final Entry entry : list) {
                    entry.active = false;
                }
                means.forEachEntry((key, value) -> {
                    Entry entry = map.get(key);
                    if (entry == null) {
                        entry = new Entry(key);
                        list.add(entry);
                        map.put(key, entry);
                        invokeIntervalAdded(list.size() - 1, list.size() - 1);
                    }
                    entry.active = true;
                    entry.mean = value;
                    return true;
                });
                spikes.forEachEntry((key, value) -> {
                    Entry entry = map.get(key);
                    if (entry != null) {
                        entry.spike = value;
                    }
                    return true;
                });

                Collections.sort(list);
                invokeContentsChanged(0, list.size() - 1);
            }
        }

        @Override
        public int getSize() {
            return list.size();
        }

        @Override
        public Object getElementAt(int index) {
            return list.get(index);
        }
    }
}
