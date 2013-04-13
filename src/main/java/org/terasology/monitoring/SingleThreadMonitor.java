package org.terasology.monitoring;

import java.util.LinkedList;

public interface SingleThreadMonitor extends Comparable<SingleThreadMonitor> {

    public boolean isAlive();
    
    public boolean isActive();
    
    public void setActive(boolean value);
    
    public String getName();
    
    public long getThreadId();
    
    public boolean hasErrors();
    
    public int getNumErrors();
    
    public Throwable getLastError();
    
    public LinkedList<Throwable> getErrors();
    
    public void addError(Throwable error);
    
    public int getNumCounters();
    
    public String getKey(int index);
    
    public long getCounter(int index);
    
    public void increment(int index);
    
}
