package org.terasology.monitoring.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.monitoring.SingleThreadMonitor;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.ThreadMonitor.ThreadMonitorEvent;

import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class ThreadMonitorPanel extends JPanel {

    protected static final Logger logger = LoggerFactory.getLogger(ThreadMonitorPanel.class);

    private final JList list;
    
    public ThreadMonitorPanel() {
        setLayout(new BorderLayout());
        list = new JList(new ThreadListModel());
        list.setCellRenderer(new ThreadListRenderer());
        list.setVisible(true);
        add(list, BorderLayout.CENTER);
    }
    
    protected abstract static class Task {
        
        public abstract void execute();
        
    }
    
    protected static class ThreadListRenderer implements ListCellRenderer {

        protected static class MyRenderer extends JPanel {
            
            private final JPanel pHead = new JPanel();
            private final JPanel pList = new JPanel();
            private final JLabel lName = new JLabel();
            private final JLabel lId = new JLabel();
            private final JLabel lCounters = new JLabel();
            private final JLabel lActive = new JLabel();
            
            private Dimension dId = new Dimension(0, 0), dName = new Dimension(0, 0);
            
            public MyRenderer() {
                setBackground(Color.white);
                setLayout(new BorderLayout());
                
                pHead.setLayout(new BorderLayout());
                pHead.add(pList, BorderLayout.LINE_START);
                pHead.add(lActive, BorderLayout.LINE_END);
                
                lId.setHorizontalAlignment(SwingConstants.RIGHT);
                lName.setForeground(Color.blue);
                lCounters.setForeground(Color.gray);

                pList.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
                pList.add(lId);
                pList.add(lName);
                pList.add(lCounters);
                
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
                    
                    String counters = "";
                    for (int i = 0; i < monitor.getNumCounters(); i++) {
                        if (i > 0) counters += ", ";
                        counters += monitor.getKey(i) + ": " + monitor.getCounter(i);
                    }
                    lCounters.setText(counters);
                    
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
                } else {
                    lName.setText("");
                    lId.setText("");
                    lActive.setText("");
                }
            }
            
        }
        
        protected final MyRenderer renderer = new MyRenderer();
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof SingleThreadMonitor)
                renderer.setMonitor((SingleThreadMonitor) value);
            else 
                renderer.setMonitor(null);
            return renderer;
        }
        
    }
    
    protected static class ThreadListModel extends AbstractListModel {

        private final ArrayList<SingleThreadMonitor> monitors = new ArrayList<SingleThreadMonitor>();
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final BlockingQueue<Task> queue = new LinkedBlockingQueue<Task>();
        
        protected void invokeIntervalAdded(final int a, final int b) {
            final Object source = this;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireIntervalAdded(source, a, b);
                }
            });
        }
        
        protected void invokeIntervalRemoved(final int a, final int b) {
            final Object source = this;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireIntervalRemoved(source, a, b);
                }
            });
        }
        
        protected void invokeContentsChanged(final int a, final int b) {
            final Object source = this;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireContentsChanged(source, a, b);
                }
            });
        }
        
        protected ThreadListModel() {
            ThreadMonitor.getEventBus().register(this);
            queue.add(new Task() {
                @Override
                public void execute() {
                    ThreadMonitor.getThreadMonitors(monitors, false);
                    if (monitors.size() > 0) {
                        Collections.sort(monitors);
                        invokeIntervalAdded(0, monitors.size()-1);
                    }
                }
            });
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    final SingleThreadMonitor monitor = ThreadMonitor.create("Monitoring.Threads", "Tasks", "Polls");
                    try {
                        while (true) {
                            final Task task = queue.poll(500, TimeUnit.MILLISECONDS);
                            if (task != null) {
                                task.execute();
                                monitor.increment(0);
                            } else {
                                Collections.sort(monitors);
                                invokeContentsChanged(0, monitors.size()-1);
                                monitor.increment(1);
                            }
                        }
                    } catch (Exception e) {
                        monitor.addError(e);
                        e.printStackTrace();
                    } finally {
                        monitor.setActive(false);
                    }
                    invokeContentsChanged(0, monitors.size()-1);
                }
            });
        }
        
        @Subscribe
        public void recieveThreadMonitorEvent(final ThreadMonitorEvent event) {
            if (event != null)
                switch (event.type) {
                case MonitorAdded: 
                    queue.add(new Task() {
                        @Override
                        public void execute() {
                            if (!monitors.contains(event.monitor)) {
                                monitors.add(event.monitor);
                                Collections.sort(monitors);
                                invokeContentsChanged(0, monitors.size()-1);
                            }
                        }
                    });
                    break;
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
