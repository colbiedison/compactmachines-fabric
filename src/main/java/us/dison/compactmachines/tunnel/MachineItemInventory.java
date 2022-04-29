package us.dison.compactmachines.tunnel;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.util.math.Direction;

public class MachineItemInventory {

    public static final MachineItemInventory EMPTY = new MachineItemInventory(new InventoryStorage[6]);

    private final InventoryStorage[] inventories;

    public MachineItemInventory(InventoryStorage[] invs) {
        this.inventories = invs;
    }

//    public void readNbt(NbtElement tag) {
//        for (Direction dir : Direction.values()) {
//            if (get(dir) == null) continue;
//            get(dir).readNbtList( (NbtList)  ( (NbtCompound) tag ).get(dir.asString()) );
//        }
//    }
//
//    public NbtCompound toNbt() {
//        NbtCompound nbt = new NbtCompound();
//        for (Direction dir : Direction.values()) {
//            if (get(dir) == null) continue;
//            nbt.put(dir.asString(), get(dir).toNbtList());
//        }
//        return nbt;
//    }

    public InventoryStorage get(Direction side) {
        if (side == null) return null;
        return inventories[side.ordinal()];
    }

    public void set(Direction side, InventoryStorage inv) {
        if (side == null) return;
        inventories[side.ordinal()] = inv;
    }

}
