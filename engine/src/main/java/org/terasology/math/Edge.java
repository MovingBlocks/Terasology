/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.math;

import org.terasology.naming.Name;

import java.util.*;

/**
 * A cube has 12 edges. For asymmetric cubes each edge can have 2 orientations, giving 24 different combinations.
 * First 12 edges are for symmetric blocks, all 24 are for asymmetric blocks.
 */
public enum Edge {

    BOTTOM_BACK(Side.BOTTOM, Side.BACK, Rotation.none()),
    BOTTOM_RIGHT(Side.BOTTOM, Side.RIGHT, Rotation.rotate(Yaw.CLOCKWISE_90)),
    BOTTOM_FRONT(Side.BOTTOM, Side.FRONT, Rotation.rotate(Yaw.CLOCKWISE_180)),
    BOTTOM_LEFT(Side.BOTTOM, Side.LEFT, Rotation.rotate(Yaw.CLOCKWISE_270)),
    //
    RIGHT_BACK(Side.RIGHT, Side.BACK, Rotation.rotate(Roll.CLOCKWISE_90)),
    RIGHT_FRONT(Side.RIGHT, Side.FRONT, Rotation.rotate(Pitch.CLOCKWISE_180, Roll.CLOCKWISE_90)),
    LEFT_BACK(Side.LEFT, Side.BACK, Rotation.rotate(Roll.CLOCKWISE_270)),
    LEFT_FRONT(Side.LEFT, Side.FRONT, Rotation.rotate(Yaw.CLOCKWISE_180, Roll.CLOCKWISE_90)),
    //
    TOP_BACK(Side.TOP, Side.BACK, Rotation.rotate(Roll.CLOCKWISE_180)),
    TOP_RIGHT(Side.TOP, Side.RIGHT, Rotation.rotate(Yaw.CLOCKWISE_270, Pitch.CLOCKWISE_180)),
    TOP_FRONT(Side.TOP, Side.FRONT, Rotation.rotate(Pitch.CLOCKWISE_180)),
    TOP_LEFT(Side.TOP, Side.LEFT, Rotation.rotate(Yaw.CLOCKWISE_90, Pitch.CLOCKWISE_180)),
    //
    //
    BACK_BOTTOM(Side.BACK, Side.BOTTOM, Rotation.rotate(Yaw.CLOCKWISE_180, Pitch.CLOCKWISE_90)),
    RIGHT_BOTTOM(Side.RIGHT, Side.BOTTOM, Rotation.rotate(Pitch.CLOCKWISE_90, Roll.CLOCKWISE_90)),
    FRONT_BOTTOM(Side.FRONT, Side.BOTTOM, Rotation.rotate(Pitch.CLOCKWISE_90)),
    LEFT_BOTTOM(Side.LEFT, Side.BOTTOM, Rotation.rotate(Pitch.CLOCKWISE_90, Roll.CLOCKWISE_270)),
    //
    BACK_RIGHT(Side.BACK, Side.RIGHT, Rotation.rotate(Yaw.CLOCKWISE_90, Roll.CLOCKWISE_270)),
    FRONT_RIGHT(Side.FRONT, Side.RIGHT, Rotation.rotate(Yaw.CLOCKWISE_90, Roll.CLOCKWISE_90)),
    BACK_LEFT(Side.BACK, Side.LEFT, Rotation.rotate(Yaw.CLOCKWISE_270, Roll.CLOCKWISE_90)),
    FRONT_LEFT(Side.FRONT, Side.LEFT, Rotation.rotate(Yaw.CLOCKWISE_270, Roll.CLOCKWISE_270)),
    //
    BACK_TOP(Side.BACK, Side.TOP, Rotation.rotate(Pitch.CLOCKWISE_270)),
    RIGHT_TOP(Side.RIGHT, Side.TOP, Rotation.rotate(Yaw.CLOCKWISE_90, Pitch.CLOCKWISE_270)),
    FRONT_TOP(Side.FRONT, Side.TOP, Rotation.rotate(Yaw.CLOCKWISE_180, Pitch.CLOCKWISE_270)),
    LEFT_TOP(Side.LEFT, Side.TOP, Rotation.rotate(Yaw.CLOCKWISE_270, Pitch.CLOCKWISE_270)),;

