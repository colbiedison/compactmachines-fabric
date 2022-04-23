package us.dison.compactmachines.block.enums;

import net.minecraft.util.StringIdentifiable;

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

    private final String direciton;

    TunnelDirection(String direction) {
        this.direciton = direction;
    }

    @Override
    public String asString() {
        return this.direciton;
    }
}
