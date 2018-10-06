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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.impl.SingleThreadMonitor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MyThreadMonitorPanel extends JPanel {

    private static final Color BACKGROUND = Color.white;
    private static final Logger logger = LoggerFactory.getLogger(MyThreadMonitorPanel.class);

    private final JList list;
    private DefaultListModel model;
    private UpdateRunner updateRunner;

    public MyThreadMonitorPanel() {
        model = new DefaultListModel();
        setLayout(new BorderLayout());
        list = new JList(model);
        list.setCellRenderer(new ThreadListRenderer());
        list.setVisible(true);
        add(list, BorderLayout.CENTER);
        updateRunner = new UpdateRunner();
        updateRunner.execute();
    }

    public  void cancel() {
        updateRunner.cancel(true);
    }

    public void updateList(List<SingleThreadMonitor> entries) {
        model.removeAllElements();
        for (SingleThreadMonitor entry : entries) {
            model.addElement(entry);
        }
    }

    private class UpdateRunner extends SwingWorker<List<SingleThreadMonitor>, List<SingleThreadMonitor>> {

        @Override
        protected List<SingleThreadMonitor> doInBackground() throws Exception {
            while (!isCancelled()) {
                List<SingleThreadMonitor> monitors = new ArrayList<>();
                ThreadMonitor.getThreadMonitors(monitors, false);

//                int entryCount = model.size() + 1;
//
//                System.out.println("Should be " + entryCount + " entries");
//
//                String[] entries = new String[entryCount];
//
//                for (int i = 0; i< entries.length; i++) {
//                    entries[i] = "Entry " + i;
//                }

                publish(new ArrayList<>(monitors));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }

            return null;
        }

        protected void process(List<List<SingleThreadMonitor>> entries) {
            for (List<SingleThreadMonitor> entry : entries) {
                updateList(entry);
            }
        }
        @Override
        protected void done() {
//            updateList(Arrays.asList("done"));
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
                lName.setForeground(Color.blue);
                lCounters.setForeground(Color.gray);

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

                lError.setForeground(Color.red);

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
                            lActive.setForeground(Color.green);
                            lActive.setText("Active");
                        } else {
                            lActive.setForeground(Color.gray);
                            lActive.setText("Inactive");
                        }
                    } else {
                        lActive.setForeground(Color.red);
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
}
