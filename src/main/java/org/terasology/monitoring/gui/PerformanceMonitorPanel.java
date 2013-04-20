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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.monitoring.impl.SingleThreadMonitor;

import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public class PerformanceMonitorPanel extends JPanel {

    private final HeaderPanel header;
    private final JList list;
    
    public PerformanceMonitorPanel() {
        setLayout(new BorderLayout());
        header = new HeaderPanel();
        list = new JList(new PerformanceListModel());
        list.setCellRenderer(new PerformanceListRenderer(header));
        list.setVisible(true);
        add(header, BorderLayout.PAGE_START);
        add(list, BorderLayout.CENTER);
    }

    protected static class HeaderPanel extends JPanel {
        
        private final JLabel lName = new JLabel("Title");
        private final JLabel lMean = new JLabel("Running Means");
        private final JLabel lSpike = new JLabel("Decaying Spikes");
        
        public HeaderPanel() {
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
            
            private final HeaderPanel header;
            private final DecimalFormat format = new DecimalFormat ("#####0.00");
            private final JLabel lName = new JLabel();
            private final JLabel lMean = new JLabel();
            private final JLabel lSpike = new JLabel();
            
            private Dimension dName = new Dimension(0, 0);
            
            public MyRenderer(HeaderPanel header) {
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
        
        protected final MyRenderer renderer;
        
        public PerformanceListRenderer(HeaderPanel header) {
            renderer = new MyRenderer(header);
        }
        
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