    private static Set<Edge> symmetricSubset = Collections.unmodifiableSet(EnumSet.range(Edge.BOTTOM_BACK, Edge.TOP_LEFT));
    private static Map<Name, Edge> nameEdgeMap;
    private final Name name;
    private final Rotation rotationFromBottomBack;
    private static EnumMap<Side, EnumMap<Side, Edge>> sidesMap;

    public Name getName() {
        return name;
    }

    public Rotation getRotationFromBottomBack() {
        return rotationFromBottomBack;
    }

    private static EnumMap<Edge, Edge> oppositeMap;
    private static Set<Edge> parallelTopBottom = EnumSet.of(LEFT_BACK, LEFT_FRONT, RIGHT_BACK, RIGHT_FRONT, BACK_LEFT, BACK_RIGHT, FRONT_LEFT, FRONT_RIGHT);
    private static Set<Edge> parallelBackFront = EnumSet.of(BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT, TOP_LEFT, RIGHT_BOTTOM, RIGHT_TOP, LEFT_BOTTOM, LEFT_TOP);
    private static Set<Edge> parallelLeftRight = EnumSet.of(BOTTOM_BACK, BOTTOM_FRONT, TOP_BACK, TOP_FRONT, BACK_BOTTOM, BACK_TOP, FRONT_BOTTOM, FRONT_TOP);
    private static EnumMap<Side, EnumSet<Edge>> commonSideMap;

    static {
        nameEdgeMap = new HashMap<Name, Edge>();
        for (Edge e : values()) {
            nameEdgeMap.put(e.name, e);
        }
    }

    static {
        sidesMap = new EnumMap<Side, EnumMap<Side, Edge>>(Side.class);
        for (Side firstSide : Side.values()) {
            EnumMap<Side, Edge> em = new EnumMap<Side, Edge>(Side.class);
            sidesMap.put(firstSide, em);
            for (Side secondSide : Side.values()) {
                if (firstSide == secondSide || secondSide == firstSide.reverse()) {
                    continue;
                }
                for (Edge e : values()) {
                    if (e.firstSide == firstSide && e.secondSide == secondSide) {
                        em.put(secondSide, e);
                        break;
                    }
                }
            }
        }
    }

    static {
        oppositeMap = new EnumMap<Edge, Edge>(Edge.class);
        oppositeMap.put(BOTTOM_BACK, TOP_FRONT);
        oppositeMap.put(BOTTOM_RIGHT, TOP_LEFT);
        oppositeMap.put(BOTTOM_FRONT, TOP_BACK);
        oppositeMap.put(BOTTOM_LEFT, TOP_RIGHT);
        //
        oppositeMap.put(RIGHT_BACK, LEFT_FRONT);
        oppositeMap.put(RIGHT_FRONT, LEFT_BACK);
        oppositeMap.put(LEFT_BACK, RIGHT_FRONT);
        oppositeMap.put(LEFT_FRONT, RIGHT_BACK);
        //
        oppositeMap.put(TOP_BACK, BOTTOM_FRONT);
        oppositeMap.put(TOP_RIGHT, BOTTOM_LEFT);
        oppositeMap.put(TOP_FRONT, BOTTOM_BACK);
        oppositeMap.put(TOP_LEFT, BOTTOM_RIGHT);
        //
        //
        oppositeMap.put(BACK_BOTTOM, FRONT_TOP);
        oppositeMap.put(RIGHT_BOTTOM, LEFT_TOP);
        oppositeMap.put(FRONT_BOTTOM, BACK_TOP);
        oppositeMap.put(LEFT_BOTTOM, RIGHT_TOP);
        //
        oppositeMap.put(BACK_RIGHT, FRONT_LEFT);
        oppositeMap.put(FRONT_RIGHT, BACK_LEFT);
        oppositeMap.put(BACK_LEFT, FRONT_RIGHT);
        oppositeMap.put(FRONT_LEFT, BACK_RIGHT);
        //
        oppositeMap.put(BACK_TOP, FRONT_BOTTOM);
        oppositeMap.put(RIGHT_TOP, LEFT_BOTTOM);
        oppositeMap.put(FRONT_TOP, BACK_BOTTOM);
        oppositeMap.put(LEFT_TOP, RIGHT_BOTTOM);
    }

