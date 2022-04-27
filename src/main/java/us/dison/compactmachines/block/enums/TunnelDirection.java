package us.dison.compactmachines.block.enums;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

import java.util.Objects;

// Using a custom enum because DirectionProperty doesn't have a NONE direction
public enum TunnelDirection implements StringIdentifiable {
    NORTH("north"),
    SOUTH("south"),
    EAST("east"),
    WEST("west"),
    UP("up"),
    DOWN("down"),
    NONE("none")
    ;

    public static final Codec<TunnelDirection> CODEC = StringIdentifiable.createCodec(TunnelDirection::values, TunnelDirection::byName);

    private final String direction;

    TunnelDirection(String direction) {
        this.direction = direction;
    }

    public static TunnelDirection byName(String name) {
        for (TunnelDirection d : TunnelDirection.values())
            if (Objects.equals(d.asString(), name)) return d;

        return null;
    }

    public Direction toDirection() {
        return switch (this) {
            case NORTH ->   Direction.NORTH;
            case SOUTH ->   Direction.SOUTH;
            case EAST ->    Direction.EAST;
            case WEST ->    Direction.WEST;
            case UP ->      Direction.UP;
            case DOWN ->    Direction.DOWN;
            default ->      null;
        };
    }

    @Override
    public String asString() {
        return this.direction;
    }
}
