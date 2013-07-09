package org.terasology.pathfinding.maze;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;

/**
 * @author synopia
 */
public class MazeGenerator {
    private final int x;
    private final int y;
    private final int[][] maze;
    private Random random;

    public MazeGenerator(int x, int y, Random random) {
        this.random = random;
        this.x = (x-1)/3;
        this.y = (y-1)/3;
        maze = new int[x][y];
        generateMaze(0, 0);
    }

    public void display(BitSet[] target) {
        for (int i = 0; i < y; i++) {
            // draw the north edge
            for (int j = 0; j < x; j++) {
                if( (maze[j][i] & 1) == 0 ) {
                    target[i*3].set(j*3);
                    target[i*3].set(j*3+1);
                    target[i*3].set(j*3+2);
                } else {
                    target[i*3].set(j*3);
                }
            }
            // draw the west edge
            for (int j = 0; j < x; j++) {
                if( (maze[j][i] & 8) == 0 ) {
                    target[i*3+1].set(j*3);
                    target[i*3+2].set(j*3);
                }
            }
            target[i*3].set(x*3);
            target[i*3+1].set(x*3);
            target[i*3+2].set(x*3);
        }
        // draw the bottom line
        target[y*3].set(0, x * 3+1);

    }

    private void generateMaze(int cx, int cy) {
        DIR[] dirs = DIR.values();
        Collections.shuffle(Arrays.asList(dirs), random);
        for (DIR dir : dirs) {
            int nx = cx + dir.dx;
            int ny = cy + dir.dy;
            if (between(nx, x) && between(ny, y)
                    && (maze[nx][ny] == 0)) {
                maze[cx][cy] |= dir.bit;
                maze[nx][ny] |= dir.opposite.bit;
                generateMaze(nx, ny);
            }
        }
    }

    private static boolean between(int v, int upper) {
        return (v >= 0) && (v < upper);
    }

    private enum DIR {
        N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);
        private final int bit;
        private final int dx;
        private final int dy;
        private DIR opposite;

        // use the static initializer to resolve forward references
        static {
            N.opposite = S;
            S.opposite = N;
            E.opposite = W;
            W.opposite = E;
        }

        private DIR(int bit, int dx, int dy) {
            this.bit = bit;
            this.dx = dx;
            this.dy = dy;
        }
    };
}
