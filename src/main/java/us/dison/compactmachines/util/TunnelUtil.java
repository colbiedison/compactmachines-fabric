package us.dison.compactmachines.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import us.dison.compactmachines.block.enums.TunnelDirection;
import us.dison.compactmachines.data.persistent.Room;
import us.dison.compactmachines.data.persistent.tunnel.Tunnel;
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

    public static Tunnel rotate(Tunnel t) {
        return new Tunnel(
                t.getPos(),
                nextSide(t.getFace()),
                t.getType(),
                t.isConnected(),
                t.isOutgoing()
        );
    }

    public static Tunnel fromRoomAndPos(Room room, BlockPos pos) {
        if (room == null) return null;
        for (Tunnel tunnel : room.getTunnels()) {
            if (equalBlockPos(tunnel.getPos(), pos)) {
                return tunnel;
            }
        }
        return null;
    }

    public static boolean equalBlockPos(BlockPos a, BlockPos b) {
        return (
                a.getX() == b.getX() &&
                a.getY() == b.getY() &&
                a.getZ() == b.getZ()
        );
    }
}
