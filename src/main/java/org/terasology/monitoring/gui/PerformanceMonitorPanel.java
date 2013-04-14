package org.terasology.monitoring.gui;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.procedure.TObjectDoubleProcedure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.impl.SingleThreadMonitor;

@SuppressWarnings("serial")
public class PerformanceMonitorPanel extends JPanel {

    private final JList list;
    
    public PerformanceMonitorPanel() {
        setLayout(new BorderLayout());
        list = new JList(new PerformanceListModel());
        list.setCellRenderer(new PerformanceListRenderer());
        list.setVisible(true);
        add(list, BorderLayout.CENTER);
    }

    protected static class Entry implements Comparable<Entry> {
        
        public final String name;
        public boolean active = false;
        public double mean = 0.0;
        public double spike = 0.0;
        
        public Entry(String name) {
            if (name == null)
                name = "";
            this.name = name;
        }

        @Override
        public int compareTo(Entry o) {
            final String a = this.name, b = o.name;
            return a.compareTo(b);
        }
    }

    protected static class PerformanceListRenderer implements ListCellRenderer {

        protected static class MyRenderer extends JPanel {
            
            private final DecimalFormat format = new DecimalFormat ("#####0.00");
            private final JLabel lName = new JLabel();
            private final JLabel lMean = new JLabel();
            private final JLabel lSpike = new JLabel();
            
            private Dimension dName = new Dimension(0, 0), dMean = new Dimension(0, 0);
            
            public MyRenderer() {
                setBackground(Color.white);
                setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
                
                lMean.setForeground(Color.gray);
                lSpike.setForeground(Color.gray);
             
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
                    }
                    lName.setPreferredSize(dName);
                    
                    lMean.setPreferredSize(null);
                    lMean.setText("Running Mean: " + format.format(entry.mean));
                    tmp = lMean.getPreferredSize();
                    if (tmp.width > dMean.width || tmp.height > dMean.height) {
                        dMean = tmp;
                    }
                    lMean.setPreferredSize(dMean);
                    
                    lSpike.setText("Decaying Spike: " + format.format(entry.spike));
                } else {
                    lName.setText("");
                    lMean.setText("");
                }
            }
        }
        
        protected final MyRenderer renderer = new MyRenderer();
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Entry) 
                renderer.setEntry((Entry) value);
            else
                renderer.setEntry(null);
            return renderer;
        }
    }
    
    protected static class PerformanceListModel extends AbstractListModel {

        private final List<Entry> list = new ArrayList<Entry>();
        private final Map<String, Entry> map = new HashMap<String, Entry>();
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        
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

        protected final void updateEntries(TObjectDoubleMap<String> means, TObjectDoubleMap<String> spikes) {
            if (means != null) {
                for (final Entry entry : list) {
                    entry.active = false;
                }
                means.forEachEntry(new TObjectDoubleProcedure<String>() {
                    @Override
                    public boolean execute(String key, double value) {
                        Entry entry = map.get(key);
                        if (entry == null) {
                            entry = new Entry(key);
                            list.add(entry);
                            map.put(key, entry);
                            invokeIntervalAdded(list.size()-1, list.size()-1);
                        }
                        entry.active = true;
                        entry.mean = value;
                        return true;
                    }
                });
                spikes.forEachEntry(new TObjectDoubleProcedure<String>() {
                    @Override
                    public boolean execute(String key, double value) {
                        Entry entry = map.get(key);
                        if (entry != null) {
                            entry.spike = value;
                        }
                        return true;
                    }
                });
                Collections.sort(list);
                invokeContentsChanged(0, list.size()-1);
            }
        }
        
        protected PerformanceListModel() {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    final SingleThreadMonitor monitor = ThreadMonitor.create("Monitoring.Performance", "Polls");
                    try {
                        while (true) {
                            Thread.sleep(1000);
                            updateEntries(PerformanceMonitor.getRunningMean(), PerformanceMonitor.getDecayingSpikes());
                            monitor.increment(0);
                        }
                    } catch (Exception e) {
                        monitor.addError(e);
                        e.printStackTrace();
                    } finally {
                        monitor.setActive(false);
                    }
                }
            });
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
