/*
 * Copyright 2018 MovingBlocks
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

import javax.swing.SwingWorker;
import javax.swing.JPanel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Objects;

public class MyPerformanceMonitorPanel extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyPerformanceMonitorPanel.class);

    private DefaultListModel performanceListModel;
    private UpdateRunner updateRunner;

    public MyPerformanceMonitorPanel() {
        performanceListModel = new DefaultListModel();

        setLayout(new BorderLayout());
        HeaderPanel header = new HeaderPanel();

        JList list = new JList(performanceListModel);
        list.setCellRenderer(new PerformanceListRenderer(header));
        list.setVisible(true);

        add(header, BorderLayout.PAGE_START);
        add(list, BorderLayout.CENTER);

        updateRunner = new UpdateRunner();
        updateRunner.execute();
    }

    private void updateList(List<Entry> entries) {
        performanceListModel.removeAllElements();
        for (Entry entry : entries) {
            performanceListModel.addElement(entry);
        }
    }

    public void cancelThreadsRunning() {
        LOGGER.info("Closing SwingWorker threads for Performance Monitor Panel...");
        updateRunner.cancel(true);
    }

    private class UpdateRunner extends SwingWorker<List<Entry>, List<Entry>> {
        private final List<Entry> performanceList = new ArrayList<>();
        private final Map<String, Entry> map = new HashMap<>();

        @Override
        protected List<Entry> doInBackground() throws Exception {
            while (!isCancelled()) {
                Thread.sleep(1000);
                updateEntries(PerformanceMonitor.getRunningMean(), PerformanceMonitor.getDecayingSpikes());

                publish(new ArrayList<>(performanceList));
            }

            return null;
        }

        protected void process(List<List<Entry>> entries) {
            for (List<Entry> entry : entries) {
                updateList(entry);
            }
        }

        private void updateEntries(TObjectDoubleMap<String> means, TObjectDoubleMap<String> spikes) {
            if (means != null) {
                for (final Entry entry : performanceList) {
                    entry.active = false;
                }

                means.forEachEntry((key, value) -> {
                    Entry entry = map.get(key);
                    if (entry == null) {
                        entry = new Entry(key);
                        performanceList.add(entry);
                        map.put(key, entry);
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

                Collections.sort(performanceList);
            }
        }
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

        private final PerformanceListRenderer.MyRenderer renderer;

        PerformanceListRenderer(HeaderPanel header) {
            renderer = new PerformanceListRenderer.MyRenderer(header);
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

                setBackground(Color.white);
                setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));

                lMean.setHorizontalAlignment(SwingConstants.RIGHT);
                lMean.setForeground(Color.gray);
                lMean.setPreferredSize(header.lMean.getPreferredSize());

                lSpike.setHorizontalAlignment(SwingConstants.RIGHT);
                lSpike.setForeground(Color.gray);
                lSpike.setPreferredSize(header.lSpike.getPreferredSize());

                add(lName);
                add(lMean);
                add(lSpike);
            }

            public void setEntry(Entry entry) {
                if (entry != null) {
                    lName.setPreferredSize(null);
                    lName.setForeground(entry.active ? Color.blue : Color.gray);
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
}
