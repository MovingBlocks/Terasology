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

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

/**
 * A cube has 12 edges. For asymmetric cubes each edge can have 2 orientations, giving 24 different combinations.
 * First 12 edges are for symmetric blocks, all 24 are for asymmetric blocks.
 */
public enum Edge {

    BottomBack(Rotation.none()),
    BottomRight(Rotation.rotate(Yaw.CLOCKWISE_90)),
    BottomFront(Rotation.rotate(Yaw.CLOCKWISE_180)),
    BottomLeft(Rotation.rotate(Yaw.CLOCKWISE_270)),
    //
    RightBack(Rotation.rotate(Roll.CLOCKWISE_90)),
    RightFront(Rotation.rotate(Pitch.CLOCKWISE_180,Roll.CLOCKWISE_90 )),
    LeftBack(Rotation.rotate(Roll.CLOCKWISE_270)),
    LeftFront(Rotation.rotate(Yaw.CLOCKWISE_180,Roll.CLOCKWISE_90)),
    //
    TopBack(Rotation.rotate(Roll.CLOCKWISE_180)),
    TopRight(Rotation.rotate(Yaw.CLOCKWISE_270,Pitch.CLOCKWISE_180)),
    TopFront(Rotation.rotate(Pitch.CLOCKWISE_180)),
    TopLeft(Rotation.rotate(Yaw.CLOCKWISE_90,Pitch.CLOCKWISE_180)),
    //
    //
    BackBottom(Rotation.rotate(Yaw.CLOCKWISE_180,Pitch.CLOCKWISE_90)),
    RightBottom(Rotation.rotate(Pitch.CLOCKWISE_90,Roll.CLOCKWISE_90)),
    FrontBottom(Rotation.rotate(Pitch.CLOCKWISE_90)),
    LeftBottom(Rotation.rotate(Pitch.CLOCKWISE_90,Roll.CLOCKWISE_270)),
    //
    BackRight(Rotation.rotate(Yaw.CLOCKWISE_90,Roll.CLOCKWISE_270)),
    FrontRight(Rotation.rotate(Yaw.CLOCKWISE_90,Roll.CLOCKWISE_90 )),
    BackLeft(Rotation.rotate(Yaw.CLOCKWISE_270,Roll.CLOCKWISE_90)),
    FrontLeft(Rotation.rotate(Yaw.CLOCKWISE_270,Roll.CLOCKWISE_270)),
    //
    BackTop(Rotation.rotate(Pitch.CLOCKWISE_270)),
    RightTop(Rotation.rotate(Yaw.CLOCKWISE_90,Pitch.CLOCKWISE_270)),
    FrontTop(Rotation.rotate(Yaw.CLOCKWISE_180,Pitch.CLOCKWISE_270)),
    LeftTop(Rotation.rotate(Yaw.CLOCKWISE_270,Pitch.CLOCKWISE_270)),
    ;

    private final Name name;
    private final Rotation rotationFromBottomBack;

    private Edge(Rotation rotationFromBottomBack) {
        this.name = new Name(name());
        this.rotationFromBottomBack = rotationFromBottomBack;
    }



    public Name getName() {
        return name;
    }

    public Rotation getRotationFromBottomBack() {
        return rotationFromBottomBack;
    }

    public static Set<Edge> symmetricSubset() {
        return getOrCreatSymmetricSubset();
    }

    private static Set<Edge> symmetricSubset;
    private static Set<Edge> getOrCreatSymmetricSubset() {
        if( symmetricSubset != null ) {
            return symmetricSubset;
        }
        symmetricSubset = Collections.unmodifiableSet(EnumSet.range(Edge.BottomBack, Edge.TopLeft));
        return symmetricSubset;
    }

    public static Edge of(Name name) {
        for (Edge e : values()) {
            if (e.name.equals(name)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid name " + name.toString());
    }

    public static Edge forSides(Side major, Side minor) {
        return getOrCreateSidesMap().get(major).get(minor);

    }

    private static EnumMap<Side,EnumMap<Side,Edge>> sides;

    private static EnumMap<Side,EnumMap<Side,Edge>> getOrCreateSidesMap() {
        if( sides != null ) {
            return sides;
        }
        EnumMap<Side,EnumMap<Side,Edge>> map = new EnumMap<Side,EnumMap<Side,Edge>>(Side.class);
        for( Side major : Side.values() ) {
            EnumMap<Side,Edge> em = new EnumMap<Side,Edge>(Side.class);
            map.put(major,em);
            for( Side minor : Side.values() ) {
                if( minor == major || minor == major.reverse() ) {
                    continue;
                }
                String edgeName = (major.name()+minor.name()).toLowerCase();
                for( Edge e : values() ) {
                    if( e.getName().toLowerCase().equals(edgeName) ) {
                        em.put(minor, e );
                        break;
                    }
                }
            }
        }
        sides = map;
        return sides;
    }

    public Edge getSymmetricEquivalent() {
        if( ordinal() < 12 ) {
            return values()[ordinal()+12];
        } else {
            return values()[ordinal()-12];
        }
    }

}
