package us.dison.compactmachines.block.enums;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public enum MachineSize {
    TINY("tiny", 3),
    SMALL("small", 5),
    NORMAL("normal", 7),
    LARGE("large", 9),
    GIANT("giant", 11),
    MAXIMUM("maximum", 13);

    private final String name;
    private final int size;


    private MachineSize(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public static MachineSize getFromSize(String size) {
        return switch (size.toLowerCase()) {
            case "small" -> SMALL;
            case "normal" -> NORMAL;
            case "large" -> LARGE;
            case "giant" -> GIANT;
            case "maximum" -> MAXIMUM;
            default -> TINY;
        };

    }


    public String getName() {
        return this.name;
    }

    public int getSize() {
        return this.size;
    }
}
