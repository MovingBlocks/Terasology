package org.terasology.pathfinding.model;

import org.terasology.math.TeraMath;

import java.util.HashSet;
import java.util.Set;

/**
 * @author synopia
 */
public abstract class BaseRegion<N extends BaseRegion> {
    public int id;
    protected Set<N> neighborRegions = new HashSet<N>();
    protected BitMap map;
    protected BitMap contour;
    protected Floor floor;
    private static boolean[] EDGES;
    private static int POS_0=1<<0;
    private static int POS_1=1<<1;
    private static int POS_2=1<<2;
    private static int POS_3=1<<3;
    private static int POS_4=1<<4;
    private static int POS_5=1<<5;
    private static int POS_6=1<<6;
    private static int POS_7=1<<7;
    private static int POS_8=1<<8;
    private static int POS_ALL=(1<<9)-1;


    static {
        EDGES = new boolean[1<<9];
        EDGES[POS_4] = true;
        EDGES[POS_4+POS_0+POS_1+POS_3] = true;
        EDGES[POS_4+POS_1+POS_2+POS_5] = true;
        EDGES[POS_4+POS_5+POS_7+POS_8] = true;
        EDGES[POS_4+POS_3+POS_6+POS_7] = true;
        EDGES[POS_4+POS_1] = true;
        EDGES[POS_4+POS_5] = true;
        EDGES[POS_4+POS_7] = true;
        EDGES[POS_4+POS_3] = true;
        EDGES[POS_ALL-POS_0] = true;
        EDGES[POS_ALL-POS_2] = true;
        EDGES[POS_ALL-POS_8] = true;
        EDGES[POS_ALL-POS_6] = true;
        EDGES[POS_4+POS_5+POS_7] = true;
        EDGES[POS_4+POS_5+POS_1] = true;
        EDGES[POS_4+POS_1+POS_3] = true;
        EDGES[POS_4+POS_7+POS_3] = true;
        EDGES[POS_4+POS_0+POS_3+POS_6] = true;
        EDGES[POS_4+POS_0+POS_1+POS_2] = true;
        EDGES[POS_4+POS_2+POS_5+POS_8] = true;
        EDGES[POS_4+POS_6+POS_7+POS_8] = true;
    }
    /**
     * 0 1 2
     * 3 4 5
     * 6 7 8
     */
    public static class EdgeFinder implements BitMap.Kernel {
        @Override
        public boolean apply(boolean[] input) {
            int mask = 0;
            if( input[0]) mask+= 1<<0;
            if( input[1]) mask+= 1<<1;
            if( input[2]) mask+= 1<<2;
            if( input[3]) mask+= 1<<3;
            if( input[4]) mask+= 1<<4;
            if( input[5]) mask+= 1<<5;
            if( input[6]) mask+= 1<<6;
            if( input[7]) mask+= 1<<7;
            if( input[8]) mask+= 1<<8;

            return EDGES[mask];
        }
    }
    public static EdgeFinder EDGE_KERNEL = new EdgeFinder();

    protected BaseRegion( int id ) {
        this.id = id;
        map = new BitMap();
        contour = new BitMap();
    }

    public void resetContour() {
        contour = new BitMap();
    }

    public void findContour() {
        BitMap newContour = new BitMap();
        newContour.runKernel(EDGE_KERNEL, contour);
        contour = newContour;
    }

    public boolean isContour(WalkableBlock block) {
        return isContour(TeraMath.calcBlockPosX(block.x()), TeraMath.calcBlockPosZ(block.z()));
    }
    public boolean isContour(int x, int y) {
        return contour.isPassable(x,y);
    }
    public void setContour( int x, int y ) {
        contour.setPassable(x, y);
    }
    public void setContour( WalkableBlock block ) {
        setContour(TeraMath.calcBlockPosX(block.x()), TeraMath.calcBlockPosZ(block.z()));
    }

    public void setPassable(int x, int y) {
        map.setPassable(x, y);
    }

    public BitMap getMap() {
        return map;
    }

    public Set<N> getNeighborRegions() {
        return neighborRegions;
    }

    @Override
    public String toString() {
        return id+"\nrc = " + neighborRegions.size();
    }
}
