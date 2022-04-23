package us.dison.compactmachines.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import us.dison.compactmachines.data.persistent.tunnel.TunnelType;

public class TunnelUtil {
    public static TunnelType typeFromStackNbt(NbtCompound stackNbt) {
        if (stackNbt != null) {
            NbtElement typeNbt = stackNbt.get("type");
            if (typeNbt != null) {
                String strType = typeNbt.asString();
                return TunnelType.byName(strType);
            }
        }
        return null;
    }
}
