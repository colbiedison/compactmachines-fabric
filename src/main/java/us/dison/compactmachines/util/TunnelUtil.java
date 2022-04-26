package us.dison.compactmachines.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import us.dison.compactmachines.block.enums.TunnelDirection;
import us.dison.compactmachines.data.persistent.tunnel.TunnelType;

public class TunnelUtil {
    public static TunnelType typeFromNbt(NbtCompound stackNbt) {
        if (stackNbt != null) {
            NbtElement typeNbt = stackNbt.get("type");
            if (typeNbt != null) {
                String strType = typeNbt.asString();
                return TunnelType.byName(strType);
            }
        }
        return null;
    }

    public static TunnelDirection nextSide(TunnelDirection cur) {
        int i = -1;
        for (TunnelDirection direction : TunnelDirection.values()) {
            i++;
            if (direction.name().equals(cur.name())) {
                if (i >= TunnelDirection.values().length-1) {
                    i = 0;
                } else {
                    i++;
                }
                break;
            }
        }
        return TunnelDirection.values()[i];
    }
}
