package org.terasology.monitoring.impl;

import java.util.LinkedList;


public class NullThreadMonitor implements SingleThreadMonitor {

    private static final NullThreadMonitor instance = new NullThreadMonitor();
    
    private NullThreadMonitor() {}
    
    public static NullThreadMonitor getInstance() {
        return instance; 
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void setActive(boolean value) {}

    @Override
    public String getName() {
        return "";
    }

    @Override
    public long getThreadId() {
        return 0;
    }

    @Override
    public boolean hasErrors() {
        return false;
    }

    @Override
    public int getNumErrors() {
        return 0;
    }

    @Override
    public Throwable getLastError() {
        return null;
    }

    @Override
    public LinkedList<Throwable> getErrors() {
        return null;
    }

    @Override
    public void addError(Throwable error) {}

    @Override
    public int getNumCounters() {
        return 0;
    }

    @Override
    public String getKey(int index) {
        return "";
    }

    @Override
    public long getCounter(int index) {
        return 0;
    }

    @Override
    public void increment(int index) {}

    @Override
    public int compareTo(SingleThreadMonitor o) {
        return 0;
    }
}
