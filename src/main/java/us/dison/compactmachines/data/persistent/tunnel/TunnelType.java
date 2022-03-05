package us.dison.compactmachines.data.persistent.tunnel;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

import java.util.Objects;

public enum TunnelType implements StringIdentifiable {
    ITEM("Item", 0),
    REDSTONE("Redstone", 1),
    ENERGY("Energy", 2);

    public static final Codec<TunnelType> CODEC = StringIdentifiable.createCodec(TunnelType::values, TunnelType::byName);

    private final String name;
    private final int id;

    TunnelType(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String asString() {
        return this.getName();
    }

    public static TunnelType byID(int id) {
        for (TunnelType value : TunnelType.values()) {
            if (value.getID() == id) return value;
        }
        return null;
    }

    public static TunnelType byName(String name) {
        for (TunnelType value : TunnelType.values()) {
            if (Objects.equals(value.getName(), name)) return value;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }
}
