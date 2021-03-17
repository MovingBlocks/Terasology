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
package org.terasology.engine.monitoring.gui;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.monitoring.ThreadActivity;
import org.terasology.engine.monitoring.ThreadMonitor;
import org.terasology.engine.monitoring.impl.SingleThreadMonitor;
import org.terasology.engine.monitoring.impl.ThreadMonitorEvent;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class ThreadMonitorPanel extends JPanel {

    private static final Color BACKGROUND = Color.WHITE;
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadMonitorPanel.class);

    /**
     * If true, the active monitoring thread in executor service should stop.
     */
    private boolean stopThread;

    public ThreadMonitorPanel() {
        setLayout(new BorderLayout());
        JList list = new JList(new ThreadListModel());
        list.setCellRenderer(new ThreadListRenderer());
        list.setVisible(true);
        add(list, BorderLayout.CENTER);
    }

    public void stopThread() {
        this.stopThread = true;
    }

    private abstract static class Task {

        private String name;

        Task(String name) {
            this.name = name;
        }

        public abstract void execute();

        public String getName() {
            return name;
        }

    }

    private static class ThreadListRenderer implements ListCellRenderer {

        private final MyRenderer renderer = new MyRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof SingleThreadMonitor) {
                renderer.setMonitor((SingleThreadMonitor) value);
            } else {
                renderer.setMonitor(null);
            }
            return renderer;
        }

        private static class MyRenderer extends JPanel {

            private final JPanel pHead = new JPanel();
            private final JPanel pList = new JPanel();
            private final JLabel lName = new JLabel();
            private final JLabel lId = new JLabel();
            private final JLabel lCounters = new JLabel();
            private final JLabel lActive = new JLabel();
            private final JPanel pError = new JPanel();
            private final JLabel lErrorSpacer = new JLabel();
            private final JLabel lError = new JLabel();

            private Dimension dId = new Dimension(0, 0);
            private Dimension dName = new Dimension(0, 0);

            MyRenderer() {
                setBackground(BACKGROUND);
                setLayout(new BorderLayout());

                pHead.setLayout(new BorderLayout());
                pHead.setBackground(BACKGROUND);
                pHead.add(pList, BorderLayout.LINE_START);
                pHead.add(lActive, BorderLayout.LINE_END);
                pHead.add(pError, BorderLayout.PAGE_END);

                lId.setHorizontalAlignment(SwingConstants.RIGHT);
                lName.setForeground(Color.BLUE);
                lCounters.setForeground(Color.GRAY);

                pList.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
                pList.setBackground(BACKGROUND);
                pList.add(lId);
                pList.add(lName);
                pList.add(lCounters);

                pError.setVisible(false);
                pError.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
                pError.setBackground(BACKGROUND);
                pError.add(lErrorSpacer);
                pError.add(lError);

                lError.setForeground(Color.RED);

                add(pHead, BorderLayout.PAGE_START);
            }

            public void setMonitor(SingleThreadMonitor monitor) {
                if (monitor != null) {

                    lName.setPreferredSize(null);
                    lName.setText(monitor.getName());
                    Dimension tmp = lName.getPreferredSize();
                    if (tmp.width > dName.width || tmp.height > dName.height) {
                        dName = tmp;
                    }
                    lName.setPreferredSize(dName);

                    lId.setPreferredSize(null);
                    lId.setText("" + monitor.getThreadId());
                    tmp = lId.getPreferredSize();
                    if (tmp.width > dId.width || tmp.height > dId.height) {
                        dId = tmp;
                    }
                    lId.setPreferredSize(dId);

                    lCounters.setText(monitor.getLastTask());

                    if (monitor.isAlive()) {
                        if (monitor.isActive()) {
                            lActive.setForeground(Color.GREEN);
                            lActive.setText("Active");
                        } else {
                            lActive.setForeground(Color.GRAY);
                            lActive.setText("Inactive");
                        }
                    } else {
                        lActive.setForeground(Color.RED);
                        lActive.setText("Disposed");
                    }

                    pError.setVisible(monitor.hasErrors());
                    if (monitor.hasErrors()) {
                        lErrorSpacer.setPreferredSize(dId);
                        lError.setText(monitor.getNumErrors() + " Error(s), [" + monitor.getLastError().getClass().getSimpleName() + "] "
                                + monitor.getLastError().getMessage());
                    }
                } else {
                    lName.setText("");
                    lId.setText("");
                    lActive.setText("");
                }
            }

        }
    }

    private final class ThreadListModel extends AbstractListModel {

        private final java.util.List<SingleThreadMonitor> monitors = new ArrayList<>();
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

        private ThreadListModel() {
            ThreadMonitor.registerForEvents(this);
            queue.add(new Task("Sort Monitors") {
                @Override
                public void execute() {
                    ThreadMonitor.getThreadMonitors(monitors, false);
                    if (monitors.size() > 0) {
                        Collections.sort(monitors);
                        invokeIntervalAdded(0, monitors.size() - 1);
                    }
                }
            });

            executor.execute(() -> {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                try {
                    while (!stopThread) {
                        final Task task = queue.poll(500, TimeUnit.MILLISECONDS);
                        if (task != null) {
                            try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(task.getName())) {
                                task.execute();
                            }
                        } else {
                            try (ThreadActivity ignored = ThreadMonitor.startThreadActivity("Sort Monitors")) {
                                Collections.sort(monitors);
                                invokeContentsChanged(0, monitors.size() - 1);
                            }
                        }
                    }
                } catch (Exception e) {
                    ThreadMonitor.addError(e);
                    LOGGER.error("Error executing thread monitor update", e);
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

        @Subscribe
        public void receiveThreadMonitorEvent(final ThreadMonitorEvent event) {
            if (event != null) {
                switch (event.type) {
                    case MonitorAdded:
                        queue.add(new Task("Register Monitor") {
                            @Override
                            public void execute() {
                                if (!monitors.contains(event.monitor)) {
                                    monitors.add(event.monitor);
                                    Collections.sort(monitors);
                                    invokeContentsChanged(0, monitors.size() - 1);
                                }
                            }
                        });
                        break;
                }
            }
        }

        @Override
        public int getSize() {
            return monitors.size();
        }

        @Override
        public Object getElementAt(int index) {
            return monitors.get(index);
        }
    }
}