    static {
        commonSideMap = new EnumMap<Side, EnumSet<Edge>>(Side.class);
        for (Side side : Side.values()) {
            EnumSet<Edge> edges = EnumSet.noneOf(Edge.class);
            commonSideMap.put(side, edges);
            for (Edge e : values()) {
                if (e.firstSide == side || e.secondSide == side) {
                    edges.add(e);
                }
            }
        }
    }

    private final Side firstSide;
    private final Side secondSide;

    private Edge(Side firstSide, Side secondSide, Rotation rotationFromBottomBack) {
        this.firstSide = firstSide;
        this.secondSide = secondSide;
        this.name = new Name(name());
        this.rotationFromBottomBack = rotationFromBottomBack;
    }

    /**
     * @return Set<EDGE> Edges for symmetric blocks
     */
    public static Set<Edge> symmetricSubset() {
        return symmetricSubset;
    }

    /**
     * Returns EDGE for given name without the need to convert name to uppercase string
     *
     * @param name
     * @return EDGE or throws IllegalArgumentException if no edge for name exists
     */
    public static Edge of(Name name) {
        Edge edge = nameEdgeMap.get(name);
        if (edge == null) {
            throw new IllegalArgumentException("Invalid name " + name.toString());
        }
        return edge;
    }

    /**
     * Returns EDGE for given name without the need to convert name to uppercase string
     *
     * @param name
     * @return EDGE or null
     */
    public static Edge ofOrNull(Name name) {
        return nameEdgeMap.get(name);
    }

    /**
     * Returns the edge between given sides.
     *
     * @param first  first SIDE
     * @param second second SIDE
     * @return EDGE
     */
    public static Edge forSides(Side first, Side second) {
        return sidesMap.get(first).get(second);
    }

    public Side getFirstSide() {
        return firstSide;
    }

    public Side getSecondSide() {
        return secondSide;
    }

    public Side getOtherSide(Side side) {
        if (side != firstSide) {
            return firstSide;
        }
        return secondSide;
    }

    /**
     * Returns the equivalent edge for symmetric blocks. Example: TOP_RIGHT for RIGHT_TOP and vice versa.
     *
     * @return EDGE
     */
    public Edge getSymmetricEquivalent() {
        if (ordinal() < 12) {
            return values()[ordinal() + 12];
        } else {
            return values()[ordinal() - 12];
        }
    }

    /**
     * Returns the opposite EDGE. Example: for BOTTOM_BACK returns TOP_FRONT
     * TODO "point-mirrored by cube center point" or "mirrored by parallel axis through cube center"?
     *
     * @return EDGE
     */
    public Edge getOpposite() {
        return oppositeMap.get(this);
    }

    /**
     * Tests if given edge is parallel to this.
     *
     * @param edge
     * @return
     */
    public boolean isParallelTo(Edge edge) {
        if (parallelTopBottom.contains(this)) {
            return parallelTopBottom.contains(edge);
        }
        if (parallelBackFront.contains(this)) {
            return parallelBackFront.contains(edge);
        }
        if (parallelLeftRight.contains(this)) {
            return parallelLeftRight.contains(edge);
        }
        return false;
    }

    /**
     * Returns the side common with the other edge or null if no side is common.
     *
     * @param edge
     * @return SIDE
     */
    public Side getCommonSideOrNull(Edge edge) {
        for (Side side : Side.values()) {
            EnumSet<Edge> edges = commonSideMap.get(side);
            if (edges.contains(this) && edges.contains(edge)) {
                return side;
            }
        }
        return null;
    }

}
