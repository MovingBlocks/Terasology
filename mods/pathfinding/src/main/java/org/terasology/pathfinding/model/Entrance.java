package org.terasology.pathfinding.model;

import com.google.common.collect.Sets;
import org.newdawn.slick.geom.Rectangle;
import org.terasology.math.Rect2i;

import java.util.Set;

/**
 * @author synopia
 */
public class Entrance {
    public enum Type {
        VERTICAL,
        HORIZONTAL
    }
    private Rect2i area;
    private Type type;
    private Floor floor;
    public final Set<Floor> neighborFloors = Sets.newHashSet();

    public Entrance(Floor floor) {
        this.floor = floor;
    }

    public boolean isPartOfEntrance( int x, int y ) {
        if( area==null ) {
            return true;
        }
        if( area.contains(x,y) ) {
            return true;
        }
        int x1 = Math.min(area.x, x);
        int y1 = Math.min(area.y, y);
        int x2 = Math.max(area.maxX(), x);
        int y2 = Math.max(area.maxY(), y);

        if( type==Type.VERTICAL ) {
            return y2-y1==0 && area.w+1==x2-x1;
        } else if( type==Type.HORIZONTAL ) {
            return x2-x1==0 && area.h+1==y2-y1;
        } else {
            return x2-x1<=1 && y2-y1<=1;
        }
    }

    public void addToEntrance( int x, int y ) {
        if( area==null ) {
            area = new Rect2i(x,y,0,0);
        } else {
            if( !area.contains(x,y) ) {
                int x1 = Math.min(area.x, x);
                int y1 = Math.min(area.y, y);
                int x2 = Math.max(area.maxX(), x);
                int y2 = Math.max(area.maxY(), y);
                area.x = x1;
                area.y = y1;
                area.w = x2-x1;
                area.h = y2-y1;
                if( area.w>area.h ) {
                    type = Type.VERTICAL;
                } else if( area.w<area.h ) {
                    type = Type.HORIZONTAL;
                }
            }
        }
    }

    public WalkableBlock getAbstractBlock() {
        int mx = area.x+area.w/2;
        int my = area.y+area.h/2;

        return floor.getBlock(mx, my);
    }
}
