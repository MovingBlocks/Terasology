package org.terasology.math;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class DirectionTests {

    public static class InDirectionTests {
        @Test
        public void left() {
            Direction dir = Direction.inDirection(1, 0, 0);
            assertEquals(Direction.LEFT, dir);
        }
        
        @Test
        public void right() {
            Direction dir = Direction.inDirection(-1, 0, 0);
            assertEquals(Direction.RIGHT, dir);
        }
        
        @Test
        public void up() {
            Direction dir = Direction.inDirection(0, 1, 0);
            assertEquals(Direction.UP, dir);
        }
        
        @Test
        public void down() {
            Direction dir = Direction.inDirection(0, -1, 0);
            assertEquals(Direction.DOWN, dir);
        }
        
        @Test
        public void forward() {
            Direction dir = Direction.inDirection(0, 0, 1);
            assertEquals(Direction.FORWARD, dir);
        }
        
        @Test
        public void backward() {
            Direction dir = Direction.inDirection(0, 0, -1);
            assertEquals(Direction.BACKWARD, dir);
        }
    }
    
    public static class InHorizontalDirectionTests {
        @Test
        public void left() {
            Direction dir = Direction.inHorizontalDirection((float) 1, (float) 0);
            assertEquals(Direction.LEFT, dir);
        }
        
        @Test
        public void right() {
            Direction dir = Direction.inHorizontalDirection((float) -1, (float) 0);
            assertEquals(Direction.RIGHT, dir);
        }
        
        @Test
        public void forward() {
            Direction dir = Direction.inHorizontalDirection((float) 0, (float) 1);
            assertEquals(Direction.FORWARD, dir);
        }
        
        @Test
        public void backward() {
            Direction dir = Direction.inHorizontalDirection((float) 0, (float) -1);
            assertEquals(Direction.BACKWARD, dir);
        }
    }
}