package org.terasology.config;

/**
 * @author Immortius
 */
public class SystemConfig {
    private long dayNightLengthInMs = 1800000;
    private int maxThreads = 2;
    private int verticalChunkMeshSegments = 1;
    private boolean debugEnabled;
    private boolean renderChunkBoundingBoxes;

    public long getDayNightLengthInMs() {
        return dayNightLengthInMs;
    }

    public void setDayNightLengthInMs(long dayNightLengthInMs) {
        this.dayNightLengthInMs = dayNightLengthInMs;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getVerticalChunkMeshSegments() {
        return verticalChunkMeshSegments;
    }

    public void setVerticalChunkMeshSegments(int verticalChunkMeshSegments) {
        this.verticalChunkMeshSegments = verticalChunkMeshSegments;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public boolean isRenderChunkBoundingBoxes() {
        return renderChunkBoundingBoxes;
    }

    public void setRenderChunkBoundingBoxes(boolean renderChunkBoundingBoxes) {
        this.renderChunkBoundingBoxes = renderChunkBoundingBoxes;
    }
}
