package org.terasology.blockNetwork;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.math.Direction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DirectionsUtil {
    private static Map<Direction, Byte> directionBits = Maps.newHashMap();

    static {
        directionBits.put(Direction.UP, (byte) 1);
        directionBits.put(Direction.LEFT, (byte) 2);
        directionBits.put(Direction.FORWARD, (byte) 4);
        directionBits.put(Direction.DOWN, (byte) 8);
        directionBits.put(Direction.RIGHT, (byte) 16);
        directionBits.put(Direction.BACKWARD, (byte) 32);
    }

    public static byte getDirections(Collection<Direction> directions) {
        byte result = 0;
        for (Direction direction : directions)
            result += directionBits.get(direction);
        return result;
    }

    public static byte getDirection(Direction direction) {
        return directionBits.get(direction);
    }

    public static Collection<Direction> getDirections(byte directionBit) {
        Set<Direction> result = Sets.newHashSet();
        for (Map.Entry<Direction, Byte> aDirectionBit : directionBits.entrySet()) {
            if ((aDirectionBit.getValue() & directionBit) > 0)
                result.add(aDirectionBit.getKey());
        }

        return result;
    }

    public static boolean hasDirection(byte directionBit, Direction direction) {
        return (directionBit & directionBits.get(direction)) > 0;
    }

    public static byte addDirection(byte directionBit, Direction direction) {
        return (byte) (directionBit | directionBits.get(direction));
    }
}
