package us.dison.compactmachines.util;

import java.util.ArrayList;

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

    public static TunnelDirection nextSide(Room room, TunnelDirection cur) {
        int i = 0;
        int attempts = 0;
        ArrayList<TunnelDirection> tunnelDirections = new ArrayList<>();
        for (Tunnel tunnel : room.getTunnels()) {
            final TunnelDirection dir = tunnel.getFace();
            if (dir != TunnelDirection.NONE) tunnelDirections.add(dir); 
        }
        boolean exitOnNextGood = false;
        final TunnelDirection[] dirs = TunnelDirection.values();
        while (attempts <= 12) {
            final TunnelDirection direction = dirs[i];
            if (exitOnNextGood && !tunnelDirections.contains(direction)) {
                break; 
            } 
            if (direction.name().equals(cur.name())) {
                exitOnNextGood = true; 

            }
            i++;
            if (i >= dirs.length) {
                i = 0; 
            }
            attempts++;

        }
        return TunnelDirection.values()[i];
    }

    public static Tunnel rotate(Room room, Tunnel t) {
        return new Tunnel(
                t.getPos(),
                nextSide(room, t.getFace()),
                t.getType(),
                t.isConnected(),
                t.isOutgoing()
        );
    }
    public static Tunnel withFace(Tunnel t, TunnelDirection face) {
        return new Tunnel(
                t.getPos(),
                face,
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
